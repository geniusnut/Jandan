package com.nut.Jandan.Fragment;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.alensw.support.picture.Picture;
import com.alensw.view.PictureView;
import com.dao.PictureLoader;
import com.nut.Jandan.Activity.BaseFragmentActivity;
import com.nut.Jandan.JandanApp;
import com.nut.Jandan.R;
import com.nut.Jandan.model.MediaModel;
import com.nut.cache.Pic;

import java.util.ArrayList;

/**
 * Created by yw07 on 15-5-28.
 */
public class PictureFragment extends Fragment implements Handler.Callback, BaseFragmentInterface {
	public static final int MSG_REQUEST_PICTURES = 3000;
	private static final int REQUEST_PICTURES_DELAY = 20;
	public static final String EXTRA_PIC = "pic";

	private PictureView mPictureView;
	private ImageView mShowingView;
	private Pic mPic;

	@Override
	public void show(BaseFragmentActivity activity) {
		if (activity == null) {
			return;
		}
		activity.showFragment(this);
	}

	@Override
	public boolean onBackPressed() {
		return false;
	}


	private static class MediasModel {
		final ArrayList<MediaModel> mMedias = new ArrayList<>();
		int mIndex = 0;

		private MediasModel() {

		}

		public void add(MediaModel mediaModel) {
			mMedias.add(mediaModel);
		}

		public MediaModel get(int i) {
			return mMedias.get(i);
		}

		public int count() {
			return mMedias.size();
		}
	}
	private MediasModel mMedias;
	private MediaModel mCurrMedia;
	private Picture mNextPicture;

	protected Handler mHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();

		mMedias = new MediasModel();
		mPic = args.getParcelable(EXTRA_PIC);
		for (int i = 0; i < mPic.mUrls.size(); i++) {

			mMedias.add(new MediaModel(mPic.mUrls.get(i)));
		}


		mCurrMedia = mMedias.get(0);
		mHandler = new Handler(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_picture, container, false);

//		mHandler.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				if (getBaseActivity() != null)
//					getBaseActivity().showBars(false);
//			}
//		}, 300);
		mPictureView = (PictureView) rootView.findViewById(R.id.image);
		mPictureView.setListener(mPictureListener);
		mPictureView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
//				mActivity.cancelDelayHideBars();
//				if (mState == STATE_RUNNING)
//					mActivity.showBars(!hasFocus);
			}
		});

		mShowingView = (ImageView) rootView.findViewById(R.id.showing);

		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();


		final Picture picture = loadThumbnail(mCurrMedia, true);
		setPicture(picture, true);
		if (picture != null)
			picture.release();

		//	if not resume mode, check long delay because it is animating without hardware acceleration
		mHandler.sendEmptyMessageDelayed(MSG_REQUEST_PICTURES, 20);
	}


	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case PictureLoader.MSG_PICTURE_LOADED:
				if (msg.obj instanceof Picture) {
					final Picture picture = (Picture) msg.obj;
					onPictureLoaded(picture);
					picture.release();
				}
				break;

			case MSG_REQUEST_PICTURES:
				requestPictures();
				break;
		}
		return true;
	}

	private PictureLoader.LoadTask mCurrThumbTask;
	private PictureLoader.LoadTask mFullPicTask;
	private PictureLoader.LoadTask mNextThumbTask;

	private void cancelTasks() {
		if (mFullPicTask != null) {
			mFullPicTask.cancel(false);
			mFullPicTask = null;
		}
		if (mCurrThumbTask != null) {
			mCurrThumbTask.cancel(false);
			mCurrThumbTask = null;
		}
		if (mNextThumbTask != null) {
			mNextThumbTask.cancel(false);
			mNextThumbTask = null;
		}
		mHandler.removeMessages(MSG_REQUEST_PICTURES);
	}

	private Picture loadThumbnail(MediaModel media, boolean fast) {
		final Uri uri = media.getUri();
		final PictureLoader picLoader = JandanApp.mPictureLoader;
		Picture picture = picLoader.getFromCache(uri);
		if (picture != null)
			return picture;

		if (mCurrThumbTask != null && mCurrThumbTask.isHandlingUri(uri)) {
			picture = mCurrThumbTask.getPicture();
			//	Log.d("PictureInteraction", "get curr: " + picture);
			return picture;
		} else if (mNextThumbTask != null && mNextThumbTask.isHandlingUri(uri)) {
			picture = mNextThumbTask.getPicture();
			//	Log.d("PictureInteraction", "get next: " + picture);
			//	if (picture != null)
			return picture;
		}

		if (fast) {
			cancelTasks();    //	Cancel pending workers to save CPU
			mCurrThumbTask = picLoader.request(media, PictureLoader.REQUEST_THUMB, mHandler);
			picture = mCurrThumbTask.getPicture();
		} else {
			if (mNextThumbTask != null)
				mNextThumbTask.cancel(false);
			mNextThumbTask = picLoader.request(media, PictureLoader.REQUEST_THUMB, mHandler);
			picture = mNextThumbTask.getPicture();
		}
		return picture;
	}

	private void requestPictures() {
		requestCurrThumbnail();

		final Uri uri = mCurrMedia.getUri();
		if (mPictureView.isFullPicture() || !needRequestFull()
				|| PictureLoader.canRegionDecode(JandanApp.mPictureLoader.getPictureInfo(uri)))    //	Don't load next if load full because of memory usage
			requestNextThumbnail();

		requestFullPicture();
	}

	private void requestCurrThumbnail() {
		final Uri uri = mCurrMedia.getUri();

		if (mCurrThumbTask != null && !mCurrThumbTask.isHandlingUri(uri)) {
			mCurrThumbTask.cancel(false);
			mCurrThumbTask = null;
		}

		final int ptype = mPictureView.getPictureType();
		if (ptype == Picture.TYPE_BLANK) {
			final PictureLoader picLoader = JandanApp.mPictureLoader;
			final Picture picture = picLoader.getFromCache(uri);
			if (picture != null) {
				setPicture(picture, true);
				picture.release();
			} else if (mCurrThumbTask == null) {
				//	Log.d("PictureInteraction", "request current thumbnail: " + mCurrUri);
				mCurrThumbTask = picLoader.request(mCurrMedia, PictureLoader.REQUEST_THUMB, mHandler);
			}
		}
	}

	private void requestNextThumbnail() {
		final int ptype = mPictureView.getPictureType();
		if (mMedias != null && (ptype == Picture.TYPE_ERROR || ptype >= Picture.TYPE_THUMB)) {
			final int nextIndex = getNextIndex(mMedias.mIndex, mForward);
			if (nextIndex != mMedias.mIndex) {
				final MediaModel media2 = mMedias.get(nextIndex);
				final Uri uri2 = media2 != null ? media2.getUri() : Uri.EMPTY;
				if (mNextThumbTask != null && !mNextThumbTask.isHandlingUri(uri2)) {
					mNextThumbTask.cancel(false);
					mNextThumbTask = null;
				}

				final PictureLoader picLoader = JandanApp.mPictureLoader;
				if (mNextThumbTask == null && media2 != null && !picLoader.touchCache(uri2)) {
					//	Log.d("PictureInteraction", "request next thumbnail: " + uri2);
					mNextThumbTask = picLoader.request(media2, PictureLoader.REQUEST_THUMB, mHandler);
				}
			}
		}
	}

	private boolean needRequestFull() {
		final int ptype = mPictureView.getPictureType();
		return (ptype == Picture.TYPE_THUMB) || "image/gif".equals(mPictureView.getMimeType());
	}

	private void requestFullPicture() {
		final Uri uri = mCurrMedia.getUri();

		if (mFullPicTask != null && !mFullPicTask.isHandlingUri(uri)) {
			mFullPicTask.cancel(false);
			mFullPicTask = null;
		}

		if (mFullPicTask == null && needRequestFull()) {
			final PictureLoader picLoader = JandanApp.mPictureLoader;
			//	Log.d("PictureInteraction", "request current full: " + mCurrUri);
			mFullPicTask = picLoader.request(mCurrMedia, PictureLoader.REQUEST_FULL, mHandler);
		}
	}

	protected void onPictureLoaded(Picture picture) {
		final Uri uri = mCurrMedia.getUri();
		//	Log.i("PictureInteraction", "picture loaded: " + picture);

		if (picture.equalsUri(uri)) {
			setPicture(picture, false);
		} else if (mMedias != null) {
			final Uri uri2 = mNextPicture != null ? mNextPicture.mUri : mMedias.get(getNextIndex(mMedias.mIndex, mForward)).getUri();
			if (picture.equalsUri(uri2)) {
				if (mNextPicture != null && mNextPicture.mType < picture.mType) {
					mNextPicture.release();
					mNextPicture = null;
				}
				if (mNextPicture == null) {
					mNextPicture = picture.addRef();
					mPictureView.invalidate();
				}
			}
		}

		if (!mHandler.hasMessages(MSG_REQUEST_PICTURES))
			mHandler.sendEmptyMessageDelayed(MSG_REQUEST_PICTURES, 20);
	}

	private boolean mForward = true;

	private boolean mFirstZoom;

	private final PictureView.OnListener mPictureListener = new PictureView.OnListener() {
		public boolean hasNextPicture(int dir) {
			if (mMedias != null) {
				final boolean forward = dir > 0;
				final int index = getNextIndex(mMedias.mIndex, forward);
				//	Log.d("PictureInteraction", "has next:" + mFolder.mIndex + " -> " + index);
				return forward ? index > mMedias.mIndex : index < mMedias.mIndex;
			}
			return false;
		}

		public Picture getNextPicture(int dir, boolean fast) {
			if (mMedias != null) {
				mForward = dir > 0;
				final int index = getNextIndex(mMedias.mIndex, mForward);
				final MediaModel media2 = mMedias.get(index);
				final Uri uri2 = media2 != null ? media2.getUri() : Uri.EMPTY;
				if (mNextPicture != null) {
					if (/*mNextPicture.mType == Picture.TYPE_BLANK && */mNextPicture.equalsUri(uri2))
						return mNextPicture.addRef();
					mNextPicture.release();
					mNextPicture = null;
				}
				if (media2 != null)
					mNextPicture = loadThumbnail(media2, fast);
				//	Log.d("PictureInteraction", "load next: " + mFolder.mIndex + " -> " + index + ", " + mNextPicture);
				return mNextPicture != null ? mNextPicture.addRef() : null;
			}
			return null;
		}

		public void onMoveToNext(int dir) {
			if (mMedias != null) {
				//	Log.d("PictureInteraction", "switch pictures: " + mCurrUri + " -> " + mNextUri + ", dir=" + dir);
				mForward = dir > 0;
				mMedias.mIndex = getNextIndex(mMedias.mIndex, mForward);
				mCurrMedia = mMedias.get(mMedias.mIndex);

				final Uri uri = mCurrMedia.getUri();
				Picture picture;
				if (mNextPicture != null && mNextPicture.equalsUri(uri))
					picture = mNextPicture.addRef();
				else
					picture = JandanApp.mPictureLoader.getFromCache(uri);
				setPicture(picture, true);
				if (picture != null)
					picture.release();

				if (mNextPicture != null) {
					mNextPicture.release();
					mNextPicture = null;
				}

				if (!mHandler.hasMessages(MSG_REQUEST_PICTURES))
					mHandler.sendEmptyMessageDelayed(MSG_REQUEST_PICTURES, REQUEST_PICTURES_DELAY);
			}
		}

		public void onSingleTap(float pivotX, float pivotY) {
//			if (mActivity.mAutoHideNaviBar && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
//				//	Hack: check first single tap is eaten by Android system
//				if (System.currentTimeMillis() < mActivity.mShowNaviBarTime + 500) {
//					onDoubleTap(pivotX, pivotY);
//					return;
//				}
//			}
			// mActivity.toggleShowBars();
		}

		public void onDoubleTap(float pivotX, float pivotY) {
			changeScaleMode(pivotX, pivotY);
		}

		public void onZoom(float scaleBy, boolean fromUser) {
			if (mFirstZoom && fromUser && scaleBy > 1 && mPictureView.getPictureType() == Picture.TYPE_THUMB) {
				mFirstZoom = false;
				if (mFullPicTask == null)
					mFullPicTask = JandanApp.mPictureLoader.request(mCurrMedia, PictureLoader.REQUEST_FULL, mHandler);
			}

		}

		private void changeScaleMode(float pivotX, float pivotY) {
			final float scaleBy = mPictureView.changeScaleMode(pivotX, pivotY);
			onZoom(scaleBy, true);
		}
	};

	protected int getNextIndex(int index, boolean forward) {
		final int count = mMedias.count();
		if (forward) {
			if (++index >= count)
				index = 0;
		} else {
			if (--index < 0)
				index = count - 1;
		}
		return index;
	}


	protected void setPicture(Picture picture, boolean reset) {
		final Uri uri = mCurrMedia.getUri();

		if (picture != null && picture.isError()
				&& mMedias == null
				&& picture.equalsUri(uri)) {
			//	Can't load image (maybe because of permission)
//			String mtype = mIntent.getType();
//			if (mtype == null || mtype.length() == 0)
//				mtype = "image/*";
//			picture.release();
//			finish();
			return;
		}

		//	final boolean isEmpty = mPictureView.mPicture == null;
		//	mPictureView.clearAnimation();
		mPictureView.setPicture(picture, reset);
		mPictureView.start();
		mFirstZoom = reset;
		//	if (isEmpty && picture != null && ! isSlideShowing() && mPictureView.getWidth() > 0)
		//		mPictureView.startAnimation(getFadeAnimation(true));	//	Will flicker if be called in switchPictures()
	}
}
