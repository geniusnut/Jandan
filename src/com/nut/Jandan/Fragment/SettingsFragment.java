package com.nut.Jandan.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.nut.Jandan.Activity.BarFragmentActivity;
import com.nut.Jandan.Activity.BaseFragmentActivity;
import com.nut.Jandan.R;
import com.nut.Jandan.Utility.FileUtils;

/**
 * Created by yw07 on 15-5-22.
 */
public class SettingsFragment extends PreferenceFragment implements BaseFragmentInterface {
	private SharedPreferences mPreferences;
	private Preference mUpdate;
	private Preference mCache;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		((BarFragmentActivity) getActivity()).showBars(false);
		return super.onCreateView(inflater, container, savedInstanceState);
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

	@Override
	public void show(BaseFragmentActivity activity) {
		if (activity == null) {
			return;
		}
		activity.showFragment(this);
	}

	@Override
	public boolean onBackPressed() {
		((BarFragmentActivity) getActivity()).showBars(true);
		return false;
	}
}
