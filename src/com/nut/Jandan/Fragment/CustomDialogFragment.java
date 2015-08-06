package com.nut.Jandan.Fragment;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import com.nut.Jandan.R;

public class CustomDialogFragment extends DialogFragment {
	private EditText mEditText;
	private EditText mEditEmail;

	public CustomDialogFragment(String content) {

	}

	public interface UserNameListener {
		void onFinishUserDialog(String user, String email);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.login_reply, container);
		mEditText = (EditText) view.findViewById(R.id.username);
		mEditEmail = (EditText) view.findViewById(R.id.user_email);

		// set this instance as callback for editor action
		// mEditText.setOnEditorActionListener(this);
		mEditText.requestFocus();
		getDialog().getWindow().setBackgroundDrawableResource(android.R.color.white);
		getDialog().setTitle("游客留言");

		Button cancelButton = (Button) view.findViewById(R.id.cancel);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		Button okButton = (Button) view.findViewById(R.id.ok);
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mEditText.getText() == null)
					return;

				UserNameListener listener = (UserNameListener) getTargetFragment();
				listener.onFinishUserDialog(mEditText.getText().toString(), mEditEmail.getText().toString());
				dismiss();
			}
		});
		return view;
	}
}
