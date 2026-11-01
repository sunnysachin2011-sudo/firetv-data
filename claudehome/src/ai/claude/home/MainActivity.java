package ai.claude.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
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
    static final long[] IDLE_CHOICES = { 5 * 60000L, 15 * 60000L, 0 };

    static class Tile {
        String key, label; Drawable icon; Intent intent; int uses;
        Tile(String k, String l, Drawable i, Intent in) { key = k; label = l; icon = i; intent = in; }
    }

    /** One card in the For you row; the hero shows the focused one. */
    static class Card {
        String top, title, sub, eyebrow, heroTitle, heroSub, action; int tint; boolean live, big;
        Posters.Item poster; Runnable open;
    }

    final List<Tile> tiles = new ArrayList<>();
    final Map<String, Tile> byKey = new HashMap<>();
    final Handler handler = new Handler();
    SharedPreferences prefs, settings;
    TextView clock; GridView grid; TileAdapter adapter;
    TextView[] tabs; View[] pages; int tab = 0;
    Backdrop backdrop; Ambient ambient;
    Card heroCard; boolean resumed;
    final Runnable idle = () -> { if (resumed && !settingsOpen()) ambient.show(); };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Ui.init(this);
        Ui.apply(findViewById(R.id.root));
        prefs = getSharedPreferences("usage", MODE_PRIVATE);
        settings = getSharedPreferences("settings", MODE_PRIVATE);
        L.lang = settings.getString("lang", "en");
        backdrop = new Backdrop(this);
        ambient = new Ambient(this);
        ((TextView) findViewById(R.id.heroTitle)).setTypeface(Ui.semibold);
        ((TextView) findViewById(R.id.newsTitle)).setTypeface(Ui.semibold);
        ((TextView) findViewById(R.id.ambClock)).setTypeface(Ui.medium);
        clock = findViewById(R.id.clock);
        grid = findViewById(R.id.grid);
        tabs = new TextView[]{ findViewById(R.id.tabForYou), findViewById(R.id.tabFootball), findViewById(R.id.tabNews), findViewById(R.id.tabApps) };
        pages = new View[]{ findViewById(R.id.pageForYou), findViewById(R.id.pageFootball), findViewById(R.id.pageNews), grid };
        for (int i = 0; i < tabs.length; i++) {
            final int idx = i;
            tabs[i].setOnClickListener(v -> showTab(idx));
            tabs[i].setOnFocusChangeListener((v, f) -> { if (f) showTab(idx); });
        }
        findViewById(R.id.gear).setOnClickListener(v -> openSettings());
        applyTexts();
        buildTiles();
        adapter = new TileAdapter();
        grid.setAdapter(adapter);
        grid.setOnItemClickListener((p, v, pos, id) -> { Tile t = tiles.get(pos); Motion.zoomLaunch(this, v, tintFor(t.key), () -> launch(t)); });
        grid.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            View last;
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                if (last != null) last.animate().scaleX(1f).scaleY(1f).setDuration(140).start();
                if (v != null) v.animate().scaleX(1.08f).scaleY(1.08f).setDuration(140).start();
                last = v;
            }
            public void onNothingSelected(AdapterView<?> p) {}
        });
        buildDock();
        showTab(0);
        backdrop.tint(0xFF2D5BFF);
        backdrop.loadWallpaper(settings.getString("wallpaper", "photo"), null);
        Posters.load(this, this::buildForYou);
        News.load(this, this::buildNews);
        buildForYou();
        tick();
        handler.postDelayed(() -> { View first = ((LinearLayout) findViewById(R.id.heroRow)).getChildAt(0); if (first != null) first.requestFocus(); else tabs[0].requestFocus(); }, 700);
    }

    void applyTexts() {
        String[] keys = { "tab.foryou", "tab.sport", "tab.news", "tab.apps" };
        for (int i = 0; i < tabs.length; i++) tabs[i].setText(L.t(keys[i]));
        ((TextView) findViewById(R.id.footballNote)).setText(L.t("sportnote"));
        ((TextView) findViewById(R.id.newsNote)).setText(L.t("newsnote"));
        ((TextView) findViewById(R.id.alertHint)).setText(L.t("oktoopen"));
    }

    void showTab(int idx) {
        int from = tab;
        tab = idx;
        for (int i = 0; i < tabs.length; i++) tabs[i].setSelected(i == idx);
        Motion.movePill(findViewById(R.id.tabPill), tabs[idx], from != idx);
        View dock = findViewById(R.id.dockScroll);
        boolean dockOn = idx != 3;
        if (dockOn != (dock.getVisibility() == View.VISIBLE)) Motion.fade(dock, dockOn);
        if (from == idx || !Motion.on()) {
            for (int i = 0; i < pages.length; i++) { pages[i].setVisibility(i == idx ? View.VISIBLE : View.GONE); pages[i].setAlpha(1f); pages[i].setTranslationX(0); }
            return;
        }
        Motion.swapPages(pages[from], pages[idx], idx > from ? 1 : -1, Ui.dp(this, 48));
        if (idx == 0 && heroCard != null) backdrop.tint(heroCard.tint);
        if (idx == 1) Motion.stagger(findViewById(R.id.footballRow2), Ui.dp(this, 24), 120);
        if (idx == 2) { Motion.stagger(findViewById(R.id.newsRow), Ui.dp(this, 24), 120); backdrop.tint(0xFF6E7BFF); }
    }

    @Override protected void onNewIntent(Intent i) { super.onNewIntent(i); setIntent(i); if (resumed && i.getBooleanExtra("ambient", false)) { i.removeExtra("ambient"); handler.postDelayed(ambient::show, 800); } }

    @Override protected void onResume() {
        super.onResume();
        resumed = true;
        Motion.uncover();
        Football.load(this, findViewById(R.id.footballRow2), findViewById(R.id.footballTitle2), findViewById(R.id.footballScroll2));
        sortTiles(); if (adapter != null) adapter.notifyDataSetChanged();
        buildDock();
        backdrop.loadWallpaper(settings.getString("wallpaper", "photo"), null);
        News.load(this, this::buildNews);
        resetIdle();
        if (getIntent().getBooleanExtra("ambient", false)) { getIntent().removeExtra("ambient"); handler.postDelayed(ambient::show, 1500); }
    }

    @Override protected void onPause() { resumed = false; handler.removeCallbacks(idle); ambient.hide(); super.onPause(); }

    @Override public boolean dispatchKeyEvent(KeyEvent e) {
        if (settingsOpen() && e.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (e.getAction() == KeyEvent.ACTION_UP) closeSettings();
            return true;
        }
        if (ambient.shown()) {
            if (e.getAction() == KeyEvent.ACTION_UP) { ambient.hide(); resetIdle(); }
            return true;
        }
        resetIdle();
        return super.dispatchKeyEvent(e);
    }

    void resetIdle() {
        handler.removeCallbacks(idle);
        long ms = IDLE_CHOICES[settings.getInt("idle", 0)];
        if (ms > 0) handler.postDelayed(idle, ms);
    }

    void tick() {
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        ((TextView) findViewById(R.id.greeting)).setText(L.t(h < 12 ? "greet.m" : h < 18 ? "greet.a" : "greet.e"));
        clock.setText(new SimpleDateFormat("EEE d MMM  HH:mm", L.dateLocale()).format(new Date()));
        updateAlert();
        if (ambient.shown()) ambient.update();
        handler.postDelayed(this::tick, 30000);
    }

    long lastRender = 0;
    void updateAlert() {
        View alert = findViewById(R.id.alert);
        Football.Match m = Football.alertMatch();
        long now = System.currentTimeMillis();
        // Live matches already lead the hero; the banner is only for kick-off soon.
        if (m == null || m.live(now)) { alert.setVisibility(View.GONE); }
        else {
            ((TextView) findViewById(R.id.alertText)).setText(Football.alertText(m));
            alert.setOnClickListener(x -> Football.open(this, m));
            if (alert.getVisibility() != View.VISIBLE) { alert.setVisibility(View.VISIBLE); if (!ambient.shown()) alert.requestFocus(); }
        }
        if (now - lastRender > 60000 && !Football.matches.isEmpty()) {
            lastRender = now;
            Football.render(this, findViewById(R.id.footballRow2), findViewById(R.id.footballTitle2), findViewById(R.id.footballScroll2));
        }
    }

    /** Called by Football after each render. */
    void footballRendered(boolean hasMatches) { buildForYou(); }

    static int regionTint(String r) {
        if ("India".equalsIgnoreCase(r)) return 0xFFFF9933;
        if ("Germany".equalsIgnoreCase(r)) return 0xFFE0B400;
        return 0xFF3D8BFF;
    }

    void buildNews() {
        LinearLayout row = findViewById(R.id.newsRow);
        row.removeAllViews();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        boolean fresh = today.equals(News.date);
        ((TextView) findViewById(R.id.newsEyebrow)).setText(News.stories.isEmpty() ? L.t("topstories") : L.t("topstories") + " · " + (fresh ? new SimpleDateFormat("EEE d MMM", L.dateLocale()).format(new Date()) : L.f("from", News.date)));
        ((TextView) findViewById(R.id.newsTitle)).setText(News.stories.isEmpty() ? L.t("nostories") : L.t("ingw"));
        for (News.Story st : News.stories) {
            View v = LayoutInflater.from(this).inflate(R.layout.news_card, row, false);
            Ui.apply(v);
            int tint = regionTint(st.region);
            v.findViewById(R.id.nBar).setBackgroundColor(tint);
            ((TextView) v.findViewById(R.id.nRegion)).setText(L.t(st.region));
            TextView h = v.findViewById(R.id.nHead); h.setText(st.h()); h.setTypeface(Ui.semibold);
            ((TextView) v.findViewById(R.id.nSum)).setText(st.s());
            v.setOnClickListener(x -> { Tile t = byKey.get(LOUNGE_KEY); if (t != null) Motion.zoomLaunch(this, x, tint, () -> launch(t)); });
            v.setOnFocusChangeListener((x, f) -> {
                Motion.focus(x, f, Ui.dp(this, 10));
                if (f) backdrop.tint(tint);
            });
            row.addView(v);
        }
        if (tab == 2) Motion.stagger(row, Ui.dp(this, 24), 0);
    }

    // ---------- For you row + hero ----------

    static int tintFor(String pkg) {
        if (pkg == null) return 0xFF2D5BFF;
        switch (pkg) {
            case "in.startv.hotstar": return 0xFF1F80E0;
            case "com.onemainstream.sonyliv.android": return 0xFFC9971C;
            case "com.fancode.tv": return 0xFFFF5B2E;
            case "com.netflix.ninja": return 0xFFE50914;
            case "com.amazon.firetv.youtube": return 0xFFE62117;
            case "com.zee5.amazon": return 0xFF8230C6;
            case "com.spotify.tv.android": return 0xFF1DB954;
            case "com.amazon.cloud9": case TVBRO: case LOUNGE_KEY: return 0xFFD97757;
            default: return 0xFF2D5BFF;
        }
    }

    List<Card> cards() {
        List<Card> out = new ArrayList<>();
        long now = System.currentTimeMillis();
        SimpleDateFormat hm = new SimpleDateFormat("HH:mm", Locale.US); hm.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        List<Football.Match> ms = new ArrayList<>();
        for (Football.Match m : Football.matches) if (now < m.kick + m.dur) ms.add(m);
        Collections.sort(ms, (x, y) -> x.live(now) != y.live(now) ? (x.live(now) ? -1 : 1) : Long.compare(x.kick, y.kick));
        for (Football.Match m : ms) {
            if (out.size() >= 6) break;
            Card c = new Card();
            boolean live = m.live(now);
            String sport = L.t(m.sport.equals("cricket") ? "cricket" : "football");
            c.live = live; c.big = m.big;
            c.top = live ? L.t("live") + " · " + shortComp(m.c()) : hm.format(new Date(m.kick)) + " · " + shortComp(m.c());
            c.title = m.h() + " – " + m.a();
            c.sub = m.app;
            c.eyebrow = (live ? L.t("livenow") + " · " : L.t("today") + " " + hm.format(new Date(m.kick)) + " · ") + sport;
            c.heroTitle = m.h() + " " + L.t("vs") + " " + m.a();
            c.heroSub = firstPart(m.c()) + (live ? "  ·  " + L.f("ends", hm.format(new Date(m.kick + m.dur))) : "  ·  " + until(m.kick - now));
            c.action = m.pkg.isEmpty() ? L.t("notapps") : "▶  " + m.app;
            c.tint = tintFor(m.pkg);
            c.open = () -> Football.open(this, m);
            out.add(c);
        }
        for (Posters.Item it : Posters.items) {
            Card c = new Card();
            String head = L.t("tonight");
            c.top = head; c.title = it.t(); c.sub = it.s();
            c.eyebrow = head; c.heroTitle = it.t(); c.heroSub = it.s();
            String app = appName(it.pkg);
            c.action = it.url != null && it.url.equals(LOUNGE_URL) ? "▶  " + L.t("openlounge") : app.isEmpty() ? "▶  " + L.t("open") : "▶  " + app;
            c.tint = it.url != null && it.url.equals(LOUNGE_URL) ? 0xFFD97757 : tintFor(it.pkg);
            c.poster = it;
            c.open = () -> Posters.open(this, it);
            out.add(c);
        }
        if (out.isEmpty()) {
            Card c = new Card();
            c.top = "Claude"; c.title = L.t("lounge"); c.sub = L.t("askshort");
            c.eyebrow = "Claude"; c.heroTitle = L.t("askany"); c.heroSub = L.t("asksub");
            c.action = "▶  " + L.t("openlounge"); c.tint = 0xFFD97757;
            c.open = () -> { Tile t = byKey.get(LOUNGE_KEY); if (t != null) launch(t); };
            out.add(c);
        }
        return out;
    }

    static String firstPart(String comp) {
        if (comp == null) return "";
        int i = comp.indexOf(',');
        return i > 0 ? comp.substring(0, i) : comp;
    }

    static String shortComp(String comp) {
        if (comp == null) return "";
        int i = comp.indexOf(',');
        String s = i > 0 ? comp.substring(0, i) : comp;
        return s.length() > 18 ? s.substring(0, 17) + "…" : s;
    }

    static String until(long ms) {
        long min = Math.max(1, ms / 60000);
        return min < 60 ? L.f("startsmin", min) : L.f("startsh", min / 60, min % 60);
    }

    String appName(String pkg) {
        if (pkg == null || pkg.isEmpty()) return "";
        Tile t = byKey.get(pkg);
        return t != null ? t.label : "";
    }

    void buildForYou() {
        LinearLayout row = findViewById(R.id.heroRow);
        boolean first = row.getChildCount() == 0;
        int focused = -1;
        for (int i = 0; i < row.getChildCount(); i++) if (row.getChildAt(i).hasFocus()) focused = i;
        row.removeAllViews();
        List<Card> list = cards();
        for (Card c : list) {
            View v = LayoutInflater.from(this).inflate(R.layout.hero_card, row, false);
            Ui.apply(v);
            ((TextView) v.findViewById(R.id.cTop)).setText(c.top);
            TextView title = v.findViewById(R.id.cTitle); title.setText(c.title); title.setTypeface(Ui.semibold);
            ((TextView) v.findViewById(R.id.cSub)).setText(c.sub);
            if (c.live) { View dot = v.findViewById(R.id.cDot); dot.setVisibility(View.VISIBLE); Motion.pulse(dot); ((TextView) v.findViewById(R.id.cTop)).setTextColor(0xFFFF8A8E); }
            if (c.big) v.findViewById(R.id.cStar).setVisibility(View.VISIBLE);
            v.setOnClickListener(x -> Motion.zoomLaunch(this, x, c.tint, c.open));
            v.setOnFocusChangeListener((x, f) -> {
                Motion.focus(x, f, Ui.dp(this, 10));
                if (f) setHero(c);
            });
            row.addView(v);
        }
        if (focused >= 0 && row.getChildCount() > 0) row.getChildAt(Math.min(focused, row.getChildCount() - 1)).requestFocus();
        if (first && tab == 0) Motion.stagger(row, Ui.dp(this, 28), 250);
        if (heroCard == null || focused < 0) setHero(list.get(0));
    }

    void setHero(Card c) {
        boolean same = heroCard != null && Objects.equals(heroCard.heroTitle, c.heroTitle) && Objects.equals(heroCard.heroSub, c.heroSub);
        heroCard = c;
        backdrop.tint(c.tint);
        backdrop.showImage(c.poster != null ? c.poster.bitmap : null);
        if (same) return;
        View box = findViewById(R.id.heroText);
        Motion.heroSwap((ViewGroup) box, Ui.dp(this, 16), () -> {
            ((TextView) findViewById(R.id.heroEyebrow)).setText(c.eyebrow);
            ((TextView) findViewById(R.id.heroTitle)).setText(c.heroTitle);
            ((TextView) findViewById(R.id.heroSub)).setText(c.heroSub);
            ((TextView) findViewById(R.id.heroAction)).setText(c.action);
        });
    }

    // ---------- settings ----------

    // ---------- settings panel ----------

    boolean settingsOpen() { return findViewById(R.id.settings).getVisibility() == View.VISIBLE; }

    void openSettings() {
        View box = findViewById(R.id.settings), panel = findViewById(R.id.setPanel), scrim = findViewById(R.id.setScrim);
        ((TextView) findViewById(R.id.setTitle)).setText(L.t("s.title"));
        ((TextView) findViewById(R.id.setHint)).setText(L.t("s.hint"));
        LinearLayout rows = findViewById(R.id.setRows);
        rows.removeAllViews();
        String[] langs = { "en", "hi", "de" };
        View first = addRow(rows, L.t("s.lang"), new String[]{ "English", "हिन्दी", "Deutsch" }, Arrays.asList(langs).indexOf(L.lang), (i, chips) -> {
            if (langs[i].equals(L.lang)) return;
            settings.edit().putString("lang", langs[i]).apply();
            Motion.curtain(this, this::recreate);
        });
        boolean photo = settings.getString("wallpaper", "photo").equals("photo");
        addRow(rows, L.t("s.wall"), new String[]{ L.t("s.photoshort"), L.t("s.darkshort") }, photo ? 0 : 1, (i, chips) -> {
            String w = i == 0 ? "photo" : "dark";
            settings.edit().putString("wallpaper", w).apply();
            backdrop.loadWallpaper(w, null);
        });
        addRow(rows, L.t("s.idle"), new String[]{ L.f("s.min", 5), L.f("s.min", 15), L.t("s.off") }, settings.getInt("idle", 0), (i, chips) -> {
            settings.edit().putInt("idle", i).apply();
            resetIdle();
        });
        Ui.apply(box);
        ((TextView) findViewById(R.id.setTitle)).setTypeface(Ui.semibold);
        ((ViewGroup) findViewById(R.id.main)).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        box.setVisibility(View.VISIBLE);
        int w = Ui.dp(this, 500);
        if (Motion.on()) {
            scrim.setAlpha(0f); scrim.animate().alpha(1f).setDuration(260).start();
            panel.setTranslationX(w); panel.animate().translationX(0).setDuration(380).setInterpolator(Motion.EASE_OUT).start();
            Motion.stagger(rows, Ui.dp(this, 18), 140);
        }
        first.requestFocus();
    }

    interface Pick { void on(int i, List<View> chips); }

    /** One labelled row of pill chips; returns the selected chip (for first focus). */
    View addRow(LinearLayout rows, String label, String[] names, int sel, Pick pick) {
        View row = LayoutInflater.from(this).inflate(R.layout.set_row, rows, false);
        ((TextView) row.findViewById(R.id.rowLabel)).setText(label);
        LinearLayout chipsBox = row.findViewById(R.id.rowChips);
        List<View> chips = new ArrayList<>();
        View selected = null;
        for (int i = 0; i < names.length; i++) {
            TextView c = (TextView) LayoutInflater.from(this).inflate(R.layout.set_chip, chipsBox, false);
            c.setText((i == sel ? "✓  " : "") + names[i]);
            c.setSelected(i == sel);
            final int idx = i;
            c.setOnClickListener(x -> {
                for (int k = 0; k < chips.size(); k++) {
                    TextView o = (TextView) chips.get(k);
                    o.setSelected(k == idx);
                    o.setText((k == idx ? "✓  " : "") + names[k]);
                }
                if (Motion.on()) x.animate().scaleX(0.94f).scaleY(0.94f).setDuration(70).withEndAction(() -> x.animate().scaleX(1.06f).scaleY(1.06f).setDuration(160).setInterpolator(new android.view.animation.OvershootInterpolator(2f)).start()).start();
                pick.on(idx, chips);
            });
            c.setOnFocusChangeListener((x, f) -> x.animate().scaleX(f ? 1.06f : 1f).scaleY(f ? 1.06f : 1f).setDuration(160).setInterpolator(Motion.EASE_OUT).start());
            chips.add(c); chipsBox.addView(c);
            if (i == sel) selected = c;
        }
        rows.addView(row);
        return selected != null ? selected : chips.get(0);
    }

    void closeSettings() {
        View box = findViewById(R.id.settings), panel = findViewById(R.id.setPanel), scrim = findViewById(R.id.setScrim);
        ((ViewGroup) findViewById(R.id.main)).setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        Runnable done = () -> { box.setVisibility(View.GONE); panel.setTranslationX(0); scrim.setAlpha(1f); findViewById(R.id.gear).requestFocus(); };
        if (!Motion.on()) { done.run(); return; }
        scrim.animate().alpha(0f).setDuration(220).start();
        panel.animate().translationX(Ui.dp(this, 500)).setDuration(240).setInterpolator(Motion.EASE_IN).withEndAction(done).start();
    }

    @Override protected void onDestroy() { handler.removeCallbacksAndMessages(null); super.onDestroy(); }

    // ---------- apps ----------

    void buildTiles() {
        tiles.clear(); byKey.clear();
        PackageManager pm = getPackageManager();
        Intent lounge = new Intent(Intent.ACTION_VIEW, Uri.parse(LOUNGE_URL)).setPackage(TVBRO);
        if (pm.resolveActivity(lounge, 0) == null) lounge = new Intent(Intent.ACTION_VIEW, Uri.parse(LOUNGE_URL)).setPackage(SILK);
        if (pm.resolveActivity(lounge, 0) == null) lounge = new Intent(Intent.ACTION_VIEW, Uri.parse(LOUNGE_URL));
        add(new Tile(LOUNGE_KEY, L.t("lounge"), getDrawable(R.drawable.banner), lounge));
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
                if (pkg.equals("com.amazon.tv.launcher")) label = L.t("firetvhome");
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
        if (keys.size() > 8) keys = keys.subList(0, 8);
        for (String k : keys) {
            Tile t = byKey.get(k);
            View v = LayoutInflater.from(this).inflate(R.layout.dock_item, row, false);
            ((ImageView) v.findViewById(R.id.icon)).setImageDrawable(t.icon);
            v.setContentDescription(t.label);
            v.setOnClickListener(x -> Motion.zoomLaunch(this, x, tintFor(t.key), () -> launch(t)));
            v.setOnFocusChangeListener((x, f) -> Motion.dockFocus(x, f, Ui.dp(this, 5)));
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
        } catch (Exception e) { Toast.makeText(this, L.f("cannotopen", t.label), Toast.LENGTH_SHORT).show(); }
    }

    class TileAdapter extends BaseAdapter {
        public int getCount() { return tiles.size(); }
        public Object getItem(int i) { return tiles.get(i); }
        public long getItemId(int i) { return i; }
        public View getView(int i, View v, ViewGroup parent) {
            if (v == null) { v = LayoutInflater.from(MainActivity.this).inflate(R.layout.tile, parent, false); Ui.apply(v); }
            Tile t = tiles.get(i);
            ((ImageView) v.findViewById(R.id.icon)).setImageDrawable(t.icon);
            ((TextView) v.findViewById(R.id.label)).setText(t.label);
            return v;
        }
    }
}
