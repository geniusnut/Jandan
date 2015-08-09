package com.nut.Jandan.Activity;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.larvalabs.svgandroid.SVG;
import com.nut.Jandan.Fragment.*;
import com.nut.Jandan.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JandanActivity extends BarFragmentActivity implements
		OnItemClickListener {
	private final String TAG = "JandanActivity";
	private NewsFragment newsFrag = null;
	private PicsFragment picFrag = null;
	private JokeFragment jokeFrag;

	private ViewGroup mDrawerPanel;
	private Toolbar mToolbar;
	private TabLayout mTableLayout;
	private ListView mDrawerList;

	private int mSelected = -1;

	protected List<Map<String, Object>> items = new ArrayList<Map<String,Object>>();
	private ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
	private static String[] strings = {"img", "title", "info"};
	private int[] ids = {R.id.img, R.id.title, R.id.info};
	private ImageView mAccountToggle;
	private NavigationView mNavigationView;

	private SampleFragmentPagerAdapter mFragmentAdapter;
	private ViewPager mViewPager;
	private Menu mNaviMenu;

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		final SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean("news", false);
		editor.apply();

		// setStatusBarColor(findViewById(R.id.statusBar), getResources().getColor(R.color.yellowA200));

		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		if (mToolbar != null) {
			mToolbar.setTitle("煎蛋");
			setSupportActionBar(mToolbar);
		}
		mToolbar.setLogo(R.drawable.jandan);
		View NavView = getNavButtonView(mToolbar);

		initDrawer();
		showFragment(new JandanFragment());
	}

	public void setStatusBarColor(View statusBar, int color) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Window w = getWindow();
			w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			//status bar height

			int statusBarHeight = getStatusBarHeight();
			//action bar height
			statusBar.getLayoutParams().height = statusBarHeight;
			statusBar.setBackgroundColor(color);
		}
	}

	public int getActionBarHeight() {
		int actionBarHeight = 0;
		TypedValue tv = new TypedValue();
		if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
			actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
		}
		return actionBarHeight;
	}

	public int getStatusBarHeight() {
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	@Override
	public int getContentId() {
		return R.id.container;
	}

	private ImageButton getNavButtonView(Toolbar toolbar) {
		for (int i = 0; i < toolbar.getChildCount(); i++)
			if (toolbar.getChildAt(i) instanceof ImageButton)
				return (ImageButton) toolbar.getChildAt(i);

		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return(true);
		}

		return(super.onOptionsItemSelected(item));
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{

	}

	private void initDrawer(){
		mDrawerLayout = (DrawerLayout) this.findViewById(R.id.drawer_layout);
		mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

		mDrawerLayout.setDrawerListener(mDrawerToggle);
		setDrawerLayout(mDrawerLayout);

		final Resources res = getResources();
		mNaviMenu = mNavigationView.getMenu();
//		mNaviMenu.getItem(0).setIcon(SVG.getDrawable(res, R.raw.ic_explore_24px));
//		mNaviMenu.getItem(1).setIcon(SVG.getDrawable(res, R.raw.ic_photo_library_24px));
//		mNaviMenu.getItem(2).setIcon(SVG.getDrawable(res, R.raw.ic_face_24px));
//		mNaviMenu.getItem(3).setIcon(SVG.getDrawable(res, R.raw.ooxx));
//
//		mNaviMenu.findItem(R.id.nav_setting).setIcon(SVG.getDrawable(res, R.raw.ic_settings_applications_24px));
		if (mNavigationView != null) {
			setupDrawerContent();
		}
	}

	private void setupDrawerContent() {
		mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(MenuItem menuItem) {
				menuItem.setChecked(true);
				int id = menuItem.getItemId();
				switch (id) {
					case R.id.nav_news:
						mViewPager.setCurrentItem(0);
						break;
					case R.id.nav_pics:
						mViewPager.setCurrentItem(1);
						break;
					case R.id.nav_jokes:
						mViewPager.setCurrentItem(2);
						break;
					case R.id.nav_ooxx:
						mViewPager.setCurrentItem(3);
						break;
				}
				mDrawerLayout.closeDrawers();
				return true;
			}
		});
	}

	private class ViewHolder {
		ImageView image;
		TextView title;
		TextView info;
	}

	private class DrawerAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder viewHolder;
			if (convertView == null) {
				LayoutInflater inflater = getLayoutInflater();
				convertView = inflater.inflate(R.layout.drawer_row, null);
				viewHolder = new ViewHolder();
				viewHolder.image = (ImageView) convertView.findViewById(R.id.img);
				viewHolder.title = (TextView) convertView.findViewById(R.id.title);
				viewHolder.info = (TextView) convertView.findViewById(R.id.info);
				convertView.setTag(viewHolder);
			} else
				viewHolder = (ViewHolder) convertView.getTag();

			HashMap<String, Object> item = list.get(position);

			viewHolder.image.setImageDrawable((Drawable) item.get("img"));
			viewHolder.title.setText((String) item.get("title"));
			viewHolder.info.setText((String) item.get("info"));

			if (mSelected == position)
				convertView.setBackgroundColor(getResources().getColor(R.color.tealA200));
			else
				convertView.setBackgroundColor(getResources().getColor(R.color.teal500));
			return convertView;
		}
	}

	public Toolbar getToolbar() {
		if (mToolbar != null)
			return mToolbar;
		return null;
	}
	private void showNews() {
		if (newsFrag == null)
			newsFrag = new NewsFragment();
		this.showOnly(newsFrag);
	}
	private void showPic() {
		if (picFrag == null)
			picFrag = new PicsFragment();
		this.showOnly(picFrag);
	}

	private void showOOXX() {

	}

	static int mActionBarSize;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static int getActionBarSize(Context context) {
		if (mActionBarSize == 0) {
			final Resources res = context.getResources();
			final TypedValue value = new TypedValue();
			context.getTheme().resolveAttribute(android.R.attr.actionBarSize, value, true);
			mActionBarSize = (int) value.getDimension(res.getDisplayMetrics());
		}
		return mActionBarSize;
	}

	public class SampleFragmentPagerAdapter extends FragmentPagerAdapter {
		final int PAGE_COUNT = 4;
		private int tabTitles[] = new int[]{R.string.nav_news, R.string.nav_pics, R.string.nav_joke, R.string.nav_ooxx};
		private Context context;

		public SampleFragmentPagerAdapter(FragmentManager fm, Context context) {
			super(fm);
			this.context = context;
		}

		@Override
		public int getCount() {
			return PAGE_COUNT;
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
				case 0:
					if (newsFrag == null)
						newsFrag = new NewsFragment();
					return newsFrag;
				case 1:
					if (picFrag == null)
						picFrag = new PicsFragment();
					return picFrag;
				case 2:
					if (jokeFrag == null)
						jokeFrag = new JokeFragment();
					return jokeFrag;
			}
			return PageFragment.newInstance(position + 1);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			// Generate title based on item position
			return getString(tabTitles[position]);
		}
	}
}
