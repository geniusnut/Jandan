package com.nut.Jandan.Fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.alensw.Jandan.CommentModel;
import com.nut.Jandan.Activity.PostActivity;
import com.nut.Jandan.Adapter.PostAdapter;
import com.nut.Jandan.R;
import com.nut.cache.Post;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yw07 on 15-4-9.
 */
public class PostFragment extends Fragment {
	private final String TAG = "PostFragment";

	private Toolbar mToolbar;
	private RecyclerView mRecyclerView;
	private LinearLayoutManager mLayoutManager;

	private Post mPost;
	private String mLink;
	private String mTitle;
	private String mCover;
	private int mComm;
	private PostAdapter mPostAdapter;
	private List<CommentModel> mComments = new ArrayList<>(0);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		mPost = args.getParcelable("post");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		/** Inflating the layout for this fragment **/
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.post_frag, container, false);

		mToolbar = ((PostActivity) getActivity()).getToolbar();
		mToolbar.getBackground().setAlpha(0);

		ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
			@Override
			public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
				return 0;
			}

			@Override
			public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder1) {
				return false;
			}

			@Override
			public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {

			}
		});
		mRecyclerView = (RecyclerView) view.findViewById(R.id.post_recycler);
		mLayoutManager = new LinearLayoutManager(getActivity());
		mLayoutManager.setSmoothScrollbarEnabled(true);
		mRecyclerView.setLayoutManager(mLayoutManager);
		// mRecyclerView.getItemAnimator().animateAdd();
		mPostAdapter = new PostAdapter(mPost, mRecyclerView);
		mPostAdapter.setOnParallaxScroll(new PostAdapter.OnParallaxScroll() {
			@Override
			public void onParallaxScroll(float percentage, float offset, View parallax) {
				Drawable c = mToolbar.getBackground();
				c.setAlpha(Math.round(percentage * 255));
				mToolbar.setAlpha(Math.round(percentage * 255));
				((PostActivity) getActivity()).getStatusBar().setAlpha(Math.round(percentage * 255));
			}
		});
		mRecyclerView.setAdapter(mPostAdapter);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}


	static public PostFragment newInstance(Post post) {
		PostFragment postFragment = new PostFragment();
		Bundle args = new Bundle();
		post.mCover = post.mCover.replace("custom", "medium");
		args.putParcelable("post", post);
		postFragment.setArguments(args);
		return postFragment;
	}

}
