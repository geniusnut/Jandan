package com.nut.Jandan.Fragment;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.alensw.Jandan.CommentModel;
import com.nut.Jandan.R;
import com.nut.cache.Post;
import com.nut.Jandan.Adapter.PostAdapter;
import com.nut.http.PostParser;

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
//		mLink = args.getString("link");
//		mTitle = args.getString("title", "");
//		mCover = args.getString("cover", "");
//		mComm = args.getInt("comm", 0);
//		mPost = new Post();
//		mPost.mLink = mLink;
//		mPost.mCover = mCover;
//		mPost.mCont = mComm;
//		mPost.mTitle = mTitle;

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		/** Inflating the layout for this fragment **/
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.post_frag, container, false);

		// mToolbar = (Toolbar) view.findViewById(R.id.toolbar_post);


		mRecyclerView = (RecyclerView) view.findViewById(R.id.post_recycler);
		mLayoutManager = new LinearLayoutManager(getActivity());
		mRecyclerView.setLayoutManager(mLayoutManager);
		// mRecyclerView.getItemAnimator().animateAdd();
		mPostAdapter = new PostAdapter(mPost, mRecyclerView);
		mPostAdapter.setOnParallaxScroll(new PostAdapter.OnParallaxScroll() {
			@Override
			public void onParallaxScroll(float percentage, float offset, View parallax) {
//				Drawable c = mToolbar.getBackground();
//				c.setAlpha(Math.round(percentage * 255));
//				mToolbar.setBackground(c);
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
		post.mCover.replace("custom", "medium");
		args.putParcelable("post", post);
//		args.putString("link", post.mLink);
//		args.putString("title", post.mTitle);
//		args.putInt("comm", post.mCont);
		postFragment.setArguments(args);
		return postFragment;
	}

}
