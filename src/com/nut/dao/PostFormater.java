package com.nut.dao;

/**
 * Created by yw07 on 14-11-20.
 */

import android.util.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class PostFormater {

	private final String TAG = "PostFormater";

	public static final int POST_FORMAT = 0;
	public static final int COMMENT_FORMAT = 1;

	Document document;

	int timeout = 5000;


	public String postFormater(String link){
		String data;
		String CSS = "<head>\n" +
				"\t<meta http-equiv=\"Content-Type\" content=\"text/html;\" />\n" +
				"\t<title>CSS</title>\n" +
				"\t<style type=\"text/css\">\n" +
				"\t\th2 {\n" +
				"\t\t\tcolor:#e51c23;\n" +
				"\t\t\tfont-size:1em;\n" +
				"\t\t}\n" +
				"\t\tbody {\n" +
				"\t\t\tbackground:#fafafa;\n" +
				"\t\t\tfont-family:Arial, Helvetica, sans-serif;\n" +
				"\t\t\tfont-size:1em;\n" +
				"\t\t\tcolor: #454545; \n" +
				"\t\t\tline-height:160%;"+
				"\t\t\tpadding:3%;\n" +
				"\t\t}\n" +
				"\t\tembed {\n" +
				"\t\t\tdisplay:none;\n" +
				"\t\t}\n" +
				"\t\timg {\n" +
				"\t\t\twidth: 100%;\n" +
				"\t\t\theight: auto\n" +
				"\t\t}\n" +
				"\t\tem {\n" +
				"\t\t\tfont-size:0.9em;\n" +
				"\t\t\tcolor: #b3b3b3; \n" +
				"\t\t}\n" +
				"\t\t.postinfo {\n" +
				"\t\t\tfont-size:0.9em;\n" +
				"\t\t\tcolor: #b3b3b3; \n" +
				"\t\t}\n" +
				"\t\ta {  \n" +
				"\t\t\tfont-size:1.1em;\n" +
				"\t\t\tcolor: #e51c23;  \n" +
				"\t\t\ttext-decoration: none;  \n" +
				"\t\t}  \n" +
				"\t</style>\n" +
				"</head>";

		try {
			document = Jsoup.connect(link)
					.timeout(timeout)
					.userAgent("Mozilla/5.0 (Linux; Android 4.1.1; Nexus 7 Build/JRO03D) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Safari/535.19")
					.get();
		}
		catch (Exception e){
			Log.e(TAG, e.toString());
			return "";
		}

		data = document.getElementsByClass("postinfo").toString()+
				document.getElementsByClass("entry").get(0).toString();

		Log.d(TAG, "Postinfo : " + data);
		String js = "<script type=\"text/javascript\">\n" +
				"    function showAndroidToast(toast) {\n" +
				"        Android.showToast(toast);\n" +
				"    }\n" +
				"</script>";

		return js + CSS + data;
	}

	public String commFormater(String link){
		String data;
		String CSS = "<head>\n" +
				"\t<meta http-equiv=\"Content-Type\" content=\"text/html;\" />\n" +
				"\t<title>CSS</title>\n" +
				"\t<style type=\"text/css\">\n" +
				"\t\tbody {\n" +
				"\t\t\tbackground:#f3f2f4;\n" +
				"\t\t\tfont-family:Arial, Helvetica, sans-serif;\n" +
				"\t\t\tfont-size:0.8em;\n" +
				"\t\t\tcolor: #42454c; \n" +
				"\t\t\tmargin-left:1;\n" +
				"\t\t}\n" +
				"\t\tembed {\n" +
				"\t\t\tdisplay:none;\n" +
				"\t\t}\n" +
				"\t\timg {\n" +
				"\t\t\twidth: 100%;\n" +
				"\t\t\theight: auto\n" +
				"\t\t}\n" +
				"\t\tem {\n" +
				"\t\t\tfont-size:0.9em;\n" +
				"\t\t\tcolor: #b3b4b7; \n" +
				"\t\t}\n" +
				"\t\t.postinfo {\n" +
				"\t\t\tfont-size:0.9em;\n" +
				"\t\t\tcolor: #b3b4b7; \n" +
				"\t\t}\n" +
				"\t\ta {  \n" +
				"\t\t\tdisplay:none;\n" +
				"\t\t}  \n" +
				"\t\t.vote{\n" +
				"\t\t\tdisplay:none;\n" +
				"\t\t}\n" +
				"\n" +
				"\t</style>\n" +
				"</head>";

		try {
			document = Jsoup.connect(link).timeout(timeout).get();
		}
		catch (Exception e){
			Log.e(TAG, e.toString());
			return "";
		}

		data = document.getElementsByClass("commentlist").toString();

		// Log.d(TAG, "CommentInfo : " + data);
		String js = "<script type=\"text/javascript\">\n" +
				"    function showAndroidToast(toast) {\n" +
				"        Android.showToast(toast);\n" +
				"    }\n" +
				"</script>";
		return js + CSS + data;
	}
}

