package com.nut.Jandan.Fragment;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nut.Jandan.Activity.JandanActivity;
import com.nut.Jandan.Activity.PostActivity;
import com.nut.Jandan.R;
import com.nut.cache.NewsFile;
import com.nut.cache.Post;
import com.nut.http.JandanParser;
import com.nut.http.PostParser;
import com.nut.ui.FloatingActionButton;
import com.nut.ui.FloatingActionsMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NewsFragment extends Fragment {
	private final String TAG = "NewsFragment";
	protected ListView mListView;
	protected SwipeRefreshLayout swipeLayout;
	protected JandanParser mParser;
	private Toolbar mToolbar;
	private RecyclerView mRecList;
	RecyclerView.ItemAnimator mCachedAnimator = null;


	public static NewsLoader mNewsLoader;
	protected boolean isParsing = false;
	protected boolean mNeedReload = true;
	protected Handler mHandler;
	int page = 0;
	protected List<Map<String, Object>> items = new ArrayList<>();
	protected ConcurrentHashMap<String, Drawable> mCovers;

	private HashMap<String, String> coverMaps = new HashMap(64);
	private PostParser mPostParser;
	private NewsFile mNewsFile = new NewsFile();
	private ImageLoader mImageLoader;
	private FloatingActionsMenu mFam;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.swipe_news_frag, container, false);

		mToolbar = ((JandanActivity) getActivity()).getToolbar();
		mToolbar.bringToFront();
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

		mRecList = (RecyclerView) rootView.findViewById(R.id.cardList);
		mRecList.setHasFixedSize(true);
		LinearLayoutManager llm = new LinearLayoutManager(getActivity());
		llm.setOrientation(LinearLayoutManager.VERTICAL);
		mRecList.setLayoutManager(llm);
		NewsAdapter na = new NewsAdapter();
		mRecList.setAdapter(na);

		mRecList.setOnScrollListener(new HidingScrollListener() {
			private int previousTotal = 0; // The total number of items in the dataset after the last load
			private boolean loading = true; // True if we are still waiting for the last set of data to load.
			private int visibleThreshold = 5;
			int firstVisibleItem, visibleItemCount, totalItemCount;
			@Override
			public void onHide() {
				hideViews();
			}
			@Override
			public void onShow() {
				showViews();
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				visibleItemCount = recyclerView.getChildCount();
				totalItemCount = ((LinearLayoutManager) recyclerView.getLayoutManager()).getItemCount();

				if (loading) {
					if (totalItemCount > previousTotal) {
						loading = false;
						previousTotal = totalItemCount;
					}
				}
				if (!loading && (totalItemCount - visibleItemCount)
						<= (firstVisibleItem + visibleThreshold)) {

					new NewsLoader().execute(++page);
					loading = true;
				}
			}
		});

		mCachedAnimator = mRecList.getItemAnimator();
		mCachedAnimator.setSupportsChangeAnimations(true);
		mRecList.setItemAnimator(mCachedAnimator);

		mFam = (FloatingActionsMenu) rootView.findViewById(R.id.fam);

		for (int i = 0; i < 9; i++) {
			FloatingActionButton fab = new FloatingActionButton.Builder(getActivity())
					.withDrawable(FloatingActionsMenu.createLikeDrawable(getActivity(), false))
					.withSize(64) // dp
					.withMargins(16, 0, 0, 16)// dp
					.create();
			fab.setTitle("This is the " + Integer.toString(i) + " fab.");
			mFam.addButton(fab);
		}
		return rootView;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
		swipeLayout.setProgressViewOffset(false, mToolbar.getHeight(), mToolbar.getHeight() + 128);
	}

	//borrow code from https://mzgreen.github.io/2015/02/15/How-to-hideshow-Toolbar-when-list-is-scroling(part1)/
	public static abstract class HidingScrollListener extends RecyclerView.OnScrollListener {
		private static final int HIDE_THRESHOLD = 20;
		private int scrolledDistance = 0;
		private boolean controlsVisible = true;


		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			super.onScrolled(recyclerView, dx, dy);
			int firstVisibleItem = 0;
			int[] into = new int[2];
			RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
			if (lm instanceof StaggeredGridLayoutManager) {
				((StaggeredGridLayoutManager) lm).findFirstVisibleItemPositions(into);
				firstVisibleItem = into[0];
			} else if (lm instanceof LinearLayoutManager) {
				firstVisibleItem = ((LinearLayoutManager) lm).findFirstVisibleItemPosition();
			}

			if (firstVisibleItem == 0) {
				if (!controlsVisible) {
					onShow();
					controlsVisible = true;
				}
			} else {
				if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
					onHide();
					controlsVisible = false;
					scrolledDistance = 0;
				} else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
					onShow();
					controlsVisible = true;
					scrolledDistance = 0;
				}
			}
			if ((controlsVisible && dy > 0) || (!controlsVisible && dy < 0)) {
				scrolledDistance += dy;
			}
		}

		public abstract void onHide();

		public abstract void onShow();

	}

	private void hideViews() {
		mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
		mToolbar.animate().alpha(0).setInterpolator(new AccelerateInterpolator(2));
		FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFam.getLayoutParams();
		int fabBottomMargin = lp.bottomMargin;
		mFam.animate().translationY(mFam.getHeight() + fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
	}

	private void showViews() {
		mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
		mToolbar.animate().alpha(1).setInterpolator(new DecelerateInterpolator(2));
		mFam.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mHandler = new Handler();
		mPostParser = new PostParser();

		mCovers = new ConcurrentHashMap<>(64);

		ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(getActivity());
		config.threadPriority(Thread.NORM_PRIORITY - 2);
		config.denyCacheImageMultipleSizesInMemory();
		config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
		config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
		config.tasksProcessingOrder(QueueProcessingType.LIFO);
		config.writeDebugLogs(); // Remove for release app

		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config.build());
		mImageLoader = ImageLoader.getInstance();

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
			mNewsLoader.execute(++page);
		}
	}

	private void requestLoad() {

	}

	@Override
	public void onPause() {
		super.onPause();
		mNewsFile.save();
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
			return mPostParser.parsePosts(page[0], mCovers);
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


	public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

		@Override
		public NewsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
			View itemView = LayoutInflater.from(viewGroup.getContext())
					.inflate(R.layout.card_news_item, viewGroup, false);
			return new NewsViewHolder(itemView, new IVHClickListener() {
				@Override
				public void onClickCover(View v) {
					Log.d("newsfragment", "click cover");
				}

				@Override
				public void onClickTitle(View v) {
					Log.d("newsfragment", "click title");
				}
			});
		}

		@Override
		public void onBindViewHolder(NewsViewHolder newsViewHolder, int i) {
			final Post item = mNewsFile.get(i);
			coverMaps.put(item.mCover, item.mTitle);
			setImageToView(newsViewHolder.image, item.mCover);
			newsViewHolder.link.setText(item.mLink);
			newsViewHolder.title.setText(item.mTitle);
			newsViewHolder.by.setText(item.mAuthor);
			newsViewHolder.tag.setText(item.mTag);
			newsViewHolder.cont.setText(String.valueOf(item.mCont));
		}

		@Override
		public int getItemCount() {
			return mNewsFile.size();
		}

		public class NewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
			public TextView link;
			public ImageView image;
			public TextView title;
			public TextView by;
			public TextView tag;
			public TextView cont;
			public IVHClickListener mListener;

			public NewsViewHolder(View itemView, IVHClickListener listener) {
				super(itemView);
				link = (TextView) itemView.findViewById(R.id.link);
				image = (ImageView) itemView.findViewById(R.id.scale_image);
				title = (TextView) itemView.findViewById(R.id.title);
				by = (TextView) itemView.findViewById(R.id.by);
				tag = (TextView) itemView.findViewById(R.id.tag);
				cont = (TextView) itemView.findViewById(R.id.cont);
				mListener = listener;
				image.setOnClickListener(this);
				title.setOnClickListener(this);
				itemView.setOnClickListener(this);
			}

			@Override
			public void onClick(View v) {
				Log.d("newsfragment", "click on: " + getPosition());
				Post post = mNewsFile.get(getPosition());
				Intent intent = new Intent(v.getContext(), PostActivity.class);
				intent.putExtra("link", post.mLink);
				intent.putExtra("comm", post.mCont);
				intent.putExtra(Intent.EXTRA_TITLE, post.mTitle);
				intent.putExtra("index", getPosition());
				startActivity(intent);
				if (v instanceof ImageView) {
					mListener.onClickCover(v);
				}
			}
		}
	}

	public static interface IVHClickListener {
		public void onClickCover(View v);

		public void onClickTitle(View v);
	}

	public static DisplayImageOptions options = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.loading)
			.showImageForEmptyUri(R.drawable.loading)
			.showImageOnFail(R.drawable.loading)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.considerExifParams(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();

	protected final BaseAdapter newsAdapter = new BaseAdapter() {
		private final int droidGreen = Color.parseColor("#A4C639");

		class ViewHolder {
			public TextView link;
			public ImageView image;
			public TextView title;
			public TextView by;
			public TextView tag;
			public TextView cont;
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
				convertView = inflater.inflate(R.layout.card_news_item, null);
				viewHolder = new ViewHolder();
				viewHolder.link = (TextView) convertView.findViewById(R.id.link);
				viewHolder.image = (ImageView) convertView.findViewById(R.id.scale_image);
				viewHolder.title = (TextView) convertView.findViewById(R.id.title);
				viewHolder.by = (TextView) convertView.findViewById(R.id.by);
				viewHolder.tag = (TextView) convertView.findViewById(R.id.tag);
				viewHolder.cont = (TextView) convertView.findViewById(R.id.cont);

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
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

	private void setImageToView(final ImageView imageView, final String thumbUrl) {
		imageView.setTag(thumbUrl);
		Drawable cover = mCovers.get(thumbUrl);
		if (cover != null) {
			imageView.setImageDrawable(cover);
		} else {
			imageView.setImageBitmap(null);
			mImageLoader.loadImage(thumbUrl, options, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
					// Do whatever you want with Bitmap
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							if (imageView.getTag() == thumbUrl) {
								imageView.setImageBitmap(loadedImage);
							}
						}
					});
				}
			});
		}
	}
}
