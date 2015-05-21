package com.nut.Jandan.Fragment;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import com.nut.Jandan.Activity.JandanActivity;
import com.nut.Jandan.Adapter.PicAdapter;
import com.nut.Jandan.R;
import com.nut.cache.Pic;
import com.nut.cache.PicFile;
import com.nut.http.PicParser;

import java.util.ArrayList;

public class PicsFragment extends Fragment {
	private static final String TAG = "PicFragment";

	private RecyclerView mRecyclerView;
	private SwipeRefreshLayout swipeLayout;
	private PicAdapter picAdapter;
	private Toolbar mToolbar;

	private PicLoader mPicLoader;

	private int picPage = 0;
	private boolean isParsing = false;
	public PicFile mPicFile = new PicFile();
	private PicParser mPicParser;
	private Handler mHandler;
	private boolean mNeedReload = true;
	private StaggeredGridLayoutManager mLayoutManager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		final ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.picfragment, container, false);

		mToolbar = ((JandanActivity) getActivity()).getToolbar();
		mToolbar.bringToFront();
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

		mRecyclerView = (RecyclerView) rootView.findViewById(R.id.pic_list);
		// mRecyclerView.addHeaderView();

		mRecyclerView.setOnScrollListener(new NewsFragment.HidingScrollListener() {
			private int previousTotal = 0; // The total number of items in the dataset after the last load
			private boolean loading = true; // True if we are still waiting for the last set of data to load.
			private int visibleThreshold = 5;
			int firstVisibleItem, visibleItemCount, totalItemCount;
			int[] into = new int[2];

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
				totalItemCount = mLayoutManager.getItemCount();
				mLayoutManager.findFirstVisibleItemPositions(into);
				firstVisibleItem = into[0];
				if (loading) {
					if (totalItemCount > previousTotal) {
						loading = false;
						previousTotal = totalItemCount;
					}
				}
				if (!loading && (totalItemCount - visibleItemCount)
						<= (firstVisibleItem + visibleThreshold)) {

					new PicLoader(null).execute(++picPage);
					loading = true;
				}
			}

		});


		mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

		mRecyclerView.setLayoutManager(mLayoutManager);
		mRecyclerView.setHasFixedSize(false);
		picAdapter = new PicAdapter(getActivity(), mPicFile, mHandler);
		mRecyclerView.setAdapter(picAdapter);
		return rootView;
	}

	private void hideViews() {
		mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
		mToolbar.animate().alpha(0).setInterpolator(new AccelerateInterpolator(2));
//		FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFam.getLayoutParams();
//		int fabBottomMargin = lp.bottomMargin;
//		mFam.animate().translationY(mFam.getHeight() + fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
	}

	private void showViews() {
		mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
		mToolbar.animate().alpha(1).setInterpolator(new DecelerateInterpolator(2));
//		mFam.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHandler = new Handler();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

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
			if (result.isEmpty()) {
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
					}
				});
				return;
			}

			mPicFile.addAll(result);
			picAdapter.notifyDataSetChanged();
			swipeLayout.setRefreshing(false);
			isParsing = false;
			if (picAdapter.getCount() < 10) {
				new PicLoader(null).execute(picPage);
				picPage++;
			}
		}
	}
}
