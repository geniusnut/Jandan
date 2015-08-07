package com.alensw.view;

import android.view.animation.Interpolator;

public class ViscousFluidInterpolator implements Interpolator {
	public static final float DEFAULT_SCALE = 6.0f;

	//	Scale controls the viscous fluid effect (how much of it, default is 8.0f)
	private float mScale = DEFAULT_SCALE;
	private float mNormalize = 1;

	public ViscousFluidInterpolator() {
		this(DEFAULT_SCALE);
	}

	public ViscousFluidInterpolator(float scale) {
		mScale = scale;
		mNormalize = 1 / getInterpolation(1);
	}

	public float getInterpolation(float x) {
		x *= mScale;
		if (x < 1) {
			x -= (1 - (float) Math.exp(-x));
		} else {
			float start = 0.36787944117f;    //	1/e == exp(-1)
			x = 1 - (float) Math.exp(1 - x);
			x = start + x * (1 - start);
		}
		x *= mNormalize;
		return x;
	}
};
