package com.alensw.support.picture;

import com.alensw.support.cache.ConcurrentLruHashMap;

public class PictureInfo extends ConcurrentLruHashMap.Linked {
	public int mWidth;
	public int mHeight;
	public int mComponents;
	public int mRotation;
	public boolean mPanorama;
	public boolean mValidRegion; // can be decoded by BitmapRegionDecoder
	public long mTakenTime; // in milliseconds
	public float[] mLatLong;
	public String mCamera;
	public String mDescription;
	public String mExif;
	public String mMimeType;

	public PictureInfo() {
	}

	public PictureInfo(PictureInfo src) {
		mWidth = src.mWidth;
		mHeight = src.mHeight;
		mComponents = src.mComponents;
		mRotation = src.mRotation;
		mPanorama = src.mPanorama;
		mValidRegion = src.mValidRegion;
		mTakenTime = src.mTakenTime;
		mLatLong = src.mLatLong;
		mCamera = src.mCamera;
		mDescription = src.mDescription;
		mExif = src.mExif;
		mMimeType = src.mMimeType;
	}
}
