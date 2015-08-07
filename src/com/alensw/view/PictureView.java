package com.alensw.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.animation.Animation;
import com.alensw.support.picture.Picture;

@SuppressLint("Instantiatable")
public class PictureView extends BasePictureView {
	private final GesturesDetector mGestureDetector;
	private final RectF mClipRect = new RectF();
	private boolean mHasPicture;
	private int mScreenOffsetY;

	public PictureView(Context context) {
		this(context, null);
	}

	public PictureView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mGestureDetector = new GesturesDetector(context, mGestureListener, getHandler());
	}

	@Override
	public void setPicture(Picture picture, boolean reset) {
		super.setPicture(picture, reset);

		mHasPicture = picture != null;
		if (mHasPicture)
			updateAnimation();
	}

	@Override
	public void draw(Canvas canvas) {
		boolean clip = false;

		if (mHasPicture) {
			final RectF rcClip = updateAnimation();
			clip = rcClip != null;
			if (clip) {
				canvas.save(Canvas.CLIP_SAVE_FLAG);
				canvas.clipRect(rcClip, Region.Op.INTERSECT);
			}
		}

		if (mScreenOffsetY != 0)
			canvas.translate(0, mScreenOffsetY);

		super.draw(canvas);

		if (mScreenOffsetY != 0)
			canvas.translate(0, -mScreenOffsetY);

		if (clip) {
			canvas.restore();
			// force call onDraw() next time
			invalidateOnAnimation();
		}
	}

	public void invalidateOnAnimation() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			postInvalidateOnAnimation();
		else
			invalidate();
	}

	private RectF updateAnimation() {
		final Animation animation = getAnimation();
		if (animation instanceof ThumbAnimation) {
			final RectF rcClip = mClipRect;
			rcClip.set(0, 0, mViewWidth, mViewHeight);
			rcClip.intersect(mDrawRect);
			rcClip.offset(0, mScreenOffsetY);

			final ThumbAnimation tAnimation = (ThumbAnimation) animation;
			if (tAnimation.update(rcClip, rcClip))
				return rcClip;
		}
		return null;
	}

	@Override
	protected void onSizeChanged(int width, int height, int oldw, int oldh) {
		finishCurrentTasks();

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			//	Check the screen is switching from full to normal,
			//	we draw the pictures always in full screen's coordinate to avoid jumping
			final int pos[] = new int[2];
			getLocationOnScreen(pos);
			mScreenOffsetY = -pos[1];
			height += pos[1];
			//	Log.i("PictureView", "pos: " + pos[1]);
		}
		super.onSizeChanged(width, height, oldw, oldh);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
	}

	private final GesturesDetector.OnGestureListener mGestureListener = new GesturesDetector.OnGestureListener() {
		private float mStartRotation;
		private float mLastRotateBy;

		public void onTapDown(float pivotX, float pivotY) {
			if (isFling())
				finishCurrentTasks();

			mStartRotation = getTransRotation();
			mLastRotateBy = 0;
		}

		public void onSingleTap(float pivotX, float pivotY) {
			if (mListener != null && isShown())
				mListener.onSingleTap(pivotX, pivotY);
		}

		public void onDoubleTap(float pivotX, float pivotY) {
			if (mListener != null && isShown())
				mListener.onDoubleTap(pivotX, pivotY);
		}

		public void onScrollBy(float distanceX, float distanceY) {
			moveByCheck(distanceX, distanceY);
		}

		public void onScrollEnd() {
			moveToNextOrSpringBack(mVelocityMin);
		}

		public void onFling(float velocityX, float velocityY) {
			doFling(velocityX, velocityY);
		}

		public boolean onScaleBy(float scaleBy, float pivotX, float pivotY) {
			if (getPictureType() <= Picture.TYPE_BLANK)
				return false;
			if (Float.isNaN(scaleBy) || Float.isInfinite(scaleBy))
				return false;
			if (scaleBy < 1) {
				final float minWidth = Math.min(getPictureWidth(), 16);
				final float minHeight = Math.min(getPictureHeight(), 16);
				final float cxDraw = mDrawRect.width();
				final float cyDraw = mDrawRect.height();
				if (cxDraw * scaleBy < minWidth || cyDraw * scaleBy < minHeight)
					return false;
			}

			zoomBy(scaleBy, pivotX, pivotY);

			if (mListener != null && isShown())
				mListener.onZoom(scaleBy, true);
			return true;
		}

		public boolean onRotateBy(float rotateBy, float pivotX, float pivotY) {
			if (getPictureType() <= Picture.TYPE_BLANK)
				return false;

			rotateBy(rotateBy, pivotX, pivotY);
			mLastRotateBy = rotateBy;
			return true;
		}

		public void onScaleOrRotateEnd(float pivotX, float pivotY) {
			final float rotation = getTransRotation();
			if (rotation != 0 || mStartRotation != 0) {
				final int dir = (int) Math.signum(mLastRotateBy);
				final float rotateTo;
				if (Math.abs(rotation - mStartRotation) < 20)
					rotateTo = 0;
				else if (dir > 0)
					rotateTo = rotation + 45;
				else if (dir < 0)
					rotateTo = rotation - 45;
				else
					rotateTo = rotation;
				// round to 90
				rotateToAnimate(Math.round(rotateTo / 90) * 90, false, pivotX, pivotY, mAnimationDuration, null);
				//	Log.d("Picture", "rotate: " + rotation + "->" + rotateTo + "->" + Math.round(rotateTo / 90) * 90 + ", " + dir);
			} else {
				zoomSpringBack(pivotX, pivotY);
			}
		}
	};

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public boolean onGenericMotionEvent(MotionEvent e) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			if ((e.getSource() & InputDevice.SOURCE_CLASS_POINTER) != 0) {
				switch (e.getAction()) {
					case MotionEvent.ACTION_SCROLL:
						final float deltaX = e.getAxisValue(MotionEvent.AXIS_VSCROLL);
						if (deltaX != 0) {
							moveToNextAnimate(Math.signum(deltaX) * mVelocityMax / 2);
							return true;
						}
				}
			}
			return super.onGenericMotionEvent(e);
		}
		return true;
	}

	@Override
	public void getFocusedRect(Rect rect) {
		final int left = getPaddingLeft();
		final int centerY = mViewHeight / 2;
		rect.set(left, centerY - 10, left + 20, centerY + 10);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		final float velocity = mVelocitySwipe * 2;
		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
				if (mListener != null && isShown())
					mListener.onSingleTap(mViewWidth / 2, mViewHeight / 2);
				break;

			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				finishCurrentTasks();
				if (doFling((keyCode == KeyEvent.KEYCODE_DPAD_LEFT) ? velocity : -velocity, 0))
					return true;
				break;

			case KeyEvent.KEYCODE_DPAD_DOWN:
				finishCurrentTasks();
				if (mDrawRect.bottom - mViewHeight > 1 && doFling(0, -velocity))
					return true;
				if (getTransScale() > 1 && zoom(false) < 1)
					return true;
				break;

			case KeyEvent.KEYCODE_DPAD_UP:
				finishCurrentTasks();
				if (mDrawRect.top < -1 && doFling(0, velocity))
					return true;
				//	if (getTransScale() < ZOOM_MAX && zoom(true) > 1)
				//		return true;
				break;

			case KeyEvent.KEYCODE_PAGE_DOWN:
			case KeyEvent.KEYCODE_PAGE_UP:
				finishCurrentTasks();
				if (moveToNextAnimate((keyCode == KeyEvent.KEYCODE_PAGE_UP) ? velocity : -velocity))
					return true;
				break;
		}
		return super.onKeyDown(keyCode, event);
	}
}
