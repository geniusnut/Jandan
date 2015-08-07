package com.nut.Jandan.Fragment;

import android.app.Fragment;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.widget.TextView;
import com.larvalabs.svgandroid.SVG;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nut.Jandan.Activity.BaseFragmentActivity;
import com.nut.Jandan.Activity.JandanActivity;
import com.nut.Jandan.R;
import com.nut.dao.DuoshuoComment;
import com.nut.http.PostParser;
import com.nut.ui.CircleImageView;

/**
 * Created by yw07 on 15-7-31.
 */
public class CommentFragment extends Fragment implements BaseFragmentInterface, ReplyFragment.OnCommentPostListener {
	private DuoshuoComment mComment = new DuoshuoComment();
	private ImageLoader mImageLoader;
	private DisplayImageOptions mOptions;
	private Long mCommentId;
	private SwipeRefreshLayout swipeLayout;
	private RecyclerView mRecyclerView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		mCommentId = args.getLong("commentId");
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.swipe_news_frag, container, false);
		swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
		swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				new CommentsTask().execute(mCommentId);
			}
		});

		int color = getResources().getColor(R.color.teal500);
		int toolbarSize = JandanActivity.getActionBarSize(getActivity());
		swipeLayout.setColorSchemeColors(color);
		swipeLayout.setProgressViewOffset(false, toolbarSize, toolbarSize + 128);
		mRecyclerView = (RecyclerView) rootView.findViewById(R.id.cardList);
		final LinearLayoutManager llm = new LinearLayoutManager(getActivity());
		llm.setOrientation(LinearLayoutManager.VERTICAL);
		mRecyclerView.setLayoutManager(llm);

		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mRecyclerView.setAdapter(mAdapter);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

		mImageLoader = ImageLoader.getInstance();
		mOptions = new DisplayImageOptions.Builder()
				.bitmapConfig(Bitmap.Config.RGB_565)
				.imageScaleType(ImageScaleType.EXACTLY)
				.cacheOnDisk(true)
				.showImageOnLoading(R.drawable.ic_avatar)
				.showImageOnFail(R.drawable.ic_avatar).
						build();
		swipeLayout.setRefreshing(true);
		new CommentsTask().execute(mCommentId);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.reply, menu);

		final int color = Color.BLACK;
		final int size = 56;
		Drawable iconSend = SVG.getDrawable(getResources(), R.raw.ic_send, color, size);
		menu.findItem(R.id.send).setIcon(iconSend);
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

	@Override
	public void onCommentPost() {
		// mAdapter.refreshComment();
	}

	public class CommentsTask extends AsyncTask<Long, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Long... params) {
			PostParser.getDuoshuoComments(params[0], mComment);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean res) {
			swipeLayout.setRefreshing(false);
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
			mTitle = (TextView) itemView.findViewById(R.id.comment_header);
			mTitle.setText(text);
		}
	}

	private RecyclerView.Adapter mAdapter = new RecyclerView.Adapter() {
		private static final int TYPE_HEADER_HOT = 0;
		private static final int TYPE_HEADER = 1;
		private static final int TYPE_ITEM = 10;

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
			Resources res = viewGroup.getResources();
			if (i == TYPE_HEADER_HOT) {
				View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_comment_header, viewGroup, false);
				return new HeaderViewHolder(view, res.getString(R.string.hotComments));
			} else if (i == TYPE_HEADER) {
				View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_comment_header, viewGroup, false);
				return new HeaderViewHolder(view, res.getString(R.string.allComments));
			} else if (i == TYPE_ITEM) {
				View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_duoshuo_comment, viewGroup, false);
				final CommentViewHolder vh = new CommentViewHolder(view);
				vh.itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						DuoshuoComment.Comment comment = getCommentByPos(vh.getAdapterPosition());
						Bundle args = new Bundle();
						// args.putLong("", comme);
						Fragment fragment = new ReplyFragment();
						args.putString("thread_id", mComment.mThreadId);
						args.putString("parent_id", comment.mId);
						args.putString("reply_to", comment.mAuthor);
						fragment.setArguments(args);
						fragment.setTargetFragment(CommentFragment.this, 0);
						((BaseFragmentActivity) getActivity()).showFragment(fragment);
					}
				});
				return vh;
			}
			return null;
		}

		@Override
		public int getItemViewType(int position) {
			if (mComment.mHotComments.size() > 0) {
				if (position == 0)
					return TYPE_HEADER_HOT;
				else if (position == mComment.getHotComments().size() + 1) {
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
				DuoshuoComment.Comment comment = getCommentByPos(i);
				CommentViewHolder commentVH = (CommentViewHolder) viewHolder;
				commentVH.mAuthor.setText(comment.mAuthor);
				mImageLoader.displayImage(comment.mAvatar, commentVH.mAvatar, mOptions);
				commentVH.mDate.setText(comment.mDate);
				commentVH.mContent.setText(comment.mContent);
			}
		}

		@Override
		public int getItemCount() {
			if (mComment.getHotSize() == 0)
				return mComment.getSize() + 1;
			else
				return mComment.getSize() + 1 + mComment.getHotSize() + 1;
		}

		private DuoshuoComment.Comment getCommentByPos(int pos) {
			DuoshuoComment.Comment comment;
			if (mComment.getHotSize() > 0) {
				int hotSize = mComment.getHotSize();
				if (pos < hotSize + 1)
					comment = mComment.mHotComments.get(pos - 1);
				else
					comment = mComment.mComments.get(pos - hotSize - 2);
			} else {
				comment = mComment.mComments.get(pos - 1);
			}
			return comment;
		}
	};
}
