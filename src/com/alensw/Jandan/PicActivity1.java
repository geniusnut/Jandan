package com.alensw.Jandan;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.alensw.gif.GifImageView;
import com.alensw.ui.TouchImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by yw07 on 15-4-16.
 */
public class PicActivity1 extends ActionBarActivity {
	public static final String EXTRA_PIC = "pic";

	private ViewPager mViewPager;
	private TouchImageView mTouchImageView;
	private GifImageView mGifImageView;

	private Pic mPic;
	private PicAdapter mPicAdapter;

	private class ItemInfo {
		String url;
		boolean isGif;
	}

	;
	private ArrayList<ItemInfo> mItems;
	private int progress = 0;
	private GifLoader mGifLoader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPic = getIntent().getParcelableExtra(EXTRA_PIC);
		for (int i = mPic.mUrls.size() - 1; i >= 0; i--) {
			ItemInfo item = new ItemInfo();
			item.url = mPic.mUrls.get(i);
			item.isGif = item.url.endsWith("gif");
			mItems.add(i, item);
		}

		mViewPager = new ViewPager(this);
		setContentView(mViewPager);
		mViewPager.setAdapter(mPagerAdapter);

		mPagerAdapter.instantiateItem(mViewPager, 0);
		mViewPager.setCurrentItem(0);
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (mPicAdapter != null)
			mPicAdapter.start();
	}

	private PagerAdapter mPagerAdapter = new PagerAdapter() {
		@Override
		public int getCount() {
			return mPic.mUrls.size();
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ItemInfo item = mItems.get(position);
			if (item != null) {
				PicAdapter picAdapter;
				if (item.isGif) {
					// View view = getLayoutInflater().inflate(R.layout.gif_viewer, null);
					GifImageView gifImageView = new GifImageView(getBaseContext());
					container.addView(gifImageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
					picAdapter = new PicAdapter(gifImageView, item);
				} else {
					TouchImageView view = new TouchImageView(getBaseContext());
					container.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
					picAdapter = new PicAdapter(view, item);
				}
				if (mPicAdapter == null)
					mPicAdapter = picAdapter;

				return picAdapter;
			}
			return null;
		}

		@Override
		public boolean isViewFromObject(View view, Object o) {
			return view == ((PicAdapter) o).mView;
		}

		@Override
		public void setPrimaryItem(ViewGroup container, int position, Object object) {
			if (mPicAdapter != object) {
				mPicAdapter.start();
			}
		}
	};

	private class PicAdapter {
		View mView;
		ItemInfo mItemInfo;

		public PicAdapter(View view, ItemInfo item) {
			mView = view;
			mItemInfo = item;
		}

		public void start() {
			if (mItemInfo.isGif) {
				mGifImageView = (GifImageView) mView;
				progress = 0;
				mGifLoader = new GifLoader();
				mGifLoader.execute(null, null);
			} else {

			}
		}
	}

	;

	private class GifLoader extends AsyncTask<String, Integer, Uri> {
		private Long mLength;

		protected Uri doInBackground(String... params) {
			InputStream is;
			try {
				File file = FileCache.generateCacheFile(params[1]);
				FileOutputStream os = new FileOutputStream(file);

				URL url = new URL(params[0]);
				URLConnection conn = url.openConnection();
				is = conn.getInputStream();
				mLength = Long.parseLong(conn.getHeaderField("Content-Length"));
				final byte[] data = new byte[8192];
				int bytes;
				while ((bytes = is.read(data)) >= 0) {
					if (bytes > 0) {
						os.write(data, 0, bytes);
						progress += bytes;
						int percent = (int) (100.0 * ((double) progress) / ((double) mLength));
						publishProgress(percent);
					}
				}
				is.close();
				return Uri.fromFile(file);
			} catch (IOException e) {
				return null;
			}
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			// mProgressListener.onTransferProgress(progress[0]);
		}

		protected void onPostExecute(Uri result) {
			mGifImageView.setImageURI(result);
			Log.d("PicActivity", "mUri : ");
		}
	}
}
