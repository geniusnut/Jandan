package com.alensw.support.pool;

import android.util.Log;

public class IntQueue {
	private final int[] mData;
	private int mSize;
	private int mHead;
	private int mTail;

	public IntQueue(int maxSize) {
		mData = new int[maxSize];
	}

	public void clear() {
		mHead = mTail = 0;
		mSize = 0;
	}

	public boolean contains(int value) {
		int index = mHead;
		for (int i = 0; i < mSize; i++) {
			if (mData[index] == value)
				return true;
			if (++index >= mData.length)
				index = 0;
		}
		return false;
	}

	public int popHead() {
		if (mSize > 0) {
			final int value = mData[mHead];
			mData[mHead] = 0;
			mHead = (mHead + 1) % mData.length;
			mSize--;
			return value;
		} else {
			Log.e("IntQueue", "empty!");
			return 0;
		}
	}

	public void pushTail(int value) {
		if (mSize < mData.length) {
			mData[mTail] = value;
			mTail = (mTail + 1) % mData.length;
			mSize++;
		} else {
			Log.e("IntQueue", "full!");
		}
	}

	public final int size() {
		return mSize;
	}
}
