package com.alensw.view;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ThumbAnimation extends Animation {
	private final boolean mEnter;
	private final boolean mFade;
	private final RectF mThumb;

	private float mFromS, mDeltaS;
	private float mCenterX, mCenterY;
	private float mFromX, mFromY;
	private float mDeltaX, mDeltaY;

	private float mPercent;
	private boolean mUpdated;

	public ThumbAnimation(boolean enter, boolean fade, RectF rcThumb, RectF rcFull) {
		mEnter = enter;
		mFade = fade;
		mThumb = rcThumb;

		if (rcFull != null)
			update(rcFull, null);
	}

	@Override
	protected void applyTransformation(float interpolation, Transformation tf) {
		mPercent = mEnter ? interpolation : (1 - interpolation);

		if (!mUpdated) {
			tf.setAlpha(mEnter ? 0 : 255);
			tf.setTransformationType(Transformation.TYPE_ALPHA);
			return;
		}

		final Matrix matrix = tf.getMatrix();
		final float scale = mFromS + mDeltaS * interpolation;
		matrix.setScale(scale, scale, mCenterX, mCenterY);
		matrix.postTranslate(mFromX + mDeltaX * interpolation, mFromY + mDeltaY * interpolation);

		if (mFade)
			tf.setAlpha(mEnter ? interpolation : (1 - interpolation));
		else
			tf.setTransformationType(Transformation.TYPE_MATRIX);
	}

	@Override
	public boolean willChangeBounds() {
		return false;
	}

	public boolean update(RectF rcFull, RectF rcClip) {
		final float cxFull = rcFull.width();
		final float cyFull = rcFull.height();
		final float cxThumb = mThumb.width();
		final float cyThumb = mThumb.height();

		final float scaleX = cxThumb / cxFull;
		final float scaleY = cyThumb / cyFull;
		final float scale = Math.max(scaleX, scaleY);
		final float toS = mEnter ? 1 : scale;
		mFromS = mEnter ? scale : 1;
		mDeltaS = toS - mFromS;

		final RectF rcFrom = mEnter ? mThumb : rcFull;
		final RectF rcTo = mEnter ? rcFull : mThumb;
		final float fromX = rcFrom.centerX();
		final float fromY = rcFrom.centerY();
		final float toX = rcTo.centerX();
		final float toY = rcTo.centerY();
		mCenterX = mEnter ? toX : fromX;
		mCenterY = mEnter ? toY : fromY;
		mDeltaX = toX - fromX;
		mDeltaY = toY - fromY;
		mFromX = mEnter ? -mDeltaX : 0;
		mFromY = mEnter ? -mDeltaY : 0;

		boolean clip = false;
		if (rcClip != null) {
			final float cx = cxThumb / scale;
			final float cy = cyThumb / scale;
			rcClip.set(rcFull);
			if (cxFull > cx) {
				final float dx = (cxFull - cx) / 2 * (1 - mPercent);
				rcClip.inset(dx, 0);
				clip = true;
			} else if (cyFull > cy) {
				final float dy = (cyFull - cy) / 2 * (1 - mPercent);
				rcClip.inset(0, dy);
				clip = true;
			}
		}
		mUpdated = true;
		return clip;
	}
}