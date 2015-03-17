package com.alensw.Jandan;


import android.util.Log;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class ListFile<V> implements Iterable<V> {
	protected static final String TAG = "ListFile";

	private static final int VERSION = 1;

	private File mFile;
	private boolean mModified;
	private final CopyOnWriteArrayList<V> mArray;

	public ListFile() {
		mArray = new CopyOnWriteArrayList<V>();
	}

	public abstract String getTypeName();

	public abstract V readEntry(DataInputStream dis) throws Throwable;

	public abstract void writeEntry(DataOutputStream dos, V entry) throws Throwable;

	public boolean isEmpty() {
		return mArray.isEmpty();
	}

	public int size() {
		return mArray.size();
	}

	public Iterator<V> iterator() {
		return mArray.iterator();
	}

	public boolean contains(V value) {
		return mArray.contains(value);
	}

	public int indexOf(V value) {
		return mArray.indexOf(value);
	}

	public void add(V value) {
		mArray.add(value);
		mModified = true;
	}

	public void addAll(Collection<? extends V> collection) {
		mArray.addAll(collection);
		mModified = true;
	}

	public V get(int index) {
		return mArray.get(index);
	}

	public V set(int index, V value) {
		final V prev = mArray.set(index, value);
		if (prev == value)
			return prev;
		if (prev == null || value == null)
			mModified = true;
		else if (!prev.equals(value))
			mModified = true;
		return prev;
	}

	public List<V> snapshot() {
		final Object[] a = mArray.toArray();
		final ArrayList<V> list = new ArrayList<V>(a.length);
		for (Object o : a)
			list.add((V) o);
		return list;
	}

	public V remove(int index) {
		final V prev = mArray.remove(index);
		if (prev != null)
			mModified = true;
		return prev;
	}

	public boolean remove(V value) {
		final boolean ret = mArray.remove(value);
		if (ret)
			mModified = true;
		return ret;
	}

	public void clear() {
		mModified = mArray.size() > 0;
		mArray.clear();
	}

	public boolean isModified() {
		return mModified;
	}

	public void setModified(boolean modified) {
		mModified = modified;
	}

	public long lastModified() {
		return mFile != null ? mFile.lastModified() : 0;
	}

	public boolean load(String filename) {
		return filename != null && load(new File(filename));
	}

	public boolean load(File file) {
		clear();
		mFile = file;
		mModified = false;

		try {
			final LinkedList<V> data = new LinkedList<V>();
			final FileInputStream fis = new FileInputStream(file);
			final DataInputStream dis = new DataInputStream(fis);
			final int version = dis.readInt();
			final String typeName = dis.readUTF();
			if (version == VERSION && getTypeName().equals(typeName)) {
				final int count = dis.readInt();
				for (int i = 0; i < count; i++) {
					final V value = readEntry(dis);
					if (value != null)
						data.add(value);
				}
				Log.d(TAG, "read entry: " + file.getPath() + " " + count);
			}
			// add all items at once, better performance than CopyOnWriteArrayList.add()
			mArray.addAll(data);

			dis.close();
			fis.close();
			//	Log.d(TAG, "load: " + data.size() + " items");
			return data.size() > 0;
		} catch (FileNotFoundException e) {
		} catch (Throwable e) {
			Log.e(TAG, "load: ", e);
		}
		return false;
	}

	public void saveAs(String filename) {
		if (filename != null)
			mFile = new File(filename);
		save();
	}

	public void saveAs(File file) {
		if (file != null)
			mFile = file;
		save();
	}

	public boolean save() {
		if (mFile != null) try {
			final boolean deleted = mFile.delete();//Utilities.deleteFile(file);
			if (isEmpty())
				return deleted;

			final Object[] a = mArray.toArray();
			final FileOutputStream fos = new FileOutputStream(mFile);
			final DataOutputStream dos = new DataOutputStream(fos);
			dos.writeInt(VERSION);
			dos.writeUTF(getTypeName());
			dos.writeInt(a.length);
			for (Object o : a)
				writeEntry(dos, (V) o);
			dos.close();
			fos.close();
			//	Log.d(TAG, "save: " + data.size() + " items");
			return true;
		} catch (Throwable e) {
			Log.e(TAG, "save: ", e);
		}
		return false;
	}
}
