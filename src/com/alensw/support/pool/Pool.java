package com.alensw.support.pool;

import android.util.Log;

public class Pool<V> {
	private final V[] mData;
	private int mSize;

	@SuppressWarnings("unchecked")
	public Pool(int maxSize) {
		mData = (V[]) new Object[maxSize];
	}

	public void clear() {
		V obj;
		while ((obj = poll()) != null)
			discard(obj);
	}

	public V poll() {
		if (mSize > 0) {
			final int last = mSize - 1;
			V obj = mData[last];
			mData[last] = null;
			mSize--;
			return obj;
		}
		return null;
	}

	public void recycle(V obj) {
		if (inPool(obj)) {
			discard(obj);
			Log.e("Pool", "already in pool" + obj);
			return;
		}
		if (mSize < mData.length)
			mData[mSize++] = obj;
		else
			discard(obj);
	}

	private boolean inPool(V obj) {
		for (int i = 0; i < mSize; i++) {
			if (mData[i] == obj)
				return true;
		}
		return false;
	}

	//	Overridable
	protected void discard(V obj) {
	}
}
