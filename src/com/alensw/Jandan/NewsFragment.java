package com.alensw.Jandan;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
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
public class NewsFragment extends Fragment {
	private final String TAG = "NewsFragment";
	protected ListView mListView;
	protected JandanParser mParser;
	public static NewsLoader mNewsLoader;
	protected SimpleAdapter mAdapter;
	protected boolean isParsing = false;
	int page = 0;
	protected List<Map<String, Object>> items = new ArrayList<Map<String,Object>>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.main, container, false);

		mListView = (ListView) rootView.findViewById(R.id.news_list);
		mAdapter = new SimpleAdapter(getActivity(), items, R.layout.news_item,
				new String[]{"link", "image", "title", "by", "tag", "cont"},
				new int[]{R.id.link, R.id.image, R.id.title, R.id.by, R.id.tag, R.id.cont});
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
		mListView.setAdapter(mAdapter);
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
		});
		return rootView;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mParser = new JandanParser(getActivity().getApplicationContext());
		mNewsLoader = new NewsLoader();
		mNewsLoader.execute(++page);
		mParser.setOnImageChangedlistener(new JandanParser.OnImageChangedlistener() {
			@Override
			public void OnImageChanged() {
				//mAdapter.notifyDataSetChanged();
				new notifyDataSetChanged().execute();
			}
		});
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
