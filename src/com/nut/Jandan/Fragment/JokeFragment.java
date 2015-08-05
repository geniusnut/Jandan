package com.nut.Jandan.Fragment;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.nut.Jandan.Activity.BaseFragmentActivity;
import com.nut.Jandan.Activity.JandanActivity;
import com.nut.Jandan.R;
import com.nut.dao.JokeModel;
import com.nut.http.PostParser;

import java.util.ArrayList;

/**
 * Created by yw07 on 15-7-28.
 */
public class JokeFragment extends Fragment {
	private int mPage;
	private ArrayList<JokeModel> mJokes = new ArrayList<>(0);
	private SwipeRefreshLayout swipeLayout;
	private RecyclerView mRecList;

	private int mLastTotal = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.swipe_news_frag, container, false);
		swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
		swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				mPage = 1;
				new JokesTask().execute(mPage);
			}
		});

		int color = getResources().getColor(R.color.teal500);
		int toolbarSize = JandanActivity.getActionBarSize(getActivity());
		swipeLayout.setColorSchemeColors(color);
		swipeLayout.setProgressViewOffset(false, toolbarSize, toolbarSize + 128);

		mRecList = (RecyclerView) rootView.findViewById(R.id.cardList);
		mRecList.setHasFixedSize(true);
		final LinearLayoutManager llm = new LinearLayoutManager(getActivity());
		llm.setOrientation(LinearLayoutManager.VERTICAL);
		mRecList.setLayoutManager(llm);
		mRecList.setAdapter(mAdapter);
		mRecList.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				int visibleItemCount = recyclerView.getChildCount();
				int totalItemCount = llm.getItemCount();
				int firstVisibleItem = llm.findFirstVisibleItemPosition();

				if (firstVisibleItem + visibleItemCount >= totalItemCount
						&& mLastTotal != totalItemCount) {
					mLastTotal = totalItemCount;
					new JokesTask().execute(mPage++);
				}
			}
		});
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		swipeLayout.setRefreshing(true);
		new JokesTask().execute(mPage++);
	}

	private RecyclerView.Adapter mAdapter = new RecyclerView.Adapter() {
		private static final int ITEM_TYPE = 0;

		@Override
		public int getItemViewType(int position) {
			return ITEM_TYPE;
		}

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
			if (viewType == ITEM_TYPE) {
				View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_jokes_item, viewGroup, false);
				final JokeViewHolder jokeVH = new JokeViewHolder(view);
				final Resources res = viewGroup.getContext().getResources();

				jokeVH.mOO.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						jokeVH.mOO.setTextColor(res.getColor(R.color.redA200));
						jokeVH.mOO.setEnabled(false);
						jokeVH.mXX.setEnabled(false);
					}
				});
				jokeVH.mXX.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						jokeVH.mXX.setTextColor(res.getColor(R.color.cyanA200));
						jokeVH.mOO.setEnabled(false);
						jokeVH.mXX.setEnabled(false);
					}
				});
				jokeVH.mComments.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						final JokeModel joke = mJokes.get(jokeVH.getAdapterPosition());
						Bundle args = new Bundle();
						args.putLong("commentId", joke.mCommentId);
						CommentFragment fragment = new CommentFragment();
						fragment.setArguments(args);
						fragment.show((BaseFragmentActivity) getActivity());
					}
				});
				return jokeVH;
			} else {
				return null;
			}
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
			JokeModel jokeModel = mJokes.get(i);
			if (viewHolder instanceof JokeViewHolder) {
				JokeViewHolder vh = (JokeViewHolder) viewHolder;
				vh.mAuthor.setText(jokeModel.mAuthor);
				vh.mContent.setText(jokeModel.mContent);
				vh.mDate.setText(jokeModel.mDate);
				vh.mOO.setText(String.format(getString(R.string.joke_oo), jokeModel.mPositive));
				vh.mXX.setText(String.format(getString(R.string.joke_xx), jokeModel.mNegative));
				vh.mComments.setText(String.format(getString(R.string.joke_comments), jokeModel.mComments));
			}
		}

		@Override
		public int getItemCount() {
			return mJokes.size();
		}
	};

	public static class JokeViewHolder extends RecyclerView.ViewHolder {
		public TextView mAuthor;
		public TextView mDate;
		public TextView mContent;
		public TextView mOO;
		public TextView mXX;
		public TextView mComments;
		public JokeViewHolder(View itemView) {
			super(itemView);
			mAuthor = (TextView) itemView.findViewById(R.id.joke_author);
			mDate = (TextView) itemView.findViewById(R.id.joke_date);
			mContent = (TextView) itemView.findViewById(R.id.joke_content);
			mOO = (TextView) itemView.findViewById(R.id.joke_oo);
			mXX = (TextView) itemView.findViewById(R.id.joke_xx);
			mComments = (TextView) itemView.findViewById(R.id.joke_comments);
		}
	}

	public class JokesTask extends AsyncTask<Integer, Void, ArrayList<JokeModel>> {
		@Override
		protected ArrayList<JokeModel> doInBackground(Integer... params) {
			return PostParser.parseJokes(params[0]);
		}

		@Override
		protected void onPostExecute(ArrayList<JokeModel> jokes) {
			swipeLayout.setRefreshing(false);
			if (mPage == 1) {
				if (mJokes.size() > 0)
					mJokes.clear();
				mLastTotal = 0;
				mJokes.addAll(jokes);
				mAdapter.notifyDataSetChanged();
			} else {
				int size = mJokes.size();
				if (jokes.size() <= 0 )
					return;
				long lastId = mJokes.get(mJokes.size() - 1).mId;
				while (jokes.get(0) != null && jokes.get(0).mId >= lastId)
					jokes.remove(0);
				mJokes.addAll(jokes);
				mAdapter.notifyItemRangeInserted(size, jokes.size());
			}
		}
	}
}
