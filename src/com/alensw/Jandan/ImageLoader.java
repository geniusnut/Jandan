package com.alensw.Jandan;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
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
		private final WeakReference<ImageView> mImageViewReference;
		private final Callback mCallback;

		public Loader(String thumbUrl, ImageView imageView, Callback callback) {
			mThumbUrl = thumbUrl;
			mImageViewReference = new WeakReference<ImageView>(imageView);
			mCallback = callback;
		}

		@Override
		public void run() {
			Bitmap bitmap = null;
			String hashName = Utilities.md5(mThumbUrl);
			File file = FileCache.generateCacheFile(hashName);
			if (!file.exists()) {
				InputStream is = null;
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(file);
					URL url = new URL(mThumbUrl);
					URLConnection conn = url.openConnection();
					is = conn.getInputStream();
					final byte[] data = new byte[8192];
					int bytes = 0;
					while ((bytes = is.read(data)) >= 0) {
						if (bytes > 0)
							fos.write(data, 0, bytes);
					}
					fos.flush();
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			bitmap = JandanParser.createThumbnail(file.getPath());
			if (bitmap != null) {
				final Bitmap cover = Bitmap.createBitmap(bitmap);
				//bitmap.recycle();
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						if (mImageViewReference != null && cover != null) {
							final ImageView imageView = mImageViewReference.get();
							if (imageView != null) {
								imageView.setImageBitmap(cover);
							}
						}
					}
				});
				mCallback.onLoaded(mThumbUrl, bitmap);
			}
		}
	}
}
