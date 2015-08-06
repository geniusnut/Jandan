package com.nut.http;

import android.os.Build;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

/**
 * Created by yw07 on 15-3-19.
 * A static class for download things from Jandan api.
 */
public class HttpClient {
	private static final int TIMEOUT_CONNECT = 15 * 1000;
	private static final int TIMEOUT_READ = 60 * 1000;

	private static final int BLOCK_SIZE = 16 * 1024;
	private static String TAG = "HttpClient";

	public static String downloadJson(String url) {
		String content = null;
		ByteArrayOutputStream bos = null;
		try {
			final HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			InputStream is = conn.getInputStream();

			if (conn.getContentEncoding()!= null && conn.getContentEncoding().equals("gzip")) {
				is = new GZIPInputStream(is, BLOCK_SIZE);
			}
			bos = new ByteArrayOutputStream(1024 * 256);
			final byte[] data = new byte[4096];
			int bytes = 0;
			while ((bytes = is.read(data)) >= 0) {
				if (bytes > 0) {
					bos.write(data, 0, bytes);
				}
			}
			return new String(bos.toByteArray(), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
		return content;
	}

	public static void uploadString(String url, String content) {
		try {
			final HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setConnectTimeout(TIMEOUT_CONNECT);
			conn.setReadTimeout(TIMEOUT_READ);
			conn.setRequestMethod("POST");
			// conn.setRequestProperty("Cookie", "duoshuo_unique=485b62b18abf63e9");

			byte[] data = content.getBytes("UTF-8");
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length", String.valueOf(data.length));
			setContentLength(conn, data.length);

			final OutputStream os = conn.getOutputStream();
			os.write(data);
			os.flush();
			os.close();

			int statusCode = 0;
			try {
				statusCode = conn.getResponseCode();
				Log.d(TAG, "statusCode: " + statusCode);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
		}
	}

	public static void setContentLength(HttpURLConnection conn, long length) {
		// set the length of internal delegate
		final Class<?> cls = conn.getClass();
		try {
			// for Android 3.0+
			final Field field = cls.getDeclaredField("delegate");
			field.setAccessible(true);
			setContentLength((HttpURLConnection) field.get(conn), length);
		} catch (NoSuchFieldException e) {
			try {
				// for Android 2.x
				final Field field = cls.getDeclaredField("httpsEngine");
				field.setAccessible(true);
				setContentLength((HttpURLConnection) field.get(conn), length);
			} catch (Exception e1) {
			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		conn.setRequestProperty("Content-Length", Long.toString(length));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			conn.setFixedLengthStreamingMode(length);
		else if (length <= Integer.MAX_VALUE)
			conn.setFixedLengthStreamingMode((int) length);
	}

}
