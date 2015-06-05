//package com.nut.Jandan.model;
//
//import android.net.Uri;
//import com.nut.cache.FileCache;
//import com.nut.thread.AsyncExecutor;
//import com.nut.thread.FutureTaskEx;
//
//import java.io.File;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * Created by yw07 on 15-5-28.
// */
//public class MediaModel {
//	private final MediaModel mMedia;
//	private FileCache mFileCache;
//
//
//	private final ConcurrentHashMap<String, FutureTaskEx<?>> mLoadingTasks = new ConcurrentHashMap<String, FutureTaskEx<?>>(8);
//
//	private static final AsyncExecutor mExecutor = new AsyncExecutor(2, 2 + 4, 16,
//			android.os.Process.THREAD_PRIORITY_BACKGROUND);
//
//	public MediaModel(MediaModel media) {
//		mMedia = media;
//		mFileCache = DiscoveryApp.mFileCache;
//	}
//
//
//	public Uri getUri() {
//		String url = DiscoveryDrive.getContentUrl(mMedia.hash);
//		return Uri.parse(url);
//	}
//
//	public File openFile() {
//		String url = DiscoveryDrive.getContentUrl(mMedia.hash);
//		final FutureTaskEx<File> newTask = new FutureTaskEx<File>(new FileTask(mMedia));
//		FutureTaskEx<?> futureTask = mLoadingTasks.putIfAbsent(url, newTask);
//		if (futureTask == null)
//			futureTask = mExecutor.submit(newTask);
//
//		File file = null;
//		try {
//			file = (File) futureTask.get();
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//		return file;
//	}
//
//	public String getLocalPath() {
//		File file = mFileCache.queryCacheFile(mMedia.hash, 0);
//		if (file != null) {
//			return file.getPath();
//		}
//		return null;
//	}
//
//	public char getType() {
//		return 'I';
//	}
//
//	public String getTitle() {
//		return mMedia.title;
//	}
//
//	@Override
//	public int hashCode() {
//		return getUri().hashCode();
//	}
//
//	@Override
//	public boolean equals(Object o) {
//		if (this == o)
//			return true;
//
//		if (o instanceof MediaModel) {
//			final MediaModel that = (MediaModel) o;
//			// compare uri without good performance, the derived class should always override equals()
//			final Uri uri = getUri();
//			final Uri uri2 = that.getUri();
//			return uri == uri2 || uri.equals(uri2);
//		}
//		return false;
//	}
//}
