package ai.claude.home;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Football {
    static final String URL_JSON = "https://raw.githubusercontent.com/sunnysachin2011-sudo/firetv-data/main/football-today.json";

    static void load(Activity a, LinearLayout row, View title, View scroll) {
        new Thread(() -> {
            String json = fetch(a);
            a.runOnUiThread(() -> render(a, row, title, scroll, json));
        }).start();
    }

    static String fetch(Activity a) {
        File cache = new File(a.getCacheDir(), "football.json");
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(URL_JSON + "?t=" + System.currentTimeMillis()).openConnection();
            c.setConnectTimeout(6000); c.setReadTimeout(6000);
            try (InputStream in = c.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] b = new byte[4096]; int n; while ((n = in.read(b)) > 0) out.write(b, 0, n);
                String s = out.toString("UTF-8");
                try (FileOutputStream f = new FileOutputStream(cache)) { f.write(s.getBytes("UTF-8")); }
                return s;
            }
        } catch (Exception e) {
            try (FileInputStream f = new FileInputStream(cache); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] b = new byte[4096]; int n; while ((n = f.read(b)) > 0) out.write(b, 0, n);
                return out.toString("UTF-8");
            } catch (Exception ignored) { return null; }
        }
    }

    static void render(Activity a, LinearLayout row, View title, View scroll, String json) {
        row.removeAllViews();
        try {
            JSONObject o = new JSONObject(json);
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
            JSONArray ms = o.optJSONArray("matches");
            boolean stale = !today.equals(o.optString("date"));
            if (ms == null || ms.length() == 0) { empty(a, row, title, scroll, stale ? "No list for today yet" : "No big matches today"); return; }
            ((TextView) title).setText(stale ? "Football  (list from " + o.optString("date") + ")" : "Football today");
            SimpleDateFormat inF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
            SimpleDateFormat outF = new SimpleDateFormat("HH:mm", Locale.US);
            outF.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
            long now = System.currentTimeMillis();
            for (int i = 0; i < ms.length(); i++) {
                JSONObject m = ms.getJSONObject(i);
                View v = LayoutInflater.from(a).inflate(R.layout.match_card, row, false);
                String t = m.optString("kickoff"); long kick = 0;
                try { kick = inF.parse(t).getTime(); t = outF.format(new Date(kick)); } catch (Exception e) { t = t.length() >= 16 ? t.substring(11, 16) : t; }
                boolean live = kick > 0 && now >= kick && now < kick + 2 * 3600 * 1000L;
                ((TextView) v.findViewById(R.id.mTime)).setText(live ? "LIVE  " + t : t);
                ((TextView) v.findViewById(R.id.mTeams)).setText(m.optString("home") + "  –  " + m.optString("away"));
                ((TextView) v.findViewById(R.id.mComp)).setText(m.optString("competition"));
                String app = m.optString("app"), pkg = m.optString("package");
                TextView tvApp = v.findViewById(R.id.mApp);
                tvApp.setText(app); if (pkg.isEmpty()) tvApp.setAlpha(0.5f);
                if (m.optBoolean("big")) v.findViewById(R.id.mBig).setVisibility(View.VISIBLE);
                v.setOnClickListener(x -> {
                    if (pkg.isEmpty()) { Toast.makeText(a, "Not in your apps", Toast.LENGTH_SHORT).show(); return; }
                    Intent li = a.getPackageManager().getLeanbackLaunchIntentForPackage(pkg);
                    if (li == null) li = a.getPackageManager().getLaunchIntentForPackage(pkg);
                    if (li == null) { Toast.makeText(a, app + " not installed", Toast.LENGTH_SHORT).show(); return; }
                    a.startActivity(li);
                });
                v.setOnFocusChangeListener((x, f) -> x.animate().scaleX(f ? 1.05f : 1f).scaleY(f ? 1.05f : 1f).setDuration(120).start());
                row.addView(v);
            }
            title.setVisibility(View.VISIBLE); scroll.setVisibility(View.VISIBLE);
        } catch (Exception e) { empty(a, row, title, scroll, "Football list not available"); }
    }

    static void empty(Activity a, LinearLayout row, View title, View scroll, String msg) {
        ((TextView) title).setText(msg);
        title.setVisibility(View.VISIBLE); scroll.setVisibility(View.GONE);
    }
}
