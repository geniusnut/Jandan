package com.alensw.Jandan;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.provider.DocumentsContract.Document;
import android.util.Log;
import org.w3c.dom.Node;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yw07 on 14-12-11.
 */
public class FileCache extends SQLiteOpenHelper {
	private static final String TAG = "FileCache";
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "images.db";
	protected static final String TABLE_CACHE = "cache";
	public static final String TABLE_COMMENTS = "comments";
	protected Context mContext;
	protected File mCacheDir;
	protected static final String SQL_ID_EQUAL = Document.COLUMN_DOCUMENT_ID + "=?";
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_CACHE + "("
			+ NodeColumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ NodeColumns.TITLE + " TEXT,"
			+ NodeColumns.URL + " TEXT,"
			+ NodeColumns.ORG_URL + " TEXT,"
			+ NodeColumns.LAST_MODIFIED + " TEXT)";

	public FileCache(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mCacheDir = buildPath(Environment.getExternalStorageDirectory(), "Android", "data", context.getPackageName(), "cache");
		mCacheDir.mkdirs();
		Log.d(TAG, mCacheDir + " : " + mCacheDir.isDirectory());
		//mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Cursor c=db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='cache'", null);
		Log.d(TAG, "db query count : " + c.getCount());
		try {
			if (c.getCount()==0) {
				ContentValues values = new ContentValues(2);
				values.put(NodeColumns.ID, "1");
				values.put(NodeColumns.URL, "localHost");
				db.execSQL(DATABASE_CREATE);
				db.insert("cache", null, values);
			}
		} finally {
			c.close();
		}

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(FileCache.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENTS);
		onCreate(db);
	}
	public void updateCache(final ContentValues values) {
		try {
			getWritableDatabase().replace(TABLE_CACHE, null, values);
		} catch (Throwable e) {
			Log.e(TAG, "insert cache: ", e);
		}

	}
	private File getExternalFile() {
		return null;
	}

	public File generateCacheFile(String id) {
		return new File(mCacheDir,id);
	}

	public static File buildPath(File base, String... segments) {
		File file = base;
		for (String segment : segments) {
			if (file == null)
				file = new File(segment);
			else if (segment != null)
				file = new File(file, segment);
		}
		return file;
	}
}
