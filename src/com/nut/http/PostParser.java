package com.nut.http;

import android.graphics.drawable.Drawable;
import android.util.Log;
import com.nut.dao.CommentModel;
import com.nut.cache.Post;
import com.nut.dao.JokeModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yw07 on 15-3-19.
 */
public class PostParser {
	final static String TAG = "";
	final static String POSTS_URL = "http://i.jandan.net/?oxwlxojflwblxbsapi=get_recent_posts&include=url,date,tags,author,title,comment_count,custom_fields&custom_fields=thumb_c&dev=1";
	final static String POST_URL = "http://i.jandan.net/?oxwlxojflwblxbsapi=get_post";
	final static String JOKE_URL = "http://i.jandan.net/?oxwlxojflwblxbsapi=jandan.get_duan_comments";
	// ?oxwlxojflwblxbsapi=get_post&id=62799&include=content
	// http://i.jandan.net/?oxwlxojflwblxbsapi=get_post&id=62799&include=comments

	//  http://jandan.duoshuo.com/api/threads/listPosts.json?thread_key=comment-2822415

	private static final DateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static {
		mDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	public static ArrayList<Post> parsePosts(int page, final ConcurrentHashMap<String, Drawable> mCovers) {
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
				post.mId = jsonPost.getInt("id");
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

	public static ArrayList<CommentModel> parseComments(String id) {
		final String url = POST_URL + "&id=" + id + "&include=comments";
		final String content = HttpClient.downloadJson(url);

		ArrayList<CommentModel> comments = new ArrayList<CommentModel>();
		try {
			JSONObject json = new JSONObject(content);
			Log.d(TAG, "Post json: " + json.toString());
			JSONArray jsonPosts = json.getJSONObject("post").getJSONArray("comments");
			for (int i = 0; i < jsonPosts.length(); i++) {
				CommentModel comment = new CommentModel();
				JSONObject jsonComment = (JSONObject) jsonPosts.get(i);
				comment.mAuthor = jsonComment.getString("name");
				comment.mContent = jsonComment.getString("content");
				comment.mDate = jsonComment.getString("date");
				comment.mPositive = jsonComment.getInt("vote_positive");
				comment.mNegative = jsonComment.getInt("vote_negative");
				comment.mIndex = jsonComment.getInt("index");
				comments.add(comment);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return comments;
	}

	public static ArrayList<JokeModel> parseJokes(int page) {
		final String url = JOKE_URL + "&page=" + page;
		final String content = HttpClient.downloadJson(url);

		ArrayList<JokeModel> jokes = new ArrayList<JokeModel>();
		try {
			JSONObject json = new JSONObject(content);
			Log.d(TAG, "Post json: " + json.toString());
			JSONArray jsonPosts = json.getJSONArray("comments");
			for (int i = 0; i < jsonPosts.length(); i++) {
				JokeModel joke = new JokeModel();

				jokes.add(joke);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jokes;
	}

	private long parseTime(String time) {
		try {
			return mDateFormat.parse(time).getTime() / 1000;
		} catch (ParseException e) {
			return 0;
		}
	}

	// http://i.jandan.net/?oxwlxojflwblxbsapi=jandan.get_duan_comments
	// http://i.jandan.net/?oxwlxojflwblxbsapi=jandan.get_duan_comments&page=2
	// http://i.jandan.net/?oxwlxojflwblxbsapi=get_post&id=66451&include=content
}
