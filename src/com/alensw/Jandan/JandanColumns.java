package com.alensw.Jandan;

import java.util.HashMap;

/**
 * Created by yw07 on 14-11-25.
 */
public class JandanColumns {
	public static class Column {
		public ColumnID cid;
		public int icon;
		public int title;
		public int summary;
	}

	private static final Column[] mColumns = new Column[] {
			new Column() {{
				cid = ColumnID.NEWS;
				icon = R.raw.news;
				title  = R.string.column_news;
				summary = 0;
			}},
	};

	private static HashMap<String, Object> init(Column column) {
		return null;
	};
}
