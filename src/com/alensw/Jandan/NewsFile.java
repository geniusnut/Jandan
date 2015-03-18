package com.alensw.Jandan;

import android.content.Context;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Created by yw07 on 15-3-17.
 */
public class NewsFile extends ListFile<News> {
	public static final String NES_FILE_NAME = "news.bin";

	public boolean load(Context context, String name) {
		return load(context.getFileStreamPath(name));
	}

	@Override
	public String getTypeName() {
		return "News";
	}

	@Override
	public News readEntry(DataInputStream dis) throws Throwable {
		final News news = new News();
		news.mLink = dis.readUTF();
		news.mTitle = dis.readUTF();
		news.mCover = dis.readUTF();
		news.mAuthor = dis.readUTF();
		news.mTag = dis.readUTF();
		news.mCont = dis.readInt();
		return news;
	}

	@Override
	public void writeEntry(DataOutputStream dos, News news) throws Throwable {

		dos.writeUTF(news.mLink);
		dos.writeUTF(news.mTitle);
		dos.writeUTF(news.mCover);
		dos.writeUTF(news.mAuthor);
		dos.writeUTF(news.mTag);
		dos.writeInt(news.mCont);
	}
}
