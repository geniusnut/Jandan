package com.nut.Jandan.Fragment;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.nut.Jandan.Activity.JandanActivity;

/**
 * Created by yw07 on 15-6-2.
 */
public class SettingsBaseFragment extends BaseFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((JandanActivity) getActivity()).getSupportActionBar().hide();
		((JandanActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		((JandanActivity) getActivity()).mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public boolean onBackPressed() {
		((JandanActivity) getActivity()).getSupportActionBar().show();
		return super.onBackPressed();
	}
}
