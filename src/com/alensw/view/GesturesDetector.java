package com.alensw.view;

import android.content.Context;
import android.graphics.PointF;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

public class GesturesDetector {
	private static final int ACTION_NONE = 0;
	private static final int ACTION_TAP = 1;      //  single finger tap
	private static final int ACTION_SCROLL = 2;   //  single finger scroll
	private static final int ACTION_1SCALE = 3;   //  single finger scale after double tap
	private static final int ACTION_2FINGERS = 4; //  two fingers scale and rotate

	private int mActionType = ACTION_NONE;
	private boolean mIsDoubleTapping;
	private boolean mStillDown;
	private boolean mStillInTapRegion;
	private boolean mRotating;
	private float mX0, mY0;
	private float mX1, mY1;
	private float mScale1Factor;
	private float mCurrSpan;
	private float mPrevSpan;
	private float mCurrAngle;
	private float mPrevAngle;
	private MotionEvent mCurrentDownEvent;
	private MotionEvent mPreviousUpEvent;
	private VelocityTracker mVelocityTracker;

	private final int mMaxVelocity;
	private final int mMinVelocity;
	private final int mDoubleTapTimeout;
	private final float mTouchSlop;
	private final float mSpanSlop;
	private final float mDoubleTapSlop;
	private final float mRotateSlop;
	private final Handler mHandler;
	private final OnGestureListener mListener;
	//	private final TouchHistory mTouchHistory;
	private final PointF mTapPoint = new PointF();            //	for tap
	private final PointF mPrevScrollPoint = new PointF();    //	for scroll
	private final PointF mCurrPivot = new PointF();        //	for scale
	private final PointF mPrevPivot = new PointF();

	public interface OnGestureListener {
		public abstract void onTapDown(float pivotX, float pivotY);

		public abstract void onSingleTap(float pivotX, float pivotY);

		public abstract void onDoubleTap(float pivotX, float pivotY);

		public abstract void onScrollBy(float distanceX, float distanceY);

		public abstract void onScrollEnd();

		public abstract void onFling(float velocityX, float velocityY);

		public abstract boolean onScaleBy(float scaleBy, float pivotX, float pivotY);

		public abstract boolean onRotateBy(float rotateBy, float pivotX, float pivotY);

		public abstract void onScaleOrRotateEnd(float pivotX, float pivotY);
	}

	private boolean mSingleTapPosted = false;
	private final Runnable mSingleTapTask = new Runnable() {
		public void run() {
			mSingleTapPosted = false;
			// If the user's finger is still down, do not count it as a tap
			if (!mStillDown && mActionType == ACTION_TAP)
				mListener.onSingleTap(mTapPoint.x, mTapPoint.y);
		}
	};

	public GesturesDetector(Context context, OnGestureListener listener, Handler handler) {
		mListener = listener;

		if (handler == null)
			handler = new Handler();
		mHandler = handler;

		//	mTouchHistory = new TouchHistory(context);

		final ViewConfiguration configuration = ViewConfiguration.get(context);
		mTouchSlop = mSpanSlop = configuration.getScaledTouchSlop();
		mDoubleTapSlop = configuration.getScaledDoubleTapSlop();
		mMaxVelocity = configuration.getScaledMaximumFlingVelocity();
		mMinVelocity = configuration.getScaledMinimumFlingVelocity();
		mDoubleTapTimeout = ViewConfiguration.getDoubleTapTimeout();
		mRotateSlop = context.getResources().getDisplayMetrics().density * 2;
		//	Log.d("GesturesDetector", "touchslop=" + mTouchSlop + ", maxvelocity=" + mMaxVelocity + ", minvelocity=" + mMinVelocity);
	}

	public float getCurrentVelocity() {
		if (mVelocityTracker != null) {
			final float velocityX = mVelocityTracker.getXVelocity();
			final float velocityY = mVelocityTracker.getYVelocity();
			return (float) Math.hypot(velocityX, velocityY);
		}
		return 0;
	}

	public boolean onTouchEvent(final MotionEvent e) {
		final int action = e.getAction() & MotionEvent.ACTION_MASK;
		final float x = e.getX();
		final float y = e.getY();

		if (mVelocityTracker == null)
			mVelocityTracker = VelocityTracker.obtain();
		mVelocityTracker.addMovement(e);

		//	mTouchHistory.add(e);

		switch (action) {
			case MotionEvent.ACTION_MOVE:
				if (mActionType == ACTION_2FINGERS) {
					final float hx0 = mX0, hy0 = mY0;
					final float hx1 = mX1, hy1 = mY1;
					if (e.getPointerCount() > 1)
						computerScaleAndRotation(e);
					if (checkSameDirection(hx0, hy0, hx1, hy1)) {
						mListener.onScrollBy(mCurrPivot.x - mPrevPivot.x, mCurrPivot.y - mPrevPivot.y);
						mRotating = false;
					} else {
						final float deltaSpan = Math.abs(mCurrSpan - mPrevSpan);
						if (mPrevSpan > 0 && mPrevSpan != mCurrSpan) {
							if (mListener.onScaleBy(mCurrSpan / mPrevSpan, mCurrPivot.x, mCurrPivot.y))
								mPrevSpan = mCurrSpan;
						}
						if (!mRotating) {
							if (deltaSpan > mRotateSlop) {
								mPrevAngle = mCurrAngle;
							} else if (Math.abs(mCurrAngle - mPrevAngle) > 3) {
								//	Log.d("Gestures", "2 fingers: " + deltaSpan + "/" + mRotateSlop + ", " + mPrevAngle + "->" + mCurrAngle);
								mRotating = true;
								mPrevAngle = mCurrAngle;
							}
						}
						if (mRotating && mPrevAngle != mCurrAngle) {
							if (mListener.onRotateBy(mCurrAngle - mPrevAngle, mCurrPivot.x, mCurrPivot.y))
								mPrevAngle = mCurrAngle;
						}
					}
					mPrevPivot.set(mCurrPivot.x, mCurrPivot.y);
				} else if (mActionType == ACTION_1SCALE) {
					final float scaleBy = computerScaleBy1(y);
					if (mListener.onScaleBy(scaleBy, mCurrPivot.x, mCurrPivot.y))
						mPrevSpan = mCurrSpan;
				} else if (mActionType == ACTION_SCROLL) {
					mListener.onScrollBy(x - mPrevScrollPoint.x, y - mPrevScrollPoint.y);
					mPrevScrollPoint.set(x, y);
				} else if (mActionType == ACTION_TAP) {
					final float dx = x - mTapPoint.x;
					final float dy = y - mTapPoint.y;
					if (mIsDoubleTapping) {
						mCurrPivot.set(mTapPoint);
						computerScaleBy1(y);
						if (Math.abs(dy) > mSpanSlop) {
							mActionType = ACTION_1SCALE;
							mPrevSpan = mCurrSpan;
							mScale1Factor = (float) Math.sqrt((Math.abs(mTapPoint.y) + mSpanSlop) / (mSpanSlop * 2));
						}
					} else if (Math.max(Math.abs(dx), Math.abs(dy)) > mTouchSlop) {
						mActionType = ACTION_SCROLL;
						mStillInTapRegion = false;
						mPrevScrollPoint.set(x, y);
					}
				}
				break;

			case MotionEvent.ACTION_DOWN: {
				final boolean posted = mSingleTapPosted;
				if (mSingleTapPosted) {
					mHandler.removeCallbacks(mSingleTapTask);
					mSingleTapPosted = false;
				}
				if (posted && mCurrentDownEvent != null && mPreviousUpEvent != null
						&& isConsideredDoubleTap(mCurrentDownEvent, mPreviousUpEvent, e)) {
					// This is a second tap
					mIsDoubleTapping = true;
				} else {
					// This is a first tap
					mHandler.postDelayed(mSingleTapTask, mDoubleTapTimeout);
					mSingleTapPosted = true;
				}
			}
			mStillDown = true;
			mStillInTapRegion = true;
			mActionType = ACTION_TAP;
			mTapPoint.set(x, y);
			if (mCurrentDownEvent != null)
				mCurrentDownEvent.recycle();
			mCurrentDownEvent = MotionEvent.obtain(e);
			mListener.onTapDown(mTapPoint.x, mTapPoint.y);
			break;

			case MotionEvent.ACTION_POINTER_DOWN:
				if (e.getPointerCount() > 1)
					computerScaleAndRotation(e);
				if (mCurrSpan > mSpanSlop) {
					mActionType = ACTION_2FINGERS;
					mPrevPivot.set(mCurrPivot.x, mCurrPivot.y);
					mPrevSpan = mCurrSpan;
					mPrevAngle = mCurrAngle;
				}
				mIsDoubleTapping = false;
				break;

			case MotionEvent.ACTION_POINTER_UP:
				if (mActionType == ACTION_2FINGERS) {
					mListener.onScaleOrRotateEnd(mCurrPivot.x, mCurrPivot.y);
					mActionType = ACTION_NONE;
					mPrevSpan = 0;
					mRotating = false;
				}
				break;

			case MotionEvent.ACTION_UP:
				mStillDown = false;
				if (mActionType == ACTION_1SCALE) {
					mListener.onScaleOrRotateEnd(mCurrPivot.x, mCurrPivot.y);
				} else if (mActionType == ACTION_SCROLL) {
					final VelocityTracker velocityTracker = mVelocityTracker;
					velocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
					final float velocityX = velocityTracker.getXVelocity();
					final float velocityY = velocityTracker.getYVelocity();
					final float velocity = Math.max(Math.abs(velocityX), Math.abs(velocityY));
					if (velocity > mMinVelocity)
						mListener.onFling(velocityX, velocityY);
					else
						mListener.onScrollEnd();
				} else if (mIsDoubleTapping) {
					mListener.onDoubleTap(mTapPoint.x, mTapPoint.y);
				}
				if (mPreviousUpEvent != null)
					mPreviousUpEvent.recycle();
				mPreviousUpEvent = MotionEvent.obtain(e);
				//	pass through;

			case MotionEvent.ACTION_CANCEL:
				if (mVelocityTracker != null) {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}
				if (action == MotionEvent.ACTION_CANCEL)
					mActionType = ACTION_NONE;
				mStillDown = false;
				mIsDoubleTapping = false;
				mRotating = false;
				//	mTouchHistory.reset();
				break;
		}
		return true;
	}

	private boolean isConsideredDoubleTap(MotionEvent firstDown,
										  MotionEvent firstUp, MotionEvent secondDown) {
		if (!mStillInTapRegion)
			return false;
		if (secondDown.getEventTime() - firstUp.getEventTime() > mDoubleTapTimeout)
			return false;

		final float x0 = firstDown.getX(), y0 = firstDown.getY();
		final float x1 = secondDown.getX(), y1 = secondDown.getY();
		return distance(x0, y0, x1, y1) < mDoubleTapSlop;
	}

	private boolean checkSameDirection(float hx0, float hy0, float hx1, float hy1) {
		float dx0 = mX0 - hx0, dy0 = mY0 - hy0;
		float dx1 = mX1 - hx1, dy1 = mY1 - hy1;
		final float sx = dx0 * dx1;
		final float sy = dy0 * dy1;
		final boolean same = sx > 0 && sy > 0;// && !(sx == 0 && sy == 0);
		//	Log.d("GesturesDetector", "check Direction: (" + dx0 + "," + dx1 + "),(" + dy0 + "," + dy1 + ")" + ", same=" + same);
		return same;
	}

	private float computerScaleBy1(float y) {
		mCurrSpan = Math.abs(y) + mSpanSlop;
		if (mCurrSpan > mSpanSlop && mPrevSpan > 0) {
			final boolean scaleUp = mCurrSpan >= mPrevSpan;
			final float spanDiff = Math.abs(1 - (mCurrSpan / mPrevSpan)) * mScale1Factor;
			return scaleUp ? (1 + spanDiff) : (1 - spanDiff);
		}
		return 1;
	}

	private void computerScaleAndRotation(MotionEvent e) {
		mX0 = e.getX(0);
		mY0 = e.getY(0);
		mX1 = e.getX(1);
		mY1 = e.getY(1);

		mCurrPivot.set((mX0 + mX1) / 2, (mY0 + mY1) / 2);
		mCurrSpan = distance(mX0, mY0, mX1, mY1);
		mCurrAngle = angle(mX0, mY0, mX1, mY1);
	}

	public static float distance(float x1, float y1, float x2, float y2) {
		final float dx = x2 - x1;
		final float dy = y2 - y1;
		return (float) Math.hypot(dx, dy);
	}

	public static float angle(float x1, float y1, float x2, float y2) {
		final float dx = x2 - x1;
		final float dy = y2 - y1;
		return (float) Math.toDegrees(Math.atan2(dy, dx));
	}
}

/**    copy from android.view.ScaleGestureDetector
 * The touchMajor/touchMinor elements of a MotionEvent can flutter/jitter on
 * some hardware/driver combos. Smooth it out to get kinder, gentler
 * behavior.
 */
/*class TouchHistory {
	private static final long TOUCH_STABILIZE_TIME = 128;	//	ms

	private int mTouchHistoryDirection;
	private int mTouchMinMajor;
	private float mTouchUpper;
	private float mTouchLower;
	private float mTouchHistoryLastAccepted;
	private long mTouchHistoryLastAcceptedTime;

	public TouchHistory(Context context) {
		final Resources res = context.getResources();
		final int id = res.getIdentifier("config_minScalingTouchMajor", "dimen", "android");
		mTouchMinMajor = id != 0 ? res.getDimensionPixelSize(id) : Math.round(48 * res.getDisplayMetrics().density);

		reset();
	}

	public final float lastAccepted() {
		return mTouchHistoryLastAccepted;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public void add(MotionEvent ev) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			final long currentTime = SystemClock.uptimeMillis();
			final int count = ev.getPointerCount();
			boolean accept = currentTime - mTouchHistoryLastAcceptedTime >= TOUCH_STABILIZE_TIME;
			float total = 0;
			int sampleCount = 0;
			for (int i = 0; i < count; i++) {
				final boolean hasLastAccepted = !Float.isNaN(mTouchHistoryLastAccepted);
				final int historySize = ev.getHistorySize();
				final int pointerSampleCount = historySize + 1;
				for (int h = 0; h < pointerSampleCount; h++) {
					float major;
					if (h < historySize)
						major = ev.getHistoricalTouchMajor(i, h);
					else
						major = ev.getTouchMajor(i);
					if (major < mTouchMinMajor)
						major = mTouchMinMajor;
					total += major;

					if (Float.isNaN(mTouchUpper) || major > mTouchUpper)
						mTouchUpper = major;
					if (Float.isNaN(mTouchLower) || major < mTouchLower)
						mTouchLower = major;

					if (hasLastAccepted) {
						final int directionSig = (int) Math.signum(major - mTouchHistoryLastAccepted);
						if (directionSig != mTouchHistoryDirection
								|| (directionSig == 0 && mTouchHistoryDirection == 0)) {
							mTouchHistoryDirection = directionSig;
							final long time = h < historySize ? ev.getHistoricalEventTime(h) : ev.getEventTime();
							mTouchHistoryLastAcceptedTime = time;
							accept = false;
						}
					}
				}
				sampleCount += pointerSampleCount;
			}

			if (accept) {
				final float avg = total / sampleCount;
				float newAccepted = (mTouchUpper + mTouchLower + avg) / 3;
				mTouchUpper = (mTouchUpper + newAccepted) / 2;
				mTouchLower = (mTouchLower + newAccepted) / 2;
				mTouchHistoryLastAccepted = newAccepted;
				mTouchHistoryDirection = 0;
				mTouchHistoryLastAcceptedTime = ev.getEventTime();
			}
		} else {
			//	version < 2.3
			mTouchHistoryLastAccepted = mTouchMinMajor;
		}
	}

	public void reset() {
		mTouchUpper = Float.NaN;
		mTouchLower = Float.NaN;
		mTouchHistoryLastAccepted = Float.NaN;
	}
}*/
