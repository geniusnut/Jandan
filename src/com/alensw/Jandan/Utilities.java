package com.alensw.Jandan;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by yw07 on 15-3-18.
 */
public class Utilities {
	private static final DateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	//	static {
//		mDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//	}
	private static String convertToHex(byte[] data) {
		StringBuilder buf = new StringBuilder();
		for (byte b : data) {
			int halfbyte = (b >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
				halfbyte = b & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	public static String md5(String text) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(text.getBytes("iso-8859-1"), 0, text.length());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		byte[] sha1hash = md.digest();
		return convertToHex(sha1hash);
	}

	public static String convertTime(long mTime) {
		Log.d("convertTime", "mTime = " + mTime);
		long seconds = System.currentTimeMillis() / 1000 - mTime;

		if (seconds < 3600) {
			return seconds / 60 + "分钟前";
		} else if (seconds < 86400) {
			return seconds / 3600 + "小时前";
		} else {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(mTime);
			return mDateFormat.format(calendar.getTime());
		}
	}
}
