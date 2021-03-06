package com.nut.dao;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import com.nut.Jandan.Utility.Utilities;
import com.nut.cache.FileCache;
import com.nut.gif.GifDrawable;
import com.nut.http.JandanParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

	private final ThreadPoolExecutor mExecutor = new ThreadPoolExecutor(2, 4,
			10, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(256));


	public static interface Callback {
		public void onLoading(int progress);

		public void onLoaded(String thumbUrl, Drawable drawable); // called in loader thread
	}

	public ImageLoader(Context context) {
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
			FileCache.mCacheDir.mkdirs();
			File file = FileCache.generateCacheFile(hashName);
			if (!file.exists()) {
				InputStream is = null;
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(file);
					URL url = new URL(mThumbUrl);
					URLConnection conn = url.openConnection();
					is = conn.getInputStream();
					long length = is.available();
					final byte[] data = new byte[8192];
					int bytes = 0, written = 0, percent;
					while ((bytes = is.read(data)) >= 0) {
						if (bytes > 0) {
							fos.write(data, 0, bytes);
							written += bytes;
							percent = (int) (written / length * 100);
							mCallback.onLoading(percent);
						}
					}
					fos.flush();
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			Drawable drawable = null;
			if (!mThumbUrl.endsWith("gif")) {
				bitmap = JandanParser.createThumbnail(file.getPath());
				drawable = new BitmapDrawable(bitmap);
			} else {
				try {
					drawable = new GifDrawable(file);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}


			if (drawable != null) {
				//bitmap.recycle();

				mCallback.onLoaded(mThumbUrl, drawable);
			}
		}
	}
}
