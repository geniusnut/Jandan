package com.nut.Jandan.Fragment;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nut.Jandan.R;
import com.nut.dao.DuoshuoComment;
import com.nut.http.PostParser;
import com.nut.ui.CircleImageView;

/**
 * Created by yw07 on 15-7-31.
 */
public class CommentFragment extends Fragment {
	private DuoshuoComment mComment;
	private ImageLoader mImageLoader;
	private String mPostId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		mPostId = args.getString("postId");
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		RecyclerView recyclerView = new RecyclerView(container.getContext());
		final LinearLayoutManager llm = new LinearLayoutManager(getActivity());
		llm.setOrientation(LinearLayoutManager.VERTICAL);
		recyclerView.setLayoutManager(llm);
		return recyclerView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		RecyclerView recyclerView = (RecyclerView) view;
		recyclerView.setAdapter(mAdapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

		mImageLoader = ImageLoader.getInstance();
		new CommentsTask().execute(mPostId);
	}

	public class CommentsTask extends AsyncTask<String, Void, DuoshuoComment> {
		@Override
		protected DuoshuoComment doInBackground(String... params) {
			return PostParser.getDuoshuoComments(params[0]);
		}

		@Override
		protected void onPostExecute(DuoshuoComment duoshuoComment) {
			mComment = duoshuoComment;
			mAdapter.notifyDataSetChanged();
		}
	}

	private static class CommentViewHolder extends RecyclerView.ViewHolder {
		public CircleImageView mAvatar;
		public TextView mAuthor;
		public TextView mDate;
		public TextView mContent;

		public CommentViewHolder(View itemView) {
			super(itemView);
			mAvatar = (CircleImageView) itemView.findViewById(R.id.avatar);
			mAuthor = (TextView) itemView.findViewById(R.id.author);
			mDate = (TextView) itemView.findViewById(R.id.time);
			mContent = (TextView) itemView.findViewById(R.id.comment_content);
		}
	}

	private RecyclerView.Adapter mAdapter = new RecyclerView.Adapter() {
		private static final int TYPE_HEADER = 0;
		private static final int TYPE_ITEM = 1;

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
			if (i == TYPE_HEADER) {
				View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_duoshuo_comment, viewGroup, false);
				return new CommentViewHolder(view);
			} else if (i == TYPE_ITEM) {
				View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_duoshuo_comment, viewGroup, false);
				return new CommentViewHolder(view);
			}
			return null;
		}

		@Override
		public int getItemViewType(int position) {
			if (position == 0)
				return TYPE_HEADER;
			else if (position == mComment.getHotComments().size()) {
				return TYPE_HEADER;
			} else {
				return TYPE_ITEM;
			}
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
			if (viewHolder instanceof CommentViewHolder) {
				DuoshuoComment.Comment comment = new DuoshuoComment.Comment();
				if (i < mComment.getHotComments().size())
					comment = mComment.mHotComments.get(i);
				CommentViewHolder commentVH = (CommentViewHolder) viewHolder;
				commentVH.mAuthor.setText(comment.mAuthor);
				mImageLoader.displayImage(comment.mAvatar, commentVH.mAvatar);
				commentVH.mDate.setText(comment.mDate);
				commentVH.mContent.setText(comment.mContent);
			}
		}

		@Override
		public int getItemCount() {
			return mComment.getSize();
		}
	};
}
