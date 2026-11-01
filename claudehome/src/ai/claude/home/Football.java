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
import java.util.*;

public class Football {
    static final String URL_JSON = "https://raw.githubusercontent.com/sunnysachin2011-sudo/firetv-data/main/football-today.json";
    static final long MATCH_MS = 115 * 60 * 1000L;   // typical length incl. half time
    static final long ALERT_MS = 10 * 60 * 1000L;    // banner from 10 min before kick-off

    static class Match {
        long kick, dur = MATCH_MS; String home, away, comp, app, pkg, sport = "football"; boolean big;
        boolean live(long now) { return kick > 0 && now >= kick && now < kick + dur; }
        boolean soon(long now) { return kick > 0 && now >= kick - ALERT_MS && now < kick; }
        String teams() { return h() + "  –  " + a(); }
        java.util.Map<String, String[]> tr = new java.util.HashMap<>();   // lang -> {home, away, comp}
        String pick(int i, String en) { String[] v = tr.get(L.lang); return v == null || v[i] == null || v[i].isEmpty() ? en : v[i]; }
        String h() { return pick(0, home); }
        String a() { return pick(1, away); }
        String c() { return pick(2, comp); }
    }

    static List<Match> matches = new ArrayList<>();
    static String listDate = "";

    static void load(Activity a, LinearLayout row, View title, View scroll) {
        new Thread(() -> {
            String json = fetch(a);
            parse(json);
            a.runOnUiThread(() -> { render(a, row, title, scroll); if (a instanceof MainActivity) ((MainActivity) a).updateAlert(); });
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

    static void parse(String json) {
        List<Match> list = new ArrayList<>();
        try {
            JSONObject o = new JSONObject(json);
            listDate = o.optString("date");
            JSONArray ms = o.optJSONArray("matches");
            SimpleDateFormat inF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
            for (int i = 0; ms != null && i < ms.length(); i++) {
                JSONObject j = ms.getJSONObject(i); Match m = new Match();
                try { m.kick = inF.parse(j.optString("kickoff")).getTime(); } catch (Exception e) { m.kick = 0; }
                m.home = j.optString("home"); m.away = j.optString("away"); m.comp = j.optString("competition");
                for (String lg : new String[]{"hi", "de"}) m.tr.put(lg, new String[]{ j.optString("home_" + lg), j.optString("away_" + lg), j.optString("competition_" + lg) });
                m.app = j.optString("app"); m.pkg = j.optString("package"); m.big = j.optBoolean("big");
                m.sport = j.optString("sport", "football");
                if (j.has("duration_min")) m.dur = j.optLong("duration_min") * 60000L; else if (m.sport.equals("cricket")) m.dur = 8 * 3600 * 1000L;
                list.add(m);
            }
        } catch (Exception ignored) { listDate = ""; }
        matches = list;
    }

    static void render(Activity a, LinearLayout row, View title, View scroll) { render(a, row, title, scroll, true); }

    static void render(Activity a, LinearLayout row, View title, View scroll, boolean notify) {
        if (notify && a instanceof MainActivity) ((MainActivity) a).footballRendered(!matches.isEmpty());
        row.removeAllViews();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        boolean stale = !today.equals(listDate);
        if (matches.isEmpty()) { empty(title, scroll, listDate.isEmpty() ? L.t("nosport") : stale ? L.t("nolist") : L.t("nomatches")); return; }
        ((TextView) title).setText(stale ? L.f("sportfrom", listDate) : L.t("sporttoday"));
        SimpleDateFormat outF = new SimpleDateFormat("HH:mm", Locale.US);
        outF.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        long now = System.currentTimeMillis();
        List<Match> ordered = new ArrayList<>(matches);
        Collections.sort(ordered, (x, y) -> {
            boolean lx = x.live(now), ly = y.live(now);
            if (lx != ly) return lx ? -1 : 1;
            boolean ox = now >= x.kick + x.dur, oy = now >= y.kick + y.dur;   // finished go last
            if (ox != oy) return ox ? 1 : -1;
            return Long.compare(x.kick, y.kick);
        });
        for (Match m : ordered) {
            View v = LayoutInflater.from(a).inflate(R.layout.match_card, row, false); Ui.apply(v);
            TextView tvTime = v.findViewById(R.id.mTime);
            boolean live = m.live(now), over = now >= m.kick + m.dur;
            String t = m.kick > 0 ? outF.format(new Date(m.kick)) : "";
            if (live) {
                long left = Math.max(1, (m.kick + m.dur - now) / 60000);
                tvTime.setText("● " + L.t("live") + "   " + (left > 90 ? L.f("ends", outF.format(new Date(m.kick + m.dur))) : L.f("endsin", left)));
                tvTime.setTextColor(a.getColor(R.color.live));
            } else if (over) { tvTime.setText(t + "   " + L.t("finished")); tvTime.setTextColor(a.getColor(R.color.muted)); v.setAlpha(0.55f); }
            else tvTime.setText(t);
            ((TextView) v.findViewById(R.id.mTeams)).setText(m.teams());
            ((TextView) v.findViewById(R.id.mComp)).setText((m.sport.equals("cricket") ? "\uD83C\uDFCF " : "\u26BD ") + m.c());
            TextView tvApp = v.findViewById(R.id.mApp);
            tvApp.setText(m.app); if (m.pkg.isEmpty()) tvApp.setAlpha(0.5f);
            if (m.big) v.findViewById(R.id.mBig).setVisibility(View.VISIBLE);
            v.setOnClickListener(x -> Motion.zoomLaunch(a, x, MainActivity.tintFor(m.pkg), () -> open(a, m)));
            v.setOnFocusChangeListener((x, f) -> x.animate().scaleX(f ? 1.05f : 1f).scaleY(f ? 1.05f : 1f).setDuration(120).start());
            row.addView(v);
        }
        title.setVisibility(View.VISIBLE); scroll.setVisibility(View.VISIBLE);
    }

    static void open(Activity a, Match m) {
        if (m.pkg.isEmpty()) { Toast.makeText(a, L.t("notapps"), Toast.LENGTH_SHORT).show(); return; }
        Intent li = a.getPackageManager().getLeanbackLaunchIntentForPackage(m.pkg);
        if (li == null) li = a.getPackageManager().getLaunchIntentForPackage(m.pkg);
        if (li == null) { Toast.makeText(a, L.f("notinstalled", m.app), Toast.LENGTH_SHORT).show(); return; }
        a.startActivity(li);
    }

    /** Banner: next match that starts within 10 min, or a match that is live. Returns null when nothing to show. */
    static Match alertMatch() {
        long now = System.currentTimeMillis();
        Match best = null;
        for (Match m : matches) if ((m.soon(now) || m.live(now)) && !m.pkg.isEmpty() && (best == null || m.kick < best.kick)) best = m;
        return best;
    }

    static String alertText(Match m) {
        long now = System.currentTimeMillis();
        if (m.live(now)) return L.f("alertlive", m.h() + " – " + m.a(), m.app);
        long min = Math.max(1, (m.kick - now) / 60000);
        return L.f("alertsoon", m.h() + " – " + m.a(), min, m.app);
    }

    static void empty(View title, View scroll, String msg) {
        ((TextView) title).setText(msg);
        title.setVisibility(View.VISIBLE); scroll.setVisibility(View.GONE);
    }
}
