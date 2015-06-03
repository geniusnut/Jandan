package com.nut.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.widget.FrameLayout;

/**
 * Created by yw07 on 15-5-18.
 */
public class AddFloatingActionButton extends FloatingActionButton {
	public AddFloatingActionButton(Context context, FrameLayout.LayoutParams params) {
		super(context, params);
	}

	private float mRotation;

	public AddFloatingActionButton(Builder builder) {
		super(builder);
	}

	@SuppressWarnings("UnusedDeclaration")
	public float getRotation() {
		return mRotation;
	}

	@SuppressWarnings("UnusedDeclaration")
	public void setRotation(float rotation) {
		mRotation = rotation;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.save();
		Matrix rotator = new Matrix();

		int cx = (getWidth() - mBitmap.getWidth()) >> 1;
		int cy = (getHeight() - mBitmap.getHeight()) >> 1;
		rotator.postRotate(mRotation, mBitmap.getWidth() / 2, mBitmap.getHeight() / 2);
		rotator.postTranslate((getWidth() - mBitmap.getWidth()) / 2,
				(getHeight() - mBitmap.getHeight()) / 2);
		setClickable(true);
		//
		canvas.drawCircle(getWidth() / 2, getHeight() / 2, (float) (getWidth() / 2.6), mButtonPaint);
		canvas.drawBitmap(mBitmap, rotator, mDrawablePaint);
	}

	public static class Builder extends FloatingActionButton.Builder<Builder> {
		/**
		 * Constructor using a context for this builder and the
		 * {@link com.nut.ui.FloatingActionButton} it creates
		 *
		 * @param context
		 */
		public Builder(Context context) {
			super(context);
		}


		public AddFloatingActionButton create() {
			params.gravity = this.gravity;

			final AddFloatingActionButton button = new AddFloatingActionButton(context, params);
			button.setColor(this.color);
			button.setDrawable(this.drawable);
			return button;
		}
	}
}
