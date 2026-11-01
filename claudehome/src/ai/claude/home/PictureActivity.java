package ai.claude.home;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Picture setup: a step-by-step wizard (default) or a free test-pattern viewer (extra "mode" = "patterns").
 * The app cannot change TV settings; it shows patterns and tells the user what to change with the TV remote.
 * Start from ADB: adb shell am start -n ai.claude.home/.PictureActivity [--es mode patterns]
 */
public class PictureActivity extends Activity {
    /** Wizard: text key and pattern for each step. The last step is the summary. */
    static final String[][] WIZ = { { "mode", "bars" }, { "bright", "pluge" }, { "contrast", "white" }, { "sharp", "sharp" },
            { "color", "bars" }, { "gray", "ramp" }, { "size", "size" }, { "done", "ramp" } };

    boolean wizard; int pos; boolean hidden;
    Patterns view; LinearLayout dots, overlay; TextView eye, title, body, hint;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        L.lang = getSharedPreferences("settings", MODE_PRIVATE).getString("lang", L.lang);
        Ui.init(this);
        wizard = !"patterns".equals(getIntent().getStringExtra("mode"));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        view = new Patterns(this);
        root.addView(view, new FrameLayout.LayoutParams(-1, -1));

        dots = new LinearLayout(this);
        FrameLayout.LayoutParams dl = new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.RIGHT);
        dl.topMargin = dp(20); dl.rightMargin = dp(32);
        root.addView(dots, dl);

        overlay = new LinearLayout(this);
        overlay.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable g = new GradientDrawable();
        g.setColor(0xDB0B0D14); g.setCornerRadius(dp(14)); g.setStroke(dp(1), 0x1FFFFFFF);
        overlay.setBackground(g);
        overlay.setPadding(dp(22), dp(14), dp(22), dp(14));
        eye = text(11, 0xFF9FC1FF); eye.setAllCaps(true); eye.setLetterSpacing(0.15f);
        title = text(22, Color.WHITE); title.setTypeface(Ui.semibold);
        body = text(14, 0xD1FFFFFF); body.setLineSpacing(0, 1.15f);
        hint = text(11, 0x80FFFFFF);
        overlay.addView(eye); overlay.addView(title); overlay.addView(body); overlay.addView(hint);
        ((LinearLayout.LayoutParams) title.getLayoutParams()).topMargin = dp(2);
        ((LinearLayout.LayoutParams) body.getLayoutParams()).topMargin = dp(4);
        ((LinearLayout.LayoutParams) hint.getLayoutParams()).topMargin = dp(8);
        FrameLayout.LayoutParams ol = new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM);
        ol.leftMargin = ol.rightMargin = dp(32); ol.bottomMargin = dp(28);
        root.addView(overlay, ol);

        setContentView(root);
        immersive();
        render();
    }

    TextView text(int sp, int color) {
        TextView t = new TextView(this);
        t.setTextSize(sp); t.setTextColor(color); t.setTypeface(Ui.regular);
        t.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        return t;
    }

    int dp(float v) { return Ui.dp(this, v); }

    void immersive() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    @Override public void onWindowFocusChanged(boolean f) { super.onWindowFocusChanged(f); if (f) immersive(); }

    int count() { return wizard ? WIZ.length : Patterns.ALL.length; }

    void render() {
        if (wizard) {
            String k = WIZ[pos][0];
            view.show(WIZ[pos][1]);
            eye.setText("done".equals(k) ? "Claude Home" : L.f("pic.step", pos + 1, WIZ.length - 1));
            title.setText(L.t("w." + k + ".t"));
            body.setText(L.t("w." + k + ".b")); body.setVisibility(View.VISIBLE);
            hint.setText(L.t("pic.hintw"));
        } else {
            String p = Patterns.ALL[pos];
            view.show(p);
            eye.setText(L.f("pic.pat", pos + 1, Patterns.ALL.length));
            title.setText(L.t("p." + p));
            body.setVisibility(View.GONE);
            hint.setText(L.t("pic.hintp"));
        }
        dots.removeAllViews();
        for (int i = 0; i < count(); i++) {
            View d = new View(this);
            GradientDrawable s = new GradientDrawable();
            s.setShape(GradientDrawable.OVAL); s.setColor(i == pos ? Color.WHITE : 0x40FFFFFF);
            d.setBackground(s);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(7), dp(7));
            lp.leftMargin = dp(6);
            dots.addView(d, lp);
        }
        setHidden(hidden);
    }

    void setHidden(boolean h) {
        hidden = h;
        float a = h ? 0f : 1f;
        if (Motion.on()) {
            overlay.animate().alpha(a).translationY(h ? dp(10) : 0).setDuration(220).setInterpolator(Motion.EASE_OUT).start();
            dots.animate().alpha(a).setDuration(220).start();
        } else { overlay.setAlpha(a); dots.setAlpha(a); }
    }

    @Override public boolean onKeyDown(int code, KeyEvent e) {
        if (e.getRepeatCount() > 0 && code != KeyEvent.KEYCODE_DPAD_LEFT && code != KeyEvent.KEYCODE_DPAD_RIGHT) return true;
        boolean ok = code == KeyEvent.KEYCODE_DPAD_CENTER || code == KeyEvent.KEYCODE_ENTER || code == KeyEvent.KEYCODE_NUMPAD_ENTER;
        boolean up = code == KeyEvent.KEYCODE_DPAD_UP || code == KeyEvent.KEYCODE_DPAD_DOWN;
        if (code == KeyEvent.KEYCODE_BACK) { finish(); return true; }
        if (wizard) {
            if (ok && pos == WIZ.length - 1) { finish(); return true; }
            if (ok || code == KeyEvent.KEYCODE_DPAD_RIGHT) { if (pos < WIZ.length - 1) pos++; hidden = false; render(); return true; }
            if (code == KeyEvent.KEYCODE_DPAD_LEFT) { if (pos > 0) pos--; hidden = false; render(); return true; }
            if (up) { setHidden(!hidden); return true; }
        } else {
            int n = Patterns.ALL.length;
            if (code == KeyEvent.KEYCODE_DPAD_RIGHT) { pos = (pos + 1) % n; render(); return true; }
            if (code == KeyEvent.KEYCODE_DPAD_LEFT) { pos = (pos + n - 1) % n; render(); return true; }
            if (ok || up) { setHidden(!hidden); return true; }
        }
        return super.onKeyDown(code, e);
    }
}
