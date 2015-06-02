package com.nut.ui;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import com.nut.gif.GifMovie;

/**
 * Created by Administrator on 2015/5/28.
 */
public class GifTouchImageView extends TouchImageView implements Gif {

    private Paint mBkgndPaint;
    private GifMovie mGifMovie;

    public GifTouchImageView(Context context) {
        super(context);
    }

    public GifTouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GifTouchImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setGifMovie(GifMovie gifMovie) {
        mBkgndPaint = new Paint(Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
        mGifMovie = gifMovie;
        setImageBitmap(mGifMovie.mBitmap);
        mGifMovie.start(this, mBkgndPaint);
        invalidate();
    }

    @Override
    public GifMovie getGifMovie() {
        return mGifMovie != null ? mGifMovie : null;
    }
}
