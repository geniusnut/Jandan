package com.nut.ui;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import com.nut.gif.GifMovie;

/**
 * Created by yw07 on 15-5-20.
 */
public class GifImageView extends ScaleImageView {
	private GifMovie mGifMovie;
	private Paint mBkgndPaint;
	private Paint mBitmapPaint;

	public GifImageView(Context context) {
		super(context);
		init();
	}

	public GifImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public GifImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();

	}

	private void init() {
		mBitmapPaint = new Paint(Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
		mBkgndPaint = new Paint(Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
	}

	public void setGif(GifMovie gifMovie) {
		mGifMovie = gifMovie;
		setImageBitmap(mGifMovie.mBitmap);
		mGifMovie.start(this, mBkgndPaint);
	}
}