package com.nut.Jandan.model;

import android.net.Uri;

import java.io.File;

/**
 * Created by yw07 on 15-8-7.
 */
public class MediaModel {
	private Uri uri;
	private String localPath;
	private String title;

	public Uri getUri() {
		return uri;
	}

	public String getLocalPath() {
		return localPath;
	}

	public File openFile() {
		return null;
	}

	public String getTitle() {
		return title;
	}
}
