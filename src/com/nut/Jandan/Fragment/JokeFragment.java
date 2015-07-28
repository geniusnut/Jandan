package com.nut.Jandan.Fragment;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.nut.Jandan.R;
import com.nut.dao.JokeModel;
import com.nut.http.PostParser;
import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by yw07 on 15-7-28.
 */
public class JokeFragment extends Fragment {
	private RecyclerView mRecyclerView;
	private ArrayList<JokeModel> mJokes;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return new RecyclerView(container.getContext());
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {

		// final RecyclerListAdapter adapter = new RecyclerListAdapter(getActivity(), this);

		RecyclerView recyclerView = (RecyclerView) view;
		recyclerView.setHasFixedSize(true);
		recyclerView.setAdapter(mAdapter);

		final int spanCount = 2;
		final GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), spanCount);
		recyclerView.setLayoutManager(layoutManager);
	}

	private RecyclerView.Adapter mAdapter = new RecyclerView.Adapter() {
		private static final int ITEM_TYPE = 0;

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
			if (viewType == )
			return null;
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

		}

		@Override
		public int getItemCount() {
			return 0;
		}
	};

	public static class JokeViewHolder extends RecyclerView.ViewHolder {
		public TextView mJokeId;
		public TextView mAuthor;
		public TextView mDate;
		public TextView mContent;
		public TextView mOO;
		public JokeViewHolder(View itemView) {
			super(itemView);
			mJokeId = (TextView) itemView.findViewById(R.id.joke_id);
			mAuthor = (TextView) itemView.findViewById(R.id.joke_author);
			mDate = (TextView) itemView.findViewById(R.id.joke_date);
			mContent = (TextView) itemView.findViewById(R.id.joke_content);
		}
	}
	public class JokesTask extends AsyncTask<Integer, Void, ArrayList<JokeModel>> {

		@Override
		protected ArrayList<JokeModel> doInBackground(Integer... params) {
			return PostParser.parseJokes(params[0]);
		}

		@Override
		protected void onPostExecute(ArrayList<JokeModel> commentModels) {
			if (mJokes.size() > 0 )
				mJokes.clear();
			mJokes.addAll(commentModels);
			mAdapter.notifyItemRangeChanged(2, mJokes.size());
			//  mCommToggle.setEnabled(true);
			if (mRecyclerView != null)
				mRecyclerView.smoothScrollToPosition(2);
		}
	}
}
