package com.alensw.support.picture;

import android.graphics.*;
import android.graphics.drawable.shapes.Shape;

import java.io.ByteArrayOutputStream;

public class Image extends RefObject<Image> {
	public static class Info {
		public int width;
		public int height;
		public int components;    //	0, 1, 3
		public int rotation;    //	0, 90, 180, 270
		public boolean stereo;
	}

	public final Info mInfo;
	private Bitmap mBitmap;

	public Image(Bitmap bitmap) {
		this(bitmap, 0, 0);
	}

	public Image(Bitmap bitmap, int components, int rotation) {
		this(bitmap.getWidth(), bitmap.getHeight(), components, rotation);
		mBitmap = bitmap;
	}

	public Image(int width, int height, int components, int rotation) {
		mInfo = new Info();
		mInfo.width = width;
		mInfo.height = height;
		mInfo.components = components;
		mInfo.rotation = (rotation + 360) % 360;
	}

	/*	@Override
		protected void finalize() {
			if (mBitmap != null && !mBitmap.isRecycled())
				Log.w("Image", "leak bitmap: " + this + ", ref=" + refCount());
		}
	*/
	@Override
	protected void recycle() {
		final Bitmap bitmap = mBitmap;
		mBitmap = null;
		if (bitmap != null)
			recycle(bitmap);
	}

	//	Overridable
	protected void recycle(Bitmap bitmap) {
		bitmap.recycle();
	}

	@Override
	public String toString() {
		return "(" + getWidth() + "x" + getHeight() + "x" + mInfo.rotation + '\u00b0' + (mInfo.stereo ? "/2)" : ")");
	}

	public final Bitmap getBitmap() {
		return mBitmap;
	}

	public final int getBitmapWidth() {
		return mInfo.width;
	}

	public final int getBitmapHeight() {
		return mInfo.height;
	}

	public final int getWidth() {
		if (mInfo.rotation % 180 == 0)
			return mInfo.stereo ? mInfo.width / 2 : mInfo.width;
		else
			return mInfo.height;
	}

	public final int getHeight() {
		if (mInfo.rotation % 180 == 0)
			return mInfo.height;
		else
			return mInfo.stereo ? mInfo.width / 2 : mInfo.width;
	}

	public final boolean hasAlpha() {
		return mBitmap != null && mBitmap.hasAlpha() && mBitmap.getConfig() == Bitmap.Config.ARGB_8888;
	}

	public final boolean hasBitmap() {
		return mBitmap != null && !mBitmap.isRecycled();
	}

	public final boolean isError() {
		return mInfo.width <= 0 || mInfo.height <= 0;
	}

	public int rotate(int degrees) {
		mInfo.rotation += degrees;
		if (mInfo.rotation < 0)
			mInfo.rotation += 360;
		mInfo.rotation %= 360;
		return mInfo.rotation;
	}

	public void getMatrix(Matrix matrix) {
		matrix.reset();
		if (mInfo.rotation != 0) {
			// We want to do the rotation at origin, but since the bounding
			// rectangle will be changed after rotation, so the delta values
			// are based on old & new width/height respectively.
			final int cxBmp = mInfo.stereo ? mInfo.width / 2 : mInfo.width;
			final int cyBmp = mInfo.height;
			matrix.preTranslate(-cxBmp / 2, -cyBmp / 2);
			matrix.postRotate(mInfo.rotation);
			matrix.postTranslate(getWidth() / 2, getHeight() / 2);
		}
	}

	public static final int MATRIX_FULL = 0;
	public static final int MATRIX_INSIDE = 1;
	public static final int MATRIX_CROP = 2;

	public void getProperMatrix(Matrix matrix, float cxView, float cyView, int type) {
		final int cxImage = getWidth();
		final int cyImage = getHeight();
		getMatrix(matrix);
		if (cxImage >= 0 && cyImage >= 0)
			getProperMatrix(matrix, cxImage, cyImage, cxView, cyView, type);
	}

	public static void getProperMatrix(Matrix matrix, float cxImage, float cyImage, float cxView, float cyView, int type) {
		float scaleX = cxView / cxImage;
		float scaleY = cyView / cyImage;
		if (type == MATRIX_INSIDE)
			scaleX = scaleY = Math.min(scaleX, scaleY);
		else if (type == MATRIX_CROP)
			scaleX = scaleY = Math.max(scaleX, scaleY);
		matrix.postScale(scaleX, scaleY);
		matrix.postTranslate((cxView - cxImage * scaleX) / 2, (cyView - cyImage * scaleY) / 2);
	}

	public void mapRect(Matrix matrix, RectF rect) {
		rect.set(0, 0, mInfo.width, mInfo.height);
		matrix.mapRect(rect);
		if (mInfo.stereo) {
			switch (mInfo.rotation) {
				case 0:
					rect.right = rect.left + rect.width() / 2;
					break;
				case 180:
					rect.left = rect.right - rect.width() / 2;
					break;
				case 90:
					rect.bottom = rect.top + rect.height() / 2;
					break;
				case 270:
					rect.top = rect.bottom - rect.height() / 2;
					break;
			}
		}
	}

	public Image extractThumbnail(int width, int height, boolean crop, Bitmap.Config config) {
		if (!hasBitmap())
			return null;

		final Matrix matrix = new Matrix();
		if (crop) {
			getProperMatrix(matrix, width, height, MATRIX_CROP);
		} else {
			final int cxBmp = getWidth();
			final int cyBmp = getHeight();
			final float scale = Math.min((float) width / cxBmp, (float) height / cyBmp);
			final float cxView = cxBmp * scale;
			final float cyView = cyBmp * scale;
			getProperMatrix(matrix, cxView, cyView, MATRIX_INSIDE);
			width = Math.round(cxView);
			height = Math.round(cyView);
		}

		final Bitmap bitmap = mBitmap;
		if (config == null)
			config = bitmap.getConfig();
		if (config == null)
			config = Bitmap.Config.RGB_565;

		final Bitmap newBmp = BitmapUtils.create(width, height, config);
		if (newBmp == null)
			return null;

		final Canvas canvas = new Canvas(newBmp);
//		if (mBitmap.hasAlpha() && mBitmap.getConfig() == Bitmap.Config.ARGB_8888)
//			canvas.drawRect(0, 0, width, height, mPatternPaint);
		canvas.drawBitmap(bitmap, matrix, new Paint(Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG));
		return new Image(newBmp);
	}

	public void overlayShape(int cxView, int cyView, float scale, Shape shape, Paint paint) {
		if (!hasBitmap())
			return;

		final Bitmap bitmap = mBitmap;
		final int cxBmp = bitmap.getWidth();
		final int cyBmp = bitmap.getHeight();
		Bitmap newBmp = null;
		if (!bitmap.isMutable()) {
			newBmp = BitmapUtils.create(cxBmp, cyBmp, mBitmap.getConfig());
			if (newBmp == null)
				return;
		}

		final Canvas canvas = new Canvas(newBmp != null ? newBmp : bitmap);
		if (newBmp != null)
			canvas.drawBitmap(bitmap, 0, 0, null);

		final float size = Math.max(cxView * scale, cyView * scale);
		final float dx = (cxBmp - size) / 2;
		final float dy = (cyBmp - size) / 2;
		canvas.translate(dx, dy);
		shape.resize(size, size);
		shape.draw(canvas, paint);
		canvas.translate(-dx, -dy);

		if (newBmp != null) {
			bitmap.recycle();
			mBitmap = newBmp;
			mInfo.width = cxBmp;
			mInfo.height = cyBmp;
		}
	}

	public byte[] compress(Bitmap.CompressFormat format, int quality) {
		try {
			final ByteArrayOutputStream stream = new ByteArrayOutputStream(8192);
			if (mBitmap.compress(format, quality, stream)) {
				final byte[] data = stream.toByteArray();
				stream.close();
				return data;
			}
		} catch (Throwable e) {
		}
		return null;
	}

	public void draw(Canvas canvas, Rect src, RectF dst, Paint paint) {
		if (hasBitmap())
			canvas.drawBitmap(mBitmap, src, dst, paint);
	}

	public static Image create(int width, int height, Bitmap.Config config) {
		final Bitmap bitmap = BitmapUtils.create(width, height, config);
		if (bitmap != null)
			return new Image(bitmap);
		return null;
	}

	public static Image decodeByteArray(byte[] data, BitmapFactory.Options opts) {
		try {
			final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
			if (bitmap != null)
				return new Image(bitmap);
		} catch (Throwable e) {
		}
		return null;
	}
}
