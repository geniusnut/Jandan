package com.alensw.Jandan;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by yw07 on 15-3-19.
 */
public class Pic implements Parcelable {
	public int mId;
	public String mAuthor;
	public String mDesc;
	public int mPhotos;
	public ArrayList<String> mUrls;
	public int mOO;
	public int mXX;
	public long mTime;

	public Pic() {

	}

	public Pic(Parcel source) {
		mId = source.readInt();
		mAuthor = source.readString();
		mDesc = source.readString();
		mPhotos = source.readInt();
		mUrls = source.createStringArrayList();
		mOO = source.readInt();
		mXX = source.readInt();
		mTime = source.readLong();
	}

	public static final Creator<Pic> CREATOR = new Creator<Pic>() {
		@Override
		public Pic createFromParcel(Parcel source) {
			return new Pic(source);
		}

		@Override
		public Pic[] newArray(int size) {
			return new Pic[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mId);
		dest.writeString(mAuthor);
		dest.writeString(mDesc);
		dest.writeInt(mPhotos);
		dest.writeStringList(mUrls);
		dest.writeInt(mOO);
		dest.writeInt(mXX);
		dest.writeLong(mTime);
	}
}
