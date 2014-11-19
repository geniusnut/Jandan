package com.larvalabs.svgandroid;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.PictureDrawable;

public class SvgDrawable extends PictureDrawable {
	private final int mIntrinsicWidth;
	private final int mIntrinsicHeight;
	private Bitmap mBitmap;

	public SvgDrawable(Picture picture, int intrinsicWidth, int intrinsicHeight) {
		super(picture);
		mIntrinsicWidth = intrinsicWidth;
		mIntrinsicHeight = intrinsicHeight;
	}

	@Override
	public void draw(Canvas canvas) {
		if (mBitmap != null) {
			final Rect bounds = getBounds();
			final float x = (bounds.left + bounds.right - mBitmap.getWidth()) / 2f;
			final float y = (bounds.top + bounds.bottom - mBitmap.getHeight()) / 2f;
			canvas.drawBitmap(mBitmap, x, y, null);
		} else {
			super.draw(canvas);
		}
	}

	/**
	 * Returns -1 if it has no intrinsic width, since SVG can scaled smoothly.
	 */
	@Override
	public int getIntrinsicWidth() {
		return mIntrinsicWidth;
	}

	/**
	 * Returns -1 if it has no intrinsic height, since SVG can scaled smoothly.
	 */
	@Override
	public int getIntrinsicHeight() {
		return mIntrinsicHeight;
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		final Picture picture = getPicture();
		final int cxBounds = bounds.right - bounds.left;
		final int cyBounds = bounds.bottom - bounds.top;
		final int cxPicture = picture.getWidth();
		final int cyPicture = picture.getHeight();
		final float scaleX = (float) cxBounds / cxPicture;
		final float scaleY = (float) cyBounds / cyPicture;
		final float scale = Math.min(scaleX, scaleY);
		final int width = Math.round(cxPicture * scale);
		final int height = Math.round(cyPicture * scale);

		if (mBitmap != null) {
			if (mBitmap.getWidth() == width && mBitmap.getHeight() == height)
				return;
			mBitmap.recycle();
			mBitmap = null;
		}

		//  drawing a picture to a hardware acceleration can not work, so we create a bitmap as a buffer
		try {
			mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			if (mBitmap != null) {
				final Canvas canvas = new Canvas(mBitmap);
				canvas.scale(scale, scale);
				canvas.translate((width - cxPicture * scale) / 2, (height - cyPicture * scale) / 2);
				picture.draw(canvas);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
