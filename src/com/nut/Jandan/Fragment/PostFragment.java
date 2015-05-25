package com.nut.Jandan.Fragment;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nut.Jandan.R;
import com.nut.cache.Post;
import com.nut.dao.PostFormater;
import com.nut.ui.CustomScrollView;
import com.nut.ui.HeaderAdapter;

/**
 * Created by yw07 on 15-4-9.
 */
public class PostFragment extends Fragment {
	private final String TAG = "PostFragment";

	private Toolbar toolbar;
	private RecyclerView mRecyclerView;
	private LinearLayoutManager mLayoutManager;

	private Post mPost;
	private String mLink;
	private String mTitle;
	private String mCover;
	private int mComm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		mLink = args.getString("link");
		mTitle = args.getString("title", "");
		mCover = args.getString("cover", "");
		mComm = args.getInt("comm", 0);
		mPost = new Post();
		mPost.mLink = mLink;
		mPost.mCover = mCover;
		mPost.mCont = mComm;
		mPost.mTitle = mTitle;

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		/** Inflating the layout for this fragment **/
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.post_frag, container, false);


		mRecyclerView = (RecyclerView) view.findViewById(R.id.post_recycler);
		mLayoutManager = new LinearLayoutManager(getActivity());
		mRecyclerView.setLayoutManager(mLayoutManager);
		mRecyclerView.setAdapter(new HeaderAdapter(mPost));
		mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				recyclerView.getChildAt(0).scrollTo(0, dy/2);
			}
		});
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	static public PostFragment newInstance(Post post) {
		PostFragment postFragment = new PostFragment();
		Bundle args = new Bundle();
		args.putString("link", post.mLink);
		args.putString("title", post.mTitle);
		args.putInt("comm", post.mCont);
		args.putString("cover", post.mCover.replace("custom", "medium"));
		postFragment.setArguments(args);
		return postFragment;
	}

}
