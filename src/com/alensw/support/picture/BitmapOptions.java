package com.alensw.support.picture;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.alensw.support.pool.Pool;

import java.lang.reflect.Field;

public class BitmapOptions extends BitmapFactory.Options {
	public static final int REQUEST_ROTATION = 360;

	public static Bitmap.Config PREFER_CONFIG = Bitmap.Config.ARGB_8888;

	public int outComponents;
	public int outRotation;
	public boolean outValidRegion; // can be decoded by BitmapRegionDecoder

	protected boolean outFDFailed;

	public void reset() {
		mCancel = false;
		inDither = false;
		inJustDecodeBounds = false;
		inPreferredConfig = null;
		inSampleSize = 1;
		outWidth = 0;
		outHeight = 0;
		outMimeType = null;
		outComponents = 0;
		outRotation = 0;
		outValidRegion = false;
		outFDFailed = false;
	}

	public void initForThumbnail() {
		inJustDecodeBounds = false;
		inSampleSize = 1;
		inPreferredConfig = PREFER_CONFIG;
	}

	public boolean isFailed() {
		return outWidth < 0 || outHeight < 0;
	}

	public boolean isNativeAlloc() {
		if (mNativeAlloc != null) try {
			return mNativeAlloc.getBoolean(this);
		} catch (Throwable e) {
		}
		return false;
	}

	public void setNativeAlloc(boolean nativeAlloc) {
		if (mNativeAlloc != null) try {
			mNativeAlloc.setBoolean(this, nativeAlloc);
		} catch (Throwable e) {
		}
	}

	@Override
	public String toString() {
		return "(" + outWidth + "x" + outHeight + "/" + inSampleSize + "x" + outRotation + '\u00b0' + "), type=" + outMimeType + ", cancel=" + mCancel;
	}

	private static final Pool<BitmapOptions> mPool = new Pool<BitmapOptions>(4);

	public static BitmapOptions obtain() {
		BitmapOptions opts;
		synchronized (mPool) {
			opts = mPool.poll();
		}
		if (opts != null)
			opts.reset();
		else
			opts = new BitmapOptions();
		return opts;
	}

	public static void recycle(BitmapOptions opts) {
		synchronized (mPool) {
			mPool.recycle(opts);
		}
	}

	private static Field mNativeAlloc;

	static {
		try {
			mNativeAlloc = BitmapFactory.Options.class.getField("inNativeAlloc");
		} catch (Throwable e) {
		}
	}
}
