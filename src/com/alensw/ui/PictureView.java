package com.alensw.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.view.View;

/**
 * Created by yw07 on 15-1-5.
 */
public class PictureView extends View {


	public PictureView(Context context) {
		super(context);
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
	}

	@Override
	public void onDraw(Canvas canvas) {

	}
}
