package com.nut.Jandan.Activity;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import com.nut.Jandan.Fragment.BaseFragmentInterface;

import java.util.LinkedList;
import java.util.Map;

public abstract class BaseFragmentActivity extends ActionBarActivity {
	private LinkedList<Fragment> currentList;
	private Fragment mCurrentFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		currentList = new LinkedList<>();
		super.onCreate(savedInstanceState);
	}

	public boolean showOnly(Fragment fragment) {
		if (fragment == null) {
			return false;
		}
		try {
			final Fragment old = mCurrentFragment;
			FragmentTransaction transaction = getTransaction();
			boolean isShown = false;
			for (Fragment f : currentList) {
				if (f != fragment) {
					transaction.remove(f);
				} else {
					isShown = true;
					transaction.show(fragment);
				}
			}
			currentList.clear();
			currentList.add(fragment);
			if (!isShown) {
				transaction.add(getContentId(), fragment);
			}
			transaction.commit();
			mCurrentFragment = fragment;
			onFragmentChanged(fragment, old);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean showPreFrag(Map<String, Object> map) {
		if (mCurrentFragment == null) {
			return false;
		}
		int length = currentList.size();
		if (length <= 1) {
			return false;
		}
		try {
			currentList.removeLast();
			FragmentTransaction transaction = getTransaction();
			final Fragment oldFrag = mCurrentFragment;
			transaction.remove(oldFrag);
			//
			mCurrentFragment = currentList.getLast();
			hideOtherFragment(mCurrentFragment, transaction);
			//
			transaction.commit();

			onFragmentChanged(oldFrag, mCurrentFragment);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public boolean showFragment(Fragment fragment) {
		if (fragment == null) {
			return false;
		}
		//
		mCurrentFragment = fragment;
		FragmentTransaction transaction = getTransaction();
		int index = hideOtherFragment(fragment, transaction);
		if (index < 0) {
			transaction.add(getContentId(), fragment);
		} else {
			// exists before. so make it at the last of the list
			currentList.remove(index);
		}
		currentList.add(fragment);
		transaction.commit();
		onFragmentChanged(fragment, mCurrentFragment);
		return true;
	}

	public abstract int getContentId();

	private int hideOtherFragment(Fragment shown, FragmentTransaction transaction) {
		if (shown == null || transaction == null) {
			return -1;
		}
		if (currentList == null || currentList.isEmpty()) {
			return -1;
		}
		int index = -1;
		for (int i = 0; i < currentList.size(); i++) {
			Fragment Fragment = currentList.get(i);
			if (Fragment == shown) {
				index = i;
				if (Fragment.isHidden()) {
					transaction.show(Fragment);
				}
			} else {
				if (Fragment.isVisible())
					transaction.hide(Fragment);
			}
		}
		return index;
	}

	@Override
	public void onBackPressed() {
		if (onBack(null)) {
			return;
		}
		super.onBackPressed();
	}


	private boolean onBack(Map<String, Object> map) {
		if (mCurrentFragment == null) {
			return false;
		}
		if (mCurrentFragment instanceof BaseFragmentInterface) {
			BaseFragmentInterface baseFragmentInterface = (BaseFragmentInterface) mCurrentFragment;
			if (baseFragmentInterface.onBackPressed())
				return true;
		}
		return showPreFrag(map);
	}


	@SuppressLint("CommitTransaction")
	protected FragmentTransaction getTransaction() {
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
//        transaction.setCustomAnimations(
//                android.R.anim.slide_in_left,//
//                android.R.anim.slide_out_right,//
//                android.R.anim.fade_in,//
//                android.R.anim.fade_out//
//        );
		return transaction;
	}

	protected void onFragmentChanged(Fragment shown, Fragment hidden) {
	}

	public int getFragmentSize() {
		if (currentList == null)
			return 0;
		return currentList.size();
	}
}
