package com.nut.Jandan.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import com.larvalabs.svgandroid.SVG;
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
	private ShareActionProvider mShareActionProvider;

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

			Log.d(TAG, "mToolbar width" + mToolbar.getMeasuredWidth());
			//share.setImageDrawable(SVG.getDrawable(getResources(), R.raw.ic_menu_share));
			setSupportActionBar(mToolbar);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			mToolbar.bringToFront();
		}

	}

	public Toolbar getToolbar() {
		return mToolbar;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.post, menu);
		int color = getIconColor(this);
		int size = 72;
		MenuItem shareItem = menu.findItem(R.id.share);
		shareItem.setIcon(SVG.getDrawable(getResources(), R.raw.ic_menu_share, color, size));
//		mShareActionProvider = (ShareActionProvider)
//				MenuItemCompat.getActionProvider(shareItem);
//		mShareActionProvider.setShareIntent(getDefaultIntent());

		return super.onCreateOptionsMenu(menu);
	}

	private Intent getDefaultIntent() {
		String mimeType = "text/plain";
		final Intent intentShare = new Intent(Intent.ACTION_SEND);
		intentShare.setType(mimeType);
		intentShare.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		intentShare.putExtra(Intent.EXTRA_TEXT, mNewsFile.get(mViewPager.getCurrentItem()).mLink);
		return intentShare;
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
			case R.id.share:
				// share();
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

	public static int getIconColor(Context context) {
		TypedValue typedValue = new TypedValue();
		//context.getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
		TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{android.R.attr.textColorSecondary});
		return a.getColor(0, 0);
	}
}
