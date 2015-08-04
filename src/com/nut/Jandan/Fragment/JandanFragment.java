package com.nut.Jandan.Fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.nut.Jandan.Activity.BaseFragmentActivity;
import com.nut.Jandan.R;

/**
 * Created by yw07 on 15-8-4.
 */
public class JandanFragment extends Fragment implements BaseFragmentInterface {
	private NewsFragment newsFrag = null;
	private PicsFragment picFrag = null;
	private JokeFragment jokeFrag;

	private ViewPager mViewPager;
	private TabLayout mTableLayout;
	private SampleFragmentPagerAdapter mFragmentAdapter;

	private int mSelected = -1;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.mainfragment, container, false);
		// init table layout;
		mViewPager = (ViewPager) rootView.findViewById(R.id.viewpager);
		mFragmentAdapter = new SampleFragmentPagerAdapter(getFragmentManager(), getActivity());
		mViewPager.setAdapter(mFragmentAdapter);

		mTableLayout = (TabLayout) rootView.findViewById(R.id.tabs);
		mTableLayout.setupWithViewPager(mViewPager);

		mTableLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				int pos = tab.getPosition();
				mViewPager.setCurrentItem(pos);
				// mNaviMenu.getItem(pos).setChecked(true);
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {

			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {

			}
		});

		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (getFragmentManager().findFragmentById(R.id.viewpager) == null) {
			mSelected = 0;
		}
	}

	@Override
	public void show(BaseFragmentActivity activity) {

	}

	@Override
	public boolean onBackPressed() {
		return false;
	}

	public class SampleFragmentPagerAdapter extends FragmentPagerAdapter {
		final int PAGE_COUNT = 4;
		private int tabTitles[] = new int[]{R.string.nav_news, R.string.nav_pics, R.string.nav_joke, R.string.nav_ooxx};
		private Context context;

		public SampleFragmentPagerAdapter(FragmentManager fm, Context context) {
			super(fm);
			this.context = context;
		}

		@Override
		public int getCount() {
			return PAGE_COUNT;
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
				case 0:
					if (newsFrag == null)
						newsFrag = new NewsFragment();
					return newsFrag;
				case 1:
					if (picFrag == null)
						picFrag = new PicsFragment();
					return picFrag;
				case 2:
					if (jokeFrag == null)
						jokeFrag = new JokeFragment();
					return jokeFrag;
			}
			return PageFragment.newInstance(position + 1);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			// Generate title based on item position
			return getString(tabTitles[position]);
		}
	}
}
