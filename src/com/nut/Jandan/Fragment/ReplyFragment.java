package com.nut.Jandan.Fragment;

import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.larvalabs.svgandroid.SVG;
import com.nut.Jandan.Activity.BaseFragmentActivity;
import com.nut.Jandan.R;
import com.nut.Jandan.model.ReplyModel;
import com.nut.http.PostParser;

/**
 * Created by yw07 on 15-6-15.
 */
public class ReplyFragment extends Fragment implements CustomDialogFragment.UserNameListener {

	private EditText mContentView;

	private OnCommentPostListener mListener;
	private String mParentId;
	private String mThreadId;
	private String mReplyTo;
	private String mContent;

	public interface OnCommentPostListener {
		public void onCommentPost();
	}

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		Bundle args = getArguments();

		mThreadId = args.getString("thread_id");
		mParentId = args.getString("parent_id", "");
		mReplyTo = args.getString("reply_to", "");
		mContent = "";

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
		mContentView.setHint(String.format(getString(R.string.hint), mReplyTo));
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
				CustomDialogFragment editNameDialog = new CustomDialogFragment(content);
				editNameDialog.setTargetFragment(ReplyFragment.this, 0);
				editNameDialog.show(getFragmentManager(), "fragment_edit_name");
				mContent = content;
//				postComment(content);
			}
		});
		return rootView;
	}

	@Override
	public void onFinishUserDialog(String user, String email) {
		ReplyModel replyModel = new ReplyModel();
		replyModel.name = user;
		replyModel.email = email;
		replyModel.message = mContent;
		replyModel.parentId = mParentId;
		replyModel.threadId = mThreadId;
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

			((BaseFragmentActivity) getActivity()).onBackPressed();
		}
	}
}
