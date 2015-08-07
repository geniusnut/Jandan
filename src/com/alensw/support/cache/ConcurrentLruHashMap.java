package com.alensw.support.cache;

import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentLruHashMap<K, V extends ConcurrentLruHashMap.Linked> {
	private static final String TAG = "ConcurrentLruHashMap";

	private int mMaxSize;
	private final ConcurrentHashMap<K, V> mMap;
	private final LinkedList mList;

	public ConcurrentLruHashMap(int maxSize) {
		mMaxSize = maxSize;
		if (maxSize <= 0)
			maxSize = 256;
		mMap = new ConcurrentHashMap<K, V>(maxSize);
		mList = new LinkedList();
	}

	public void setMaxSize(int maxSize) {
		mMaxSize = maxSize;
	}

	public final int size() {
		return mMap.size();
	}

	public final boolean contains(K key) {
		return mMap.containsKey(key);
	}

	public final V get(K key) {
		final V v = mMap.get(key);
		if (v != null) {
			synchronized (mList) {
				// If still in the link, move to first
				if (mList.unlink(v)) {
					mList.link(v);
				} else {
					// It was unlinked, to be removed from the cache in put()
					Log.i(TAG, "entry was unlinked: " + v);
				}
			}
			return v;
		}
		return null;
	}

	public V put(K key, V value) {
		value.key = key;

		synchronized (mList) {
			mList.link(value);
		}

		final V prev = mMap.put(key, value);
		if (prev != null) {
			synchronized (mList) {
				mList.unlink(prev);
			}
			prev.key = null;
			discard(prev);
			return prev;
		} else {
			// Remove eldest entry if full
			final boolean discard;
			final V eldest;
			synchronized (mList) {
				eldest = mList.eldest();
				discard = eldest != null && mMap.size() > mMaxSize;
				if (discard)
					mList.unlink(eldest);
			}
			if (discard) {
				mMap.remove(eldest.key);
				eldest.key = null;
				discard(eldest);
			}
			return null;
		}
	}

	public V remove(K key) {
		final V v = mMap.remove(key);
		if (v != null) {
			synchronized (mList) {
				mList.unlink(v);
			}
			v.key = null;
			discard(v);
			return v;
		}
		return null;
	}

	public V removeEldest() {
		final V eldest;
		synchronized (mList) {
			eldest = mList.eldest();
			if (eldest != null)
				mList.unlink(eldest);
		}
		if (eldest != null) {
			mMap.remove(eldest.key);
			eldest.key = null;
			discard(eldest);
			return eldest;
		}
		return null;
	}

	public void trimToSize(int maxSize) {
		int count = mMap.size() - maxSize;
		while (count-- > 0) {
			if (removeEldest() == null) {
				Log.v(TAG, "eldest is null!");
				break;
			}
		}
		//	Log.d(TAG, "trim to size: " + mMap.size() + "/" + maxSize);
	}

	public void clear() {
		mMap.clear();

		synchronized (mList) {
			mList.clear();
		}
	}

	//	Overridable
	protected void discard(V value) {
	}

	public static abstract class Linked {
		protected Object key;
		protected Linked prev = this;
		protected Linked next = this;
	}

	private class LinkedList {
		/**
		 * A dummy entry in the circular linked list of entries in the map.
		 * The first real entry is header.next, and the last is header.prev.
		 * If the map is empty, header.next == header && header.prev == header.
		 */
		private final Linked mHeader = new Linked() {
		};

		public void clear() {
			final Linked header = mHeader;
			for (Linked v = header.next; v != header && v != null; ) {
				if (v != null) {
					Linked nxt = v.next;
					v.next = v.prev = null;
					v.key = null;
					discard((V) v);
					v = nxt;
				} else {
					Log.v(TAG, "entry is null!");
					break;
				}
			}
			header.next = header.prev = header;
		}

		public V eldest() {
			final Linked eldest = mHeader.next;
			return eldest != mHeader ? (V) eldest : null;
		}

		public void link(Linked v) {
			final Linked header = mHeader;
			final Linked oldTail = header.prev;
			v.next = header;
			v.prev = oldTail;
			oldTail.next = header.prev = v;
		}

		public boolean unlink(Linked v) {
			final boolean linked = v.prev != null && v.next != null;
			if (linked) {
				v.prev.next = v.next;
				v.next.prev = v.prev;
			}
			v.next = v.prev = null;
			return linked;
		}
	}
}
