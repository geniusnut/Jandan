package com.nut.Jandan;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.nut.cache.Post;
import com.nut.dao.PostFormater;
import com.nut.ui.CustomScrollView;

/**
 * Created by yw07 on 15-4-9.
 */
public class PostFragment extends Fragment {
	private final String TAG = "PostFragment";

	private Toolbar toolbar;
	private CustomScrollView mScrollView;
	private View mWebImage;
	private View mWebContent;
	private WebView mWebView;
	private WebView mCommWebView;
	private ImageView ivCarat;

	private String mLink;
	private String mTitle;
	private int mComm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		mLink = args.getString("link");
		mTitle = args.getString("title", "");
		mComm = args.getInt("comm", 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		/** Inflating the layout for this fragment **/
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.post_frag, container, false);


		//setActionBar();


		mWebView = (WebView) view.findViewById(R.id.webview);
		mWebView.clearCache(true);
		mWebView.getSettings().setAppCacheEnabled(true);
		// mWebView.addJavascriptInterface();
		Log.d(TAG, "webViewLoad : " + mLink);
		new webViewLoad().execute(mLink);

		TextView commTextView = (TextView) view.findViewById(R.id.btn_post_comm_text);

		commTextView.setText(getString(R.string.post_comments, mComm));
		//Drawable iconExpand = getResources().getDrawable(R.drawable.icon_expand);
		//iconExpand.setColorFilter(R.color.lightBlue, Mode.MULTIPLY);
		ivCarat = (ImageView) view.findViewById(R.id.ivCarat);
		//((ImageView)findViewById(R.id.ivCarat)).setImageDrawable(iconExpand);
		//((ImageView)findViewById(R.id.ivCarat)).setBackgroundColor(Color.GRAY);
		mScrollView = (CustomScrollView) view.findViewById(R.id.scrollView);
//		mScrollView.setTag(mTitle);
		mScrollView.setScrollViewListener(new CustomScrollView.ScrollViewListener() {
			@Override
			public void onScrollChanged(CustomScrollView scrollView, int x, int y, int oldx, int oldy) {
				Log.d("PostFragment", "mScrollView: " + mScrollView.getTag() + ", y: " + y);
				Log.d("PostFragment", "mWebContent y: " + mWebContent.getScrollY());
				Log.d("PostFragment", "mWebImage y: " + mWebImage.getScrollY());
				if (y >= 0)
					mWebImage.scrollTo(0, -y / 2);
			}
		});
		mWebImage = (View) mScrollView.findViewById(R.id.web_image);
		mWebContent = mScrollView.findViewById(R.id.web_content);

		mCommWebView = (WebView) view.findViewById(R.id.post_comm);
		mCommWebView.clearCache(true);
		new commCommWebViewLoad().execute(mLink);

		mCommWebView.setVisibility(View.GONE);
		final RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(R.id.btn_post_comm);
		relativeLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mCommWebView.getVisibility() == View.VISIBLE) {
					ivCarat.setRotation(0);
					mCommWebView.removeAllViews();
					mCommWebView.setVisibility(View.GONE);
				} else {
					ivCarat.setRotation(180);
					mCommWebView.setVisibility(View.VISIBLE);

					new Handler().post(new Runnable() {
						@Override
						public void run() {
							mScrollView.smoothScrollTo(0, relativeLayout.getTop() - 10);
						}
					});
					/*scrollView.post(new Runnable() {
						@Override
						public void run() {
							scrollView.smoothScrollTo(0, linearLayout.getTop()-10);
						}
					});*/
				}
			}
		});

		return view;
	}

	private class webViewLoad extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... strings) {
			PostFormater postFormater = new PostFormater(getActivity());
			return postFormater.postFormater(strings[0]);
		}

		@Override
		protected void onPostExecute(String data) {
			mWebView.loadDataWithBaseURL("", data, "text/html", "UTF-8", "");
		}
	}

	private class commCommWebViewLoad extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... strings) {
			PostFormater postFormater = new PostFormater(getActivity());
			return postFormater.commFormater(strings[0]);
		}

		protected void onPostExecute(String data) {
			mCommWebView.loadDataWithBaseURL("", data, "text/html", "UTF-8", "");
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d("PostFragment", "start: " + mScrollView.getTag() + ", y: " + mScrollView.getScrollY());
	}


	static public PostFragment newInstance(Post post) {
		PostFragment postFragment = new PostFragment();
		Bundle args = new Bundle();
		args.putString("link", post.mLink);
		args.putString("title", post.mTitle);
		args.putInt("comm", post.mCont);
		postFragment.setArguments(args);
		return postFragment;
	}
}
