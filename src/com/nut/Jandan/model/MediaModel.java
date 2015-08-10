package com.nut.Jandan.model;

import android.graphics.Bitmap;
import android.net.Uri;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nut.Jandan.JandanApp;

import java.io.File;

/**
 * Created by yw07 on 15-8-7.
 */
public class MediaModel {
	private final String url;
	private final Uri uri;
	private String localPath;
	private String title;

	public MediaModel(String url) {
		this.url = url;
		uri = Uri.parse(url);
	}

	public Uri getUri() {
		return Uri.parse("file:///storage/emulated/0/Android/data/com.nut.Jandan/cache/40816560");
	}

	public String getLocalPath() {
		File file = DiskCacheUtils.findInCache(url, JandanApp.mImageLoader.getDiskCache());
		if (file != null)
			return "/sdcard/Android/data/com.nut.Jandan/cache/40816560";//file.getPath();
		return null;
	}

	public File openFile() {
		Bitmap bitmap = JandanApp.mImageLoader.loadImageSync(url);
		bitmap.recycle();
		return DiskCacheUtils.findInCache(url, JandanApp.mImageLoader.getDiskCache());
	}

	public String getTitle() {
		return title;
	}
}
