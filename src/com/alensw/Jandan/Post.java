package com.alensw.Jandan;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yw07 on 15-3-17.
 */
public class Post {
	public String mLink;
	public String mTitle;
	public String mCover;
	public String mAuthor;
	public String mTag;
	public int mCont;

	static Parcelable writeToParcel(Parcel dest) {
		return null;
	}
}
