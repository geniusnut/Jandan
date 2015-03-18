package com.alensw.Jandan;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by yw07 on 15-3-18.
 */
public class ImageLoader {

	private final ThreadPoolExecutor mExecutor = new ThreadPoolExecutor(2, 256,
			10, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(25));

	public static interface Callback {
		public abstract void onLoaded(String thumbUrl, Bitmap bitmap); // called in loader thread
	}

	ImageLoader(Context context) {
		mExecutor.prestartCoreThread();
	}

	public void request(String thumbUrl, ImageView imageView, Callback callback) {
		final Loader loader = new Loader(thumbUrl, imageView, callback);
		mExecutor.execute(loader);
	}

	private class Loader implements Runnable {
		private final String mThumbUrl;
		private final ImageView mImageView;
		private final Callback mCallback;

		public Loader(String thumbUrl, ImageView imageView, Callback callback) {
			mThumbUrl = thumbUrl;
			mImageView = imageView;
			mCallback = callback;
		}

		@Override
		public void run() {
			Bitmap bitmap = null;
			if (hasLocal()) {

			} else {
				bitmap = JandanParser.getBitMap(mThumbUrl);
			}

			if (bitmap != null) {
				final Bitmap cover = Bitmap.createBitmap(bitmap);
				//bitmap.recycle();
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						mImageView.setImageBitmap(cover);
					}
				});

				mCallback.onLoaded(mThumbUrl, bitmap);
			}
		}

		private boolean hasLocal() {
			return false;
		}
	}
}
