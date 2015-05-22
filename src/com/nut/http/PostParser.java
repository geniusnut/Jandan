package com.nut.http;

import android.graphics.drawable.Drawable;
import android.util.Log;
import com.nut.cache.Post;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yw07 on 15-3-19.
 */
public class PostParser {
	final static String TAG = "";
	final static String POSTS_URL = "http://i.jandan.net/?oxwlxojflwblxbsapi=get_recent_posts&include=url,date,tags,author,title,comment_count,custom_fields&custom_fields=thumb_c&dev=1";
	final static String POST_URL = "http://i.jandan.net/?oxwlxojflwblxbsapi=get_posts";

	private static final DateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static {
		mDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	public ArrayList<Post> parsePosts(int page, final ConcurrentHashMap<String, Drawable> mCovers) {
		final String url = POSTS_URL + "&page=" + page;
		final String content = HttpClient.downloadJson(url);


		ArrayList<Post> posts = new ArrayList<Post>();
		try {
			JSONObject json = new JSONObject(content);
			Log.d(TAG, "Post json: " + json.toString());
			JSONArray jsonPosts = json.getJSONArray("posts");
			for (int i = 0; i < jsonPosts.length(); i++) {
				Post post = new Post();
				JSONObject jsonPost = (JSONObject) jsonPosts.get(i);
				post.mLink = jsonPost.getString("url");
				post.mTitle = jsonPost.getString("title");
				post.mCover = jsonPost.getJSONObject("custom_fields").getJSONArray("thumb_c").getString(0);
				post.mAuthor = jsonPost.getJSONObject("author").getString("name");
				if (jsonPost.getJSONArray("tags").isNull(0))
					post.mTag = "";
				else
					post.mTag = jsonPost.getJSONArray("tags").getJSONObject(0).getString("title");
				post.mCont = jsonPost.getInt("comment_count");
				posts.add(post);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return posts;
	}


	private long parseTime(String time) {
		try {
			return mDateFormat.parse(time).getTime() / 1000;
		} catch (ParseException e) {
			return 0;
		}
	}
}
