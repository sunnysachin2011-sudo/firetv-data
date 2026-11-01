package ai.claude.home;

import android.app.Activity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

/** Three top stories (India, Germany, World). Data: news-today.json, written by the 06:46 task. */
public class News {
    static final String URL_JSON = "https://raw.githubusercontent.com/sunnysachin2011-sudo/firetv-data/main/news-today.json";
    static class Story {
        String region, headline, summary; Map<String, String> head = new HashMap<>(), sum = new HashMap<>();
        String h() { String v = head.get(L.lang); return v == null || v.isEmpty() ? headline : v; }
        String s() { String v = sum.get(L.lang); return v == null || v.isEmpty() ? summary : v; }
    }
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
                    for (String lg : new String[]{"hi", "de"}) { s.head.put(lg, j.optString("headline_" + lg)); s.sum.put(lg, j.optString("summary_" + lg)); }
                    list.add(s);
                }
            } catch (Exception ignored) {}
            stories = list; date = d;
            a.runOnUiThread(done);
        }).start();
    }
}
