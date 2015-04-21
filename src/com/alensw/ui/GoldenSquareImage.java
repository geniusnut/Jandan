package com.alensw.ui;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by yw07 on 15-3-13.
 */
public class GoldenSquareImage extends ImageView {

	private final Matrix mMatrix = new Matrix();
	;
	float x_gravity = 0.5f;
	float y_gravity = 0.5f;

	public GoldenSquareImage(Context context) {
		super(context);
	}

	public GoldenSquareImage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GoldenSquareImage(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@SuppressWarnings("unused")
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int childWidthSize;
		if (widthMode == MeasureSpec.EXACTLY) {
			childWidthSize = MeasureSpec.getSize(widthMeasureSpec);
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			childWidthSize = getMeasuredWidth();
		}
		heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (childWidthSize * 0.618), MeasureSpec.EXACTLY);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		resetMatrix(drawable);
		super.setImageDrawable(drawable);
	}

	public void setGravity(float x, float y) {
		this.x_gravity = x;
		this.y_gravity = y;
	}

	public void setImageDrawable(Drawable drawable, float x, float y) {
		super.setImageDrawable(drawable);
		resetDrawableGravity(x, y);
	}

	public void resetDrawableGravity(float x, float y) {
		setGravity(x, y);
		resetMatrix(getDrawable());
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		resetMatrix(getDrawable());
		super.onSizeChanged(w, h, oldw, oldh);
	}

	private void resetMatrix(Drawable d) {
		if (d == null) {
			return;
		}
		if (getScaleType() != ImageView.ScaleType.MATRIX) {
			return;
		}
		final Matrix mDrawMatrix = mMatrix;
		final int dwidth = d.getIntrinsicWidth();
		final int dheight = d.getIntrinsicHeight();
		final int vwidth = getWidth() - getPaddingLeft() - getPaddingRight();
		final int vheight = getHeight() - getPaddingTop() - getPaddingBottom();

		float scale;
		float dx = 0, dy = 0;
		if (dwidth * vheight > vwidth * dheight) {
			scale = (float) vheight / (float) dheight;
			final float vHalfW = vwidth * 0.5f;
			final float dScaledW = dwidth * scale;
			final float dPositionW = dScaledW * x_gravity;
			if (dPositionW > vHalfW) {
				dx = -Math.min(dPositionW - vHalfW, dScaledW - vwidth);
			}
		} else {
			scale = (float) vwidth / (float) dwidth;
			final float vHalfH = vheight * 0.5f;
			final float dScaledH = dheight * scale;
			final float dPositionH = dScaledH * y_gravity;
			if (dPositionH > vHalfH) {
				dy = -Math.min(dPositionH - vHalfH, dScaledH - vheight);
			}
		}
		mDrawMatrix.setScale(scale, scale);
		mDrawMatrix.postTranslate((int) (dx + x_gravity), (int) (dy + y_gravity));
		setImageMatrix(mDrawMatrix);
	}
}
