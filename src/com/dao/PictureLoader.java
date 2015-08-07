package com.dao;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.util.FloatMath;
import android.util.Log;
import com.alensw.support.cache.ConcurrentLruHashMap;
import com.alensw.support.file.ParcelFile;
import com.alensw.support.picture.*;
import com.alensw.support.thread.AsyncExecutor;
import com.alensw.support.thread.BaseThread;
import com.alensw.support.thread.FutureTaskEx;
import com.alensw.support.thread.Task;
import com.alensw.view.GifMovie;
import com.nut.Jandan.JandanApp;
import com.nut.Jandan.model.MediaModel;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class PictureLoader {
	public static final int MSG_PICTURE_LOADED = 30;

	public static final int REQUEST_THUMB = 1;  //	thumbnail
	public static final int REQUEST_SHOW = 2;   //	slide show
	public static final int REQUEST_FULL = 3;   //	full image
	public static final int REQUEST_NATIVE = 4; //	crop image

	public static final int MAX_CACHES = 3;

	public static final int DB_VERSION = 1;

	public boolean mCacheThumb = true;

	protected int mScreenPixels;

	private int mThumbSide;
	private int mThumbPixels;
	private int mThumbMaxPixels;
	private int mShowSide;
	private int mShowMaxPixels;
	private final Context mContext;
	private final ContentResolver mResolver;
	//private final FileCache mCache;

	private final AsyncExecutor mLoadExecutor = new AsyncExecutor(3, 4, 8, BaseThread.THREAD_PRIORITY_LOAD);
	private final AsyncExecutor mSaveExecutor = new AsyncExecutor(1, 1, 8, BaseThread.THREAD_PRIORITY_SAVE);
	private final ConcurrentLinkedQueue<Loader> mRunningLoaders = new ConcurrentLinkedQueue<Loader>();
	private final ConcurrentLruHashMap<Uri, PictureInfo> mInfoCache = new ConcurrentLruHashMap<Uri, PictureInfo>(MAX_CACHES);

	private final ConcurrentLruHashMap<Uri, Picture> mPictureCache = new ConcurrentLruHashMap<Uri, Picture>(MAX_CACHES) {
		protected void discard(Picture picture) {
			picture.release();
			//	Log.d("PictureLoader", "discard picture: " + picture);
		}
	};

	public PictureLoader(Context context) {
		mContext = context;
		mResolver = context.getContentResolver();

//		mCache = new FileCache();
//		mCache.setMaxCacheSize(0);

		mScreenPixels = JandanApp.mScreenMaxSidePX * JandanApp.mScreenMinSidePX;

		final int maxSide = JandanApp.mScreenMaxSidePX;
		int side = 512;
		if (maxSide <= 320)
			side = 320;
		else if (maxSide <= 480)
			side = 480;
		else if (maxSide <= 960)
			side = 512;
		else
			side = Math.min(maxSide / 2, 1024);
		mThumbSide = side;
		mThumbPixels = mThumbSide * (mThumbSide * 3 / 4);
		mThumbMaxPixels = mThumbPixels * 9 / 8;

		mShowSide = Math.max(JandanApp.mScreenMaxSidePX, JandanApp.mScreenMinSidePX);
		mShowMaxPixels = mScreenPixels * 9 / 8;
	}

	private synchronized boolean checkOpenCache() {
//		if (mCache.isOpen())
//			return true;
//		return mCache.open(mContext, ExternalFile.getExternalCacheFile(mContext, "preview"), "preview", DB_VERSION);
		return false;
	}

	public void start(Context context) {
	}

	public void quit() {
//		if (mCache.isOpen())
//			mSaveExecutor.submit(new Runnable() {
//				@Override
//				public void run() {
//					mCache.close();
//				}
//			});
	}

	public void addToCache(Picture picture) {
		mPictureCache.put(picture.mUri, picture.addRef());
	}

	public void clearCache() {
		mPictureCache.clear();
		mInfoCache.clear();
	}

	public void clearCacheFromStorage() {
		//QuickApp.mMediaStore.clearThumbnails();
//		if (checkOpenCache())
//			mCache.clearCache();
	}

	public Picture getFromCache(Uri uri) {
		final Picture picture = mPictureCache.get(uri);
		return picture != null ? picture.addRef() : null;
	}

	public PictureInfo getPictureInfo(Uri uri) {
		return mInfoCache.get(uri);
	}

	public boolean touchCache(Uri uri) {
		return mPictureCache.get(uri) != null;
	}

	public void removeCache(Uri uri, boolean deleteCache) {
		mPictureCache.remove(uri);
		mInfoCache.remove(uri);

//		if (deleteCache && checkOpenCache())
//			mCache.deleteCache(uri.toString());
	}

	private Picture mFullPicture;

	public boolean canFastLoad(ParcelFile pfd, BitmapOptions opts) {
		if (opts.outWidth > 0 && opts.outHeight > 0
				&& opts.outWidth * opts.outHeight > mThumbMaxPixels)
			return false;

		final long MIN_BYTES = 512 * 1024;
		final long bytes = pfd.getStatSize();
		return bytes > 0 && bytes < MIN_BYTES;
	}

	public static final int MIN_REGION_PIXELS_JPG = 1280 * 960;
	public static final int MIN_REGION_PIXELS_PNG = 2560 * 1600; // The bottom part of screen shots decode failed?

	public static boolean canRegionDecode(PictureInfo picInfo) {
		if (!TilePicture.HAS_REGION_DECODER)
			return false;

		if (picInfo == null)
			return false;

		//  Don't region decode small images to work around bugs of the Region Decoder
		final int pixels = picInfo.mWidth * picInfo.mHeight;
		if ("image/jpeg".equals(picInfo.mMimeType))
			return picInfo.mValidRegion && pixels > MIN_REGION_PIXELS_JPG;
		else if ("image/png".equals(picInfo.mMimeType))
			return pixels > MIN_REGION_PIXELS_PNG;
		return false;
	}

	private Bitmap checkScaledBitmap(Bitmap bitmap, int targetPixels, int maxPixels) {
		if (bitmap == null)
			return null;

		final int width = bitmap.getWidth();
		final int height = bitmap.getHeight();
		final int pixels = width * height;
		if (pixels > maxPixels) {
			//	check not big than 4096, can not draw with hardware canvas
			float scale = FloatMath.sqrt((float) targetPixels / pixels);
			final float scaleX = Math.round(width * scale) / 4096f;
			final float scaleY = Math.round(height * scale) / 4096f;
			if (scaleX > 1f || scaleY > 1f)
				scale /= Math.max(scaleX, scaleY);

			final Bitmap bmpNew = BitmapUtils.createScaledBitmap(bitmap, scale);
			if (bmpNew != null) {
				bitmap.recycle();
				bitmap = bmpNew;
			}
		}
		return bitmap;
	}

	private final Object mReservedLock = new Object();
	private Bitmap mReservedBitmap;

	private void allocReservedMemory() {
		synchronized (mReservedLock) {
			if (mReservedBitmap == null)
				mReservedBitmap = BitmapUtils.create(mScreenPixels / 16, 32, Bitmap.Config.ARGB_8888);
		}
	}

	private void freeReservedMemory() {
		synchronized (mReservedLock) {
			if (mReservedBitmap != null) {
				mReservedBitmap.recycle();
				mReservedBitmap = null;
			}
		}
	}

	public void execute(Runnable r) {
		mLoadExecutor.execute(r);
	}

	public LoadTask request(MediaModel media, int reqMode, Handler handler) {
		final Loader loader = new Loader(media, reqMode, handler);
		final LoadTask atask = new LoadTask(loader);
		mLoadExecutor.submit(atask);
		return atask;
	}

	private class SaveState {
		boolean updating;
	}

	private final ConcurrentHashMap<Uri, SaveState> mUpdatingIDs = new ConcurrentHashMap<Uri, SaveState>(8);

	private boolean isUpdatingThumbnail(Uri uri) {
		final SaveState state = mUpdatingIDs.get(uri);
		return state != null && state.updating;
	}

//	private void requestSaveToCache(final Uri uri, final String localFile, final long lastModified) {
//		final SaveState state = new SaveState();
//
//		if (mUpdatingIDs.contains(uri)) {
//			Log.w("PictureLoader", "saving thumbnail: " + uri);
//			return;
//		}
//		mUpdatingIDs.put(uri, state);
//
//		final Task<Void> task = new Task<Void>() {
//			public Void call() {
//				final Picture picture = getFromCache(uri);
//				if (picture != null) {
//					final boolean hasBitmap = picture.hasBitmap();
//					if (hasBitmap && !isUpdatingThumbnail(uri)) {
//						state.updating = true;
//						final File cacheFile = mCache.generateCacheFile();
//						final String path = cacheFile.getPath();
//						if (SaveThumbnailToCacheFile(path, picture)) {
//							final String key = localFile != null ? ContentResolver.SCHEME_FILE + "://" + localFile : uri.toString();
//							mCache.updateCache(key, lastModified, cacheFile);
//						}
//						state.updating = false;
//					}
//					if (hasBitmap && localFile == null) {
//						// update the micro thumbnail if the picture from cloud,
////						// because the default thumbnail from cloud maybe not so good
////						final ThumbLibrary library = QuickApp.mLibrary;
////						final Image image = picture.extractThumbnail(library.mThumbWidth, library.mThumbHeight, library.mCropThumb, ThumbLibrary.PREFER_CONFIG);
////						if (image != null) {
////							library.update(uri.toString(), lastModified, image);
////							image.release();
////						}
//					}
//					picture.release();
//					//	Log.d("PictureLoader", "save thumbnail: " + picture);
//				}
//				return null;
//			}
//
//			public void cancel() {
//				mUpdatingIDs.remove(uri);
//			}
//
//			public void done() {
//				mUpdatingIDs.remove(uri);
//			}
//		};
//		mSaveExecutor.submit(task);
//	}

	private static Bitmap loadThumbnailFromCacheFile(String path, BitmapOptions opts, int minPixels) {
		Bitmap bitmap = null;
		try {
			final ParcelFile pfd = ParcelFile.openFile(new File(path), true);
			opts.initForThumbnail();
			bitmap = BitmapFactory.decodeFileDescriptor(pfd.getDescriptor(), null, opts);
			//	Log.d("PictureLoader", "load thumbnail: " + file + ", " + bitmap);
			pfd.close();
		} catch (FileNotFoundException e) {
			//	ignore
		} catch (Throwable e) {
			Log.e("PictureLoader", "load thumbnail: " + path, e);
		}
		if (bitmap != null && minPixels > 0 && opts.outWidth * opts.outHeight < minPixels) {
			// bitmap is too small, discard it
			bitmap.recycle();
			bitmap = null;
		}
		return bitmap;
	}

	private static boolean SaveThumbnailToCacheFile(String path, Picture picture) {
		boolean result = false;
		try {
			final FileOutputStream stream = new FileOutputStream(path);
			final BufferedOutputStream bos = new BufferedOutputStream(stream);
			final boolean png = picture.hasAlpha() && "image/png".equals(picture.mMimeType);
			result = picture.getBitmap().compress(png ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 80, bos);
			bos.close();
			stream.close();
			//	Log.d("PictureLoader", "save thumbnail: " + path + ", " + result);// + ", size=" + bitmap.getWidth() + "x" + bitmap.getHeight());
		} catch (Throwable e) {
			Log.e("PictureLoader", "save thumbnail: " + path + ", " + e);
		}
		return result;
	}

	public class LoadTask extends FutureTaskEx<Void> {
		private Loader mLoader;

		public LoadTask(Loader loader) {
			super(loader);
			mLoader = loader;
		}

		public final boolean equalsUri(Uri uri) {
			final Uri uri1 = mLoader.mMedia.getUri();
			return uri == uri1 || uri.equals(uri1);
		}

	/*	public final boolean isDecodingUri(Uri uri) {
			return (mLoader.isRunning() || isDone()) && equalsUri(uri);
		}*/

		public final boolean isHandlingUri(Uri uri) {
			return !isCancelled() && equalsUri(uri);
		}

		public Picture getPicture() {
			Picture picture = isDone() ? getFromCache(mLoader.mMedia.getUri()) : null;
			if (picture != null)
				return picture;

			picture = mLoader.mBlankPic.get();
			return picture != null ? picture.addRef() : null;
		}

		@Override
		public String toString() {
			return mLoader.toString();
		}
	}

	private class Loader implements Task<Void> {
		public final MediaModel mMedia;
		public final int mReqMode;
		private final Handler mHandler;
		private final Uri mUri;
		private final String mLocalFile;
		private boolean mCanRegionLoad;
		private long mLastModified; // in seconds
		private BitmapOptions mOptions;

		private final AtomicReference<Picture> mBlankPic = new AtomicReference<Picture>();

		public Loader(MediaModel media, int reqMode, Handler handler) {
			mMedia = media;
			mReqMode = reqMode;
			mHandler = handler;

			final String localFile = media.getLocalPath();
			mUri = media.getUri();
			mLocalFile = localFile != null ? localFile : (ParcelFile.isLocal(mUri) ? mUri.getPath() : null);
		}

		private ParcelFile openFile() {
			// try open the local file if valid
			if (mLocalFile != null) try {
				return ParcelFile.openFile(new File(mLocalFile), true);
			} catch (FileNotFoundException e) {
				// the local file may changed
				Log.w("PictureLoader", "open failed: " + mUri);
			} catch (Throwable e) {
				mOptions.outWidth = mOptions.outHeight = -1;
				Log.e("PictureLoader", "open failed: " + mUri, e);
				return null;
			}

			// try open the uri
			try {
				return ParcelFile.openFile(mMedia.openFile(), true);
			} catch (Throwable e) {
				mOptions.outWidth = mOptions.outHeight = -1;
				setBlankPicture(true);
				Log.e("PictureLoader", "open failed: " + mUri, e);
			}
			return null;
		}

		private ParcelFile prepare() {
			final BitmapOptions opts = mOptions;

			PictureInfo info = mInfoCache.get(mUri);

			final boolean cachedInfo = info != null;
			if (info != null) {
				opts.outWidth = info.mWidth;
				opts.outHeight = info.mHeight;
				opts.outComponents = info.mComponents;
				opts.outRotation = info.mRotation;
				opts.outValidRegion = info.mValidRegion;
				opts.outMimeType = info.mMimeType;
				setBlankPicture(true);
			} else {
				info = new PictureInfo();
				opts.outRotation = BitmapOptions.REQUEST_ROTATION;
			}

			final ParcelFile pfd = openFile();
			if (pfd == null)
				return null;

			if (mLastModified == 0 && mLocalFile != null)
				mLastModified = pfd.getLastModified() / 1000;

			if (opts.outRotation == BitmapOptions.REQUEST_ROTATION && !opts.mCancel) {

				opts.outRotation = 0;
				//	Log.i("PictureLoader", "open exif failed: " + mUri)}
			}
			if ((opts.outWidth <= 0 || opts.outHeight <= 0) && !opts.mCancel) {
				if (BitmapUtils.getBitmapSize(pfd, opts)) {
					info.mWidth = opts.outWidth;
					info.mHeight = opts.outHeight;
					info.mRotation = 0;
					info.mValidRegion = "image/jpeg".equals(opts.outMimeType) || "image/png".equals(opts.outMimeType);
					info.mMimeType = opts.outMimeType;
					//	Log.d("PictureLoader", "open size: " + mUri + ", " + opts);
					setBlankPicture(true);
				} else {
					opts.outWidth = opts.outHeight = -1;
					setBlankPicture(true);
					//	Log.e("PictureLoader", "get size failed: " + mUri);
					return null;
				}
			}

			if (!cachedInfo)
				mInfoCache.put(mUri, info);

			if (mReqMode == REQUEST_FULL || mReqMode == REQUEST_NATIVE)
				mCanRegionLoad = canRegionDecode(info);

			if (mReqMode == REQUEST_NATIVE)
				opts.setNativeAlloc(true);

			return pfd;
		}

		private Picture loadThumbnail(ParcelFile pfd, Uri uri) {
			final Picture pic0 = mPictureCache.get(uri);
			if (pic0 != null) {
				//	Log.d("PictureLoader", "load from cache: " + pic0);
				return pic0.addRef();
			}
			//	Discard the oldest item for this new one
			mPictureCache.trimToSize(MAX_CACHES - 1);

			checkOpenCache();
			freeReservedMemory();

			//	final boolean fast = false;//mReqMode == REQUEST_FAST;

			final String localFile = mLocalFile;
			final BitmapOptions opts = mOptions;
			final String mimeType = opts.outMimeType;
			final int rotation = opts.outRotation;
			final int width = opts.outWidth;
			final int height = opts.outHeight;
			final int minPixels = Math.min(width * height, mThumbPixels * 2 / 3);

			Bitmap bitmap = null;
			boolean fromFile = false;
			boolean sampled = false;
			boolean updating = false;

			//	Log.d("PictureLoader", "load thumbnail: " + uri + ", " + opts);

			//	2. load from our cache
//			if (mCacheThumb && bitmap == null && !(updating = isUpdatingThumbnail(uri)) && !opts.mCancel)
//				bitmap = loadThumbnailFromCache(uri, localFile, mLastModified, opts, minPixels);

			// restore the important info
			opts.outWidth = width;
			opts.outHeight = height;
			opts.outRotation = rotation;
			opts.outMimeType = mimeType;

			// 4. load from original file
			if (bitmap == null && !opts.mCancel/* && ! fast*/) {
				fromFile = true;
				sendPicture(mBlankPic.get());


				bitmap = BitmapUtils.loadThumbnail(pfd, false, mThumbSide, opts);
				sampled = opts.inSampleSize > 1 || "image/gif".equals(opts.outMimeType);//	Treat GIF as thumbnail, to be loaded as GifMovie later

			}

			if (bitmap == null) {
				//	Log.e("PictureLoader", "load thumbnail: " + uri + ", opts=" + opts);
				return null;
			}

			final Bitmap bmpNew = checkScaledBitmap(bitmap, mThumbPixels, mThumbMaxPixels);
			sampled |= bmpNew != bitmap;
			bitmap = bmpNew;

			final int ptype = ((fromFile && !sampled) ? Picture.TYPE_IMAGE : Picture.TYPE_THUMB);
			final Picture picture = new Picture(bitmap, ptype, uri, opts);
			//	Log.d("PictureLoader", "load thumbnail: " + picture);
			addToCache(picture);
			sendPicture(picture);

			return picture;
		}

		private Picture loadSlideShow(ParcelFile pfd, Uri uri) {
			final BitmapOptions opts = mOptions;

			Picture picture = null;
			if ("image/gif".equals(opts.outMimeType) && !opts.mCancel)    //	assume GIF is small enough
				picture = GifMovie.create(pfd, uri, mHandler);
			if (picture != null) {
				picture.mInfo.rotation = opts.outRotation;
				if (!opts.mCancel)
					sendPicture(picture);
				//	Log.d("PictureLoader", "load show: " + picture + ", opts=" + opts);
				return picture;
			} else if (opts.mCancel) {
				//	Log.w("PictureLoader", "load show: " + uri + ", opts=" + opts);
				return null;
			}

			//	Reserve scaled pixels memory
			allocReservedMemory();
			Bitmap bitmap = BitmapUtils.loadThumbnail(pfd, false, mShowSide, opts);
			freeReservedMemory();
			if (bitmap == null) {
				//	Log.e("PictureLoader", "load show: " + uri + ", opts=" + opts);
				return null;
			}

			bitmap = checkScaledBitmap(bitmap, mScreenPixels, mShowMaxPixels);

			picture = new Picture(bitmap, Picture.TYPE_THUMB, uri, opts);
			//	Log.d("PictureLoader", "load show: " + picture);
			sendPicture(picture);
			return picture;
		}

		private Picture loadPicture(ParcelFile pfd, Uri uri) {
			final BitmapOptions opts = mOptions;
			//	Log.d("PictureLoader", "load: " + uri + ", " + opts);

			Picture picture = null;
			if (mCanRegionLoad && !opts.mCancel)
				picture = TilePicture.create(pfd, uri, opts.outMimeType, mHandler);
			else if ("image/gif".equals(opts.outMimeType) && !opts.mCancel)
				picture = GifMovie.create(pfd, uri, mHandler);
			if (picture != null) {
				picture.mInfo.rotation = opts.outRotation;
				if (!opts.mCancel)    //	TODO: tile and gif can not be canceled by opts.requestCancelDecode()
					sendPicture(picture);
				//	Log.d("PictureLoader", "load picture: " + picture + ", opts=" + opts);
				return picture;
			} else if (opts.mCancel) {
				//	Log.w("PictureLoader", "load picture: " + uri + ", opts=" + opts);
				return null;
			}

			//	Reserve 2 screen pixels memory
			allocReservedMemory();
			Bitmap bitmap = BitmapUtils.loadBitmap(pfd, 0, opts);
			if (bitmap == null && !opts.mCancel && opts.isNativeAlloc()) {
				opts.setNativeAlloc(false);
				bitmap = BitmapUtils.loadBitmap(pfd, BitmapUtils.MIN_PICTURE_SIZE, opts);
			}
			freeReservedMemory();

			if (bitmap != null && opts.mCancel) {
				bitmap.recycle();    //	free the bitmap to release the large memory
				bitmap = null;
				//	Log.w("PictureLoader", "cancel bitmap: " + uri + ", opts=" + opts);
			}

			if (bitmap == null) {
				//	Log.e("PictureLoader", "load picture: " + uri + ", opts=" + opts);
				return null;
			}

			picture = new Picture(bitmap, Picture.TYPE_IMAGE, uri, opts);
			//	Log.d("PictureLoader", "load picture: " + picture);
			sendPicture(picture);
			return picture;
		}

		private void setBlankPicture(boolean set) {
			final Picture picture = set && mOptions.outWidth != 0 && mOptions.outHeight != 0 ? new Picture(mMedia.getUri(), mOptions) : null;
			final Picture prev = mBlankPic.getAndSet(picture);
			if (prev != null)
				prev.release();
		}

		private void sendPicture(Picture picture) {
			if (mHandler != null && picture != null)
				mHandler.obtainMessage(MSG_PICTURE_LOADED, 0, 0, picture.addRef()).sendToTarget();
		}

		public void recycle() {
			if (mLoaded)    //	don't release if not loaded, the blank picture is to be used
				setBlankPicture(false);
		}

		private final AtomicInteger mResource = new AtomicInteger(1);

		public int acquireResource() {
			return mResource.incrementAndGet();
		}

		public int releaseResource() {
			final int ret = mResource.decrementAndGet();
			if (ret == 0) {
				recycle();
				//	Log.d("PictureLoader", "recycle: " + mUri);
			}
			return ret;
		}

	/*	public boolean isRunning() {
			return mResource.get() > 1;
		}*/

		@Override
		public int hashCode() {
			return mMedia.hashCode() + mReqMode;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;

			if (obj instanceof Loader) {
				final Loader that = (Loader) obj;
				return mReqMode == that.mReqMode
						&& mHandler == that.mHandler
						&& mMedia.equals(that.mMedia);
			}
			return false;
		}

		@Override
		public void cancel() {
			if (mLocalFile == null) try {

			} catch (Throwable e) {
			}
			synchronized (this) {
				if (mOptions != null)
					mOptions.requestCancelDecode();
			}
			//	Log.d("PictureLoader", "cancel: " + this);
		}

		@Override
		public void done() {
			releaseResource();
		}

		private boolean mLoaded = false;

		@Override
		public Void call() {
			final boolean thumbnail = mReqMode < REQUEST_FULL;
			if (thumbnail && mRunningLoaders.contains(this)) {
				// if loading thumbnail and slideshow, the picture will be cached, so we don't load it again
				//	Log.w("PictureLoader", "running: " + mUri + ", " + mReqMode);
				return null;
			}

			acquireResource();
			if (thumbnail)
				mRunningLoaders.add(this);

			synchronized (this) {
				mOptions = BitmapOptions.obtain();
			}

			final Uri uri = mUri;
			final ParcelFile pfd = prepare();
			if (pfd == null) {
				if (!mOptions.mCancel)
					sendPicture(mBlankPic.get());
			} else if (!mOptions.mCancel) {
				Picture picture = null;
				if (mReqMode <= REQUEST_THUMB)
					picture = loadThumbnail(pfd, uri);
				else if (mReqMode == REQUEST_SHOW)
					picture = loadSlideShow(pfd, uri);
				else
					picture = loadPicture(pfd, uri);
				if (picture != null) {
					picture.release();
					mLoaded = true;
				}
			}

			if (pfd != null)
				pfd.close();

			synchronized (this) {
				BitmapOptions.recycle(mOptions);
				mOptions = null;
			}

			if (thumbnail)
				mRunningLoaders.remove(this);
			releaseResource();
			return null;
		}

		@Override
		public String toString() {
			return "uri=" + mMedia.getTitle() + ", req=" + mReqMode;
		}
	}
}
