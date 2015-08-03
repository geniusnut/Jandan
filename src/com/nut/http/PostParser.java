package com.nut.http;

import android.graphics.drawable.Drawable;
import android.util.Log;
import com.nut.cache.Post;
import com.nut.dao.CommentModel;
import com.nut.dao.DuoshuoComment;
import com.nut.dao.JokeModel;
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

	public static String parseContent(String id) {
		final String url = POST_URL + "&id=" + id + "&include=content";
		final String content = HttpClient.downloadJson(url);
		try {
			JSONObject json = new JSONObject(content);
			return json.getJSONObject("post").getString("content");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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

	//	comments":[
//	{
//		"comment_ID":"2880802",
//			"comment_post_ID":"55592",
//			"comment_author":"光消失的地方",
//			"comment_author_email":"aqua5200@qq.com",
//			"comment_author_url":"",
//			"comment_author_IP":"1.57.117.249",
//			"comment_date":"2015-07-29 08:05:32",
//			"comment_date_gmt":"2015-07-29 00:05:32",
//			"comment_content":"天秤座的我在爱情方面总是犹豫不决，不知道该喜欢女生还是男生。",
//			"comment_karma":"0",
//			"comment_approved":"1",
//			"comment_agent":"UCWEB/2.0 (MIDP-2.0; U; zh-CN; TCL M2M) U2/1.0.0 UCBrowser/10.6.0.620 U2/1.0.0 Mobile",
//			"comment_type":"",
//			"comment_parent":"0",
//			"user_id":"0",
//			"comment_subscribe":"N",
//			"comment_reply_ID":"0",
//			"vote_positive":"12",
//			"vote_negative":"0",
//			"text_content":"天秤座的我在爱情方面总是犹豫不决，不知道该喜欢女生还是男生。",
//			"videos":{
//	}
	public static ArrayList<JokeModel> parseJokes(int page) {
		final String url = JOKE_URL + "&page=" + page;
		final String content = HttpClient.downloadJson(url);

		ArrayList<JokeModel> jokes = new ArrayList<JokeModel>();
		try {
			JSONObject json = new JSONObject(content);
			Log.d(TAG, "Post json: " + json.toString());
			int totalJokes = json.getInt("total_comments");
			int firstId = totalJokes - (page - 1) * 25 - 1;
			JSONArray jsonJokes = json.getJSONArray("comments");
			for (int i = 0; i < jsonJokes.length(); i++) {
				JokeModel joke = new JokeModel();
				JSONObject jsonJoke = (JSONObject) jsonJokes.get(i);
				joke.mId = firstId - i;
				joke.mAuthor = jsonJoke.getString("comment_author");
				joke.mDate = jsonJoke.getString("comment_date");
				joke.mContent = jsonJoke.getString("text_content");
				joke.mPositive = jsonJoke.getInt("vote_positive");
				joke.mNegative = jsonJoke.getInt("vote_negative");
				joke.mCommentId = jsonJoke.getLong("comment_ID");
				jokes.add(joke);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jokes;
	}


	// http://jandan.duoshuo.com/api/threads/counts.json?threads=comment-2881436,comment-2881435,comment-2881340,comment-2881324,comment-2881322,comment-2881307,comment-2881287,comment-2881257,comment-2881217,comment-2881158,comment-2881157,comment-2881147,comment-2881142,comment-2881110,comment-2881102,comment-2881100,comment-2881061,comment-2881053,comment-2881042,comment-2881023,comment-2881013,comment-2880945,comment-2880905,comment-2880903,comment-2880802,
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

	// http://jandan.duoshuo.com/api/threads/listPosts.json?thread_key=comment-2886207

	public static DuoshuoComment getDuoshuoComments(String commentId) {
		final String url = " http://jandan.duoshuo.com/api/threads/listPosts.json?thread_key=comment-" + commentId;
		final String content = HttpClient.downloadJson(url);

		return new DuoshuoComment(content);
	}


}
