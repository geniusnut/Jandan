package com.nut.Jandan.Fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by yw07 on 15-7-28.
 */
public class JokeFragment extends Fragment {
	private RecyclerView mRecyclerView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return new RecyclerView(container.getContext());
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {

		// final RecyclerListAdapter adapter = new RecyclerListAdapter(getActivity(), this);

		RecyclerView recyclerView = (RecyclerView) view;
		recyclerView.setHasFixedSize(true);
		recyclerView.setAdapter(adapter);

		final int spanCount = 2;
		final GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), spanCount);
		recyclerView.setLayoutManager(layoutManager);
	}
}
