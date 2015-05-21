package com.nut.Jandan.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.nut.Jandan.Fragment.PostFragment;
import com.nut.Jandan.R;
import com.nut.cache.NewsFile;
import com.nut.cache.Post;

/**
 * Created by yw07 on 14-11-20.
 */
public class PostActivity extends ActionBarActivity {
	private final String TAG = "PostActivity";
	Activity postActivity = this;

	private Toolbar mToolbar;
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
		mToolbar = (Toolbar) findViewById(R.id.toolbar_post);
		if (mToolbar != null) {
			mToolbar.setTitle("News");
			ImageView share = (ImageView) mToolbar.findViewById(R.id.share);
			Log.d(TAG, "mToolbar width" + mToolbar.getMeasuredWidth());
			share.setLayoutParams(new Toolbar.LayoutParams(56, 56, Gravity.RIGHT));
			//share.setImageDrawable(SVG.getDrawable(getResources(), R.raw.ic_menu_share));
			setSupportActionBar(mToolbar);
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
		public Object instantiateItem(ViewGroup container, int position) {
			Fragment fragment = (Fragment) super.instantiateItem(container, position);
			return fragment;
		}

		@Override
		public int getCount() {
			return mNewsFile.size();
		}
	}
}
