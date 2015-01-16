package com.alensw.Jandan;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import com.alensw.ui.PictureView;

/**
 * Created by yw07 on 14-12-18.
 */
public class PicActivity extends ActionBarActivity {
	private PictureView mPictureView;

	@Override
	public void onCreate(Bundle savedBundle) {
		super.onCreate(savedBundle);
		setContentView(R.layout.viewer);
		mPictureView = (PictureView) findViewById(R.id.image);

	}
}
