package com.nut.Jandan.Activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.internal.app.ToolbarActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by yw07 on 15-6-4.
 */
public abstract class BarFragmentActivity extends BaseFragmentActivity {
	protected Toolbar mToolbar;
	protected Drawable navIcon;
	protected DrawerLayout mDrawerLayout;
	protected CharSequence navContentDescription;
	protected ActionBarDrawerToggle mDrawerToggle;

	public ActionBarDrawerToggle setDrawerLayout(DrawerLayout mDrawerLayout) {
		if (mDrawerLayout == null) {
			return null;
		}
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		saveToolbar();
		this.mDrawerLayout = mDrawerLayout;
		mDrawerToggle = new ActionBarDrawerToggle(//
				this,
				mDrawerLayout, //
				getToolbar(),//
				0,//
				0);
		mDrawerToggle.syncState();
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		return mDrawerToggle;
	}

	public void enableDrawer() {
		mDrawerToggle.setDrawerIndicatorEnabled(true);
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.LEFT);
	}

	public void disableDrawer() {
		mDrawerToggle.setDrawerIndicatorEnabled(false);
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.LEFT);
		resetToolbar();
	}

	public Toolbar getToolbar() {
		return mToolbar;
	}

	public void setToolbar(Toolbar mToolbar) {
		this.mToolbar = mToolbar;
		// mToolbar.setBackgroundColor(primaryColor);
	}

	public void saveToolbar() {
		navIcon = mToolbar.getNavigationIcon();
		navContentDescription = mToolbar.getNavigationContentDescription();
	}

	public void resetToolbar() {
		mToolbar.setNavigationIcon(navIcon);
		mToolbar.setNavigationContentDescription(navContentDescription);
	}

	@Override
	public void setSupportActionBar(Toolbar toolbar) {
		super.setSupportActionBar(toolbar);
		setToolbar(toolbar);
	}

	private View getActionBarView() {
		Window window = getWindow();
		View v = window.getDecorView();
		int resId = getResources().getIdentifier("action_bar_container", "id", "android");
		return v.findViewById(resId);
	}

	public View getBarView() {
		if (getSupportActionBar() instanceof ToolbarActionBar) {
			return getToolbar();
		}
		return getActionBarView();
	}

	public void hideBar() {
		animateBar(false);
	}

	public void showBar() {
		animateBar(true);
	}

	public View getContentView() {
		View v = findViewById(android.R.id.content);
		if (v == null) {
			return null;
		}
		return v.getRootView();
	}

//    private void changeBarVisible(){
//
//    }

	private void animateBar(final boolean show) {
		ActionBar bar = getSupportActionBar();
		if (bar == null) {
			return;
		}
		//check
		if (show && bar.isShowing()) {
			return;
		} else if (!show && !bar.isShowing()) {
			return;
		}
		// get view of actionbar
		final View barView = getBarView();
		if (barView == null) {
			if (show) {
				bar.show();
			} else {
				bar.hide();
			}
			return;
		}
		// beforeCreate params
		final int height = barView.getHeight();
		final View parentView = getContentView();
		// beforeCreate animator
		Animator animator = ObjectAnimator.ofFloat(0, 1);
		animator.setInterpolator(new AccelerateDecelerateInterpolator() {
			@Override
			public float getInterpolation(float input) {
				final float value = super.getInterpolation(input);
				//
				if (input == 0 && show) {
					barView.setVisibility(View.VISIBLE);
					parentView.scrollTo(0, height);
				}
				//
				final float y = (show ? 1 - value : value) * height;
				parentView.scrollTo(0, (int) y);
				//
				if (input == 1.0 && !show) {
					barView.setVisibility(View.GONE);
					parentView.scrollTo(0, 0);
				}
				return value;
			}
		});
		animator.setDuration(300);
		animator.start();
	}

	public void showBars(boolean visible) {

		// setSystemUiVisible(visible);

		if (visible)
			mToolbar.setVisibility(View.VISIBLE);
		else
			mToolbar.setVisibility(View.GONE);
	}

	@Override
	protected void onFragmentChanged(Fragment shown, Fragment hidden) {
		super.onFragmentChanged(shown, hidden);
		if (mDrawerLayout == null || mDrawerToggle == null) {
			return;
		}
		if (getFragmentSize() <= 1) {
			enableDrawer();
		} else {
			disableDrawer();
		}
	}

}
