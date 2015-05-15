package com.nut.Jandan;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.larvalabs.svgandroid.SVG;
import com.nut.ui.BadgeView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JandanActivity extends ActionBarActivity implements
		OnItemClickListener {
	private final String TAG = "JandanActivity";
	private NewsFragment newsFrag = null;
	private PicFragment picFrag = null;
	private DrawerLayout mDrawerLayout;
	private ViewGroup mDrawerPanel;
	private Toolbar mToolbar;
	private ListView mDrawerList;
	private ActionBarDrawerToggle toggle=null;
	private BadgeView mBadgeView;
	protected SimpleAdapter mAdapter;

	protected List<Map<String, Object>> items = new ArrayList<Map<String,Object>>();
	private ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
	private String[] strings = {"img", "title", "info"};
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
			setSupportActionBar(mToolbar);
		}
		mToolbar.setLogo(R.drawable.jandan);
		View NavView = getNavButtonView(mToolbar);
		mBadgeView = new BadgeView(this, NavView);
		mBadgeView.setBackground(SVG.getDrawable(getResources(), R.raw.ic_dot));
		mBadgeView.setBadgePosition(1);
		mBadgeView.setBadgeMargin(0, 0);
		mBadgeView.show();


		if (getFragmentManager().findFragmentById(R.id.content) == null) {
			showNews();
		}
		initDrawer();
	}

	private ImageButton getNavButtonView(Toolbar toolbar) {
		for (int i = 0; i < toolbar.getChildCount(); i++)
			if (toolbar.getChildAt(i) instanceof ImageButton)
				return (ImageButton) toolbar.getChildAt(i);

		return null;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		toggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		toggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (toggle.onOptionsItemSelected(item)) {
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
		SimpleAdapter sa = new SimpleAdapter(this, list,
				R.layout.drawer_row, strings, ids
		);

		mDrawerList.setAdapter(new DrawerAdapter());
		sa.setViewBinder(new SimpleAdapter.ViewBinder() {
			@Override
			public boolean setViewValue(View view, Object data, String textRepresentation) {
				if (view instanceof ImageView && data instanceof Drawable){
					ImageView iv = (ImageView) view;
					iv.setImageDrawable((Drawable)data);
					return true;
				}
				return false;
			}
		});
		mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position == 0) {
					final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(view.getContext());
					final SharedPreferences.Editor editor = pref.edit();
					editor.putBoolean("news", true);
					editor.apply();
					if (pref.getBoolean("news", false))
						mBadgeView.hide();
					showNews();
				} else if (position == 1) {
					showPic();
				} else if (position == 2) {
					showOOXX();
				}

				mDrawerLayout.closeDrawers();
			}
		});

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		toggle = new ActionBarDrawerToggle(this, mDrawerLayout,
						R.string.app_name,
						R.string.app_name) {
					public void onDrawerOpened(View view) {
						super.onDrawerOpened(view);
						setTitle("Drawer Opened");
						mDrawerList.requestFocus();
						Fragment fragment = getFragmentManager().findFragmentById(R.id.content);
						Log.d(TAG, "drawer open: " + fragment.getClass());
						if (fragment instanceof PicFragment) {
							mDrawerList.getItemAtPosition(1);
						}
					}
					public void onDrawerClosed(View view) {
						super.onDrawerClosed(view);
						setTitle(R.string.app_name);
					}
				};
		mDrawerLayout.setDrawerListener(toggle);

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

			viewHolder.image.setBackgroundColor(choosed(position) ? Color.BLACK : Color.TRANSPARENT);

			return convertView;
		}
	}

	;

	private boolean choosed(int pos) {
		switch (pos) {
			case 0:
				return getFragmentManager().findFragmentById(R.id.content) instanceof NewsFragment;
			case 1:
				return getFragmentManager().findFragmentById(R.id.content) instanceof PicFragment;
			case 2:
				break;
			default:
				break;
		}
		return false;
	}

	private void initList(){
		final HashMap<String, Object> newsMap = new HashMap<String, Object>();
		final TextView tv = (TextView)findViewById(R.id.title);
		//final int color = tv.getTextColors().getDefaultColor();
		//Log.d("JandanActivity", "color = " + color);
		Drawable iconNews = SVG.getDrawable(getResources(), R.raw.news,
				 0x00ffffff | 0xc0000000);
		newsMap.put("img", iconNews);
		newsMap.put("title", "新鲜事");
		newsMap.put("info", "地球上没有新鲜事");
		list.add(newsMap);

		final HashMap<String, Object> picMap = new HashMap<String, Object>();
		Drawable iconPic = SVG.getDrawable(getResources(), R.raw.pic,
				 0x00ffffff | 0xc0000000);
		picMap.put("img", iconPic);
		picMap.put("title", "无聊图");
		picMap.put("info", "");
		list.add(picMap);

		final HashMap<String, Object> ooxxMap = new HashMap<String, Object>();
		Drawable iconOOXX = SVG.getDrawable(getResources(), R.raw.ooxx,
				 0x00ffffff | 0xc0000000);
		ooxxMap.put("img", iconOOXX);
		ooxxMap.put("title", "妹子图");
		ooxxMap.put("info", "");
		list.add(ooxxMap);
	}

	public Toolbar getToolbar() {
		if (mToolbar != null)
			return mToolbar;
		return null;
	}
	private void showNews() {
		if (newsFrag == null) {
			newsFrag = new NewsFragment();
		}
		if (!newsFrag.isVisible()) {
			getFragmentManager().popBackStack();
			FragmentTransaction transaction = getFragmentManager().beginTransaction();
			transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
			transaction.replace(R.id.content, newsFrag).commit();
		}
	}
	private void showPic() {
		if (picFrag == null) {
			picFrag = new PicFragment();
		}
		if (!picFrag.isVisible()) {
			getFragmentManager().popBackStack();
			FragmentTransaction transaction = getFragmentManager().beginTransaction();
			transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
			transaction.replace(R.id.content, picFrag).commit();
		}
	}
	private void showOOXX() {

	}
}
