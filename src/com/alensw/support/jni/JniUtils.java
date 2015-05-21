package com.alensw.support.jni;

import android.graphics.Bitmap;
import android.os.Environment;

import java.lang.reflect.Method;

public class JniUtils {
	static {
		System.loadLibrary("qpicjni152");

		init(Environment.getExternalStorageDirectory().getPath());
	}

	//	Init
	private static native int init(String sd);

	//	Exif parser
//	public static native int exifOpen(String filename, boolean readonly);
	public static native int exifOpenFD(int fd, boolean readonly);

	public static native void exifClose(int obj);

	public static native int exifGetInfo(int obj, int info);

	public static native byte[] exifGetThumbnail(int obj);

	public static native Object exifGetValue(int obj, int tag, boolean gps);

	public static native double[] exifGet3RealValue(int obj, int tag, boolean gps);

	public static native boolean exifSetDegrees(int obj, int degrees);

	public static native boolean exifSaveTo(int obj, int obj2);

	//	Folder scanner
	public static native int fsCreateScanner(Method add);

	public static native void fsDestroyScanner(int obj);

	public static native void fsInitExtensions(int obj, String exts);

	public static native void fsCancelScan(int obj, boolean cancel);

	public static native int fsScanFolders(int obj, Object folder, int flags);

	public static native int fsScanPictures(int obj, Object folder, int flags);

	//	File utils
	public static native boolean fuSocketPair(int[] fds);

//	public static native int fuGetFD(FileDescriptor jfd);

	public static native long fuGetFileSize(String path);

	public static native int fuGetFileTime(String path);

	public static native long fuGetAvailBytes(String path);

	public static native boolean fuCopyFD(int src, int dst);

//	public static native boolean fuCopyFile(String src, String dst);

	public static native boolean fuHasNoMedia(String path);

	//	Gif Image
	public static native int gifAllocBuffer(int size);

	public static native void gifFreeBuffer(int buffer);

	public static native int gifOpenFD(int fd, boolean bmpMode);

	public static native void gifClose(int obj);

	public static native int gifGetDuration(int obj);

	public static native int gifGetFrameCount(int obj);

	public static native int gifGetImageWidth(int obj);

	public static native int gifGetImageHeight(int obj);

	public static native void gifSetBkColor(int obj, int color);

	public static native int gifDecodeFrame(int obj, int index, int buffer);

	public static native boolean gifDrawFrame(int obj, int index, int buffer, Bitmap bitmap);

	public static native boolean gifDrawFrame2(int obj, int index, int buffer, int[] image, int downSample);
}
