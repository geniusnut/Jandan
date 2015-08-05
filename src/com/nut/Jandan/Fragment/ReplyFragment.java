package com.nut.Jandan.Fragment;

import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.larvalabs.svgandroid.SVG;
import com.nut.Jandan.R;
import com.nut.Jandan.model.ReplyModel;
import com.nut.http.PostParser;

/**
 * Created by yw07 on 15-6-15.
 */
public class ReplyFragment extends Fragment {

	private EditText mContentView;

	private OnCommentPostListener mListener;

	public interface OnCommentPostListener {
		public void onCommentPost();
	}

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		Bundle args = getArguments();


		try {
			mListener = (OnCommentPostListener) getTargetFragment();
		} catch (ClassCastException e) {
			throw new ClassCastException("Calling fragment must implement OnCommentPostListener interface");
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_reply, container, false);
		mContentView = (EditText) rootView.findViewById(R.id.editor);
		mContentView.setHint(String.format(getString(R.string.hint), "nut"));
		ImageButton button = (ImageButton) rootView.findViewById(R.id.send);
		final int color = getResources().getColor(R.color.yellow500);
		final int size = 56;
		Drawable iconSend = SVG.getDrawable(getResources(), R.raw.ic_send, color, size);
		button.setImageDrawable(iconSend);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String content = mContentView.getText().toString();
				if (content.length() <= 1) {
					Toast.makeText(getActivity(), getString(R.string.comment_too_short), Toast.LENGTH_SHORT).show();
					return;
				}
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
						Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mContentView.getWindowToken(), 0);

				postComment(content);
			}
		});
		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.reply, menu);

		final int color = getResources().getColor(R.color.yellow500);
		final int size = 56;
		Drawable iconSend = SVG.getDrawable(getResources(), R.raw.ic_send, color, size);
		menu.findItem(R.id.send).setIcon(iconSend);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int id = item.getItemId();
		switch (id) {
			case R.id.send:
				String content = mContentView.getText().toString();
				if (content.length() <= 1) {
					Toast.makeText(getActivity(), getString(R.string.comment_too_short), Toast.LENGTH_SHORT).show();
					return false;
				}
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
						Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mContentView.getWindowToken(), 0);

				postComment(content);
				break;
			default:
				return false;
		}
		return true;
	}

	private void postComment(final String content) {

		//comment id 2888770
//		"1185181175767342657":{
//			"post_id":"1185181175767342657",
//					"thread_id":"1185181175765958285",
//					"status":"approved",
//					"source":"duoshuo",
//					"author_id":3429320,
//					"author_key":"0",
//					"message":"那你特么倒是给啊",
//					"created_at":"2015-08-05T18:00:08+08:00",
		ReplyModel replyModel = new ReplyModel();
		replyModel.email = "geniusnut@126.com";
		replyModel.name = "nut";
		replyModel.message = "Hello, world";
		replyModel.parentId = "1185181175767342657";
		replyModel.threadId = "1185181175765958285";
		new ReplyTask().execute(replyModel);
	}

	private class ReplyTask extends AsyncTask<ReplyModel, Void, Boolean> {

		@Override
		protected Boolean doInBackground(ReplyModel... params) {
			PostParser.postComment(params[0]);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean res) {
			if (res)
				mListener.onCommentPost();
		}
	}
}
