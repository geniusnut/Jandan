package com.nut.Jandan.model;

import com.nut.Jandan.Utility.Utilities;
import com.nut.cache.FileCache;
import com.nut.thread.AsyncExecutor;
import com.nut.thread.BaseThread;
import com.nut.thread.FutureTaskEx;
import com.nut.thread.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yw07 on 15-6-5.
 */
public class UrlFile {
	private String mUrl;
	private String mHash;

	private final ConcurrentHashMap<String, FutureTaskEx<File>> mLoadingTasks = new ConcurrentHashMap<>(128);
	private static AsyncExecutor mExecutor = new AsyncExecutor(4, 8, Integer.MAX_VALUE, BaseThread.THREAD_PRIORITY_LOAD);

	public UrlFile(String url) {
		mUrl = url;
		mHash = Utilities.md5(mUrl);
	}

	public File openFile(Callback callback) {
		final FutureTaskEx<File> newTask = new FutureTaskEx<File>(new FileTask(callback));
		FutureTaskEx<?> futureTask = mLoadingTasks.putIfAbsent(mUrl, newTask);
		if (futureTask == null)
			futureTask = mExecutor.submit(newTask);

		File file = null;
		try {
			file = (File) futureTask.get();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return file;
	}


	public interface Callback {
		void onLoading(int progress);
	}

	private class FileTask implements Task<File> {
		private Callback mCallback;

		public FileTask(Callback callback) {
			mCallback = callback;
		}

		@Override
		public void cancel() {

		}

		@Override
		public void done() {

		}

		@Override
		public File call() throws Exception {
			FileCache.mCacheDir.mkdirs();
			File file = FileCache.generateCacheFile(mHash);
			if (!file.exists()) {
				InputStream is = null;
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(file);
					URL url = new URL(mUrl);
					URLConnection conn = url.openConnection();
					is = conn.getInputStream();
					long length = is.available();
					final byte[] data = new byte[8192];
					int bytes = 0, written = 0, percent;
					while ((bytes = is.read(data)) >= 0) {
						if (bytes > 0) {
							fos.write(data, 0, bytes);
							written += bytes;
							percent = Math.round((float) written / (float) length * 360);
							if (mCallback != null)
								mCallback.onLoading(percent);
						}
					}
					fos.flush();
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return file;
		}
	}
}
