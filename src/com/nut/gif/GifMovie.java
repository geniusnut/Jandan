package com.nut.gif;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import com.alensw.support.jni.JniUtils;
import com.nut.dao.ParcelFile;
import com.nut.thread.Job;
import com.nut.thread.JobQueue;
import com.nut.thread.Task;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by yw07 on 15-5-20.
 */
public class GifMovie {
	public static final boolean BITMAP_MODE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO
			&& !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR2);
	private Uri mUri = Uri.EMPTY;


	private static final int MAX_BUFFER_SIZE = 2;

	private int mGifImage;
	private int mDecodeIndex;
	private int mDuration;
	private int mFrameCount;
	private int mDownSample;
	private int[] mFrameImage;
	private int[] mBufferPool;
	private Handler mHandler;
	private Loader mLoader;
	private View mView;
	private int mFrameIndex = -1;

	private int mWidth;
	private int mHeight;
	public Bitmap mBitmap;
	protected static ThreadPoolExecutor mExecutor = new ThreadPoolExecutor(10, 20, 10,
			TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(256));

	public static GifMovie create(ParcelFile pfd, Uri uri, Handler handler) {
		int gif = 0;
		try {
			gif = JniUtils.gifOpenFD(pfd.getFd(), BITMAP_MODE);
			if (gif == 0)
				throw new RuntimeException("load error");

			final int frames = JniUtils.gifGetFrameCount(gif);
			if (frames <= 1)
				throw new RuntimeException("not animated");

			int mWidth = JniUtils.gifGetImageWidth(gif);
			int mHeight = JniUtils.gifGetImageHeight(gif);
			int pixels = mWidth * mHeight;

			final Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
			if (bitmap != null)
				return new GifMovie(gif, uri, handler, bitmap);
			throw new RuntimeException("out of memory");
		} catch (Throwable e) {
			Log.e("GifMovie", "load GIF: ", e);
		}
		if (gif != 0)
			JniUtils.gifClose(gif);
		return null;
	}

	public GifMovie(int gif, Uri uri, Handler handler, Bitmap bitmap) {
		mUri = uri;
		mBitmap = bitmap;
		init(gif, handler);
	}

	private void init(int gif, Handler handler) {
		mDuration = JniUtils.gifGetDuration(gif);
		mFrameCount = JniUtils.gifGetFrameCount(gif);

		final int bytes = JniUtils.gifGetImageWidth(gif) * JniUtils.gifGetImageHeight(gif);
		mBufferPool = new int[MAX_BUFFER_SIZE];
		for (int i = 0; i < MAX_BUFFER_SIZE; i++) {
			final int buffer = JniUtils.gifAllocBuffer(bytes);
			if (buffer != 0)
				mBufferPool[i] = buffer;
			else
				throw new RuntimeException("alloc buffer failed");
		}

		mGifImage = gif;
		mHandler = handler;
	}

	public void draw(Canvas canvas, Matrix matrix, Paint paint) {
		if (mFrameIndex != -1) {
			canvas.drawBitmap(mBitmap, matrix, paint);
		}
	}

	public boolean start(View view, Paint bkPaint) {
		if (mGifImage != 0) {
			mView = view;
			JniUtils.gifSetBkColor(mGifImage, bkPaint.getColor());

			if (mLoader != null)
				mLoader.cancel();
			mLoader = new Loader();
			mExecutor.submit(mLoader);

			for (int buffer : mBufferPool)
				mLoader.request(new DecodeJob(buffer, mGifImage));
			return true;
		}
		return false;
	}

	public void stop(boolean clearCache) {
		mView = null;

		if (mLoader != null) {
			mLoader.cancel();
			mLoader = null;
		}
	}

	public void recycle() {
		//	Log.d("GifMovie", "recycle: " + this);

		stop(false);

		synchronized (this) {
			//	Log.d("GifMovie", "free buffers: " + mBufferPool);
			for (int i = 0; i < MAX_BUFFER_SIZE; i++) {
				if (mBufferPool[i] != 0) {
					JniUtils.gifFreeBuffer(mBufferPool[i]);
					mBufferPool[i] = 0;
				}
			}
			//	Log.d("GifMovie", "close GIF: " + mGifImage);
			if (mGifImage != 0) {
				JniUtils.gifClose(mGifImage);
				mGifImage = 0;
			}
		}

		mFrameCount = 0;
		mFrameIndex = -1;
		mFrameImage = null;
	}

	private int decodeFrame(int index, int buffer, int hash) {
		int duration = 0;
		synchronized (this) {
			if (mGifImage != 0 && buffer != 0 && hash == mGifImage) {
				//	final long millis = System.currentTimeMillis();
				duration = JniUtils.gifDecodeFrame(mGifImage, index, buffer);
				//	Log.d("GifMovie", "decode frame: " + index + ", used=" + (System.currentTimeMillis() - millis));
			}
		}
		return duration;
	}

	private boolean drawFrame(int index, int buffer, int hash) {
		boolean ret = false;
		synchronized (this) {
			try {
				if (mGifImage != 0 && buffer != 0 && hash == mGifImage && mBitmap != null)
					ret = JniUtils.gifDrawFrame(mGifImage, index, buffer, mBitmap);
			} catch (Throwable e) {
				Log.e("GifMovie", "draw frame: ", e);
			}
		}
		return ret;
	}

	private class DecodeJob extends Job implements Runnable {
		public int buffer;
		public int hash;
		public int index;

		public DecodeJob(int b, int h) {
			buffer = b;
			hash = h;
		}

		@Override
		public int hashCode() {
			return hash + index;
		}

		@Override
		public boolean equals(Object obj) {
			final DecodeJob job = (DecodeJob) obj;
			return buffer == job.buffer && hash == job.hash;
		}

		@Override
		public boolean equals(int a0, int a1, int a2, Object... ar) {
			return buffer == a0 && hash == a1;
		}

		@Override
		public void run() {
			if (drawFrame(index, buffer, hash))
				mFrameIndex = index;

			if (mView != null) {
				mView.invalidate();

				if (mLoader != null)
					mLoader.request(this);
			}
		}
	}


	private class Loader implements Task<Void> {
		private volatile boolean mCancelled;
		private final JobQueue mQueue = new JobQueue();
		private final Object mWaiter = new Object();

		public void request(DecodeJob job) {
			synchronized (mQueue) {
				mQueue.push(job);
				mQueue.notifyAll();
			}
		}

		public void cancelPending() {
			synchronized (mQueue) {
				mQueue.clear();
				mQueue.notifyAll();
			}
		}

		private DecodeJob pollJob() {
			synchronized (mQueue) {
				final DecodeJob job = (DecodeJob) mQueue.poll();
				if (job == null && !mCancelled) {
					try {
						mQueue.wait(100);
					} catch (Throwable e) {
					}
				}
				return job;
			}
		}

		@Override
		public void cancel() {
			mCancelled = true;
			synchronized (mWaiter) {
				mWaiter.notifyAll();
			}
			cancelPending();
			//	Log.d("GifMovie", "cancel: " + mUri);
		}

		@Override
		public void done() {
		}

		@Override
		public Void call() {
			while (!mCancelled) {
				final DecodeJob job = pollJob();
				if (job == null)
					continue;

				final long begin = SystemClock.uptimeMillis();
				job.index = mDecodeIndex;
				final int duration = decodeFrame(job.index, job.buffer, job.hash);
				if (++mDecodeIndex >= mFrameCount)
					mDecodeIndex = 0;
				if (duration > 0 && !mCancelled)
					mHandler.post(job);

				final long end = SystemClock.uptimeMillis();
				final int delay = Math.max(duration - (int) (end - begin), 10);
				if (!mCancelled) synchronized (mWaiter) {
					try {
						mWaiter.wait(delay);
					} catch (Throwable e) {
					}
				}
			}
			return null;
		}
	}
}
