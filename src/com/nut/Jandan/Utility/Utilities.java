package com.nut.Jandan.Utility;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.nut.Jandan.R;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
		long seconds = System.currentTimeMillis() / 1000 - mTime;

		if (seconds < 3600) {
			return seconds / 60 + "分钟前";
		} else if (seconds < 86400) {
			return seconds / 3600 + "小时前";
		} else {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(mTime * 1000);
			return mDateFormat.format(calendar.getTime());
		}
	}

	public static <T> boolean safeSort(List<T> list, Comparator<? super T> comparator) {
		try {
			Collections.sort(list, comparator);
			return true;
		} catch (Throwable e) {    //	IllegalArgumentException???
		}
		return false;
	}

	public static BitmapDrawable getIconWithColor(Context context) {
		final Resources res = context.getResources();
		Drawable maskDrawable = res.getDrawable(R.drawable.icon_expand);
		if (!(maskDrawable instanceof BitmapDrawable)) {
			return null;
		}

		Bitmap maskBitmap = ((BitmapDrawable) maskDrawable).getBitmap();
		final int width = maskBitmap.getWidth();
		final int height = maskBitmap.getHeight();

		Bitmap outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(outBitmap);
		canvas.drawBitmap(maskBitmap, 0, 0, null);

		Paint maskedPaint = new Paint();
		maskedPaint.setColor(res.getColor(R.color.teal500));
		maskedPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));

		canvas.drawRect(0, 0, width, height, maskedPaint);

		BitmapDrawable outDrawable = new BitmapDrawable(res, outBitmap);
		return outDrawable;
	}
}
