package com.nut.dao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by yw07 on 15-8-3.
 */
public class DuoshuoComment {
	public ArrayList<String> mHotIds = new ArrayList<>();

	public final ArrayList<Comment> mComments;
	public final ArrayList<Comment> mHotComments;

	public String mThreadId;

	public DuoshuoComment() {
		mComments = new ArrayList<>();
		mHotComments = new ArrayList<>();
	}

	public int getHotSize() {
		return mHotComments.size();
	}

	public int getSize() {
		return mComments.size();
	}

	public ArrayList<Comment> getHotComments() {
		ArrayList<Comment> hotComments = new ArrayList<>();
		for (String id : mHotIds) {
			hotComments.add(getComment(id));
		}
		return hotComments;
	}

	private Comment getComment(String id) {
		if (mComments.size() <= 0)
			throw new NullPointerException("mComments has not been initialized");
		for (Comment comment : mComments) {
			if (id.equals(comment.mId)) {
				return comment;
			}
		}
		return null;
	}

	public void update(String content) {
		mComments.clear();

		mHotIds.clear();
		mHotComments.clear();
		try {
			JSONObject json = new JSONObject(content);

			mThreadId = json.getJSONObject("thread").getString("thread_id");
			JSONArray jsonHotComments = json.getJSONArray("hotPosts");
			for (int i = 0; i < jsonHotComments.length(); i++) {
				mHotIds.add((String) jsonHotComments.get(i));
			}

			JSONArray jsonArrayComments = json.getJSONArray("response");
			JSONObject jsonComments = json.getJSONObject("parentPosts");
			for (int i = 0; i < jsonArrayComments.length(); i++) {
				Comment comment = new Comment();
				JSONObject jsonComment = jsonComments.getJSONObject((String) jsonArrayComments.get(i));
				comment.mId = jsonComment.getString("post_id");
				comment.mParentId = jsonComment.getLong("parent_id");
				comment.mContent = jsonComment.getString("message");
				comment.mDate = jsonComment.getString("created_at");
				comment.mLikes = jsonComment.getInt("likes");
				JSONObject jsonAuthor = jsonComment.getJSONObject("author");
				comment.mAuthor = jsonAuthor.getString("name");
				comment.mAvatar = jsonAuthor.getString("avatar_url");
				mComments.add(comment);
			}
			for (String id : mHotIds) {
				mHotComments.add(getComment(id));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static class Comment {
		public String mId;
		public long mParentId;
		public long mRootId;
		public String mAvatar;
		public String mAuthor;
		public String mDate;
		public String mContent;
		public int mLikes;
	}
}
