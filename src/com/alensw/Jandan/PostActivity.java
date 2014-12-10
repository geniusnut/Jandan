package com.alensw.Jandan;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by yw07 on 14-11-20.
 */
public class PostActivity extends Activity {
	private final String TAG = "PostActivity";
	Activity postActivity = this;
	String link ;
	String title ;
	String comm;
	WebView webview;
	WebView commwebview;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		setContentView(R.layout.activity_post);
		setActionBar();
		link = getIntent().getStringExtra("link");
		title = getIntent().getStringExtra(Intent.EXTRA_TITLE);
		comm = getIntent().getStringExtra("comm");

		TextView mComm = (TextView) findViewById(R.id.btn_post_comm_text);
		mComm.setText(comm);
		webview = (WebView) findViewById(R.id.webview);
		webview.clearCache(true);
		Log.d(TAG, "webViewLoad : " + link);
		new webViewLoad().execute(link);


		//commwebview = (WebView) findViewById(R.id.post_comm);
		//commwebview.clearCache(true);
		//new commViewLoad().execute(link);
	}

	private class webViewLoad extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... strings) {
			PostFormater postFormater = new PostFormater(getApplicationContext());
			return postFormater.postFormater(strings[0]);
		}
		protected void onPostExecute(String data){
			webview.loadDataWithBaseURL("", data, "text/html", "UTF-8", "");
		}
	}

	private class commViewLoad extends AsyncTask<String, Void, String>{
		@Override
		protected String doInBackground(String... strings) {
			PostFormater postFormater = new PostFormater(getApplicationContext());
			return postFormater.commFormater(strings[0]);
		}
		protected void onPostExecute(String data){
			commwebview.loadDataWithBaseURL("", data, "text/html", "UTF-8", "");
		}
	}

	private void setActionBar() {
		LayoutInflater inflater = (LayoutInflater) getActionBar()
				.getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);

		View customActionBarView = inflater.inflate(R.layout.actionbar_post, null);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(
				ActionBar.DISPLAY_SHOW_CUSTOM,
				ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
						| ActionBar.DISPLAY_SHOW_TITLE);
		actionBar.setCustomView(customActionBarView,
				new ActionBar.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));

		LinearLayout btn_back = (LinearLayout)findViewById(R.id.btn_post_back);
		btn_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				postActivity.finish();
			}
		});
	}
}
