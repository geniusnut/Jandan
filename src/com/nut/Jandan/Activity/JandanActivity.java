package com.nut.Jandan.Activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.larvalabs.svgandroid.SVG;
import com.nut.Jandan.Fragment.NewsFragment;
import com.nut.Jandan.Fragment.PicsFragment;
import com.nut.Jandan.Fragment.SettingsFragment;
import com.nut.Jandan.R;
import com.nut.ui.BadgeView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JandanActivity extends BarFragmentActivity implements
		OnItemClickListener {
	private final String TAG = "JandanActivity";
	private NewsFragment newsFrag = null;
	private PicsFragment picFrag = null;
	private ViewGroup mDrawerPanel;
	private Toolbar mToolbar;
	private ListView mDrawerList;
	private ActionBarDrawerToggle toggle=null;
	private BadgeView mBadgeView;

	private int mSelected = -1;

	protected List<Map<String, Object>> items = new ArrayList<Map<String,Object>>();
	private ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
	private static String[] strings = {"img", "title", "info"};
	private int[] ids = {R.id.img, R.id.title, R.id.info};
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);


		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		final SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean("news", false);
		editor.apply();

		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		if (mToolbar != null) {
			mToolbar.setTitle("煎蛋");
			// ToolbarColorHelper.colorizeToolbar(mToolbar, Color.BLACK, this);
			setSupportActionBar(mToolbar);
		}
		mToolbar.setLogo(R.drawable.jandan);
		View NavView = getNavButtonView(mToolbar);

		initDrawer();
		if (getFragmentManager().findFragmentById(R.id.content) == null) {
			mSelected = 0;
			showNews();
		}

	}

	@Override
	public int getContentId() {
		return R.id.content;
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
		mDrawerPanel = (ViewGroup) mDrawerLayout.findViewById(R.id.drawer_panel);
		mDrawerList = (ListView) mDrawerPanel.findViewById(R.id.drawer_list);

		initList();
		mDrawerPanel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mDrawerLayout.isDrawerOpen(Gravity.LEFT))
					mDrawerLayout.closeDrawer(Gravity.LEFT);
			}
		});

		mDrawerList.setAdapter(new DrawerAdapter());
		mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
				if (position == 0) {
					final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(view.getContext());
					final SharedPreferences.Editor editor = pref.edit();
					editor.putBoolean("news", true);
					editor.apply();
					showNews();
				} else if (position == 1) {
					showPic();
				} else if (position == 2) {
					showOOXX();
				}
				mDrawerLayout.closeDrawers();
			}
		});

		mDrawerLayout.setDrawerListener(toggle);

		setDrawerLayout(mDrawerLayout);

		final View settings = findViewById(R.id.settings);
		settings.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// ((BaseFragmentActivity) v.getContext()).showFragment(new SettingsFragment());
				new SettingsFragment().show((BaseFragmentActivity) mDrawerLayout.getContext());
				mDrawerLayout.closeDrawers();
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


	private void initList(){
		final HashMap<String, Object> newsMap = new HashMap<String, Object>();
		final TextView tv = (TextView)findViewById(R.id.title);
		final int color = 0x8A000000;

		//Log.d("JandanActivity", "color = " + color);
		Drawable iconNews = SVG.getDrawable(getResources(), R.raw.ic_explore_24px, color);
		newsMap.put("img", iconNews);
		newsMap.put("title", "新鲜事");
		newsMap.put("info", "地球上没有新鲜事");
		list.add(newsMap);

		final HashMap<String, Object> picMap = new HashMap<String, Object>();
		Drawable iconPic = SVG.getDrawable(getResources(), R.raw.ic_image_24px, color);
		picMap.put("img", iconPic);
		picMap.put("title", "无聊图");
		picMap.put("info", "");
		list.add(picMap);

		final HashMap<String, Object> ooxxMap = new HashMap<String, Object>();
		Drawable iconOOXX = SVG.getDrawable(getResources(), R.raw.ic_local_movies_24px, color);
		ooxxMap.put("img", iconOOXX);
		ooxxMap.put("title", "妹子图");
		ooxxMap.put("info", "");
		list.add(ooxxMap);

		ImageView settingImage = (ImageView) findViewById(R.id.image_setting);
		Drawable iconSetting = SVG.getDrawable(getResources(), R.raw.ic_settings_applications_24px, color);
		settingImage.setImageDrawable(iconSetting);
	}

	public Toolbar getToolbar() {
		if (mToolbar != null)
			return mToolbar;
		return null;
	}
	private void showNews() {
		mDrawerList.setItemChecked(0, true);
		if (newsFrag == null)
			newsFrag = new NewsFragment();
		this.showOnly(newsFrag);
	}
	private void showPic() {
		mDrawerList.setItemChecked(1, true);
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
}
