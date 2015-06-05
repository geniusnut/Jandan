//package com.nut.dao;
//
//import android.content.ContentResolver;
//import android.content.Context;
//import android.net.Uri;
//import android.os.Handler;
//import android.os.Parcelable;
//import android.util.Log;
//import com.nut.Jandan.model.MediaModel;
//import com.nut.cache.FileCache;
//import com.nut.thread.AsyncExecutor;
//import com.nut.thread.BaseThread;
//import com.nut.thread.FutureTaskEx;
//import com.nut.thread.Task;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//
///**
// * Created by yw07 on 15-6-5.
// */
//public class PictureLoader {
//	public static final int REQUEST_THUMB = 1;  //	thumbnail
//	public static final int REQUEST_FULL = 2;   //	full image
//
//	private final Context mContext;
//	private final FileCache mCache;
//
//	private final AsyncExecutor mLoadExecutor = new AsyncExecutor(3, 4, 8, BaseThread.THREAD_PRIORITY_LOAD);
//
//	public PictureLoader(Context context) {
//		mContext = context;
//		mCache = new FileCache(context);
//	}
//
//	public LoadTask request(MediaModel media, int reqMode, Handler handler) {
//		final Loader loader = new Loader(media, reqMode, handler);
//		final LoadTask atask = new LoadTask(loader);
//		mLoadExecutor.submit(atask);
//		return atask;
//	}
//
//	public class LoadTask extends FutureTaskEx<Void> {
//		private Loader mLoader;
//
//		public LoadTask(Loader loader) {
//			super(loader);
//			mLoader = loader;
//		}
//
//		public final boolean equalsUri(Uri uri) {
//			final Uri uri1 = mLoader.mMedia.getUri();
//			return uri == uri1 || uri.equals(uri1);
//		}
//
//	/*	public final boolean isDecodingUri(Uri uri) {
//			return (mLoader.isRunning() || isDone()) && equalsUri(uri);
//		}*/
//
//		public final boolean isHandlingUri(Uri uri) {
//			return !isCancelled() && equalsUri(uri);
//		}
//
//		public Picture getPicture() {
//			Picture picture = isDone() ? getFromCache(mLoader.mMedia.getUri()) : null;
//			if (picture != null)
//				return picture;
//
//			picture = mLoader.mBlankPic.get();
//			return picture != null ? picture.addRef() : null;
//		}
//
//		@Override
//		public String toString() {
//			return mLoader.toString();
//		}
//	}
//
//	private class Loader implements Task<Void> {
//		public final MediaModel mMedia;
//		public final int mReqMode;
//		private final Handler mHandler;
//		private final Uri mUri;
//		private final String mLocalFile;
//		private boolean mCanRegionLoad;
//		private long mLastModified; // in seconds
//		private BitmapOptions mOptions;
//
//		public Loader(MediaModel media, int reqMode, Handler handler) {
//			mMedia = media;
//			mReqMode = reqMode;
//			mHandler = handler;
//
//			final String localFile = media.getLocalPath();
//			mUri = media.getUri();
//			mLocalFile = localFile != null ? localFile : (ParcelFile.isLocal(mUri) ? mUri.getPath() : null);
//		}
//
//		@Override
//		public void cancel() {
//
//		}
//
//		@Override
//		public void done() {
//
//		}
//
//		private ParcelFile openFile() {
//			if (mLocalFile != null) try {
//				return ParcelFile.openFile(new File(mLocalFile), true);
//			} catch (FileNotFoundException e) {
//				// the local file may changed
//				Log.w("PictureLoader", "open failed: " + mUri);
//			} catch (Throwable e) {
//				mOptions.outWidth = mOptions.outHeight = -1;
//				Log.e("PictureLoader", "open failed: " + mUri, e);
//				return null;
//			}
//
//			// try open the uri
//			try {
//				return ParcelFile.openFile(mMedia.openFile(), true);
//			} catch (CancelledException e) {
//				Log.w("PictureLoader", "open cancelled: " + mUri);
//				// don't set a blank picture, so can reload it
//			} catch (Throwable e) {
//				mOptions.outWidth = mOptions.outHeight = -1;
//				setBlankPicture(true);
//				Log.e("PictureLoader", "open failed: " + mUri, e);
//			}
//
//			return null;
//		}
//
//		private void sendPicture(Picture picture) {
//			if (mHandler != null && picture != null)
//				mHandler.obtainMessage(MSG_PICTURE_LOADED, 0, 0, picture.addRef()).sendToTarget();
//		}
//
//		@Override
//		public Void call() throws Exception {
//
//			ParcelFile file = openFile();
//
//			if (mReqMode <= REQUEST_THUMB)
//				picture = loadThumbnail(pfd, uri);
//			else if (mReqMode == REQUEST_FULL)
//				picture = loadPicture(pfd, uri);
//			return null;
//		}
//	}
//
//}
