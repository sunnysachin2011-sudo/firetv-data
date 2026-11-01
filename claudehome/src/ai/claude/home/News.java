package ai.claude.home;

import android.app.Activity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

/** Three top stories (India, Germany, World). Data: news-today.json, written by the 06:46 task. */
public class News {
    static final String URL_JSON = "https://raw.githubusercontent.com/sunnysachin2011-sudo/firetv-data/main/news-today.json";
    static class Story { String region, headline, summary; }
    static List<Story> stories = new ArrayList<>();
    static String date = "";

    static void load(Activity a, Runnable done) {
        new Thread(() -> {
            List<Story> list = new ArrayList<>();
            String d = "";
            try {
                JSONObject o = new JSONObject(Net.text(a, URL_JSON, "news.json"));
                d = o.optString("date");
                JSONArray arr = o.optJSONArray("news");
                for (int i = 0; arr != null && i < arr.length() && i < 3; i++) {
                    JSONObject j = arr.getJSONObject(i); Story s = new Story();
                    s.region = j.optString("region"); s.headline = j.optString("headline"); s.summary = j.optString("summary");
                    list.add(s);
                }
            } catch (Exception ignored) {}
            stories = list; date = d;
            a.runOnUiThread(done);
        }).start();
    }
}
