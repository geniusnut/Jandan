package com.alensw.support.pool;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import com.alensw.support.picture.BitmapUtils;

public class BitmapPool extends Pool<Bitmap> {
	private final int mWidth;
	private final int mHeight;
	private final Config mConfig;

	public BitmapPool(int maxSize, int width, int height, Config config) {
		super(maxSize);

		mWidth = width;
		mHeight = height;
		mConfig = config;
	}

	@Override
	protected void discard(Bitmap bitmap) {
		bitmap.recycle();
		//	Log.i("BitmapPool", "recycle: " + bitmap);
	}

	public Bitmap obtain(int width, int height, Config config) {
		Bitmap bitmap = null;
		if (width == mWidth && height == mHeight && config == mConfig)
			synchronized (this) {
				bitmap = super.poll();
			}
		if (bitmap == null) {
			bitmap = BitmapUtils.create(mWidth, mHeight, mConfig);
			//	Log.i("BitmapPool", "create: " + width + "x" + height);
		}
		return bitmap;
	}

	public void recycle(Bitmap bitmap) {
		if (bitmap != null && !bitmap.isRecycled()) {
			if (bitmap.getWidth() == mWidth && bitmap.getHeight() == mHeight)
				synchronized (this) {
					super.recycle(bitmap);
				}
			else
				bitmap.recycle();
		}
	}
}
