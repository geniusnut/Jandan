package com.alensw.Jandan;

import android.provider.DocumentsContract.Document;
import android.provider.MediaStore;

/**
 * Created by yw07 on 14-12-12.
 */

public interface NodeColumns {
	public static final String ID = "_id"; //Document.COLUMN_DOCUMENT_ID;        //	unique id for web API
	public static final String TITLE = Document.COLUMN_DISPLAY_NAME;    //	title
	public static final String LAST_MODIFIED = Document.COLUMN_LAST_MODIFIED;//	last modified time from 1970 in seconds
	public static final String DATA = MediaStore.MediaColumns.DATA;
	public static final String URL = "url";
	public static final String ORG_URL = "org_url";

}
