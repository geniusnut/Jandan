package com.nut.Jandan.Fragment;

import android.app.Fragment;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nut.Jandan.Activity.BaseFragmentActivity;
import com.nut.Jandan.R;
import com.nut.dao.DuoshuoComment;
import com.nut.http.PostParser;
import com.nut.ui.CircleImageView;

/**
 * Created by yw07 on 15-7-31.
 */
public class CommentFragment extends Fragment implements BaseFragmentInterface {
	private DuoshuoComment mComment = new DuoshuoComment();
	private ImageLoader mImageLoader;
	private Long mCommentId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		mCommentId = args.getLong("commentId");
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
		new CommentsTask().execute(mCommentId);
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

	public class CommentsTask extends AsyncTask<Long, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Long... params) {
			PostParser.getDuoshuoComments(params[0], mComment);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean res) {
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

	private static class HeaderViewHolder extends RecyclerView.ViewHolder {
		public TextView mTitle;

		public HeaderViewHolder(View itemView, String text) {
			super(itemView);
			LinearLayout linearLayout = (LinearLayout) itemView;
			mTitle = new TextView(linearLayout.getContext());
			mTitle.setTextColor(Color.BLACK);
			mTitle.setText(text);
			linearLayout.addView(mTitle);
		}
	}

	private RecyclerView.Adapter mAdapter = new RecyclerView.Adapter() {
		private static final int TYPE_HEADER_HOT = 0;
		private static final int TYPE_HEADER = 1;
		private static final int TYPE_ITEM = 10;

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
			if (i == TYPE_HEADER_HOT) {
				LinearLayout linearLayout = new LinearLayout(viewGroup.getContext());
				return new HeaderViewHolder(linearLayout, "hot");
			} else if (i == TYPE_HEADER) {
				LinearLayout linearLayout = new LinearLayout(viewGroup.getContext());
				return new HeaderViewHolder(linearLayout, "all");
			} else if (i == TYPE_ITEM) {
				View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_duoshuo_comment, viewGroup, false);
				return new CommentViewHolder(view);
			}
			return null;
		}

		@Override
		public int getItemViewType(int position) {
			if (mComment.mHotComments.size() > 0) {
				if (position == 0)
					return TYPE_HEADER_HOT;
				else if (position == mComment.getHotComments().size()) {
					return TYPE_HEADER;
				} else {
					return TYPE_ITEM;
				}
			} else {
				if (position == 0)
					return TYPE_HEADER;
				else
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
			return mComment.getSize() + 1 + mComment.getHotSize() == 0 ? 0 : mComment.getHotSize() + 1;
		}
	};
}
