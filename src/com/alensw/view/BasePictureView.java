package com.alensw.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.widget.OverScroller;
import com.alensw.shape.Shapes;
import com.alensw.support.picture.BitmapOptions;
import com.alensw.support.picture.BitmapUtils;
import com.alensw.support.picture.Image;
import com.alensw.support.picture.Picture;
import com.nut.Jandan.JandanApp;
import com.nut.Jandan.R;

public class BasePictureView extends View {
	public static final boolean HAS_HW_ACCELERATION = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;

	public static final int SCALE_INSIDE = 0;
	public static final int SCALE_CROP = 1;
	public static final int SCALE_ACTUAL = 2;
	public static final int SCALE_COUNT = 3;

	public static final float SWIPE_VELOCITY = 256;    //	dip/second
	public static final float ZOOM_MAX = 4.0f;
	public static final float ZOOM_STEP = 1.4142135624f;

	protected int mTextColor;
	protected int mWarnColor;
	protected int mNextPicDir;
	protected int mScaleMode;
	protected int mViewWidth;
	protected int mViewHeight;
	protected int mWidthToSwitch;
	protected boolean mMoving;
	protected boolean mMovingToNext;
	protected boolean mRotating;
	protected boolean mTransforming;
	//	protected boolean mSmallerThanView;
	private Picture mPicture;        //	preview picture
	private Picture mFullPicture;    //	full picture
	protected SimpleAnimator mAnimator;
	protected OnListener mListener;
	protected final Interpolator mInterpolator;
	protected final Paint mBitmapPaint;
	protected final Paint mBkgndPaint;
	protected final Paint mTextPaint;
	protected final RectF mDrawRect = new RectF();
	protected final RectF mRestrictRect = new RectF();
	protected final Matrix mMatrixBase = new Matrix();        //	centered and scaled matrix
	protected final Matrix mMatrixTrans = new Matrix();        //	transformation matrix
	protected final Matrix mMatrixDraw = new Matrix();        //	display matrix: base + transformation
	protected final Matrix mFullMatrixBase = new Matrix();    //	for full picture
	protected final Matrix mFullMatrixDraw = new Matrix();    //	for full picture
	protected final float[] mScaleModes = new float[SCALE_COUNT];
	protected final float[] mScaleRanges = new float[2];
	protected final float[] mMatrixValues = new float[9];
	public final int mWidthGap;
	public final int mAnimationDuration;
	protected final float mVelocityMax;
	protected final float mVelocityMin;
	protected final float mVelocitySpring;
	protected final float mVelocitySwipe;
	protected final float mDensity;
	protected final String mStrLoading;
	protected final String mStrLoadFailed;

	public interface OnListener {
		public abstract boolean hasNextPicture(int dir);

		public abstract Picture getNextPicture(int dir, boolean fast);

		public abstract void onMoveToNext(int dir);

		public abstract void onSingleTap(float pivotX, float pivotY);

		public abstract void onDoubleTap(float pivotX, float pivotY);

		public abstract void onZoom(float scaleBy, boolean fromUser);
	}

	public BasePictureView(Context context) {
		this(context, null);
	}

	public BasePictureView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDrawingCacheEnabled(false);
		setWillNotCacheDrawing(true);

		final Resources res = getResources();
		final DisplayMetrics metrics = res.getDisplayMetrics();
		final ViewConfiguration config = ViewConfiguration.get(context);

		mDensity = metrics.density;
		mWidthGap = (int) (mDensity * 24);

		mTextColor = res.getColor(R.color.icon_light);
		mWarnColor = res.getColor(R.color.warning);

		mBitmapPaint = new Paint(Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
		mBkgndPaint = new Paint(Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);

		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTextAlign(Paint.Align.CENTER);
		mTextPaint.setTextSize(mDensity * 18);

		mStrLoading = res.getString(R.string.loading);
		mStrLoadFailed = res.getString(R.string.load_failed);

		mInterpolator = new ViscousFluidInterpolator();//DecelerateInterpolator();

		mAnimationDuration = 400;

		mVelocityMax = config.getScaledMaximumFlingVelocity();
		mVelocityMin = config.getScaledMinimumFlingVelocity();
		mVelocitySpring = mVelocityMin;
		mVelocitySwipe = mDensity * SWIPE_VELOCITY;    //	dip/second
	}

	/*	public boolean canRotate() {
			final String mimeType = getMimeType();
			return "image/jpeg".equals(mimeType);// || "image/png".equals(mimeType);
		}
	*/
	public String getMimeType() {
		if (mFullPicture != null)
			return mFullPicture.mMimeType;
		else if (mPicture != null)
			return mPicture.mMimeType;
		return "";
	}

	public Picture getFullPicture() {
		if (mFullPicture != null)
			return mFullPicture.addRef();
		return null;
	}

	public Picture getPicture() {
		if (mPicture != null)
			return mPicture.addRef();
		return null;
	}

	/*	public Image.Info getPictureInfo() {
			if (mFullPicture != null)
				return mFullPicture.mInfo;
			else if (mPicture != null)
				return mPicture.mInfo;
			return null;
		}
	*/
	public int getPictureWidth() {
		if (mFullPicture != null)
			return mFullPicture.getWidth();
		else if (mPicture != null)
			return mPicture.getWidth();
		return 0;
	}

	public int getPictureHeight() {
		if (mFullPicture != null)
			return mFullPicture.getHeight();
		else if (mPicture != null)
			return mPicture.getHeight();
		return 0;
	}

	public int getPictureRotation() {
		if (mFullPicture != null)
			return mFullPicture.mInfo.rotation;
		else if (mPicture != null)
			return mPicture.mInfo.rotation;
		return BitmapOptions.REQUEST_ROTATION;
	}

	public long getPictureDuration() {
		if (mFullPicture != null)
			return mFullPicture.duration();
		else if (mPicture != null)
			return mPicture.duration();
		return 0;
	}

	public int getPictureType() {
		if (mFullPicture != null)
			return mFullPicture.mType;
		else if (mPicture != null)
			return mPicture.mType;
		return Picture.TYPE_BLANK;
	}

	public Uri getPictureUri() {
		if (mFullPicture != null)
			return mFullPicture.mUri;
		else if (mPicture != null)
			return mPicture.mUri;
		return Uri.EMPTY;
	}

	public boolean isFullPicture() {
		if (mFullPicture != null)
			return true;
		else if (mPicture != null)
			return mPicture.mType > Picture.TYPE_THUMB;
		return false;
	}

	public void redraw(boolean immediate) {
		computeBaseMatrix();
		computeDrawMatrix();
		if (immediate)
			invalidate();
	}

	public void setColors(int textColor, int backColor) {
		mTextColor = textColor;
		mBkgndPaint.setColor(backColor);
		invalidate();
	}

	public static final Shader mPatternShader = Shapes.createPatternShader(16);

	public void setPattern(boolean pattern) {
		mBkgndPaint.setShader(pattern ? mPatternShader : null);
		invalidate();
	}

	public void setListener(OnListener listener) {
		mListener = listener;
	}

	public void setPicture(Picture picture, boolean reset) {
		final Uri uri = picture != null ? picture.mUri : Uri.EMPTY;

		//	be careful of the following logic steps:

		reset |= (mPicture == null || picture == null);

		//	1. release preview picture if uri changed
		if (mPicture != null && !mPicture.equalsUri(uri)) {
			mPicture.release();
			mPicture = null;
		}

		//	2. release full picture if uri changed
		if (mFullPicture != null && !mFullPicture.equalsUri(uri)) {
			mFullPicture.stop(true);
			mFullPicture.release();
			mFullPicture = null;
		}

		//	3. set picture by picture type
		if (picture != null) {
			if (picture.mType <= Picture.TYPE_THUMB) {
				//	if new picture type is better, release the current
				if (mPicture == null || mPicture.mType < picture.mType) {
					if (mPicture != null)
						mPicture.release();
					mPicture = picture.addRef();
				}
			} else {
				//	it is a full picture
				if (picture.mType == Picture.TYPE_TILE) {
					//	tile picture need a preview picture always
					if (mPicture != null && mPicture.hasBitmap()) {
						if (mFullPicture != null)
							mFullPicture.release();
						mFullPicture = picture.addRef();
					}
					//	else Log.w("PictureView", "set tile picture without preview: cur=" + mPicture + ", full=" + picture);
				} else {
					if (mFullPicture != null)
						mFullPicture.release();
					mFullPicture = picture.addRef();
				}
			}
		}

		Log.d("PictureView", "set picture: " + reset + ", preview=" + mPicture + ", full=" + mFullPicture);

		if (reset) {
			mMatrixTrans.reset();
			mScaleMode = SCALE_INSIDE;

		/*	if (mSmallerThanView) {
				mScaleMode = SCALE_ACTUAL;
				final float scaleTo = computeScaleByMode(mScaleMode);
				zoomTo(scaleTo, mViewWidth / 2f, mViewHeight / 2f);
			}*/
			redraw(true);
		} else if (!mDrawRect.isEmpty()) {
			final RectF rcDraw = mDrawRect;
			final float cxDraw = rcDraw.width();
			final float cyDraw = rcDraw.height();
			final float left = rcDraw.left;
			final float top = rcDraw.top;
			redraw(false);
			//	restore to the previous size
			final boolean vert = Math.abs(cyDraw - mViewHeight) < Math.abs(cxDraw - mViewWidth);
			final float scaleX = cxDraw / rcDraw.width();
			final float scaleY = cyDraw / rcDraw.height();
			final float scaleBy = vert ? scaleY : scaleX;
			zoomBy(scaleBy, rcDraw.centerX(), rcDraw.centerY());
			if (vert)
				moveBy(0, top - rcDraw.top);
			else
				moveBy(left - rcDraw.left, 0);
		}
	}

	public void setPictureUri(Uri uri) {
		if (uri == null)
			uri = Uri.EMPTY;
		if (mPicture != null)
			mPicture.mUri = uri;
		if (mFullPicture != null)
			mFullPicture.mUri = uri;
	}

	public void start() {
		toCache(false);
		if (mFullPicture != null) {
			mFullPicture.update(mViewWidth, mViewHeight, mFullMatrixDraw, mDrawRect);
			mFullPicture.start(this, mBkgndPaint);
			invalidate();
		}
		//	Log.i("PictureView", "start");
	}

	public void stop() {
		finishCurrentTasks();
		if (mFullPicture != null) {
			if (mFullPicture.mType == Picture.TYPE_IMAGE) {
				if (mFullPicture.getWidth() * mFullPicture.getHeight() > BitmapUtils.MIN_PICTURE_SIZE) {
					toCache(true);
					mFullPicture.release();
					mFullPicture = null;
				}
			} else {
				toCache(true);
				mFullPicture.stop(true);
			}
		}
		//	Log.w("PictureView", "stop");
	}

	private RectF mCachedRect;

	public void toCache(boolean cache) {
		final Canvas canvas = mBufferedCanvas;
		if (cache && canvas != null) {
			if (mBufferedBitmap != null)
				mBufferedBitmap.eraseColor(0);
			mCachedRect = getClipRect();
			canvas.save(Canvas.CLIP_SAVE_FLAG);
			canvas.clipRect(mCachedRect);
			drawCurrPictures(canvas, false, false);
			canvas.restore();
		} else {
			mCachedRect = null;
			invalidate();
		}
	}

	public RectF getClipRect() {
		final RectF rect = new RectF(0, 0, mViewWidth, mViewHeight);
		if (rect.isEmpty()) {
			final View root = getRootView();
			rect.right = rect.left + root.getWidth();
			rect.bottom = rect.top + root.getHeight();
		}
		rect.intersect(mDrawRect);
		return rect;
	}

	public Picture getSnapshot(Bitmap.Config config) {
		final RectF rect = getClipRect();
		final int width = Math.round(rect.width());
		final int height = Math.round(rect.height());
		final Bitmap bitmap = BitmapUtils.create(width, height, config);
		if (bitmap != null) {
			final Canvas canvas = new Canvas(bitmap);
			bitmap.eraseColor(0);
			canvas.translate(-rect.left, -rect.top);
			drawCurrPictures(canvas, false, false);
			canvas.translate(rect.left, rect.top);
			return new Picture(bitmap, Picture.TYPE_THUMB);
		}
		return null;
	}

	public float changeScaleMode(float pivotX, float pivotY) {
		if (getPictureType() <= Picture.TYPE_BLANK)
			return 1;

		final float[] scaleModes = computeScaleModes();
		final float[] scaleRanges = getScaleRanges();
		final float scale = getTransScale();
		float scaleTo = scale;
		for (int i = 0; i < SCALE_COUNT; i++) {
			mScaleMode = (mScaleMode + 1) % SCALE_COUNT;
			scaleTo = scaleModes[mScaleMode];
			if (Math.abs(scaleTo - scale) > 0.05f && scaleTo >= scaleRanges[0])
				break;
		}
		if (scaleTo == scale)
			scaleTo = scale * ZOOM_MAX / 2;
		if (scaleTo != scale)
			zoomToAnimate(scaleTo, pivotX, pivotY, mAnimationDuration);
		return scaleTo / scale;
	}

	public void setScaleMode(int mode, boolean animate) {
		final float[] scaleModes = computeScaleModes();
		if (mode >= 0 && mode < scaleModes.length) {
			final float scaleTo = computeScaleModes()[mode];
			final float scale = getTransScale();
			mScaleMode = mode;
			if (scaleTo != scale) {
				if (animate) {
					zoomToAnimate(scaleTo, mViewWidth / 2f, mViewHeight / 2f, mAnimationDuration);
				} else {
					zoomTo(scaleTo, mViewWidth / 2f, mViewHeight / 2f);
					springBack(0);
				}
			}
		}
	}

	public void rotate(int degrees) {
		rotateToAnimate(getTransRotation() + degrees, false, mViewWidth / 2f, mViewHeight / 2f, mAnimationDuration, null);
	}

	public float zoom(boolean zoomIn) {
		if (getPictureType() <= Picture.TYPE_BLANK)
			return 1;

		final float[] range = getScaleRanges();
		final float scaleCur = getTransScale();
		final float pivotX = mViewWidth / 2f;
		final float pivotY = mViewHeight / 2f;
		final float scaleBy = zoomIn ? ZOOM_STEP : 1 / ZOOM_STEP;
		final float scaleTo = scaleCur * scaleBy;

		if (scaleTo > range[1]) {
			zoomBy(scaleBy, pivotX, pivotY);
			zoomSpringBack(pivotX, pivotY);
		} else if (scaleTo < range[0]) {
			zoomBy(scaleBy, pivotX, pivotY);
			zoomSpringBack(pivotX, pivotY);
		} else if (scaleTo != scaleCur) {
			zoomToAnimate(scaleTo, pivotX, pivotY, mAnimationDuration);
		}
		return scaleTo / scaleCur;
	}

	private float checkMoveOut() {
		final RectF rcDraw = mDrawRect;
		final float cxDraw = rcDraw.width();
		float left = rcDraw.left;
		float right = rcDraw.right;
		if (cxDraw < mViewWidth) {
			left -= (mViewWidth - cxDraw) / 2;
			right = left + mViewWidth;
		}

		if (left > 0) {
			mNextPicDir = -1;
			return left;
		} else if (right < mViewWidth) {
			mNextPicDir = 1;
			return right - mViewWidth;
		} else {
			mNextPicDir = 0;
			return 0;
		}
	}

	private Bitmap mBufferedBitmap;
	private Canvas mBufferedCanvas;
	private final Matrix mBufferedMatrixDraw = new Matrix();

	private void drawPicture(Canvas canvas, Picture picture, Matrix matrix, RectF rect, boolean hardware) {
		final Bitmap bitmap = picture.getBitmap();
		final Canvas canvas2 = mBufferedCanvas;
		final Matrix matrix2 = mBufferedMatrixDraw;
		final int width = bitmap.getWidth();
		final int height = bitmap.getHeight();
		final int rotation = picture.mInfo.rotation;
		final boolean buffered = canvas2 != null && canvas2 != canvas;
		final boolean rotated = rotation % 180 != 0;
		final boolean hasAlpha = picture.hasAlpha();

		boolean tooLarge = width > mMaxBitmapWidth || height > mMaxBitmapHeight;
		if (!tooLarge && hardware) {
			//	4096x2048 can be hardware rendering? It seems if one side is <= max/2 is fast enough
			if (width > (mMaxBitmapWidth >> 1) && height > (mMaxBitmapHeight >> 1))
				tooLarge = true;
		}

		if (rotated && (!hardware || tooLarge) && buffered) {
			//	Draw to screen size then rotate to speed up drawing
			canvas2.save(Canvas.CLIP_SAVE_FLAG);
			canvas2.clipRect(0, 0, mViewHeight, mViewWidth);
			if (hasAlpha || mRotating)
				canvas2.drawRect(0, 0, mViewHeight, mViewWidth, mBkgndPaint);
			matrix2.set(matrix);
			matrix2.postRotate(360 - rotation, mViewWidth / 2f, mViewHeight / 2f);
			matrix2.postTranslate((mViewHeight - mViewWidth) / 2f, (mViewWidth - mViewHeight) / 2f);
			canvas2.drawBitmap(bitmap, matrix2, mBitmapPaint);
			matrix2.reset();
			matrix2.preTranslate(-mViewHeight / 2f, -mViewWidth / 2f);
			matrix2.postRotate(rotation);
			matrix2.postTranslate(mViewWidth / 2f, mViewHeight / 2f);
			canvas.drawBitmap(mBufferedBitmap, matrix2, null);
			canvas2.restore();
		} else if (hardware && tooLarge && buffered) {
			canvas2.save(Canvas.CLIP_SAVE_FLAG);
			canvas2.clipRect(0, 0, mViewWidth, mViewHeight);
			if (hasAlpha || mRotating)
				canvas2.drawRect(rect, mBkgndPaint);
			canvas2.drawBitmap(bitmap, matrix, mBitmapPaint);
			canvas.drawBitmap(mBufferedBitmap, 0, 0, null);
			canvas2.restore();
		} else {
			if (hasAlpha) {
				canvas.save(Canvas.MATRIX_SAVE_FLAG);
				canvas.concat(matrix);
				canvas.drawRect(0, 0, width, height, mBkgndPaint);
				canvas.restore();
			}
			canvas.drawBitmap(bitmap, matrix, mBitmapPaint);
		}
	}

	private void drawStatus(Canvas canvas, Picture picture, RectF rect) {
		if (picture.isError()) {
			mTextPaint.setColor(mWarnColor);
			canvas.drawText(mStrLoadFailed, rect.centerX(), rect.centerY() + mTextPaint.getTextSize() / 2, mTextPaint);
		} else {
			mTextPaint.setColor(0x30808080);
			canvas.drawRect(rect, mTextPaint);
		}
	}

	protected void drawCurrPictures(Canvas canvas, boolean hardware, boolean fast) {
		final int ftype = mFullPicture != null ? mFullPicture.mType : Picture.TYPE_BLANK;
		if (ftype == Picture.TYPE_IMAGE && mFullPicture.hasBitmap())
			drawPicture(canvas, mFullPicture, mFullMatrixDraw, mDrawRect, hardware);
		else if (ftype > Picture.TYPE_BLANK)
			mFullPicture.draw(canvas, mFullMatrixDraw, mPicture, mMatrixDraw, mBitmapPaint, fast);
		else if (mPicture != null && mPicture.hasBitmap())
			drawPicture(canvas, mPicture, mMatrixDraw, mDrawRect, hardware);
		else if (mPicture != null)
			drawStatus(canvas, mPicture, mDrawRect);
		else {
			mTextPaint.setColor(mTextColor);
			canvas.drawText(mStrLoading, mDrawRect.centerX(), mDrawRect.centerY() + mTextPaint.getTextSize() / 2, mTextPaint);
		}
	}

	private final RectF mNextDrawRect = new RectF();
	private final Matrix mNextMatrixDraw = new Matrix();

	private void drawNextPicture(Canvas canvas, Picture picture, boolean hardware, float dxTrans) {
		picture.getProperMatrix(mNextMatrixDraw, mViewWidth, mViewHeight, Image.MATRIX_INSIDE);
		mNextMatrixDraw.postTranslate(dxTrans, 0);    //	Don't translate the canvas because clipRect will fail when hardware rendering
	/*	final float scale =
		final boolean smallThanView = ! picNext.mThumb && scale > 1;
		if (smallThanView)
			mNextMatrixDraw.postScale(1 / scale, 1 / scale, mViewWidth / 2f, mViewHeight / 2f);
	*/
		if (picture.isError())
			mNextDrawRect.set(dxTrans, 0, dxTrans + mViewWidth, mViewHeight);
		else
			picture.mapRect(mNextMatrixDraw, mNextDrawRect);

		canvas.save(Canvas.CLIP_SAVE_FLAG);
		canvas.clipRect(mNextDrawRect);
		if (picture.hasBitmap())
			drawPicture(canvas, picture, mNextMatrixDraw, mNextDrawRect, hardware);
		else
			drawStatus(canvas, picture, mNextDrawRect);
		canvas.restore();
	}

	private int mMaxBitmapWidth;
	private int mMaxBitmapHeight;

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public void onDraw(Canvas canvas) {
		if (mAnimator != null)
			mAnimator.step();

		final boolean hardware = HAS_HW_ACCELERATION && canvas.isHardwareAccelerated();
		if (hardware) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				mMaxBitmapWidth = canvas.getMaximumBitmapWidth();
				mMaxBitmapHeight = canvas.getMaximumBitmapHeight();
			} else {
				mMaxBitmapWidth = 2048;
				mMaxBitmapHeight = 2048;
			}
		} else {
			mMaxBitmapWidth = 1024;
			mMaxBitmapHeight = 1024;
		}
		//	Log.d("PictureView", "hardware=" + hardware + ", max_size=" + mMaxBitmapWidth + "x" + mMaxBitmapHeight);

		float dxMove = 0, dxTrans = 0;
		boolean checkNextPic = (mMovingToNext || !mTransforming) && mListener != null;
		if (checkNextPic) {
			dxMove = checkMoveOut();
			checkNextPic &= Math.abs(dxMove) > mWidthGap;
			if (checkNextPic)
				dxTrans = dxMove > 0 ? (dxMove - mViewWidth - mWidthGap) : (dxMove + mViewWidth + mWidthGap);
		}
		//	Log.d("PictureView", "dxMove=" + dxMove + ", dxTrans=" + dxTrans);

		if (mCachedRect != null && canvas != mBufferedCanvas) {
			final Bitmap bitmap = mBufferedBitmap;
			canvas.save(Canvas.CLIP_SAVE_FLAG);
			canvas.clipRect(mCachedRect);
			if (bitmap != null && !bitmap.isRecycled())
				canvas.drawBitmap(bitmap, 0, 0, null);
			canvas.restore();
		} else {
			final boolean fast = mTransforming || mMovingToNext;
			final boolean filter = hardware || !(fast || mMoving);
			mBitmapPaint.setDither(filter);
			mBitmapPaint.setFilterBitmap(filter);
			canvas.save(Canvas.CLIP_SAVE_FLAG);
			canvas.clipRect(mDrawRect);
			drawCurrPictures(canvas, hardware, fast);
			canvas.restore();

			if (checkNextPic && mNextPicDir != 0 && mListener.hasNextPicture(mNextPicDir)) {
				final Picture picture = mListener != null && mNextPicDir != 0 ? mListener.getNextPicture(mNextPicDir, false) : null;
				if (picture != null) {
					drawNextPicture(canvas, picture, hardware, dxTrans);
					picture.release();
				}
			}
		}
	}

	@Override
	protected void onSizeChanged(int width, int height, int oldw, int oldh) {
		finishCurrentTasks();

		final boolean changed = mViewWidth != width || mViewHeight != height;
		mViewWidth = width;
		mViewHeight = height;
		mWidthToSwitch = Math.min(width, height) / 2;
		mRestrictRect.set(0, 0, width, height);

		if (width > 0 && height > 0) {
			int size = JandanApp.mScreenMaxSidePX;
			final int mod = size % 8;
			if (mod != 0)
				size += 8 - mod;//	Make 8 aligned for better performance and work around
			//	the Nexus7 bug: wrong stride if the image is rotated in portrait mode
			if (mBufferedBitmap != null && mBufferedBitmap.getWidth() < size) {
				mBufferedBitmap.recycle();
				mBufferedBitmap = null;
			}
			if (mBufferedBitmap == null)
				mBufferedBitmap = BitmapUtils.create(size, size, Bitmap.Config.RGB_565);
			mBufferedCanvas = mBufferedBitmap != null ? new Canvas(mBufferedBitmap) : null;
		}

		if (changed) {
			//	Save current draw size
			final float cxDraw = mDrawRect.width();

			mScaleMode = /*mSmallerThanView ? SCALE_ACTUAL : */SCALE_INSIDE;
			mMatrixTrans.reset();
			redraw(false);

			final float cxDraw2 = mDrawRect.width();
			if (cxDraw2 != cxDraw) {
				final float scaleTo = mPicture != null ? cxDraw / cxDraw2 : 1;
				final float scaleBy = Math.max(scaleTo, 1) / getTransScale();
				zoomBy(scaleBy, width / 2, height / 2);
				springBack(0);
				//	Log.d("PictureView", "zoom to: " + scaleTo);
			}
		}
	}

//	@Override
//	protected void onAnimationStart() {
//		super.onAnimationStart();
//		mTransforming = true;
//	}
//
//	@Override
//	protected void onAnimationEnd() {
//		super.onAnimationEnd();
//		mTransforming = false;
//		invalidate();
//	}

	@Override
	protected void onDetachedFromWindow() {
		if (mFullPicture != null) {
			mFullPicture.stop(true);
			mFullPicture.release();
			mFullPicture = null;
		}
		if (mPicture != null) {
			mPicture.release();
			mPicture = null;
		}

		mBufferedCanvas = null;
		if (mBufferedBitmap != null) {
			mBufferedBitmap.recycle();
			mBufferedBitmap = null;
		}
		super.onDetachedFromWindow();
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);
		invalidate();
	}

	public void finishCurrentTasks() {
		if (mAnimator != null) {
			mAnimator.stop();
			mAnimator = null;
		}

		final boolean movingToNext = mMovingToNext;
		mMoving = false;
		mMovingToNext = false;
		mTransforming = false;

		if (!movingToNext)
			springBack(0);
		else
			invalidate();
	}

	private float mScalePicturesX, mScalePicturesY;

	protected void computeBaseMatrix() {
		final float cxView = mViewWidth;
		final float cyView = mViewHeight;

		if (mPicture != null && !mPicture.isError())
			mPicture.getProperMatrix(mMatrixBase, cxView, cyView, Image.MATRIX_INSIDE);
		else
			mMatrixBase.reset();

		if (mFullPicture != null) {
			if (mPicture != null) {    //	always use the preview's rotation
				mFullPicture.mInfo.rotation = mPicture.mInfo.rotation;
				mScalePicturesX = mFullPicture.getBitmapWidth() / (float) mPicture.getBitmapWidth();
				mScalePicturesY = mFullPicture.getBitmapHeight() / (float) mPicture.getBitmapHeight();
			}
			mFullPicture.getProperMatrix(mFullMatrixBase, cxView, cyView, Image.MATRIX_INSIDE);
		} else
			mFullMatrixBase.reset();
	}

	protected void computeDrawMatrix() {
		if (mFullPicture != null) {
			mFullMatrixDraw.set(mFullMatrixBase);
			mFullMatrixDraw.postConcat(mMatrixTrans);
			mFullPicture.mapRect(mFullMatrixDraw, mDrawRect);
			mFullPicture.update(mViewWidth, mViewHeight, mFullMatrixDraw, mDrawRect);

			if (mPicture != null) {
				//	fix the preview's matrix
				mMatrixDraw.setScale(mScalePicturesX, mScalePicturesY);
				mMatrixDraw.postConcat(mFullMatrixDraw);
			} else {
				mMatrixDraw.set(mMatrixBase);
				mMatrixDraw.postConcat(mMatrixTrans);
			}
		} else {
			mMatrixDraw.set(mMatrixBase);
			mMatrixDraw.postConcat(mMatrixTrans);
			if (mPicture != null && !mPicture.isError()) {
				//	mSmallerThanView = ! mPicture.mThumb && scale > 1;
				mPicture.mapRect(mMatrixDraw, mDrawRect);
			} else {
				//	mSmallerThanView = false;
				mDrawRect.set(0, 0, mViewWidth, mViewHeight);
				mMatrixDraw.mapRect(mDrawRect);
			}
		}
	}

	public float[] computeScaleModes() {
		final float cxView = mViewWidth;
		final float cyView = mViewHeight;
		float width = getPictureWidth();
		float height = getPictureHeight();
		if (width <= 0 || height <= 0) {
			width = cxView;
			height = cyView;
		}
		final float scaleX = cxView / width;
		final float scaleY = cyView / height;
		mScaleModes[SCALE_INSIDE] = 1;
		mScaleModes[SCALE_CROP] = Math.max(scaleX, scaleY) / Math.min(scaleX, scaleY);
		mScaleModes[SCALE_ACTUAL] = 1 / Math.min(scaleX, scaleY);
		return mScaleModes;
	}

	public float[] getScaleRanges() {
		final float[] scaleModes = computeScaleModes();
		mScaleRanges[0] = Math.min(scaleModes[SCALE_ACTUAL], scaleModes[SCALE_INSIDE]);
		mScaleRanges[1] = Math.max(scaleModes[SCALE_ACTUAL] * ZOOM_MAX, scaleModes[SCALE_INSIDE]);
		return mScaleRanges;
	}

	public float getTransScale() {
		final float[] values = getTransValues();
		return Picture.getMatrixScale(values);
	}

	public float getTransRotation() {
		final float[] values = getTransValues();
		return Picture.getMatrixRotation(values);
	}

	public float[] getTransValues() {
		mMatrixTrans.getValues(mMatrixValues);
		return mMatrixValues;
	}

	public void setTransValues(float[] values) {
		if (values != null && values.length >= 9) {
			mMatrixTrans.setValues(values);
			computeDrawMatrix();
			invalidate();
		}
	}

	public void mapBaseRect(RectF rect) {
		if (mFullPicture != null)
			mFullPicture.mapRect(mFullMatrixBase, rect);
		else if (mPicture != null)
			mPicture.mapRect(mMatrixBase, rect);
	}

	public void moveBy(float dx, float dy) {
		mMatrixTrans.postTranslate(dx, dy);
		computeDrawMatrix();
		invalidate();
	}

	public void moveByCheck(float dx, float dy) {
		final RectF rcDraw = mDrawRect;
		final RectF rcRestrict = mRestrictRect;
		if ((int) (rcDraw.height() - rcRestrict.height()) <= 0)
			dy = 0;
		if (mListener == null && (int) (rcDraw.width() - rcRestrict.width()) <= 0)
			dx = 0;
		if (dx != 0 || dy != 0)
			moveBy(dx, dy);
	}

	public void moveByVelocity(float dx, float dy, float velocity, Runnable stopTask) {
		//	c = v * t / 2;
		final float TMIN = 0.40f;
		final float TMAX = 0.80f;
		final float SCALE = 1.25f;
		final float cx = Math.abs(dx);
		final float cy = Math.abs(dy);
		final float v = Math.abs(velocity);
		final float tx = v > 0 ? (cx / v / 2 * SCALE) : TMAX;
		final float ty = v > 0 ? (cy / v / 2 * SCALE) : TMAX;
		float t = Math.max(tx, ty);
		if (t < TMIN) t = TMIN;
		else if (t > TMAX) t = TMAX;
		moveByAnimate(dx, dy, (int) (t * 1000), stopTask);
		//	Log.d("PictureView", "move by velocity: " + dx + "," + dy + ", velocity=" + velocity + ", time=" + t);
	}

	public void moveByAnimate(final float dx, final float dy, final int duration, final Runnable stopTask) {
		if (mAnimator != null)
			mAnimator.stop();
		mAnimator = new SimpleAnimator(this, mInterpolator) {
			private float mLastX = 0;
			private float mLastY = 0;

			public void onStart() {
				mMoving = true;
			}

			public void onDoing(float interpolation) {
				float dxInter = dx * interpolation;
				float dyInter = dy * interpolation;
				moveBy(dxInter - mLastX, dyInter - mLastY);
				mLastX = dxInter;
				mLastY = dyInter;
			}

			public void onEnd() {
				mAnimator = null;
				mMoving = false;
				computeDrawMatrix();
				invalidate();
			}

			@Override
			public void stop() {
				super.stop();

				if (stopTask != null)
					stopTask.run();
			}
		};
		mAnimator.start(duration, false);
	}

	public boolean moveToNextAnimate(float velocity) {
		final float dxMove = checkMoveOut();
		final float dxMoveAbs = Math.abs(dxMove);

		if (dxMoveAbs > mWidthGap
				&& (int) Math.signum(dxMove) != (int) Math.signum(velocity))
			return false;

		final int dir = -(velocity > 0 ? 1 : velocity < 0 ? -1 : 0);
		if (mListener == null || !mListener.hasNextPicture(dir)) {
			mNextPicDir = 0;
			springBack(velocity);
			return true;
		}

		if (mFullPicture != null)
			mFullPicture.stop(true);

		//	Preload the next picture
		final Picture picture = mListener.getNextPicture(dir, true);
		if (picture != null)
			picture.release();

		mMovingToNext = true;
		mNextPicDir = dir;

		final float dx = velocity < 0 ? (dxMoveAbs - mViewWidth - mWidthGap) : (mViewWidth - dxMoveAbs + mWidthGap);
		moveByVelocity(dx, 0, velocity, new Runnable() {
			public void run() {
				if (mFullPicture != null) {
					mFullPicture.release();
					mFullPicture = null;
				}
				mMovingToNext = false;
				mNextPicDir = 0;
				invalidate();

				if (mListener != null)
					mListener.onMoveToNext(dir);
			}
		});
		return true;
	}

	public boolean moveToNextOrSpringBack(float velocity) {
		final float dxMove = checkMoveOut();
		velocity = Math.max(Math.abs(velocity), mVelocitySwipe);
		if (mNextPicDir != 0 && Math.abs(dxMove) > mWidthToSwitch)
			return moveToNextAnimate((mNextPicDir > 0 ? -1 : 1) * velocity);
		else
			return springBack(velocity);
	}

	public boolean springBack() {
		return springBack(mVelocitySpring);
	}

	public boolean springBack(float velocity) {
		final RectF rcRestrict = mRestrictRect;
		final RectF rcDraw = mDrawRect;
		final float cxDraw = rcDraw.width();
		final float cyDraw = rcDraw.height();
		final float cxRestrict = rcRestrict.width();
		final float cyRestrict = rcRestrict.height();
		float dx = 0, dy = 0;

		if (cxDraw <= cxRestrict)
			dx = rcRestrict.left - rcDraw.left + (cxRestrict - cxDraw) / 2;
		else if (rcDraw.left > rcRestrict.left)
			dx = rcRestrict.left - rcDraw.left;
		else if (rcDraw.right < rcRestrict.right)
			dx = rcRestrict.right - rcDraw.right;

		if (cyDraw <= cyRestrict)
			dy = rcRestrict.top - rcDraw.top + (cyRestrict - cyDraw) / 2;
		else if (rcDraw.top > rcRestrict.top)
			dy = rcRestrict.top - rcDraw.top;
		else if (rcDraw.bottom < rcRestrict.bottom)
			dy = rcRestrict.bottom - rcDraw.bottom;

		if (Math.abs(dx) > 0.01f || Math.abs(dy) > 0.01f) {
			if (velocity != 0)
				moveByVelocity(dx, dy, Math.signum(velocity) * Math.min(Math.abs(velocity), mVelocitySpring * 2), null);
			else
				moveBy(dx, dy);
			return true;
		} else {
			invalidate();
			return false;
		}
	}

	private OverScroller mFlingScroller;

	public boolean isFling() {
		return (mFlingScroller != null && !mFlingScroller.isFinished()) || mMovingToNext;
	}

	public boolean doFling(float velocityX, float velocityY) {
		final RectF rcDraw = mDrawRect;
		final float cxDraw = rcDraw.width();
		final float cyDraw = rcDraw.height();
		final float cxView = mViewWidth;
		final float cyView = mViewHeight;
		final int scrollRangeX = (int) Math.max(cxDraw - cxView, 0);
		final int scrollRangeY = (int) Math.max(cyDraw - cyView, 0);
		//	Log.d("PictureView", "fling: " + velocityX + ", " + velocityY + ", min=" + mVelocityMin);

		if (Math.ceil(rcDraw.left) >= 0 && velocityX >= mVelocitySwipe
				&& (scrollRangeY == 0 || Math.abs(velocityX) > Math.abs(velocityY))) {
			if (moveToNextAnimate(velocityX))
				return true;
		} else if (Math.floor(rcDraw.right) <= cxView && velocityX <= -mVelocitySwipe
				&& (scrollRangeY == 0 || Math.abs(velocityX) > Math.abs(velocityY))) {
			if (moveToNextAnimate(velocityX))
				return true;
		}

		final float SLOW_DOWN = 0.8f;
		final float velocity = (float) Math.hypot(velocityX, velocityY);
		if (scrollRangeX == 0)
			velocityX = 0;
		if (scrollRangeY == 0)
			velocityY = 0;
		if (velocityX == 0 && velocityY == 0)
			return moveToNextOrSpringBack(velocity * SLOW_DOWN);

		final int velocityX2 = -(int) (velocityX * SLOW_DOWN);
		final int velocityY2 = -(int) (velocityY * SLOW_DOWN);
		if (mAnimator != null)
			mAnimator.stop();
		if (mFlingScroller == null)
			mFlingScroller = new OverScroller(getContext());
		mAnimator = new SimpleAnimator(this, null) {
			private final int mOverScrollX = mWidthGap * 4;
			private final int mOverScrollY = mWidthGap * 4;
			private float mLastX = -rcDraw.left;
			private float mLastY = -rcDraw.top;

			public void onStart() {
				mMoving = true;
				mFlingScroller.fling((int) mLastX, (int) mLastY, velocityX2, velocityY2,
						0, scrollRangeX, 0, scrollRangeY, mOverScrollX, mOverScrollY);

			}

			public void onDoing(float interpolation) {
				if (mFlingScroller.computeScrollOffset()) {
					float scrollX = mFlingScroller.getCurrX();
					float scrollY = mFlingScroller.getCurrY();

					//if (mFlingScroller.isOverScroller()) {
					final float left = scrollRangeX > 0 ? 0 - mOverScrollX : mLastX;
					final float right = scrollRangeX > 0 ? scrollRangeX + mOverScrollX : mLastX;
					final float top = scrollRangeY > 0 ? 0 - mOverScrollY : mLastY;
					final float bottom = scrollRangeY > 0 ? scrollRangeY + mOverScrollY : mLastY;
					if (scrollX < left)
						scrollX = left;
					else if (scrollX > right)
						scrollX = right;
					if (scrollY < top)
						scrollY = top;
					else if (scrollY > bottom)
						scrollY = bottom;
					//}

					moveBy(mLastX - scrollX, mLastY - scrollY);
					mLastX = scrollX;
					mLastY = scrollY;

//					if (!mFlingScroller.isOverScroller()) {
//						if (mOverScrollX < rcDraw.left - 0
//								|| mOverScrollX < cxView - rcDraw.right) {
//							mFlingScroller.abortAnimation();
//							//	Log.d("PictureView", "scroll done by X");
//						}
//						if (rcDraw.top > 0 + mOverScrollY
//								|| mOverScrollY < cyView - rcDraw.bottom) {
//							mFlingScroller.abortAnimation();
//							//	Log.d("PictureView", "scroll done by Y");
//						}
//					}
					//	Log.d("PictureView", "scroll: " + scrollX + ", " + scrollY);
				} else {
					stop();
					onEnd();
				}
			}

			public void onEnd() {
				mAnimator = null;
				mMoving = false;
				springBack();
			}

			@Override
			public void stop() {
				super.stop();
				mAnimator = null;
				mMoving = false;
				mFlingScroller.abortAnimation();
			}
		};
		mAnimator.start(0, false);
		return true;
	}

	public void zoomBy(float scale, float pivotX, float pivotY) {
		mMatrixTrans.postScale(scale, scale, pivotX, pivotY);
		computeDrawMatrix();
		invalidate();

		if (!mTransforming && mListener != null)
			mListener.onZoom(scale, false);
	}

	public void zoomTo(float scaleTo, float pivotX, float pivotY) {
		final float scaleCur = getTransScale();
		zoomBy(scaleTo / scaleCur, pivotX, pivotY);
	}

	public void zoomToAnimate(final float scaleTo, final float pivotX, final float pivotY, final int duration) {
		if (duration > mAnimationDuration) {
			// if zooming very slowly, spring back in progressing
			final float scaleFrom = getTransScale();
			final float scaleDelta = scaleTo - scaleFrom;
			if (mAnimator != null)
				mAnimator.stop();
			mAnimator = new SimpleAnimator(this, mInterpolator) {
				public void onStart() {
					mTransforming = true;
				}

				public void onDoing(float interpolation) {
					final float scaleInter = scaleFrom + scaleDelta * interpolation;
					zoomTo(scaleInter, pivotX, pivotY);
					springBack(0);
				}

				public void onEnd() {
					mAnimator = null;
					mTransforming = false;
					zoomTo(scaleTo, pivotX, pivotY);
					springBack(0);
				}
			};
			mAnimator.start(duration, false);
		} else {
			final float rotation = getTransRotation();
			final float scaleFrom = getTransScale();
			transformAnimate(scaleFrom, scaleTo, rotation, rotation, pivotX, pivotY, duration, null);
		}
	}

	public void zoomSpringBack(float pivotX, float pivotY) {
		final float[] ranges = getScaleRanges();
		final float scaleCur = getTransScale();
		final float scaleTo = Math.max(ranges[0], Math.min(ranges[1], scaleCur));
		if (scaleTo != scaleCur)
			zoomToAnimate(scaleTo, pivotX, pivotY, mAnimationDuration);
		else
			springBack();
	}

	public void rotateBy(float degrees, float pivotX, float pivotY) {
		mRotating = true;
		mMatrixTrans.postRotate(degrees, pivotX, pivotY);
		computeDrawMatrix();
		invalidate();
	}

	public void rotateTo(float rotateTo, float pivotX, float pivotY) {
		final float degrees = rotateTo - getTransRotation();
		rotateBy(degrees, pivotX, pivotY);
	}

	public void rotateToAnimate(float rotateTo, boolean normalize, float pivotX, float pivotY, int duration, Runnable endTask) {
		final float rotateFrom = getTransRotation();
		final int degrees = Math.round(rotateTo + (rotateTo > 0 ? 45 : -45)) / 90 * 90;

		final float[] ranges = getScaleRanges();
		float scale0 = 1, scale1 = 1;
		if (degrees % 180 != 0) { // fix the min range if rotate
			final float[] values = mMatrixValues;
			mMatrixBase.getValues(values);
			scale0 = Picture.getMatrixScale(values);

			// rotate the pictures to fix scale ranges
			if (mPicture != null)
				mPicture.rotate(degrees);
			computeBaseMatrix();
			mMatrixBase.getValues(values);
			scale1 = Picture.getMatrixScale(values);

			// restore the rotation
			if (mPicture != null)
				mPicture.rotate(-degrees);
			computeBaseMatrix();

			// fix the min range
			if (scale0 > 0)
				ranges[0] = ranges[0] * scale1 / scale0;
			//	Log.d("Picture", "base scale: " + scale0 + "->" + scale1);
		}

		final float scaleFrom = getTransScale();
		final float scaleTo = normalize ? scale1 / scale0 : Math.max(ranges[0], Math.min(ranges[1], scaleFrom));

		transformAnimate(scaleFrom, scaleTo, rotateFrom, rotateTo, pivotX, pivotY, duration, endTask);
	}

	public void flipAnimate(final boolean horizontal, final Runnable endTask) {
		final float pivotX = mViewWidth / 2f;
		final float pivotY = mViewHeight / 2f;
		final Camera camera = new Camera();
		final Matrix matrix = new Matrix();
		final Matrix matrixSaved = new Matrix(mMatrixTrans);

		if (mAnimator != null)
			mAnimator.stop();
		mAnimator = new SimpleAnimator(this, mInterpolator) {

			@Override
			public void onStart() {
				mTransforming = true;
			}

			@Override
			public void onDoing(float interpolation) {
				final float degree = 180 * interpolation;
				camera.save();
				if (horizontal)
					camera.rotateY(degree);
				else
					camera.rotateX(degree);
				camera.getMatrix(matrix);
				camera.restore();
				matrix.preScale(0.2f, 0.2f);
				matrix.postScale(5.0f, 5.0f);
				matrix.preTranslate(-pivotX, -pivotY);
				matrix.postTranslate(pivotX, pivotY);

				mMatrixTrans.set(matrixSaved);
				mMatrixTrans.postConcat(matrix);

				computeDrawMatrix();
				invalidate();
			}

			@Override
			public void onEnd() {
				mTransforming = false;
				invalidate();

				if (endTask != null)
					endTask.run();
			}
		};
		mAnimator.start(mAnimationDuration, false);
	}

	public void transformAnimate(final float scaleFrom, final float scaleTo,
								 final float rotateFrom, final float rotateTo,
								 final float pivotX, final float pivotY,
								 final int duration,
								 final Runnable endTask) {
		final float scaleDelta = scaleTo - scaleFrom;

		final float rotateDelta = rotateTo - rotateFrom;
		final int degrees = Math.round(rotateTo + (rotateTo > 0 ? 45 : -45)) / 90 * 90;

		// save the current transform matrix, then transform to target
		final Matrix matrixSaved = new Matrix(mMatrixTrans);
		if (scaleFrom != scaleTo)
			mMatrixTrans.postScale(scaleTo / scaleFrom, scaleTo / scaleFrom, pivotX, pivotY);
		if (rotateFrom != rotateTo)
			mMatrixTrans.postRotate(rotateDelta, pivotX, pivotY);
		computeDrawMatrix();
		final float centerX = mDrawRect.centerX();
		final float centerY = mDrawRect.centerY();
		springBack(0);
		final float offsetX = mDrawRect.centerX() - centerX;
		final float offsetY = mDrawRect.centerY() - centerY;
		//	mMatrixTrans.set(matrixSaved);
		//	computeDrawMatrix();

		final boolean doScale = scaleFrom != scaleTo;
		final boolean doRotate = rotateFrom != rotateTo;
		final boolean doTranslate = offsetX != 0 || offsetY != 0;

		if (mAnimator != null)
			mAnimator.stop();
		mAnimator = new SimpleAnimator(this, mInterpolator) {
			private final Matrix mMatrixScale = doScale ? new Matrix() : null;
			private final Matrix mMatrixRotate = doRotate ? new Matrix() : null;
			private final Matrix mMatrixTranslate = doTranslate ? new Matrix() : null;

			public void onStart() {
				mTransforming = doScale || doRotate;
			}

			public void onDoing(float interpolation) {
				mMatrixTrans.set(matrixSaved);
				if (doScale) {
					final float scaleInter = (scaleFrom + scaleDelta * interpolation) / scaleFrom;
					mMatrixScale.setScale(scaleInter, scaleInter, pivotX, pivotY);
					mMatrixTrans.postConcat(mMatrixScale);
				}
				if (doRotate) {
					final float rotateInter = rotateDelta * interpolation;
					mMatrixRotate.setRotate(rotateInter, pivotX, pivotY);
					mMatrixTrans.postConcat(mMatrixRotate);
					mRotating = true;
				}
				if (doTranslate) {
					final float dx = offsetX * interpolation;
					final float dy = offsetY * interpolation;
					mMatrixTranslate.setTranslate(dx, dy);
					mMatrixTrans.postConcat(mMatrixTranslate);
				}
				computeDrawMatrix();
				invalidate();
			}

			public void onEnd() {
				mAnimator = null;
				mRotating = false;
				mTransforming = false;

				if (degrees != 0) {
					final Matrix matrixBase = mFullPicture != null ? mFullMatrixBase : mMatrixBase;
					final Matrix matrixDraw = mFullPicture != null ? mFullMatrixDraw : mMatrixDraw;

					// rotate the pictures to fix base matrix
					if (mPicture != null)
						mPicture.rotate(degrees);
					if (mFullPicture != null)
						mFullPicture.rotate(degrees);
					computeBaseMatrix();

					// fix transform matrix to fit base and draw matrix, reset transform rotation to 0
					matrixBase.invert(mMatrixTrans);
					mMatrixTrans.postConcat(matrixDraw);
					computeDrawMatrix();
				}
				invalidate();

				if (endTask != null)
					endTask.run();

				if (!mTransforming && mListener != null)
					mListener.onZoom(scaleTo / scaleFrom, false);
			}
		};
		mAnimator.start(duration, false);
	}
}
