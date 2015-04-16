package com.alensw.Jandan;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.*;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.alensw.gif.GifImageView;
import com.alensw.ui.PictureView;
import com.alensw.ui.ProgressWheel;
import com.alensw.ui.TouchImageView;

import java.io.*;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by yw07 on 14-12-18.
 */
public class PicActivity extends ActionBarActivity {
	public static final String EXTRA_FILENAME = "filename";
	public static final String EXTRA_GIF = "isgif";
	public static final String EXTRA_PIC = "pic";

	private TouchImageView mPictureView;
	private GifImageView mGifView;
	private View mFullScreenAnchorView;
	private TextView mMessageView;
	private ProgressBar mProgressWheel;
	private ProgressWheel mProgressWheel0;
	public ProgressListener mProgressListener;
	private Toolbar toolbar;

	private boolean running;
	private Pic mPic;
	int progress = 0;
	private Uri mUri;
	public Bitmap mBitmap = null;
	private Boolean isGif;
	private GifLoader gifLoader;
	@Override
	public void onCreate(Bundle savedBundle) {
		super.onCreate(savedBundle);
		setContentView(R.layout.viewer);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
			toolbar.getBackground().setAlpha(0);
		}
		toolbar.setLogo(R.drawable.jandan);

		mPic = getIntent().getParcelableExtra(EXTRA_PIC);

		if (savedBundle == null) {
			mUri = getIntent().getData();
			final Bundle extras = getIntent().getExtras();
			//mUri = "/sdcard/Android/data/com.alensw.Jandan/cache/2571613.jpg";
			isGif = extras.getBoolean(EXTRA_GIF);
			if (!isGif) {
				mPictureView = (TouchImageView) findViewById(R.id.image);
				mPictureView.setMaxZoom(20);
				mPictureView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						toggleFullScreen();
					}

				});
			}
			else {
				String path = extras.getString(EXTRA_FILENAME) + ".gif";
				File file = FileCache.generateCacheFile(path);
				if (!file.exists()) {
					mProgressWheel0 = (ProgressWheel) findViewById(R.id.progressBarTwo);
					mProgressListener = new ProgressListener(mProgressWheel0);
					//mProgressWheel0.setText("Loading");
					mProgressWheel0.resetCount();
					mProgressWheel0.setVisibility(View.VISIBLE);

					gifLoader = new GifLoader();
					gifLoader.execute(mUri.toString(), path);
				} else {
					mUri = Uri.fromFile(file);
				}

				mGifView = (GifImageView) findViewById(R.id.gifImage);
			}
		}


		if (isHoneycombOrHigher()) {
			mFullScreenAnchorView = getWindow().getDecorView();
			// to keep our UI controls visibility in line with system bars
			// visibility
			mFullScreenAnchorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
				@SuppressLint("InlinedApi")
				@Override
				public void onSystemUiVisibilityChange(int flags) {
					boolean visible = (flags & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0;
					if (visible) {
						toolbar.setVisibility(View.VISIBLE);
					} else {
						toolbar.setVisibility(View.INVISIBLE);
					}
				}
			});
		}
	}

	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available
		delayedHide(200);
	}

	Handler mHideSystemUiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (isHoneycombOrHigher()) {
				hideSystemUI(mFullScreenAnchorView);
			}
			getSupportActionBar().hide();
		}
	};

	private void delayedHide(int delayMillis) {
		mHideSystemUiHandler.removeMessages(0);
		mHideSystemUiHandler.sendEmptyMessageDelayed(0, delayMillis);
	}

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
			mProgressListener.onTransferProgress(progress[0]);
		}

		protected void onPostExecute(Uri result) {
			mProgressWheel0.setVisibility(View.GONE);
			mGifView.setImageURI(result);
			Log.d("PicActivity", "mUri : " + mUri);
		}
	}

	private class ProgressListener implements OnDatatransferProgressListener {
		int mLastPercent = 0;
		WeakReference<ProgressWheel> mProgressBar = null;

		ProgressListener(ProgressWheel progressBar) {
			mProgressBar = new WeakReference<>(progressBar);
		}

		@Override
		public void onTransferProgress(int percent) {
			if (percent != mLastPercent) {
				ProgressWheel pb = mProgressBar.get();
				if (pb != null) {
					pb.setProgress((int) (((double) percent) * 3.6));
					pb.setText(percent + "%");
					pb.postInvalidate();
				}
			}
			mLastPercent = percent;
		}

	}
	public void toggleFullScreen() {
//		if (isHoneycombOrHigher()) {
//
//			boolean visible = (mFullScreenAnchorView.getSystemUiVisibility()
//					& View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0;
//
//			if (visible) {
//				hideSystemUI(mFullScreenAnchorView);
//				// actionBar.hide(); // propagated through
//				// OnSystemUiVisibilityChangeListener()
//			} else {
//				showSystemUI(mFullScreenAnchorView);
//				// actionBar.show(); // propagated through
//				// OnSystemUiVisibilityChangeListener()
//			}
//
//		} else {
//
//			ActionBar actionBar = getSupportActionBar();
//			if (!actionBar.isShowing()) {
//				actionBar.show();
//
//			} else {
//				actionBar.hide();
//
//			}
//
//		}
	}
	@Override
	public void onStart() {
		super.onStart();
		if (mUri != null) {
			BitmapLoader bl = new BitmapLoader(mPictureView, mMessageView, mProgressWheel);
			// "/sdcard/Android/data/com.alensw.Jandan/cache/2571613.jpg" for test very long picture
			if (!isGif)
				bl.execute(mUri.getPath());
			else {
				mGifView.setImageURI(mUri);
			}
		}
	}

	private class BitmapLoader extends AsyncTask<String, Void, Bitmap> {

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
			String storagePath = params[0];
			try {
				File picture = new File(storagePath);
				if (picture != null) {
					//Decode file into a bitmap in real size for being able to make zoom on the image
					result = BitmapFactory.decodeStream(new FlushedInputStream
							(new BufferedInputStream(new FileInputStream(picture))));
				}
				if (result == null) {

				}
			} catch (OutOfMemoryError e) {
				// If out of memory error when loading image, try to load it scaled
				result = loadScaledImage(storagePath);
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
					mBitmap  = result;
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

	private void initViewPager() {
		// get parent from path

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
			// second try to scale down the image , this time depending upon the screen size
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

	@SuppressLint("InlinedApi")
	private void hideSystemUI(View anchorView) {
		anchorView.setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_HIDE_NAVIGATION         // hides NAVIGATION BAR; Android >= 4.0
						| View.SYSTEM_UI_FLAG_FULLSCREEN              // hides STATUS BAR;     Android >= 4.1
						| View.SYSTEM_UI_FLAG_IMMERSIVE               // stays interactive;    Android >= 4.4
						| View.SYSTEM_UI_FLAG_LAYOUT_STABLE           // draw full window;     Android >= 4.1
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN       // draw full window;     Android >= 4.1
						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION  // draw full window;     Android >= 4.1
		);
	}

	@SuppressLint("InlinedApi")
	private void showSystemUI(View anchorView) {
		anchorView.setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE           // draw full window;     Android >= 4.1
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN       // draw full window;     Android >= 4.1
						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION  // draw full window;     Android >= 4.1
		);
	}

	private boolean isHoneycombOrHigher() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return true;
		}
		return false;
	}
}
