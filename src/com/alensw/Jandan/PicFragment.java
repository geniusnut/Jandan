package com.alensw.Jandan;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by yw07 on 14-12-1.
 */
public class PicFragment extends Fragment {
	protected ListView mListView;
	SimpleAdapter mAdapter;

	public PicLoader mPicLoader;
	List<Map<String, Object>> items = new ArrayList<Map<String,Object>>();

	FileCache mPicCache;
	JandanParser mParser;
	int picPage = 0;
	boolean isParsing = false;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		final ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.picfragment, container, false);
		mListView = (ListView)  rootView.findViewById(R.id.pic_list);
		mAdapter = new SimpleAdapter(getActivity(), items, R.layout.pic_item,
				new String[]{"updater", "time", "text", "image", "xx", "oo"},
				new int[]{R.id.updater, R.id.time, R.id.text, R.id.image, R.id.xx, R.id.oo}){
			@Override
			public View getView(int position, View convertView,ViewGroup parent) {
				final View view=super.getView(position, convertView, parent);
				LinearLayout abtnXX = (LinearLayout) view.findViewById(R.id.btn_XX);
				LinearLayout abtnOO = (LinearLayout) view.findViewById(R.id.btn_OO);
				abtnXX.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Toast.makeText(getActivity(), "XX", Toast.LENGTH_SHORT).show();
					}
				});
				abtnOO.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Toast.makeText(getActivity(),"OO",Toast.LENGTH_SHORT).show();
					}
				});
				return view;
			}
		};
		mAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
			@Override
			public boolean setViewValue(
					View view,
					Object data,
					String textRepresentation) {
				if ((view instanceof ImageView) && (data instanceof Bitmap)) {
					ImageView imageView = (ImageView) view;
					Bitmap bmp = (Bitmap) data;
					imageView.setImageBitmap(bmp);
					return true;
				}
				return false;
			}
		});
		mListView.setAdapter(mAdapter);
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
								new PicLoader().execute(++picPage);
							}
						}
					}
					vPosition = mListView.getFirstVisiblePosition();
				}
			}
		});


		return rootView;
	}
	private class notifyDataSetChanged extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... voids) {
			return null;
		}
		protected void onPostExecute(Void voids){
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPicCache = new FileCache(getActivity());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mParser = new JandanParser(getActivity().getApplicationContext());
		mPicLoader = new PicLoader();
		mPicLoader.execute(picPage);
		mParser.setOnImageChangedlistener(new JandanParser.OnImageChangedlistener() {
			@Override
			public void OnImageChanged() {
				new notifyDataSetChanged().execute();
			}
		});
	}

	private class PicLoader extends AsyncTask<Integer, Void, List<Map<String, Object>>> {
		@Override
		protected List<Map<String, Object>> doInBackground(Integer... page) {
			isParsing = true;
			List<Map<String, Object>> list = mParser.JandanPicPage(page[0]);
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
			if (mAdapter.getCount() < 10) {
				new PicLoader().execute(picPage);
				picPage ++;
			}
		}
	}

}
