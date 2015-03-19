package com.alensw.Jandan;

import android.content.Context;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;

/**
 * Created by yw07 on 15-3-19.
 */
public class PicFile extends ListFile<Pic> {
	public static final String PIC_FILE_NAME = "pics.bin";

	public boolean load(Context context, String name) {
		return load(context.getFileStreamPath(name));
	}

	@Override
	public String getTypeName() {
		return "Pics";
	}

	@Override
	public Pic readEntry(DataInputStream dis) throws Throwable {
		final Pic pic = new Pic();
		pic.mId = dis.readInt();
		pic.mAuthor = dis.readUTF();
		pic.mDesc = dis.readUTF();
		pic.mPhotos = dis.readInt();
		pic.mUrls = new ArrayList<>(pic.mPhotos);
		if (pic.mPhotos > 0)
			for (int i = 0; i < pic.mPhotos; i++)
				pic.mUrls.add(dis.readUTF());
		pic.mOO = dis.readInt();
		pic.mXX = dis.readInt();
		pic.mTime = dis.readLong();
		return pic;
	}

	@Override
	public void writeEntry(DataOutputStream dos, Pic pic) throws Throwable {
		dos.writeInt(pic.mId);
		dos.writeUTF(pic.mAuthor);
		dos.writeUTF(pic.mDesc);
		dos.writeInt(pic.mPhotos);
		if (pic.mPhotos > 0)
			for (int i = 0; i < pic.mPhotos; i++)
				dos.writeUTF(pic.mUrls.get(i));
		dos.writeInt(pic.mOO);
		dos.writeInt(pic.mXX);
		dos.writeLong(pic.mTime);
	}
}
