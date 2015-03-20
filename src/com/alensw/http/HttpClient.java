package com.alensw.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by yw07 on 15-3-19.
 * A static class for download things from Jandan api.
 */
public class HttpClient {
	private static final int TIMEOUT_CONNECT = 15 * 1000;
	private static final int TIMEOUT_READ = 60 * 1000;

	private static final int BLOCK_SIZE = 16 * 1024;

	public static String downloadJson(String url) {
		String content = null;
		ByteArrayOutputStream bos = null;
		try {
			final HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setConnectTimeout(TIMEOUT_CONNECT);
			conn.setDoInput(true);
			conn.setReadTimeout(TIMEOUT_READ);
			conn.setRequestMethod("GET");

			InputStream is = conn.getInputStream();
			bos = new ByteArrayOutputStream(1024 * 256);
			final byte[] data = new byte[4096];
			int bytes = 0;
			while ((bytes = is.read(data)) >= 0) {
				if (bytes > 0) {
					bos.write(data, 0, bytes);
				}
			}
			return new String(bos.toByteArray(), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

		}
		return content;
	}
}
