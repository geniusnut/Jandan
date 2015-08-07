package com.alensw.support.picture;

import android.util.Log;
import com.alensw.support.cache.ConcurrentLruHashMap;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class RefObject<T> extends ConcurrentLruHashMap.Linked {
	private final AtomicInteger mCounter = new AtomicInteger(1);

	/*	@Override
		protected void finalize() {
			if (mCounter.get() > 0)
				Log.w("RefObject", "leak object: " + this + ", ref=" + mCounter.get());
		}
	*/
	@SuppressWarnings("unchecked")
	public T addRef() {
		mCounter.incrementAndGet();
		return (T) this;
	}

	public int release() {
		final int ref = mCounter.decrementAndGet();
		if (ref == 0)
			recycle();
		else if (ref < 0)
			Log.e("RefObject", "error release: " + ref + ", " + this);
		return ref;
	}

	protected int refCount() {
		return mCounter.get();
	}

	protected void recycle() {
	}
}
