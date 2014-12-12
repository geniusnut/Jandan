package com.alensw.Jandan;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.webkit.WebView;
import android.widget.*;
import com.larvalabs.svgandroid.SVG;

/**
 * Created by yw07 on 14-11-20.
 */
public class PostActivity extends ActionBarActivity {
	private final String TAG = "PostActivity";
	Activity postActivity = this;
	String link ;
	String title ;
	String comm;
	WebView webview;
	WebView commwebview;
	private Toolbar toolbar;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		setContentView(R.layout.activity_post);

		toolbar = (Toolbar) findViewById(R.id.toolbar_post);
		if (toolbar != null) {
			toolbar.setTitle("News");
			ImageView share = (ImageView) toolbar.findViewById(R.id.share);
			Log.d(TAG, "toolbar width" + toolbar.getMeasuredWidth());
			share.setLayoutParams(new Toolbar.LayoutParams(56, 56, Gravity.RIGHT));
			//share.setImageDrawable(SVG.getDrawable(getResources(), R.raw.ic_menu_share));
			setSupportActionBar(toolbar);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		ProgressBar progressBar = new ProgressBar(this);
		progressBar.setLayoutParams(new Toolbar.LayoutParams(Gravity.RIGHT));
		progressBar.setIndeterminate(true);
		ImageButton imageButton = (ImageButton) findViewById(R.id.ib);
		//imageButton.setLayoutParams(new Toolbar.LayoutParams(Gravity.RIGHT));
		if (imageButton != null)
			toolbar.addView(imageButton);
		//toolbar.addView(progressBar);

		//setActionBar();
		link = getIntent().getStringExtra("link");
		title = getIntent().getStringExtra(Intent.EXTRA_TITLE);
		comm = getIntent().getStringExtra("comm");

		//TextView mComm = (TextView) findViewById(R.id.btn_post_comm_text);
		//mComm.setText(comm);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
			case R.id.action_settings:
				return true;
			case android.R.id.home:
				postActivity.finish();
				return true;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
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
