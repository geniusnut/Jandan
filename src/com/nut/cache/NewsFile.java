package com.nut.cache;

import android.content.Context;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Created by yw07 on 15-3-17.
 */
public class NewsFile extends ListFile<Post> {
	public static final String NEWS_FILE_NAME = "news.bin";

	public boolean load(Context context, String name) {
		return load(context.getFileStreamPath(name));
	}

	@Override
	public String getTypeName() {
		return "News";
	}

	@Override
	public Post readEntry(DataInputStream dis) throws Throwable {
		final Post post = new Post();
		post.mId = dis.readInt();
		post.mLink = dis.readUTF();
		post.mTitle = dis.readUTF();
		post.mCover = dis.readUTF();
		post.mAuthor = dis.readUTF();
		post.mTag = dis.readUTF();
		post.mCont = dis.readInt();
		return post;
	}

	@Override
	public void writeEntry(DataOutputStream dos, Post post) throws Throwable {
		dos.writeInt(post.mId);
		dos.writeUTF(post.mLink);
		dos.writeUTF(post.mTitle);
		dos.writeUTF(post.mCover);
		dos.writeUTF(post.mAuthor);
		dos.writeUTF(post.mTag);
		dos.writeInt(post.mCont);
	}
}
