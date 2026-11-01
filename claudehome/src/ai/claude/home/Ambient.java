package ai.claude.home;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.*;

/** Idle screen: big clock, date, Cologne weather, next match. Any key returns to Home. */
public class Ambient {
    static final String WEATHER = "https://api.open-meteo.com/v1/forecast?latitude=50.94&longitude=6.92&current=temperature_2m,weather_code&daily=temperature_2m_max,temperature_2m_min&timezone=Europe%2FBerlin&forecast_days=1";
    final Activity a; final View root;
    String weather = ""; long weatherAt = 0;
    android.animation.ValueAnimator drift;

    Ambient(Activity a) { this.a = a; root = a.findViewById(R.id.ambient); }

    boolean shown() { return root.getVisibility() == View.VISIBLE; }

    void show() {
        if (shown()) return;
        if (Backdrop.photo != null) ((ImageView) a.findViewById(R.id.ambBack)).setImageBitmap(Backdrop.photo);
        ((TextView) a.findViewById(R.id.ambCredit)).setText(Backdrop.credit);
        update();
        root.setAlpha(0f); root.setVisibility(View.VISIBLE);
        root.animate().alpha(1f).setDuration(1200).start();
        if (drift == null) drift = Motion.kenBurns(a.findViewById(R.id.ambBack), 1.12f, 60000);
        View clock = a.findViewById(R.id.ambClock);
        if (Motion.on()) { clock.setAlpha(0f); clock.setTranslationY(40); clock.animate().alpha(1f).translationY(0).setStartDelay(500).setDuration(900).setInterpolator(Motion.EASE_OUT).start(); }
        root.requestFocus();
    }

    void hide() {
        if (!shown()) return;
        if (drift != null) { drift.cancel(); drift = null; }
        TextView big = a.findViewById(R.id.ambClock);
        TextView small = a.findViewById(R.id.clock);
        ViewGroup box = (ViewGroup) root;
        if (!Motion.on() || small.getWidth() == 0) {
            root.animate().alpha(0f).setDuration(250).withEndAction(() -> root.setVisibility(View.GONE)).start();
            return;
        }
        // The big clock flies into the small clock at the top right; everything else fades.
        int[] b = new int[2], t = new int[2];
        big.getLocationOnScreen(b); small.getLocationOnScreen(t);
        float scale = small.getTextSize() / big.getTextSize();
        big.setPivotX(big.getWidth()); big.setPivotY(big.getHeight() / 2f);
        float dx = (t[0] + small.getWidth()) - (b[0] + big.getWidth());
        float dy = (t[1] + small.getHeight() / 2f) - (b[1] + big.getHeight() / 2f);
        small.setAlpha(0f);
        java.util.List<View> others = new java.util.ArrayList<>();
        collect(box, big, others);
        for (View o : others) o.animate().alpha(0f).setStartDelay(60).setDuration(380).setInterpolator(Motion.STANDARD).start();
        box.setBackgroundColor(0x00000000);
        big.animate().scaleX(scale).scaleY(scale).translationX(dx).translationY(dy).setStartDelay(0).setDuration(560)
                .setInterpolator(new android.view.animation.PathInterpolator(0.45f, 0f, 0.15f, 1f)).withEndAction(() -> {
                    small.animate().alpha(1f).setDuration(160).start();
                    big.animate().alpha(0f).setDuration(120).withEndAction(() -> {
                        root.setVisibility(View.GONE);
                        big.setScaleX(1f); big.setScaleY(1f); big.setTranslationX(0); big.setTranslationY(0); big.setAlpha(1f);
                        for (View o : others) o.setAlpha(1f);
                        box.setBackgroundColor(0xFF0B0D14);
                    }).start();
                }).start();
    }

    /** Leaf views except the clock and its parents. */
    static void collect(ViewGroup g, View keep, java.util.List<View> out) {
        for (int i = 0; i < g.getChildCount(); i++) {
            View c = g.getChildAt(i);
            if (c == keep) continue;
            if (c instanceof ViewGroup && contains((ViewGroup) c, keep)) collect((ViewGroup) c, keep, out);
            else out.add(c);
        }
    }
    static boolean contains(ViewGroup g, View v) { for (android.view.ViewParent p = v.getParent(); p != null; p = p.getParent()) if (p == g) return true; return false; }

    void update() {
        Date now = new Date();
        ((TextView) a.findViewById(R.id.ambClock)).setText(new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(now));
        ((TextView) a.findViewById(R.id.ambDate)).setText(new SimpleDateFormat("EEEE d MMMM", L.dateLocale()).format(now));
        ((TextView) a.findViewById(R.id.ambNext)).setText(nextMatch());
        ((TextView) a.findViewById(R.id.ambWeather)).setText(weather);
        if (System.currentTimeMillis() - weatherAt > 30 * 60000L) loadWeather();
    }

    static String nextMatch() {
        long now = System.currentTimeMillis();
        Football.Match best = null;
        for (Football.Match m : Football.matches) {
            if (now >= m.kick + m.dur) continue;
            if (best == null || (m.live(now) && !best.live(now)) || (m.live(now) == best.live(now) && m.kick < best.kick)) best = m;
        }
        if (best == null) return "";
        SimpleDateFormat f = new SimpleDateFormat("HH:mm", Locale.US); f.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        return (best.live(now) ? "● " + L.t("livenow") + "   " : f.format(new Date(best.kick)) + "   ") + best.h() + " – " + best.a() + "   ·   " + best.app;
    }

    void loadWeather() {
        weatherAt = System.currentTimeMillis();
        new Thread(() -> {
            try {
                JSONObject o = new JSONObject(Net.text(a, WEATHER, "weather.json"));
                JSONObject c = o.getJSONObject("current");
                JSONObject d = o.getJSONObject("daily");
                String s = Math.round(c.getDouble("temperature_2m")) + "°  " + word(c.getInt("weather_code"))
                        + "\n" + L.t("cologne") + "  ·  " + Math.round(d.getJSONArray("temperature_2m_max").getDouble(0)) + "° / "
                        + Math.round(d.getJSONArray("temperature_2m_min").getDouble(0)) + "°";
                weather = s;
                a.runOnUiThread(() -> ((TextView) a.findViewById(R.id.ambWeather)).setText(weather));
            } catch (Exception ignored) {}
        }).start();
    }

    static String word(int code) {
        if (code == 0) return L.t("w.clear");
        if (code <= 2) return L.t("w.partly");
        if (code == 3) return L.t("w.cloudy");
        if (code == 45 || code == 48) return L.t("w.fog");
        if (code >= 51 && code <= 57) return L.t("w.drizzle");
        if (code >= 61 && code <= 67) return L.t("w.rain");
        if (code >= 71 && code <= 77) return L.t("w.snow");
        if (code >= 80 && code <= 82) return L.t("w.showers");
        if (code >= 85 && code <= 86) return L.t("w.snowshowers");
        if (code >= 95) return L.t("w.thunder");
        return "";
    }
}
