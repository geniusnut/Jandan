package com.nut.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.nut.Jandan.R;

public class FloatingActionButton extends View {

	protected Paint mButtonPaint;
	protected Paint mDrawablePaint;

	protected Bitmap mBitmap;
	protected FrameLayout.LayoutParams mParams;

	private boolean mHidden = false;
	private String mTitle;

	public FloatingActionButton(Context context, FrameLayout.LayoutParams params) {
		super(context);
		mParams = params;
		init(Color.WHITE);
	}

	public FloatingActionButton(Builder builder) {
		super(builder.context);
		mParams = builder.params;
		setColor(builder.color);
		setDrawable(builder.drawable);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void init(int color) {
		setWillNotDraw(false);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);

		mButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mButtonPaint.setColor(color);
		mButtonPaint.setStyle(Paint.Style.FILL);
		mButtonPaint.setShadowLayer(10.0f, 0.0f, 3.5f, Color.argb(100, 0, 0, 0));
		mDrawablePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		invalidate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(mParams.width, mParams.height);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		setClickable(true);
		// draw the button circle
		canvas.drawCircle(getWidth() / 2, getHeight() / 2, (float) (getWidth() / 2.6), mButtonPaint);
		// draw Bitmap
		canvas.drawBitmap(mBitmap, (getWidth() - mBitmap.getWidth()) / 2,
				(getHeight() - mBitmap.getHeight()) / 2, mDrawablePaint);

	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			setAlpha(1.0f);
		} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
			setAlpha(0.6f);
		}
		return super.onTouchEvent(event);
	}

	public void setColor(int color) {
		init(color);
	}

	public void setDrawable(Drawable drawable) {
		int w = drawable.getIntrinsicWidth();
		int h = drawable.getIntrinsicHeight();
		Bitmap.Config config = Bitmap.Config.ARGB_8888;
		final float iconSize = getResources().getDimension(R.dimen.fab_icon_size);
		if (w == 0 || h == 0) {
			w = (int) iconSize;
			h = (int) iconSize;
		}
		mBitmap = Bitmap.createBitmap(w, h, config);
		Canvas canvas = new Canvas(mBitmap);
		drawable.setBounds(0, 0, w, h);
		drawable.draw(canvas);
		// mBitmap = ((BitmapDrawable) drawable).getBitmap();
		invalidate();
	}

	public void setTitle(String title) {
		mTitle = title;
		TextView label = getLabelView();
		if (label != null) {
			label.setText(title);
		}
	}

	TextView getLabelView() {
		return (TextView) getTag(R.id.fab_label);
	}

	public String getTitle() {
		return mTitle;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void hide() {
		if (!mHidden) {
			ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1, 0);
			ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1, 0);
			AnimatorSet animSetXY = new AnimatorSet();
			animSetXY.playTogether(scaleX, scaleY);
			animSetXY.setInterpolator(new AccelerateInterpolator());
			animSetXY.setDuration(100);
			animSetXY.start();
			mHidden = true;
		}
	}

	public void setMargins(int left, int top, int right, int bottom) {
		mParams.setMargins(left, top, right, bottom);
		this.setLayoutParams(mParams);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void show() {
		if (mHidden) {
			ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 0, 1);
			ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 0, 1);
			AnimatorSet animSetXY = new AnimatorSet();
			animSetXY.playTogether(scaleX, scaleY);
			animSetXY.setInterpolator(new OvershootInterpolator());
			animSetXY.setDuration(200);
			animSetXY.start();
			mHidden = false;
		}
	}

	@Override
	public void setVisibility(int visibility) {
		TextView label = getLabelView();
		if (label != null) {
			label.setVisibility(visibility);
		}

		super.setVisibility(visibility);
	}


	public boolean isHidden() {
		return mHidden;
	}

	public static class Builder<T extends Builder> {
		public FrameLayout.LayoutParams params;
		// private final Activity activity;
		public Context context;
		private ViewGroup root;
		int gravity = Gravity.BOTTOM | Gravity.RIGHT; // default bottom right
		Drawable drawable;
		int color = Color.WHITE;
		int size = 0;
		float scale = 0;

		/**
		 * Constructor using a context for this builder and the
		 * {@link FloatingActionButton} it creates
		 *
		 * @param context
		 */
		public Builder(Context context) {
			scale = context.getResources().getDisplayMetrics().density;
			// The calculation (value * slide_in_right + 0.5f) is a widely used to convert to dps to pixel
			// units based on density slide_in_right
			// see <a href="http://developer.android.com/guide/practices/screens_support.html">
			// developer.android.com (Supporting Multiple Screen Sizes)</a>
			size = (int) (72 * scale + 0.5f); // default size is 72dp by 72dp
			params = new FrameLayout.LayoutParams(size, size);
			params.gravity = gravity;
			this.root = root;
			this.context = context;
		}

		/**
		 * Sets the FAB gravity.
		 */
		public T withGravity(int gravity) {
			this.gravity = gravity;
			return (T) this;
		}

		/**
		 * Sets the FAB margins in dp.
		 */
		public T withMargins(int left, int top, int right, int bottom) {
			params.setMargins((int) (left * scale + 0.5f), (int) (top * scale + 0.5f),
					(int) (right * scale + 0.5f), (int) (bottom * scale + 0.5f));
			return (T) this;
		}

		/**
		 * Sets the FAB drawable.
		 *
		 * @param drawable
		 */
		public T withDrawable(final Drawable drawable) {
			this.drawable = drawable;
			return (T) this;
		}

		/**
		 * Sets the FAB color.
		 *
		 * @param color
		 */
		public T withColor(final int color) {
			this.color = color;
			return (T) this;
		}

		/**
		 * Sets the FAB size.
		 *
		 * @param size
		 * @return
		 */
		public T withSize(int size) {
			size = (int) (size * scale + 0.5f);
			params = new FrameLayout.LayoutParams(size, size);
			return (T) this;
		}

		/**
		 * Creates a {@link FloatingActionButton} with the
		 * arguments supplied to this builder.
		 */
		public FloatingActionButton create() {
			params.gravity = this.gravity;

			final FloatingActionButton button = new FloatingActionButton(context, params);
			button.setColor(this.color);
			button.setDrawable(this.drawable);
			return new FloatingActionButton(this);
		}
	}

}