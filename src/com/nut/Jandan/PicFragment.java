package com.nut.Jandan;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.nut.cache.Pic;
import com.nut.cache.PicFile;
import com.nut.dao.ImageLoader;
import com.nut.gif.GifDrawable;
import com.nut.gif.GifImageView;
import com.nut.http.JandanParser;
import com.nut.http.PicParser;
import com.nut.ui.ExpandableTextView;

import java.util.ArrayList;

public class PicFragment extends Fragment {
	private static final String TAG = "PicFragment";

	private ListView mListView;
	private SwipeRefreshLayout swipeLayout;
	private PicAdapter picAdapter;

	private PicLoader mPicLoader;

	private JandanParser mParser;
	private int picPage = 0;
	private boolean isParsing = false;
	private PicFile mPicFile = new PicFile();
	private LruCache<String, Drawable> mLruCache;
	private ImageLoader mImageLoader;
	private PicParser mPicParser;
	private Handler mHandler;
	private boolean mNeedReload = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		final ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.picfragment, container, false);

		swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.pic_swipe);
		swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				picPage = 0;
				Log.d(TAG, "swipe refresh layout.");
				new PicLoader(null).execute(++picPage);
			}
		});
		swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);

		mListView = (ListView)  rootView.findViewById(R.id.pic_list);
		// mListView.addHeaderView();
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				Pic item = (Pic) picAdapter.getItem(position);
				Intent intent = new Intent(null, Uri.parse(item.mUrls.get(0)), view.getContext(), PicActivity.class);
				intent.putExtra(PicActivity.EXTRA_PIC, item);

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
						if (picAdapter.getCount() - 8 <= mListView.getFirstVisiblePosition()) {
							if (!isParsing) {
								//mNewsLoader.execute(++page);
								new PicLoader(null).execute(++picPage);
							}
						}
					}
					vPosition = mListView.getFirstVisiblePosition();
				}
			}
		});

		picAdapter = new PicAdapter();
		mListView.setAdapter(picAdapter);
		return rootView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

		// Use 1/8th of the available memory for this memory cache.
		final int cacheSize = maxMemory / 4;
		mLruCache = new LruCache<String, Drawable>(cacheSize) {
			@Override
			protected int sizeOf(String key, Drawable drawable) {
				// The cache size will be measured in kilobytes rather than
				// number of items.
				if (drawable instanceof BitmapDrawable && ((BitmapDrawable) drawable).getBitmap() != null) {
					return ((BitmapDrawable) drawable).getBitmap().getByteCount() / 1024;
				} else if (drawable instanceof GifDrawable) {
					return (int) ((GifDrawable) drawable).mInputSourceLength / 1024;
				}
				return 0;
			}
		};

		mImageLoader = new ImageLoader(getActivity());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mHandler = new Handler();
		mPicParser = new PicParser();

		if (mPicFile.load(getActivity(), PicFile.PIC_FILE_NAME)) {
			mNeedReload = false;
			picPage = mPicFile.size() / 24;
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					picAdapter.notifyDataSetChanged();
				}
			});
			ArrayList<Pic> pics = new ArrayList<>(0);
			new PicLoader(pics).execute(0);
		}

		if (mNeedReload) {
			mPicLoader = new PicLoader(null);
			mPicLoader.execute(picPage);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		mPicFile.save();
	}

	private class PicLoader extends AsyncTask<Integer, Void, ArrayList<Pic>> {
		ArrayList<Pic> mPicCol;

		PicLoader(ArrayList<Pic> pics) {
			mPicCol = pics;
		}
		@Override
		protected ArrayList<Pic> doInBackground(Integer... page) {
			isParsing = true;
			// ArrayList<Pic> pics = mParser.JandanPicPage(page[0]);
			if (mPicCol != null) {
				mPicCol = mPicParser.parse(page[0]);
				return mPicCol;
			} else {
				ArrayList<Pic> pics = mPicParser.parse(page[0]);
				if (page[0] == 1) {
					mPicFile.clear();
				}
				return pics;
			}
		}

		protected void onPostExecute(ArrayList<Pic> result) {
			if(result.isEmpty()){
				//Toast.makeText(, "载入出错了！请稍后再试。", Toast.LENGTH_SHORT).show();
			}
			if (mPicCol != null) {
				Pic pic = mPicCol.get(0);
				long seconds = pic.mTime - mPicFile.get(0).mTime;
				int delta = pic.mId - mPicFile.get(0).mId;
				if (delta <= 0)
					return;

				TextView tv = new TextView(getActivity());
				tv.setText("Here's " + delta + " unread messages");
				tv.setTextSize(20);
				tv.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						picPage = 0;
						new PicLoader(null).execute(++picPage);
						mListView.removeHeaderView(v);
					}
				});
				mListView.addHeaderView(tv);
				return;
			}

			mPicFile.addAll(result);
			picAdapter.notifyDataSetChanged();
			swipeLayout.setRefreshing(false);
			isParsing = false;
			if (picAdapter.getCount() < 10) {
				new PicLoader(null).execute(picPage);
				picPage ++;
			}
		}
	}

	private final class PicAdapter extends BaseAdapter {
		class ViewHolder {
			public TextView updater;
			public ImageView image;
			public ExpandableTextView text;
			public TextView time;
			public TextView xx;
			public TextView oo;
			public TextView cont;
		}

		//new String[]{"updater", "time", "text", "image", "isgif", "xx", "oo"},
		@Override
		public int getCount() {
			return mPicFile.size();
		}

		@Override
		public Object getItem(int position) {
			return mPicFile.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			Pic pic = mPicFile.get(position);
			if (convertView == null) {
				LayoutInflater inflater = getActivity().getLayoutInflater();
				convertView = inflater.inflate(R.layout.pic_item, null);
				viewHolder = new ViewHolder();
				viewHolder.updater = (TextView) convertView.findViewById(R.id.updater);
				viewHolder.text = (ExpandableTextView) convertView.findViewById(R.id.text);
				viewHolder.oo = (TextView) convertView.findViewById(R.id.oo);
				viewHolder.xx = (TextView) convertView.findViewById(R.id.xx);
				viewHolder.time = (TextView) convertView.findViewById(R.id.time);
				viewHolder.image = (GifImageView) convertView.findViewById(R.id.gif_image);

				convertView.setTag(viewHolder);
			} else
				viewHolder = (ViewHolder) convertView.getTag();

			viewHolder.updater.setText(pic.mAuthor);
			viewHolder.text.setText(pic.mDesc);
			viewHolder.oo.setText(String.valueOf(pic.mOO));
			viewHolder.xx.setText(String.valueOf(pic.mXX));
			viewHolder.time.setText(Utilities.convertTime(pic.mTime));


			loadPic(viewHolder.image, pic.mUrls.get(0));

			return convertView;
		}

		private void loadPic(final ImageView imageView, String thumbUrl) {
			imageView.setTag(thumbUrl);
			if (imageView instanceof GifImageView)
				Log.d(TAG, "load gifImageView: " + thumbUrl);
			final Drawable drawable = mLruCache.get(thumbUrl);
			if (drawable != null && !thumbUrl.endsWith("gif")) {
				imageView.setImageDrawable(drawable);
			} else {
				imageView.setImageResource(R.drawable.loading);
				imageView.setTag(thumbUrl);
				mImageLoader.request(thumbUrl, imageView, new ImageLoader.Callback() {
					@Override
					public void onLoaded(final String thumbUrl, final Drawable drawable) {
						mLruCache.put(thumbUrl, drawable);

						Log.d(TAG, "setImageUri: " + drawable.toString());
						imageView.post(new Runnable() {
							@Override
							public void run() {
								if (imageView.getTag() == thumbUrl)
									imageView.setImageDrawable(drawable);
							}
						});

					}
				});
			}
		}
	}


	private final ImageLoader.Callback mImageLoaderCallback = new ImageLoader.Callback() {
		@Override
		public void onLoaded(String thumbUrl, Drawable drawable) {
			mLruCache.put(thumbUrl, drawable);
		}
	};
}