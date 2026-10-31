package ai.claude.home;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    static final String LOUNGE_URL = "https://claude.ai/artifact/C7a9Le6NmwLNHvGgBMMcnV";
    static final String SILK = "com.amazon.cloud9";
    static final String TVBRO = "com.phlox.tvwebbrowser";
    static final String LOUNGE_KEY = "claude.lounge";
    static final String[] FAVOURITES = { "in.startv.hotstar", "com.onemainstream.sonyliv.android", "com.fancode.tv",
            "com.netflix.ninja", "com.amazon.firetv.youtube", "com.spotify.tv.android", "com.zee5.amazon", LOUNGE_KEY, TVBRO, "com.amazon.tv.launcher" };

    static class Tile {
        String key, label; Drawable icon; Intent intent; int uses;
        Tile(String k, String l, Drawable i, Intent in) { key = k; label = l; icon = i; intent = in; }
    }

    final List<Tile> tiles = new ArrayList<>();
    final Map<String, Tile> byKey = new HashMap<>();
    final Handler handler = new Handler();
    SharedPreferences prefs;
    TextView clock; GridView grid; TileAdapter adapter;
    TextView[] tabs; View[] pages; int tab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("usage", MODE_PRIVATE);
        clock = findViewById(R.id.clock);
        grid = findViewById(R.id.grid);
        tabs = new TextView[]{ findViewById(R.id.tabForYou), findViewById(R.id.tabFootball), findViewById(R.id.tabApps) };
        pages = new View[]{ findViewById(R.id.pageForYou), findViewById(R.id.pageFootball), grid };
        for (int i = 0; i < tabs.length; i++) {
            final int idx = i;
            tabs[i].setOnClickListener(v -> showTab(idx));
            tabs[i].setOnFocusChangeListener((v, f) -> { if (f) showTab(idx); });
        }
        buildTiles();
        adapter = new TileAdapter();
        grid.setAdapter(adapter);
        grid.setOnItemClickListener((p, v, pos, id) -> launch(tiles.get(pos)));
        grid.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            View last;
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                if (last != null) last.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
                if (v != null) v.animate().scaleX(1.08f).scaleY(1.08f).setDuration(120).start();
                last = v;
            }
            public void onNothingSelected(AdapterView<?> p) {}
        });
        buildDock();
        showTab(0);
        Posters.load(this, findViewById(R.id.heroRow), findViewById(R.id.heroTitle));
        tick();
        handler.postDelayed(() -> { View first = ((LinearLayout) findViewById(R.id.heroRow)).getChildAt(0); if (first != null) first.requestFocus(); else findViewById(R.id.tabForYou).requestFocus(); }, 600);
    }

    void showTab(int idx) {
        tab = idx;
        for (int i = 0; i < tabs.length; i++) { tabs[i].setSelected(i == idx); pages[i].setVisibility(i == idx ? View.VISIBLE : View.GONE); }
        findViewById(R.id.dockScroll).setVisibility(idx == 2 ? View.GONE : View.VISIBLE);
    }

    @Override protected void onResume() {
        super.onResume();
        Football.load(this, findViewById(R.id.footballRow), findViewById(R.id.footballTitle), findViewById(R.id.footballScroll));
        sortTiles(); if (adapter != null) adapter.notifyDataSetChanged();
        buildDock();
    }

    void tick() {
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        ((TextView) findViewById(R.id.greeting)).setText(h < 12 ? "Good morning" : h < 18 ? "Good afternoon" : "Good evening");
        clock.setText(new SimpleDateFormat("EEE d MMM  HH:mm", Locale.ENGLISH).format(new Date()));
        updateAlert();
        handler.postDelayed(this::tick, 30000);
    }

    long lastRender = 0;
    void updateAlert() {
        View alert = findViewById(R.id.alert);
        Football.Match m = Football.alertMatch();
        if (m == null) { alert.setVisibility(View.GONE); }
        else {
            ((TextView) findViewById(R.id.alertText)).setText(Football.alertText(m));
            alert.setOnClickListener(x -> Football.open(this, m));
            if (alert.getVisibility() != View.VISIBLE) { alert.setVisibility(View.VISIBLE); alert.requestFocus(); }
        }
        long now = System.currentTimeMillis();
        if (now - lastRender > 60000 && !Football.matches.isEmpty()) {
            lastRender = now;
            Football.render(this, findViewById(R.id.footballRow), findViewById(R.id.footballTitle), findViewById(R.id.footballScroll));
        }
    }

    /** Called by Football after each render: mirror the row into the Football tab and hide the row on quiet days. */
    void footballRendered(boolean hasMatches) {
        LinearLayout src = findViewById(R.id.footballRow), dst = findViewById(R.id.footballRow2);
        findViewById(R.id.footballTitle).setVisibility(hasMatches ? View.VISIBLE : View.GONE);
        findViewById(R.id.footballScroll).setVisibility(hasMatches ? View.VISIBLE : View.GONE);
        Football.render(this, dst, findViewById(R.id.footballTitle2), findViewById(R.id.footballScroll2), false);
    }

    @Override protected void onDestroy() { handler.removeCallbacksAndMessages(null); super.onDestroy(); }

    void buildTiles() {
        tiles.clear(); byKey.clear();
        PackageManager pm = getPackageManager();
        Intent lounge = new Intent(Intent.ACTION_VIEW, Uri.parse(LOUNGE_URL)).setPackage(TVBRO);
        if (pm.resolveActivity(lounge, 0) == null) lounge = new Intent(Intent.ACTION_VIEW, Uri.parse(LOUNGE_URL)).setPackage(SILK);
        if (pm.resolveActivity(lounge, 0) == null) lounge = new Intent(Intent.ACTION_VIEW, Uri.parse(LOUNGE_URL));
        add(new Tile(LOUNGE_KEY, "Claude Lounge", getDrawable(R.drawable.banner), lounge));
        for (String cat : new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER}) {
            Intent q = new Intent(Intent.ACTION_MAIN).addCategory(cat);
            for (ResolveInfo ri : pm.queryIntentActivities(q, 0)) {
                String pkg = ri.activityInfo.packageName;
                if (byKey.containsKey(pkg) || pkg.equals(getPackageName())) continue;
                if (pkg.startsWith("com.amazon.") && !pkg.equals(SILK) && !pkg.equals("com.amazon.firetv.youtube")
                        && !pkg.equals("com.amazon.tv.settings.v2") && !pkg.equals("com.amazon.tv.launcher")) continue;
                Drawable icon = ri.activityInfo.loadBanner(pm);
                if (icon == null) icon = ri.loadIcon(pm);
                Intent launch = new Intent(Intent.ACTION_MAIN).addCategory(cat)
                        .setClassName(pkg, ri.activityInfo.name).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                String label = String.valueOf(ri.loadLabel(pm));
                if (pkg.equals("com.amazon.tv.launcher")) label = "Fire TV Home";
                add(new Tile(pkg, label, icon, launch));
            }
        }
        sortTiles();
    }

    void add(Tile t) { tiles.add(t); byKey.put(t.key, t); }

    void sortTiles() {
        for (Tile t : tiles) t.uses = prefs.getInt("n." + t.key, 0);
        Collections.sort(tiles, (a, b) -> {
            if (a.uses != b.uses) return b.uses - a.uses;
            return a.label.compareToIgnoreCase(b.label);
        });
    }

    void buildDock() {
        LinearLayout row = findViewById(R.id.dockRow);
        row.removeAllViews();
        List<String> keys = new ArrayList<>();
        for (String k : prefs.getString("recent", "").split(",")) if (!k.isEmpty() && byKey.containsKey(k) && !keys.contains(k)) keys.add(k);
        for (String k : FAVOURITES) if (byKey.containsKey(k) && !keys.contains(k)) keys.add(k);
        if (keys.size() > 10) keys = keys.subList(0, 10);
        for (String k : keys) {
            Tile t = byKey.get(k);
            View v = LayoutInflater.from(this).inflate(R.layout.dock_item, row, false);
            ((ImageView) v.findViewById(R.id.icon)).setImageDrawable(t.icon);
            v.setContentDescription(t.label);
            v.setOnClickListener(x -> launch(t));
            v.setOnFocusChangeListener((x, f) -> x.animate().scaleX(f ? 1.12f : 1f).scaleY(f ? 1.12f : 1f).setDuration(140).start());
            row.addView(v);
        }
    }

    void launch(Tile t) {
        try {
            startActivity(t.intent);
            List<String> r = new ArrayList<>(Arrays.asList(prefs.getString("recent", "").split(",")));
            r.remove(t.key); r.remove(""); r.add(0, t.key);
            if (r.size() > 3) r = r.subList(0, 3);
            prefs.edit().putInt("n." + t.key, prefs.getInt("n." + t.key, 0) + 1)
                    .putString("recent", String.join(",", r)).apply();
        } catch (Exception e) { Toast.makeText(this, "Cannot open " + t.label, Toast.LENGTH_SHORT).show(); }
    }

    class TileAdapter extends BaseAdapter {
        public int getCount() { return tiles.size(); }
        public Object getItem(int i) { return tiles.get(i); }
        public long getItemId(int i) { return i; }
        public View getView(int i, View v, ViewGroup parent) {
            if (v == null) v = LayoutInflater.from(MainActivity.this).inflate(R.layout.tile, parent, false);
            Tile t = tiles.get(i);
            ((ImageView) v.findViewById(R.id.icon)).setImageDrawable(t.icon);
            ((TextView) v.findViewById(R.id.label)).setText(t.label);
            return v;
        }
    }
}
