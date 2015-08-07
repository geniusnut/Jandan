package com.alensw.support.file;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class ParcelFile {
	private final Uri mUri;
	private ParcelFileDescriptor mFile;

	public static ParcelFile openFile(File file, boolean readonly) throws Exception {
		final ParcelFileDescriptor pfd = openFileDescriptor(file, readonly);
		return new ParcelFile(Uri.fromFile(file), pfd);
	}

	public static ParcelFile openForRead(ContentResolver resolver, Uri uri) throws Exception {
		final ParcelFileDescriptor pfd = openFileDescriptor(resolver, uri, true);
		return new ParcelFile(uri, pfd);
	}

	public static ParcelFileDescriptor openFileDescriptor(File file, boolean readonly) throws Exception {
		try {
			final int mode = readonly ? ParcelFileDescriptor.MODE_READ_ONLY : (ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE);
			return ParcelFileDescriptor.open(file, mode);
		} catch (FileNotFoundException e) {
			return openFileDescriptor(file.getPath(), readonly);
		}
	}

	public static ParcelFileDescriptor openFileDescriptor(ContentResolver resolver, Uri uri, boolean readonly) throws Exception {
		try {
			return resolver.openFileDescriptor(uri, readonly ? "r" : "rw");
		} catch (FileNotFoundException e) {
			if (ContentResolver.SCHEME_FILE.equals(uri.getScheme()))
				return openFileDescriptor(uri.getPath(), readonly);
			throw e;
		}
	}

	//  work around the bug of ParcelFileDescriptor.open() if the path contains Emoji symbol
	private static ParcelFileDescriptor openFileDescriptor(String path, boolean readonly) throws Exception {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int fd = -1;
			if (readonly) {
				final FileInputStream fis = new FileInputStream(path);
				fd = mDescriptor.getInt(fis.getFD());
			} else {
				final FileOutputStream fos = new FileOutputStream(path);
				fd = mDescriptor.getInt(fos.getFD());
			}
			if (fd != -1)
				return ParcelFileDescriptor.fromFd(fd);
		}
		throw new FileNotFoundException();
	}

	public ParcelFile(Uri uri, ParcelFileDescriptor pfd) {
		mFile = pfd;
		mUri = uri;
	}

	public ParcelFile(Uri uri) {
		mUri = uri;
	}

	@Override
	protected void finalize() {
		close();
	}

	@Override
	public int hashCode() {
		return mUri.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ParcelFile && mUri.equals(((ParcelFile) obj).mUri);
	}

	public boolean isFile() {
		return ContentResolver.SCHEME_FILE.equals(mUri.getScheme());
	}

	public FileDescriptor getDescriptor() throws Exception {
		if (mFile == null)
			throw new FileNotFoundException("file not open");
		return mFile.getFileDescriptor();
	}

	public int getFd() {
		if (mFile != null) try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
				return mFile.getFd();
			return mDescriptor.getInt(mFile.getFileDescriptor());
			//	return JniUtils.fuGetFD(mFile.getFileDescriptor());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return -1;
	}

	public String getPath() {
		return mUri.getPath();
	}

	public long getStatSize() {
		if (mFile != null) try {
			return mFile.getStatSize();
		} catch (Throwable e) {
			//	Log.e("ParcelFile", "get size: " + e);
		}
		return 0;
	}

	public long getLastModified() {
		try {
			return new File(mUri.getPath()).lastModified();
		} catch (Throwable e) {
			//	Log.e("ParcelFile", "get time: " + e);
		}
		return 0;
	}

	public Uri getUri() {
		return mUri;
	}

	public void close() {
		if (mFile != null) try {
			mFile.close();
		} catch (Throwable e) {
			//	Log.e("ParcelFile", "close file: " + e);
		}
		mFile = null;
	}

	public static boolean isLocal(Uri uri) {
		return ContentResolver.SCHEME_FILE.equals(uri.getScheme());
	}

/*	public static ParcelFileDescriptor createFromFd(int fd) throws Exception {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
			return ParcelFileDescriptor.adoptFd(fd);
		else try {
			final FileDescriptor descriptor = new FileDescriptor();
			mDescriptor.setInt(descriptor, fd);
			return mConstructor.newInstance(descriptor);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ParcelFileDescriptor[] createSocketPair() {
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
				return ParcelFileDescriptor.createSocketPair();
			else {
				final int fds[] = new int[2];
				if (JniUtils.fuSocketPair(fds))
					return new ParcelFileDescriptor[]{
							createFromFd(fds[0]), createFromFd(fds[1])
					};
				else
					throw new RuntimeException("socket pair failed");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}*/

	private static Field mDescriptor;
	private static Constructor<ParcelFileDescriptor> mConstructor;

	static {
		try {
			mDescriptor = FileDescriptor.class.getDeclaredField("descriptor");
			mDescriptor.setAccessible(true);

			mConstructor = ParcelFileDescriptor.class.getDeclaredConstructor(FileDescriptor.class);
			mConstructor.setAccessible(true);
		} catch (Throwable e) {
		}
	}
}
