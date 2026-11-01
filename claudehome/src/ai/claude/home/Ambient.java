package ai.claude.home;

import android.app.Activity;
import android.view.View;
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
        root.animate().alpha(0f).setDuration(250).withEndAction(() -> root.setVisibility(View.GONE)).start();
    }

    void update() {
        Date now = new Date();
        ((TextView) a.findViewById(R.id.ambClock)).setText(new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(now));
        ((TextView) a.findViewById(R.id.ambDate)).setText(new SimpleDateFormat("EEEE d MMMM", Locale.ENGLISH).format(now));
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
        return (best.live(now) ? "● Live now   " : f.format(new Date(best.kick)) + "   ") + best.home + " – " + best.away + "   ·   " + best.app;
    }

    void loadWeather() {
        weatherAt = System.currentTimeMillis();
        new Thread(() -> {
            try {
                JSONObject o = new JSONObject(Net.text(a, WEATHER, "weather.json"));
                JSONObject c = o.getJSONObject("current");
                JSONObject d = o.getJSONObject("daily");
                String s = Math.round(c.getDouble("temperature_2m")) + "°  " + word(c.getInt("weather_code"))
                        + "\nCologne  ·  " + Math.round(d.getJSONArray("temperature_2m_max").getDouble(0)) + "° / "
                        + Math.round(d.getJSONArray("temperature_2m_min").getDouble(0)) + "°";
                weather = s;
                a.runOnUiThread(() -> ((TextView) a.findViewById(R.id.ambWeather)).setText(weather));
            } catch (Exception ignored) {}
        }).start();
    }

    static String word(int code) {
        if (code == 0) return "Clear";
        if (code <= 2) return "Partly cloudy";
        if (code == 3) return "Cloudy";
        if (code == 45 || code == 48) return "Fog";
        if (code >= 51 && code <= 57) return "Drizzle";
        if (code >= 61 && code <= 67) return "Rain";
        if (code >= 71 && code <= 77) return "Snow";
        if (code >= 80 && code <= 82) return "Showers";
        if (code >= 85 && code <= 86) return "Snow showers";
        if (code >= 95) return "Thunder";
        return "";
    }
}
