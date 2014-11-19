package com.alensw.Jandan;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.larvalabs.svgandroid.SVG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JandanActivity extends FragmentActivity implements
		OnItemClickListener {
	private DrawerLayout drawerLayout=null;
	private ActionBarDrawerToggle toggle=null;
	private ListView drawer=null;
	protected ListView mListView;
	protected SimpleAdapter mAdapter;
	protected JandanParser mParser;
	public static NewsLoader mNewsLoader;
	protected boolean isParsing;
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
		mListView = (ListView) findViewById(R.id.news_list);
		mParser = new JandanParser(this);
		mNewsLoader = new NewsLoader();
		mNewsLoader.execute(++page);
		//items.addAll(list);

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
				new notifyDataSetChanged().execute();
			}
		});


		Log.d("JandanActivity", "initDrawer()");

		initDrawer();
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
		drawer=(ListView)findViewById(R.id.drawer);
		drawer.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		initList();
		SimpleAdapter sa = new SimpleAdapter(this, list,
				R.layout.drawer_row, strings, ids
		);
		drawer.setAdapter(sa);
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
		drawer.setOnItemClickListener(this);

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
		toggle=
				new ActionBarDrawerToggle(this, drawerLayout,
						R.drawable.ic_drawer,
						R.string.app_name,
						R.string.app_name) {
					public void onDrawerOpened(View view) {
						super.onDrawerOpened(view);
						setTitle("Drawer Opened");
					}
					public void onDrawerClosed(View view) {
						super.onDrawerClosed(view);
						setTitle(R.string.app_name);
					}
				};
		drawerLayout.setDrawerListener(toggle);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
	}

	private void initList(){
		final HashMap<String, Object> newsMap = new HashMap<String, Object>();
		final TextView tv = (TextView)findViewById(R.id.title);
		//final int color = tv.getTextColors().getDefaultColor();
		//Log.d("JandanActivity", "color = " + color);
		Drawable iconNews = SVG.getDrawable(getResources(), R.raw.logo_discovery,
				 0x00ffffff | 0xc0000000);
		newsMap.put("img", iconNews);
		newsMap.put("title", "News");
		newsMap.put("info", "There is no news around world!");
		list.add(newsMap);

		final HashMap<String, Object> picMap = new HashMap<String, Object>();
		Drawable iconPic = SVG.getDrawable(getResources(), R.raw.logo_gallery,
				 0x00ffffff | 0xc0000000);
		picMap.put("img", iconPic);
		picMap.put("title", "Picture");
		picMap.put("info", "There is no news around world!");
		list.add(picMap);

		final HashMap<String, Object> ooxxMap = new HashMap<String, Object>();
		Drawable iconOOXX = SVG.getDrawable(getResources(), R.raw.logo_flickr,
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


}
