package com.alensw.Jandan;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.ActionBarDrawerToggle;

import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
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
	protected ListView mListView;
	protected SimpleAdapter mAdapter;
	protected JandanParser mParser;
	public static NewsLoader mNewsLoader;
	private Handler mHandler = new Handler(Looper.getMainLooper());
	protected boolean isParsing = false;
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
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setLogo(SVG.getDrawable(getResources(), R.raw.jandan));
		}
		toolbar.setNavigationIcon(R.drawable.ic_ab_drawer);
		/*mListView = (ListView) findViewById(R.id.news_list);
		mParser = new JandanParser(this);
		mNewsLoader = new NewsLoader();
		mNewsLoader.execute(++page);


		mAdapter = new SimpleAdapter(this, items, R.layout.news_item,
				new String[]{"link", "image", "title", "by", "tag", "cont"},
				new int[]{R.id.link, R.id.image, R.id.title, R.id.by, R.id.tag, R.id.cont});
		mListView.setAdapter(mAdapter);
		mAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
			@Override
			public boolean setViewValue(View view, Object data, String textRepresentation) {
				if ((view instanceof ImageView) && (data instanceof Bitmap)) {
					ImageView imageView = (ImageView) view;
					Bitmap bmp = (Bitmap) data;
					imageView.setImageBitmap(bmp);
					return true;
				}
				return false;
			}
		});

		mParser.setOnImageChangedlistener(new JandanParser.OnImageChangedlistener() {
			@Override
			public void OnImageChanged() {
				//mAdapter.notifyDataSetChanged();
				new notifyDataSetChanged().execute();
			}
		});

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				TextView link = (TextView) view.findViewById(R.id.link);
				TextView title = (TextView) view.findViewById(R.id.title);
				TextView comm = (TextView)view.findViewById(R.id.cont);
				String acomm = comm.getText().toString();
				String atitle = title.getText().toString();
				String alink = link.getText().toString();
				Intent intent = new Intent(view.getContext(), PostActivity.class);
				intent.putExtra("link",alink);
				intent.putExtra("comm",acomm);
				intent.putExtra(Intent.EXTRA_TITLE,atitle);
				startActivity(intent);
			}
		});
		mListView.setOnScrollListener(new AbsListView.OnScrollListener(){
			int vPosition = 0;
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (mListView.getFirstVisiblePosition() > 0) {
					if (mListView.getFirstVisiblePosition() != vPosition) {
						if (mAdapter.getCount() - 8 <= mListView.getFirstVisiblePosition()) {
							if (!isParsing) {
								//mNewsLoader.execute(++page);
								new NewsLoader().execute(++page);
							}
						}
					}
					vPosition = mListView.getFirstVisiblePosition();
				}
			}
		});*/
		if (getFragmentManager().findFragmentById(R.id.content) == null) {
			showNews();
		}
		Log.d("JandanActivity", "initDrawer()");
		initDrawer();
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
		toggle =
				new ActionBarDrawerToggle(this, mDrawerLayout,
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
		newsMap.put("title", "News");
		newsMap.put("info", "There is no news around world!");
		list.add(newsMap);

		final HashMap<String, Object> picMap = new HashMap<String, Object>();
		Drawable iconPic = SVG.getDrawable(getResources(), R.raw.pic,
				 0x00ffffff | 0xc0000000);
		picMap.put("img", iconPic);
		picMap.put("title", "Picture");
		picMap.put("info", "There is no news around world!");
		list.add(picMap);

		final HashMap<String, Object> ooxxMap = new HashMap<String, Object>();
		Drawable iconOOXX = SVG.getDrawable(getResources(), R.raw.ooxx,
				 0x00ffffff | 0xc0000000);
		ooxxMap.put("img", iconOOXX);
		ooxxMap.put("title", "OOXX");
		ooxxMap.put("info", "There is no news around world!");
		list.add(ooxxMap);
	}

	private class NewsLoader extends AsyncTask<Integer, Void, List<Map<String, Object>>> {
		@Override
		protected List<Map<String, Object>> doInBackground(Integer... page) {
			isParsing = true;
			List<Map<String, Object>> list = mParser.JandanHomePage(page[0]);
			if (page[0] == 1){
				items.clear();
			}
			return list;

		}
		protected void onPostExecute(List<Map<String, Object>> result) {
			if(result.isEmpty()){
				//Toast.makeText(, "载入出错了！请稍后再试。", Toast.LENGTH_SHORT).show();
			}
			items.addAll(result);
			mAdapter.notifyDataSetChanged();
			isParsing = false;
		}
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
