package com.nut.Jandan.Fragment;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nut.Jandan.Activity.BaseFragmentActivity;
import com.nut.Jandan.Activity.JandanActivity;
import com.nut.Jandan.Activity.PostActivity;
import com.nut.Jandan.R;
import com.nut.cache.NewsFile;
import com.nut.cache.Post;
import com.nut.http.PostParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NewsFragment extends Fragment implements BaseFragmentInterface {
	private final String TAG = "NewsFragment";

	private Toolbar mToolbar;
	protected ListView mListView;
	private RecyclerView mRecList;
	protected SwipeRefreshLayout swipeLayout;
	RecyclerView.ItemAnimator mCachedAnimator = null;

	protected Handler mHandler;
	protected boolean isParsing = false;
	protected boolean mNeedReload = true;
	protected ConcurrentHashMap<String, Drawable> mCovers;
	protected List<Map<String, Object>> items = new ArrayList<>();

	private int mPage = 0;
	private PostParser mPostParser;
	private NewsAdapter newsAdapter;
	private ImageLoader mImageLoader;
	private NewsFile mNewsFile = new NewsFile();
	private HashMap<String, String> coverMaps = new HashMap(64);

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.swipe_news_frag, container, false);

		mToolbar = ((JandanActivity) getActivity()).getToolbar();
		if (mToolbar.getTranslationY() < 0) {
			mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
			mToolbar.animate().alpha(1).setInterpolator(new DecelerateInterpolator(2));
		}
		mToolbar.bringToFront();

		swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
		swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				mPage = 0;
				Log.d(TAG, "swipe refresh layout.");
				new NewsLoader().execute(++mPage);
			}
		});
		int color = getResources().getColor(R.color.teal500);
		swipeLayout.setColorSchemeColors(color);
		int toolbarSize = JandanActivity.getActionBarSize(getActivity());
		swipeLayout.setProgressViewOffset(false, toolbarSize, toolbarSize + 128);

		mRecList = (RecyclerView) rootView.findViewById(R.id.cardList);
		mRecList.setHasFixedSize(true);
		LinearLayoutManager llm = new LinearLayoutManager(getActivity());
		llm.setOrientation(LinearLayoutManager.VERTICAL);
		mRecList.setLayoutManager(llm);
		newsAdapter = new NewsAdapter();
		mRecList.setAdapter(newsAdapter);
		mRecList.addOnScrollListener(new RecyclerView.OnScrollListener() {
			private int previousTotal = 0; // The total number of items in the dataset after the last load
			private boolean loading = true; // True if we are still waiting for the last set of data to load.
			private int visibleThreshold = 5;
			int firstVisibleItem, visibleItemCount, totalItemCount;

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

					new NewsLoader().execute(++mPage);
					loading = true;
				}
			}
		});
		mCachedAnimator = mRecList.getItemAnimator();
		mCachedAnimator.setSupportsChangeAnimations(true);
		mRecList.setItemAnimator(mCachedAnimator);

		return rootView;
	}

	public boolean backPressed() {
		return false;
	}

	@Override
	public void show(BaseFragmentActivity activity) {
		if (activity == null) {
			return;
		}
		activity.showFragment(this);
	}

	@Override
	public boolean onBackPressed() {
		return false;
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

//	private void hideViews() {
//		mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
//		mToolbar.animate().alpha(0).setInterpolator(new AccelerateInterpolator(2));
//		FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFam.getLayoutParams();
//		int fabBottomMargin = lp.bottomMargin;
//		mFam.animate().translationY(mFam.getHeight() + fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
//	}
//
//	private void showViews() {
//		mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
//		mToolbar.animate().alpha(1).setInterpolator(new DecelerateInterpolator(2));
//		mFam.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
//	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mHandler = new Handler();
		mPostParser = new PostParser();
		mCovers = new ConcurrentHashMap<>(64);
		mImageLoader = ImageLoader.getInstance();

		if (mNewsFile.load(getActivity(), NewsFile.NEWS_FILE_NAME)) {
			final ArrayList<Post> news = new ArrayList<>(mNewsFile.size());
			mNeedReload = false;
			mPage = mNewsFile.size() / 24;
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					newsAdapter.notifyDataSetChanged();
				}
			});
			requestLoad(); // Try to load all covers or just only try to load the cover visible in the listview.
		}
		if (mNeedReload) {
			new NewsLoader().execute(++mPage);
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
				Toast.makeText(getActivity(), "载入出错了！请稍后再试。", Toast.LENGTH_SHORT).show();
			}
			if (mPage == 1)
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
				intent.putExtra("cover", post.mCover.replace("custom", "medium"));
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



	private void setImageToView(final ImageView imageView, final String thumbUrl) {
		imageView.setTag(thumbUrl);
		Drawable cover = mCovers.get(thumbUrl);
		if (cover != null) {
			imageView.setImageDrawable(cover);
		} else {
			imageView.setImageBitmap(null);
			mImageLoader.displayImage(thumbUrl, imageView, options);
//			mImageLoader.loadImage(thumbUrl, options, new SimpleImageLoadingListener() {
//				@Override
//				public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
//					// Do whatever you want with Bitmap
//					mHandler.post(new Runnable() {
//						@Override
//						public void run() {
//							if (imageView.getTag() == thumbUrl) {
//								imageView.setImageBitmap(loadedImage);
//							}
//						}
//					});
//				}
//			});
		}
	}
}
