package com.nut.Jandan.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.nut.Jandan.Activity.JandanActivity;
import com.nut.Jandan.R;
import com.nut.Jandan.Utility.FileUtils;

/**
 * Created by yw07 on 15-5-22.
 */
public class SettingsFragment extends PreferenceFragment {
	private SharedPreferences mPreferences;
	private Preference mUpdate;
	private Preference mCache;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		((JandanActivity) getActivity()).getSupportActionBar().hide();
		((JandanActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		((JandanActivity) getActivity()).mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_setting, container, false);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());


		// 清除缓存
		mCache = (Preference) findPreference("pref_cache");
		mCache.setSummary(FileUtils.getFileSize(FileUtils.getCacheSize(getActivity())));
		mCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				FileUtils.clearAppCache(getActivity());
				mCache.setSummary("0KB");
				return true;
			}
		});
	}
}
