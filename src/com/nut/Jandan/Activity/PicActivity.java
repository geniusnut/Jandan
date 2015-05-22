package com.nut.Jandan.Activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.nut.Jandan.Utility.Utilities;
import com.nut.cache.FileCache;
import com.nut.cache.Pic;
import com.nut.gif.GifImageView;
import com.nut.ui.PictureView;
import com.nut.ui.TouchImageView;

import java.io.*;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by yw07 on 15-4-16.
 */
public class PicActivity extends ActionBarActivity {
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

	private ArrayList<ItemInfo> mItems = new ArrayList<>();
	private int progress = 0;
	private GifLoader mGifLoader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPic = getIntent().getParcelableExtra(EXTRA_PIC);
		for (int i = 0; i < mPic.mUrls.size(); i++) {
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
//				GifImageView gifImageView = new GifImageView(getBaseContext());
//				container.addView(gifImageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//				picAdapter = new PicAdapter(gifImageView, item);

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
		public void destroyItem(ViewGroup container, int position, Object object) {
		}

		@Override
		public void setPrimaryItem(ViewGroup container, int position, Object object) {
			if (mPicAdapter != object) {
				mPicAdapter = (PicAdapter) object;
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
				mGifLoader = new GifLoader(mGifImageView);
				mGifLoader.execute(mItemInfo.url, null);
			} else {
				mTouchImageView = (TouchImageView) mView;
				BitmapLoader bl = new BitmapLoader(mTouchImageView, null, null);
				bl.execute(mItemInfo.url);
			}
		}
	}

	private class BitmapLoader extends AsyncTask<String, Integer, Bitmap> {

		/**
		 * Weak reference to the target {@link android.widget.ImageView} where the bitmap will be loaded into.
		 *
		 * Using a weak reference will avoid memory leaks if the target ImageView is retired from memory before the load finishes.
		 */
		private final WeakReference<PictureView> mImageViewRef;

		/**
		 * Weak reference to the target {@link android.widget.TextView} where error messages will be written.
		 *
		 * Using a weak reference will avoid memory leaks if the target ImageView is retired from memory before the load finishes.
		 */
		private final WeakReference<TextView> mMessageViewRef;


		private final WeakReference<ProgressBar> mProgressWheelRef;


		/**
		 * Error message to show when a load fails
		 */
		private int mErrorMessageId;


		/**
		 * Constructor.
		 *
		 * @param imageView     Target {@link android.widget.ImageView} where the bitmap will be loaded into.
		 */
		public BitmapLoader(PictureView imageView, TextView messageView, ProgressBar progressWheel) {
			mImageViewRef = new WeakReference<>(imageView);
			mMessageViewRef = new WeakReference<>(messageView);
			mProgressWheelRef = new WeakReference<>(progressWheel);
		}


		@Override
		protected Bitmap doInBackground(String... params) {
			Bitmap result = null;
			if (params.length != 1) return result;
			String hashName = Utilities.md5(params[0]);
			FileCache.mCacheDir.mkdirs();
			File picture = FileCache.generateCacheFile(hashName);
			InputStream is = null;
			long mLength;
			try {
				if (picture.exists()) {
					//Decode file into a bitmap in real size for being able to make zoom on the scaleImage
					result = BitmapFactory.decodeStream(new FlushedInputStream
							(new BufferedInputStream(new FileInputStream(picture))));
				} else {
					FileOutputStream os = new FileOutputStream(picture);

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
					os.close();
					result = BitmapFactory.decodeStream(new FlushedInputStream
							(new BufferedInputStream(new FileInputStream(picture))));
				}
				if (result == null) {

				}
			} catch (OutOfMemoryError e) {
				// If out of memory error when loading scaleImage, try to load it scaled
				result = loadScaledImage(picture.getPath());
				if (result == null) {

				}
			} catch (NoSuchFieldError e) {
			} catch (Throwable t) {
			}
			return result;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			hideProgressWheel();
			if (result != null) {
				showLoadedImage(result);
			} else {
				showErrorMessage();
			}
		}

		@SuppressLint("InlinedApi")
		private void showLoadedImage(Bitmap result) {
			if (mImageViewRef != null) {
				final PictureView imageView = mImageViewRef.get();
				if (imageView != null) {
					imageView.setBitmap(result);
					imageView.setImageBitmap(result);
					imageView.setVisibility(View.VISIBLE);
					// mBitmap  = result;
				} // else , silently finish, the fragment was destroyed
			}
			if (mMessageViewRef != null) {
				final TextView messageView = mMessageViewRef.get();
				if (messageView != null) {
					messageView.setVisibility(View.GONE);
				} // else , silently finish, the fragment was destroyed
			}
		}

		private void showErrorMessage() {
			if (mImageViewRef != null) {
				final ImageView imageView = mImageViewRef.get();
				if (imageView != null) {
					// shows the default error icon
					imageView.setVisibility(View.VISIBLE);
				} // else , silently finish, the fragment was destroyed
			}
			if (mMessageViewRef != null) {
				final TextView messageView = mMessageViewRef.get();
				if (messageView != null) {
					messageView.setText(mErrorMessageId);
					messageView.setVisibility(View.VISIBLE);
				} // else , silently finish, the fragment was destroyed
			}
		}

		private void hideProgressWheel() {
			if (mProgressWheelRef != null) {
				final ProgressBar progressWheel = mProgressWheelRef.get();
				if (progressWheel != null) {
					progressWheel.setVisibility(View.GONE);
				}
			}
		}

	}

	static class FlushedInputStream extends FilterInputStream {
		public FlushedInputStream(InputStream inputStream) {
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException {
			long totalBytesSkipped = 0L;
			while (totalBytesSkipped < n) {
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if (bytesSkipped == 0L) {
					int byteValue = read();
					if (byteValue < 0) {
						break;  // we reached EOF
					} else {
						bytesSkipped = 1; // we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
	}

	private Bitmap loadScaledImage(String storagePath) {
		// set desired options that will affect the size of the bitmap
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = true;
		options.inPurgeable = true;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
			options.inPreferQualityOverSpeed = false;
		}
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			options.inMutable = false;
		}
		// make a false load of the bitmap - just to be able to read outWidth, outHeight and outMimeType
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(storagePath, options);

		int width = options.outWidth;
		int height = options.outHeight;
		int scale = 1;

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		int screenWidth;
		int screenHeight;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {
			display.getSize(size);
			screenWidth = size.x;
			screenHeight = size.y;
		} else {
			screenWidth = display.getWidth();
			screenHeight = display.getHeight();
		}

		if (width > screenWidth) {
			// second try to slide_in_right down the scaleImage , this time depending upon the screen size
			scale = (int) Math.floor((float)width / screenWidth);
		}
		if (height > screenHeight) {
			scale = Math.max(scale, (int) Math.floor((float)height / screenHeight));
		}
		options.inSampleSize = scale;

		// really load the bitmap
		options.inJustDecodeBounds = false; // the next decodeFile call will be real
		return BitmapFactory.decodeFile(storagePath, options);

	}

	private class GifLoader extends AsyncTask<String, Integer, Uri> {
		private Long mLength;
		private View mView;

		public GifLoader(View view) {
			mView = view;
		}
		protected Uri doInBackground(String... params) {
			InputStream is;
			try {
				String hashName = Utilities.md5(params[0]);
				FileCache.mCacheDir.mkdirs();
				File file = FileCache.generateCacheFile(hashName);
				if (file.exists())
					return Uri.fromFile(file);

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
			((GifImageView) mView).setImageURI(result);
			Log.d("PicActivity", "mUri : ");
		}
	}
}
