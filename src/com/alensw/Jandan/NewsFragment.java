package com.alensw.Jandan;

import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.alensw.http.PostParser;
import com.alensw.ui.BadgeView;
import com.larvalabs.svgandroid.SVG;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NewsFragment extends Fragment {
	private final String TAG = "NewsFragment";
	protected ListView mListView;
	protected SwipeRefreshLayout swipeLayout;
	protected JandanParser mParser;

	public static NewsLoader mNewsLoader;
	protected boolean isParsing = false;
	protected boolean mNeedReload = true;
	protected Handler mHandler;
	int page = 0;
	protected List<Map<String, Object>> items = new ArrayList<>();
	protected ConcurrentHashMap<String, Bitmap> mCovers;

	private PostParser mPostParser;
	private NewsFile mNewsFile = new NewsFile();
	private ImageLoader mImageLoader;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.swipe_news_frag, container, false);

		swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
		swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				page = 0;
				Log.d(TAG, "swipe refresh layout.");
				new NewsLoader().execute(++page);
			}
		});
		swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);

		mListView = (ListView) rootView.findViewById(R.id.news_list);
		mListView.setAdapter(newsAdapter);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				TextView link = (TextView) view.findViewById(R.id.link);
				TextView title = (TextView) view.findViewById(R.id.title);
				TextView comm = (TextView) view.findViewById(R.id.cont);
				String acomm = comm.getText().toString();
				String atitle = title.getText().toString();
				String alink = link.getText().toString();
				Intent intent = new Intent(view.getContext(), PostActivity.class);
				intent.putExtra("link", alink);
				intent.putExtra("comm", acomm);
				intent.putExtra(Intent.EXTRA_TITLE, atitle);
				startActivity(intent);
			}
		});
		mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			int vPosition = 0;

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (mListView.getFirstVisiblePosition() > 0) {
					if (mListView.getFirstVisiblePosition() != vPosition) {
						if (newsAdapter.getCount() - 8 <= mListView.getFirstVisiblePosition()) {
							if (!isParsing) {
								//mNewsLoader.execute(++page);
								Log.d(TAG, "onScroll start refresh");
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

		mHandler = new Handler();
		mPostParser = new PostParser();
		mParser = new JandanParser(getActivity().getApplicationContext());
		mParser.setOnImageChangedlistener(new JandanParser.OnImageChangedlistener() {
			@Override
			public void OnImageChanged() {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						newsAdapter.notifyDataSetChanged();
					}
				});
			}
		});
		mCovers = new ConcurrentHashMap<>(64);
		mImageLoader = new ImageLoader(getActivity());

		if (mNewsFile.load(getActivity(), NewsFile.NEWS_FILE_NAME)) {
			final ArrayList<Post> news = new ArrayList<>(mNewsFile.size());
			mNeedReload = false;
			page = mNewsFile.size() / 24;
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					newsAdapter.notifyDataSetChanged();
				}
			});
			requestLoad(); // Try to load all covers or just only try to load the cover visible in the listview.
		}
		if (mNeedReload) {
			mNewsLoader = new NewsLoader();
			Log.d(TAG, "start loading");
			mNewsLoader.execute(++page);
		}
	}

	private void requestLoad() {

	}

	@Override
	public void onStop() {
		super.onStop();
		mNewsFile.save();
	}

	private class NewsLoader extends AsyncTask<Integer, Void, ArrayList<Post>> {
		@Override
		protected ArrayList<Post> doInBackground(Integer... page) {
			isParsing = true;
			mPostParser.parse(page[0], mCovers);
			return mParser.JandanHomePage(page[0], mCovers);
		}

		protected void onPostExecute(ArrayList<Post> result) {
			if(result.isEmpty()){
				//Toast.makeText(, "载入出错了！请稍后再试。", Toast.LENGTH_SHORT).show();
			}
			if (page == 1)
				mNewsFile.clear();
			mNewsFile.addAll(result);
			newsAdapter.notifyDataSetChanged();
			swipeLayout.setRefreshing(false);
			isParsing = false;
		}
	}

	protected final BaseAdapter newsAdapter = new BaseAdapter() {
		private final int droidGreen = Color.parseColor("#A4C639");
		class ViewHolder {
			public TextView link;
			public ImageView image;
			public TextView title;
			public TextView by;
			public TextView tag;
			public TextView cont;
			public BadgeView badge;
		}
		@Override
		public int getCount() {
			return mNewsFile.size();
		}

		@Override
		public Object getItem(int position) {
			return mNewsFile.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				LayoutInflater inflater = getActivity().getLayoutInflater();
				convertView = inflater.inflate(R.layout.news_item1, null);
				viewHolder = new ViewHolder();
				viewHolder.link = (TextView) convertView.findViewById(R.id.link);
				viewHolder.image = (ImageView) convertView.findViewById(R.id.image);
				viewHolder.title = (TextView) convertView.findViewById(R.id.title);
				viewHolder.by = (TextView) convertView.findViewById(R.id.by);
				viewHolder.tag = (TextView) convertView.findViewById(R.id.tag);
				viewHolder.cont = (TextView) convertView.findViewById(R.id.cont);
				viewHolder.badge = new BadgeView(getActivity(), viewHolder.title);


				//viewHolder.badge.setBadgeMargin(100);
				Drawable iconCircle = SVG.getDrawable(getResources(), R.raw.ic_button_radio_on,
						0xCCFF0000 | 0xc0000000);
				viewHolder.badge.setBackgroundDrawable(iconCircle);

				viewHolder.badge.setOnLongClickListener(new MyClickListener());
				viewHolder.badge.setTag("1");
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			if (position % 3 == 0) {
				viewHolder.badge.show();
			} else {
				viewHolder.badge.hide();
			}

			final Post item = mNewsFile.get(position);
			viewHolder.title.setText(item.mTitle);
			viewHolder.by.setText(item.mAuthor);
			viewHolder.link.setText(item.mLink);
			viewHolder.tag.setText(item.mTag);
			viewHolder.cont.setText(String.valueOf(item.mCont));

			setImageToView(viewHolder.image, item.mCover);
			return convertView;
		}
	};

	private void setImageToView(ImageView imageView, final String thumbUrl) {
		Bitmap cover = mCovers.get(thumbUrl);
		if (cover != null) {
			imageView.setImageBitmap(cover);
		} else {
			mImageLoader.request(thumbUrl, imageView, mImageLoaderCallback);
		}
	}

	private final ImageLoader.Callback mImageLoaderCallback = new ImageLoader.Callback() {
		@Override
		public void onLoaded(String thumbUrl, Bitmap bitmap) {
			mCovers.put(thumbUrl, bitmap);
		}
	};

	private final class MyClickListener implements View.OnLongClickListener {
		// called when the item is long-clicked
		@Override
		public boolean onLongClick(View view) {
			// TODO Auto-generated method stub

			// create it from the object's tag
			ClipData.Item item = new ClipData.Item((CharSequence) view.getTag());

			String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
			ClipData data = new ClipData(view.getTag().toString(), mimeTypes, item);
			View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);

			view.startDrag(data, //data to be dragged
					shadowBuilder, //drag shadow
					view, //local data about the drag and drop operation
					0   //no needed flags
			);
			view.setVisibility(View.INVISIBLE);
			return true;
		}
	}

}
