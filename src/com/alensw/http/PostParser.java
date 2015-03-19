package com.alensw.http;

import android.graphics.Bitmap;
import com.alensw.Jandan.Post;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yw07 on 15-3-19.
 */
public class PostParser {
	final static String TAG = "";
	final static String POST_URL = "http://i.jandan.net/?oxwlxojflwblxbsapi=get_recent_posts&include=url,date,tags,author,title,comment_count,custom_fields&custom_fields=thumb_c&dev=1";

	public ArrayList<Post> parse(int page, final ConcurrentHashMap<String, Bitmap> mCovers) {
		final String url = POST_URL + "&page=" + page;
		final String content = HttpClient.downloadJson(url);


		ArrayList<Post> posts = new ArrayList<Post>();
		try {
			JSONObject json = new JSONObject(content);
			JSONArray jsonPosts = json.getJSONArray("posts");
			for (int i = 0; i < jsonPosts.length(); i++) {
				Post post = new Post();
				JSONObject jsonPost = (JSONObject) jsonPosts.get(i);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return posts;
	}
}
