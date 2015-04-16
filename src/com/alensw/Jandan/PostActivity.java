package com.alensw.Jandan;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by yw07 on 14-11-20.
 */
public class PostActivity extends ActionBarActivity {
	private final String TAG = "PostActivity";
	Activity postActivity = this;

	private Toolbar toolbar;
	private ViewPager mViewPager;

	private NewsFile mNewsFile = new NewsFile();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		setContentView(R.layout.activity_post);

		mNewsFile.load(this, NewsFile.NEWS_FILE_NAME);
		ViewPager pager = (ViewPager) findViewById(R.id.post_pager);
		pager.setAdapter(new PostAdapter(getSupportFragmentManager()));

		mViewPager = (ViewPager) findViewById(R.id.post_pager);
		mViewPager.setAdapter(new PostAdapter(getSupportFragmentManager()));

		int index = getIntent().getIntExtra("index", 0);
		mViewPager.setCurrentItem(index);
		toolbar = (Toolbar) findViewById(R.id.toolbar_post);
		if (toolbar != null) {
			toolbar.setTitle("News");
			ImageView share = (ImageView) toolbar.findViewById(R.id.share);
			Log.d(TAG, "toolbar width" + toolbar.getMeasuredWidth());
			share.setLayoutParams(new Toolbar.LayoutParams(56, 56, Gravity.RIGHT));
			//share.setImageDrawable(SVG.getDrawable(getResources(), R.raw.ic_menu_share));
			setSupportActionBar(toolbar);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
			case R.id.action_settings:
				return true;
			case android.R.id.home:
				postActivity.finish();
				return true;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void setActionBar() {
		LayoutInflater inflater = (LayoutInflater) getActionBar()
				.getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);

		View customActionBarView = inflater.inflate(R.layout.actionbar_post, null);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(
				ActionBar.DISPLAY_SHOW_CUSTOM,
				ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
						| ActionBar.DISPLAY_SHOW_TITLE);
		actionBar.setCustomView(customActionBarView,
				new ActionBar.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));

		LinearLayout btn_back = (LinearLayout)findViewById(R.id.btn_post_back);
		btn_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				postActivity.finish();
			}
		});
	}

	private class PostAdapter extends FragmentPagerAdapter {
		public PostAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			final Post post = mNewsFile.get(i);
			return PostFragment.newInstance(post);
		}

		@Override
		public int getCount() {
			return mNewsFile.size();
		}
	}
}
