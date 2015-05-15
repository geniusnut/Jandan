package com.nut.cache;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yw07 on 15-3-17.
 */
public class Post implements Parcelable {
	public String mLink;
	public String mTitle;
	public String mCover;
	public String mAuthor;
	public String mTag;
	public int mCont;

	public Post() {

	}

	public Post(Parcel in) {
		mLink = in.readString();
		mTitle = in.readString();
		mCover = in.readString();
		mCont = in.readInt();
	}

	public static final Creator<Post> CREATOR = new Creator<Post>() {
		@Override
		public Post createFromParcel(Parcel source) {
			return new Post(source);
		}

		@Override
		public Post[] newArray(int size) {
			return new Post[0];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mLink);
		dest.writeString(mTitle);
		dest.writeString(mCover);
		dest.writeInt(mCont);
	}
}
