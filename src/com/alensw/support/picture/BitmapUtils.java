package com.alensw.support.picture;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import com.alensw.support.file.ParcelFile;

import java.lang.reflect.Method;

public class BitmapUtils {
	public static final int THUMB_PIXELS_MICRO = 256 * 192;
	public static final int THUMB_PIXELS_MINI = 512 * 384;    //	Android's older gallery
	public static final int THUMB_PIXELS_MAX = 960 * 600;    //	1920x1200/4
	public static final int MIN_PICTURE_SIZE = 2048 * 1536;

	public static final int MAX_SAMPLE_SIZE = 32;

	public static boolean mTrueColorBitmap = false;

	public static int MEMORY_CLASS = 0;
	public static int MAX_PIXELS_16 = 0;
	public static int MAX_PIXELS_32 = 0;

	public static void init(Context context) {
		final ActivityManager mgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		MEMORY_CLASS = mgr.getMemoryClass();
		MAX_PIXELS_16 = (int) (Math.max(MEMORY_CLASS / 3.0f, 4.0f) * 1000000);    //	2/3 memory
		MAX_PIXELS_32 = (int) (Math.max(MEMORY_CLASS / 8.0f, MEMORY_CLASS < 24 ? 2.0f : 3.2f) * 1000000);    //	1/2 memory
		BitmapOptions.PREFER_CONFIG = MEMORY_CLASS >= 32 ? Config.ARGB_8888 : Config.RGB_565;

		// grow heap
	/*	final int growMB = Math.min(24, MEMORY_CLASS / 2);
		final int width = 8;
		final int height = growMB * 1024 * 1024 / width / 4;
		final Bitmap bitmap = create(width, height, Bitmap.Config.ARGB_8888);
		if (bitmap != null)
			bitmap.recycle();*/
	}

	public static Bitmap create(int width, int height, Config config) {
		try {
			return Bitmap.createBitmap(width, height, config != null ? config : BitmapOptions.PREFER_CONFIG);
		} catch (OutOfMemoryError e) {
			if (config == null && BitmapOptions.PREFER_CONFIG == Config.ARGB_8888)
				return create(width, height, Config.RGB_565);
			Log.e("BitmapUtils", "create bitmap: " + e);
		} catch (Throwable e) {
			Log.e("BitmapUtils", "create bitmap: " + e);
		}
		return null;
	}

	public static Bitmap createScaledBitmap(Bitmap bitmap, float scale) {
		Bitmap newBmp = null;
		Config config = BitmapOptions.PREFER_CONFIG;
		while (newBmp == null) {
			try {
				final int width = bitmap.getWidth();
				final int height = bitmap.getHeight();
				final int outWidth = Math.round(width * scale);
				final int outHeight = Math.round(height * scale);
				newBmp = Bitmap.createBitmap(outWidth, outHeight, config);
				final Canvas canvas = new Canvas(newBmp);
				canvas.scale(scale, scale);
				canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG));
				//	Log.d("BitmapUtils", "create scaled: " + width + "x" + height + " -> " + outWidth + "x" + outHeight);
				return newBmp;
			} catch (OutOfMemoryError e) {
				if (config == Config.ARGB_8888) {
					config = Config.RGB_565;
					continue;
				}
			} catch (Throwable e) {
				Log.e("BitmapUtils", "create scaled: " + e);
			}
			break;
		}
		return null;
	}

	/*	public static boolean getBitmapInfo(ParcelFile pfd, BitmapOptions opts) {
			if (opts.outRotation == BitmapOptions.REQUEST_ROTATION) {
				final ExifParser exif = new ExifParser();
				if (exif.openFile(pfd)) {
					final long size = exif.getImageSize();
					opts.outWidth = (int)(size & 0x7fffffff);
					opts.outHeight = (int)((size >> 32) & 0x7fffffff);
					opts.outComponents = exif.getComponents();
					opts.outRotation = exif.getRotation();
					opts.outMimeType = "image/jpeg";
					exif.close();
					return true;
				}
			}
			return BitmapUtils.getBitmapSize(pfd, opts);
		}
	*/
	public static Bitmap decodeParcelFile(ParcelFile pfd, BitmapOptions opts) throws Throwable {
		Bitmap bitmap = null;
		if (!opts.outFDFailed) {
			bitmap = BitmapFactory.decodeFileDescriptor(pfd.getDescriptor(), null, opts);
			if (bitmap == null && !opts.inJustDecodeBounds && !opts.mCancel) {
				//	If fail in some file system (NTFS???), try stream mode
				opts.outFDFailed = true;
				Log.e("BitmapUtils", "decode fd error!");
			}
		}
		if (bitmap == null && !opts.inJustDecodeBounds && opts.outFDFailed && pfd.isFile())
			bitmap = BitmapFactory.decodeFile(pfd.getPath(), opts);
		return bitmap;
	}

	public static Bitmap ensureGLCompatibleBitmap(Bitmap bitmap) {
		if (bitmap == null || bitmap.getConfig() != null)
			return bitmap;
		try {
			final Bitmap newBmp = bitmap.copy(Config.ARGB_8888, false);
			//	Log.i("BitmapUtils", "create GLCompatible: " + bitmap.getConfig());
			bitmap.recycle();
			bitmap = newBmp;
		} catch (Throwable e) {
			Log.e("BitmapUtils", "create GLCompatible: " + e);
		}
		return bitmap;
	}

	public static boolean getBitmapSize(ParcelFile pfd, BitmapOptions opts) {
		try {
			opts.inJustDecodeBounds = true;
			decodeParcelFile(pfd, opts);
			opts.inJustDecodeBounds = false;
			//	Log.d("BitmapUtils", "bitmap size: " + opts.outWidth + "x" + opts.outHeight + ", cancel=" + opts.mCancel);
			return opts.outWidth > 0 && opts.outHeight > 0;//! (opts.mCancel || opts.isFailed());
		} catch (Throwable e) {
			Log.e("BitmapUtils", "get size: " + e);
		}
		opts.inJustDecodeBounds = false;
		opts.outWidth = opts.outHeight = -1;
		return false;
	}

	public static Bitmap loadBitmap(ParcelFile pfd, int maxPixels, BitmapOptions opts) {
		final int origPixels = opts.outWidth * opts.outHeight;
		int pixels;

		if (opts.isNativeAlloc()) {
			opts.inSampleSize = computeSampleSize(origPixels, MEMORY_CLASS * 1000000);
			opts.inPreferredConfig = Config.ARGB_8888;
			pixels = origPixels;
		} else {
			if (maxPixels > 0)
				opts.inSampleSize = computeSampleSize(origPixels, maxPixels);
			else
				opts.inSampleSize = origPixels > MAX_PIXELS_16 ? 2 : 1;
			pixels = origPixels / (opts.inSampleSize * opts.inSampleSize);
			opts.inPreferredConfig = (mTrueColorBitmap || pixels <= MAX_PIXELS_32)
					? Config.ARGB_8888 : Config.RGB_565;
		}

		Bitmap bitmap = null;
		while (bitmap == null && !opts.mCancel) {
			try {
				bitmap = decodeParcelFile(pfd, opts);
			} catch (OutOfMemoryError e) {
				if (pixels >= MAX_PIXELS_16) {
					opts.inSampleSize *= 2;
					pixels = origPixels / (opts.inSampleSize * opts.inSampleSize);
					opts.inPreferredConfig = (mTrueColorBitmap || pixels <= MAX_PIXELS_32)
							? Config.ARGB_8888 : Config.RGB_565;
					continue;
				} else if (!mTrueColorBitmap && opts.inPreferredConfig == Config.ARGB_8888) {
					opts.inPreferredConfig = Config.RGB_565;
					continue;
				} else if (opts.inSampleSize < MAX_SAMPLE_SIZE) {
					opts.inSampleSize *= 2;
					continue;
				}
			} catch (Throwable e) {
				Log.e("BitmapUtils", "load bitmap: " + e);
			}
			break;
		}
		//	if (bitmap != null)
		//		Log.d("BitmapUtils", "bitmap output:" + bitmap.getWidth() + "x" + bitmap.getHeight() + " " + bitmap.getConfig() + " " + opts.inSampleSize);
		return bitmap;
	}

	public static Bitmap loadThumbnail(ParcelFile pfd, boolean crop, int side, BitmapOptions opts) {
		final int width = opts.outWidth;
		final int height = opts.outHeight;

		final float scale = (float) opts.outWidth / opts.outHeight;
		if (scale > 0.5f && scale < 2.0f)
			opts.inSampleSize = computeSampleSize(opts.outWidth, opts.outHeight, crop, side);
		else
			opts.inSampleSize = computeSampleSize(opts.outWidth * opts.outHeight, (crop ? side * side * 4 / 3 : side * side * 3 / 4) * 3/* Better quality *3 */);
		//	Log.d("BitmapUtils", "sample size: " + opts.outWidth + "x" + opts.outHeight  + " -> " + (opts.outWidth / opts.inSampleSize) + "x" + (opts.outHeight / opts.inSampleSize) + ", sample=" + opts.inSampleSize);

		opts.inJustDecodeBounds = false;
		opts.inPreferredConfig = BitmapOptions.PREFER_CONFIG;
		//	if ("image/png".equals(opts.outMimeType))
		//		opts.inPreferredConfig = Bitmap.Config.ARGB_8888;

		Bitmap bitmap = null;
		while (bitmap == null && !opts.mCancel) {
			try {
				bitmap = decodeParcelFile(pfd, opts);
			} catch (OutOfMemoryError e) {
				if (opts.inPreferredConfig == Config.ARGB_8888) {
					opts.inPreferredConfig = Config.RGB_565;
					continue;
				} else if (side >= 1920) {
					side /= 2;
					opts.inSampleSize *= 2;
					opts.inPreferredConfig = BitmapOptions.PREFER_CONFIG;
					continue;
				}
			} catch (Throwable e) {
				Log.e("BitmapUtils", "load thumbnail: " + pfd.getUri() + ", " + width + "x" + height + "/" + opts.inSampleSize + ", " + e);
			}
			break;
		}
		//	if (bitmap != null)
		//		Log.d("BitmapUtils", "load thumbnail: " + bitmap.getWidth() + "x" + bitmap.getHeight() + ", max=" + maxPixels);
		return bitmap;
	}

	/*	public static int Orientation2Degree(int nOrientation) {
			switch (nOrientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				return 90;
			case ExifInterface.ORIENTATION_ROTATE_180:
				return 180;
			case ExifInterface.ORIENTATION_ROTATE_270:
				return 270;
			}
			return 0;
		}

		public static int Degree2Orientation(int nDegree) {
			switch (nDegree) {
			case 90:
				return ExifInterface.ORIENTATION_ROTATE_90;
			case 180:
				return ExifInterface.ORIENTATION_ROTATE_180;
			case 270:
				return ExifInterface.ORIENTATION_ROTATE_270;
			}
			return ExifInterface.ORIENTATION_NORMAL;
		}

		public static boolean changeRotation(String filename, int degrees) {
			try {
				final ExifInterface exif = new ExifInterface(filename);
				int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
				degrees += Orientation2Degree(orientation);
				if (degrees < 0)
					degrees += 360;
				degrees %= 360;
				orientation = Degree2Orientation(degrees);
				exif.setAttribute(ExifInterface.TAG_ORIENTATION, Integer.toString(orientation));
				exif.saveAttributes();
			} catch (Throwable e) {
				return false;
			}
			return true;
		}
	*/
	public static boolean matchRotation(int cxImage, int cyImage, int cxThumb, int cyThumb, int rotation) {
		//	Current only 0 to check
		return rotation == 0 && cxImage * cyThumb == cyImage * cxThumb;
	}

	public static int computeSampleSize(int bmpPixels, int maxPixels) {
		int pixels = bmpPixels;
		int sample = 1;
		while (pixels > maxPixels) {
			if (sample < 8)
				sample *= 2;
			else
				sample += 8;
			pixels = bmpPixels / (sample * sample);
		}
		//	Log.d("BitmapUtils", "sample size: " + bmpPixels + " -> " + pixels + ", maxPixels=" + maxPixels + ", sample=" + sample);
		return sample;
	}

	//	This computes a sample size which makes the longer side at least side long.
	public static int computeSampleSize(int width, int height, boolean crop, int side) {
		int sample = (crop ? Math.min(width, height) : Math.max(width, height)) / side;
		if (sample <= 1)
			sample = 1;    //	If that's not possible, return 1
		else
			sample = sample <= 8 ? Integer.highestOneBit(sample) : sample / 8 * 8;
		//	Log.d("BitmapUtils", "sample size: " + width + "x" + height  + " -> " + (width / sample) + "x" + (height / sample) + ", sample=" + sample);
		return sample;
	}

/*	//	android.media.MediaMetadataRetriever
	private static Class<?> mMetadataRetriever;
	private static Method mSetDataSource;
	private static Method mRelease;
	private static Method mGetFrameAtTime;	//	>= 2.3
	private static Method mCaptureFrame;	//	<= 2.2
	static {
		try {
			mMetadataRetriever = Class.forName("android.media.MediaMetadataRetriever");
			mSetDataSource = mMetadataRetriever.getMethod("setDataSource", new Class[] { String.class });
			mRelease = mMetadataRetriever.getMethod("release", new Class[] {});
		} catch (Throwable e) {
		}
		try {
			mGetFrameAtTime = mMetadataRetriever.getMethod("getFrameAtTime", new Class[] {});
		} catch (Throwable e) {
		}
		try {
			mCaptureFrame = mMetadataRetriever.getMethod("captureFrame", new Class[] {});
		} catch (Throwable e) {
		}
	}

	public static Bitmap loadVideoThumbnail(String filename) {
		if (mSetDataSource == null)
			return null;

		final Object[] args = new Object[] {};
		Object retriever = null;
		Bitmap bitmap = null;
		try {
			retriever = mMetadataRetriever.newInstance();
			mSetDataSource.invoke(retriever, filename);
			if (mGetFrameAtTime != null)
				bitmap = (Bitmap)mGetFrameAtTime.invoke(retriever, args);
			else if (mCaptureFrame != null)
				bitmap = (Bitmap)mCaptureFrame.invoke(retriever, args);
		} catch (Throwable e) {
		//	Log.e("BitmapUtils", "video thumb:" + e);
		}
		try {
			if (retriever != null && mRelease != null)
				mRelease.invoke(retriever, args);
		} catch (Throwable e) {
		//	Log.e("BitmapUtils", "video thumb:" + e);
		}
	//	Log.d("BitmapUtils", "video thumb:" + filename + " bitmap:" + bitmap);
		return bitmap;
	}
*/

	private static Object mThumbnailUtil;
	private static Method mCreateVideoThumbnail;

	static {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) try {
			final Class<?> cls = Class.forName("android.media.ThumbnailUtil");
			mThumbnailUtil = cls.newInstance();
			mCreateVideoThumbnail = cls.getMethod("createVideoThumbnail", String.class);
		} catch (Throwable e) {
		}
	}

	public static Bitmap loadMediaThumbnail(ParcelFile pfd) {
		Bitmap bitmap = null;
		if (pfd.isFile()) {
			try {
				final String filename = pfd.getPath();
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
					bitmap = ThumbnailUtils.createVideoThumbnail(filename, MediaStore.Images.Thumbnails.MINI_KIND);
				else if (mThumbnailUtil != null)
					bitmap = (Bitmap) mCreateVideoThumbnail.invoke(mThumbnailUtil, filename);
			} catch (Throwable e) {
				Log.e("BitmapUtils", "video thumb:" + e);
			}
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
			final MediaMetadataRetriever retriever = new MediaMetadataRetriever();
			try {
				retriever.setDataSource(pfd.getDescriptor());
				bitmap = retriever.getFrameAtTime(-1);
			} catch (Throwable e) {
				Log.e("BitmapUtils", "video thumb:" + e);
			} finally {
				try {
					retriever.release();
				} catch (Throwable e) {
				}
			}
		}
		return bitmap;
	}
}
