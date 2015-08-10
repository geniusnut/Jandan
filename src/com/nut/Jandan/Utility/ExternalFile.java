package com.nut.Jandan.Utility;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.FileFilter;
import java.util.Locale;

public class ExternalFile {
	public static final File SD_DIR = Environment.getExternalStorageDirectory();
	public static final String SD_PATH = SD_DIR.getPath();

	public static File buildPath(File base, String... segments) {
		File file = base;
		for (String segment : segments) {
			if (file == null)
				file = new File(segment);
			else if (segment != null)
				file = new File(file, segment);
		}
		return file;
	}

	public static void deleteDir(final File dir) {
		dir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				if (file.isDirectory())
					deleteDir(file);
				else
					file.delete();
				return false;
			}
		});
		dir.delete();
	}

	public static File getExternalAppDir(Context context) {
		final File dir = buildPath(SD_DIR, "Android", "data", context.getPackageName());
		if (!dir.exists())
			dir.mkdirs();
		return dir;
	}

	public static File getExternalFile(Context context, String name) {
		return new File(getExternalAppDir(context), name);
	}

	public static File getExternalCacheDir(Context context) {
		final File dir = new File(getExternalAppDir(context), "cache");
		if (!dir.exists())
			dir.mkdirs();
		return dir;
	}

	public static File getExternalCacheFile(Context context, String name) {
		return new File(getExternalCacheDir(context), name);
	}

	public static File getExternalTempDir(Context context) {
		final File dirTemp = new File(getExternalCacheDir(context), ".temp");
		if (dirTemp.exists()) {
			if (dirTemp.lastModified() + 3 * 3600 * 1000 >= System.currentTimeMillis())
				return dirTemp;
			deleteDir(dirTemp);
		}
		dirTemp.mkdirs();
		return dirTemp;
	}

	public static File getExternalTempFile(Context context, String preferName) {
		final int pos = preferName.lastIndexOf(File.separatorChar);
		if (pos >= 0 && pos < preferName.length() - 1)
			preferName = preferName.substring(pos + 1);
		final int pos2 = preferName.lastIndexOf('.');
		if (pos2 != -1)
			preferName = preferName.substring(0, pos2) + preferName.substring(pos2).toLowerCase(Locale.ENGLISH);
		return new File(getExternalTempDir(context), preferName);
	}

	public static File getDownloadDir() {
		final String download = Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? Environment.DIRECTORY_DOWNLOADS : "Download";
		return new File(SD_DIR, download);
	}
}
