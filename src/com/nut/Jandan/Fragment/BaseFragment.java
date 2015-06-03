package com.nut.Jandan.Fragment;

import android.app.Fragment;
import com.nut.Jandan.Activity.BaseFragmentActivity;

/**
 * Created by yw07 on 15-6-2.
 */
public class BaseFragment extends Fragment {

	public void show(BaseFragmentActivity activity) {
		if (activity == null) {
			return;
		}
		activity.showFragment(this);
	}

	public boolean onBackPressed() {
		return false;
	}
}
