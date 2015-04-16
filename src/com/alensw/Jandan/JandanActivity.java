package com.alensw.Jandan;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.AsyncTask;
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
import com.alensw.ui.BadgeView;
import com.alensw.ui.FloatingActionButton;
import com.alensw.ui.FloatingActionButton1;
import com.larvalabs.svgandroid.SVG;

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
	private Toolbar toolbar;
	private ListView mDrawerList;
	private ActionBarDrawerToggle toggle=null;
	private BadgeView mBadgeView;
	protected SimpleAdapter mAdapter;
	protected FloatingActionButton mButton;
	int page = 0;
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

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setBackgroundDrawable(new ColorDrawable(Color.argb(255, 255, 235, 54)));
		if (toolbar != null) {
			toolbar.setTitle("煎蛋");
			setSupportActionBar(toolbar);
		}
		toolbar.setLogo(R.drawable.jandan);
		View NavView = getNavButtonView(toolbar);
		mBadgeView = new BadgeView(this, NavView);
		mBadgeView.setBackground(SVG.getDrawable(getResources(), R.raw.ic_dot));
		mBadgeView.setBadgePosition(1);
		mBadgeView.setBadgeMargin(0, 0);
		mBadgeView.show();

		mButton = (FloatingActionButton) findViewById(R.id.setter);
		mButton.setSize(FloatingActionButton.SIZE_MINI);
		mButton.setColorNormalResId(R.color.white);
		mButton.setColorPressedResId(R.color.white_pressed);
		mButton.setIconDrawable(SVG.getDrawable(getResources(), R.raw.ic_favorite, R.color.half_black));
		mButton.setStrokeVisible(false);

		mButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mButton.setClickState();
				mButton.setIconDrawable(createDrawable(mButton.mClicked));
				mButton.invalidate();
			}
		});


		FloatingActionButton1 mFab = new FloatingActionButton1.Builder(this)
				.withColor(getResources().getColor(R.color.white))
				.withDrawable(getResources().getDrawable(R.drawable.icon_expand))
				.withSize(72)
				.withMargins(16, 0, 0, 16)
				.create();

		if (getFragmentManager().findFragmentById(R.id.content) == null) {
			showNews();
		}
		initDrawer();
		Log.d("JandanActivity", "initDrawer()");

	}


	public static final int[] STATE_CHECKED = {android.R.attr.state_checked};
	public static final int[] STATE_NORMAL = {};

	public static Drawable createCheckButtonDrawable(Resources res, int color) {
		final StateListDrawable sd = new StateListDrawable();
		final Drawable d1 = SVG.getDrawable(res, R.raw.ic_favorite, color);
		final Drawable d2 = SVG.getDrawable(res, R.raw.ic_favorite_outline, color);
		sd.addState(STATE_CHECKED, d1);
		sd.addState(STATE_NORMAL, d2);
		return sd;
	}

	public Drawable createDrawable(boolean clicked) {
		if (clicked)
			return SVG.getDrawable(getResources(), R.raw.ic_favorite, R.color.half_black);
		else
			return SVG.getDrawable(getResources(), R.raw.ic_favorite_outline, R.color.half_black);
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
	private class notifyDataSetChanged extends AsyncTask<Void, Void, Void>{
		@Override
		protected Void doInBackground(Void... voids) {
			return null;
		}
		protected void onPostExecute(Void voids){
			mAdapter.notifyDataSetChanged();
		}
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
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerPanel = (ViewGroup) mDrawerLayout.findViewById(R.id.drawer_panel);
		mDrawerList = (ListView) mDrawerPanel.findViewById(R.id.drawer_list);

		initList();
		mDrawerPanel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "mDrawerPanel onClick");
				if (mDrawerLayout.isDrawerOpen(Gravity.LEFT))
					mDrawerLayout.closeDrawer(Gravity.LEFT);
			}
		});
		SimpleAdapter sa = new SimpleAdapter(this, list,
				R.layout.drawer_row, strings, ids
		);

		mDrawerList.setAdapter(sa);
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
					}
					public void onDrawerClosed(View view) {
						super.onDrawerClosed(view);
						setTitle(R.string.app_name);
					}
				};
		mDrawerLayout.setDrawerListener(toggle);

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

	private void setActionBar() {
		LayoutInflater inflater = (LayoutInflater) getActionBar()
				.getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);

		View customActionBarView = inflater.inflate(R.layout.actionbar, null);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(
				ActionBar.DISPLAY_SHOW_CUSTOM,
				ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
						| ActionBar.DISPLAY_SHOW_TITLE);
		actionBar.setCustomView(customActionBarView,
				new ActionBar.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));
	}
	private void showNews() {
		if (newsFrag == null) {
			newsFrag = new NewsFragment();
		}
		if (!newsFrag.isVisible()) {
			getFragmentManager().popBackStack();
			getFragmentManager().beginTransaction().replace(R.id.content, newsFrag).commit();
		}
	}
	private void showPic() {
		if (picFrag == null) {
			picFrag = new PicFragment();
		}
		if (!picFrag.isVisible()) {
			getFragmentManager().popBackStack();
			getFragmentManager().beginTransaction().replace(R.id.content, picFrag).commit();
		}
	}
	private void showOOXX() {

	}
}
