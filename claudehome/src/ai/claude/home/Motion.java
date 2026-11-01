package ai.claude.home;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.PathInterpolator;

/** All motion in one place. Turns itself off when the system animation scale is 0. */
public class Motion {
    static final PathInterpolator EASE_OUT = new PathInterpolator(0.2f, 0f, 0f, 1f);   // emphasized decelerate
    static final PathInterpolator EASE_IN = new PathInterpolator(0.3f, 0f, 1f, 1f);
    static final PathInterpolator STANDARD = new PathInterpolator(0.2f, 0f, 0f, 1f);

    static boolean on() { return ValueAnimator.areAnimatorsEnabled(); }

    /** Tab change: old page slides out and fades, new page slides in from the side we move to. */
    static void swapPages(View out, View in, int dir, int dist) {
        out.animate().cancel(); in.animate().cancel();
        out.animate().alpha(0f).translationX(-dir * dist * 0.6f).setDuration(160).setInterpolator(EASE_IN)
                .withEndAction(() -> { out.setVisibility(View.GONE); out.setTranslationX(0); out.setAlpha(1f); }).start();
        in.setVisibility(View.VISIBLE);
        in.setAlpha(0f); in.setTranslationX(dir * dist);
        in.animate().alpha(1f).translationX(0).setStartDelay(90).setDuration(320).setInterpolator(EASE_OUT).start();
    }

    /** Children rise in one after another. */
    static void stagger(ViewGroup row, int rise, long delay) {
        if (!on() || row == null) return;
        for (int i = 0; i < row.getChildCount(); i++) {
            View v = row.getChildAt(i);
            v.animate().cancel();
            v.setAlpha(0f); v.setTranslationY(rise);
            v.animate().alpha(1f).translationY(0).setStartDelay(delay + i * 55L).setDuration(380).setInterpolator(EASE_OUT).start();
        }
    }

    /** Focus: lift and grow with a soft overshoot; release settles back. */
    static void focus(View v, boolean f, int lift) {
        v.animate().cancel();
        v.animate().scaleX(f ? 1.08f : 1f).scaleY(f ? 1.08f : 1f).translationZ(f ? lift : 0).translationY(0)
                .setStartDelay(0).setDuration(f ? 260 : 180)
                .setInterpolator(f ? new OvershootInterpolator(1.6f) : STANDARD).start();
    }

    static void dockFocus(View v, boolean f, int lift) {
        v.animate().cancel();
        v.animate().scaleX(f ? 1.16f : 1f).scaleY(f ? 1.16f : 1f).translationY(f ? -lift : 0)
                .setDuration(f ? 240 : 160).setInterpolator(f ? new OvershootInterpolator(2f) : STANDARD).start();
    }

    /** Hero text: old lines leave together, new lines arrive one after another (eyebrow, title, sub, button). */
    static void heroSwap(ViewGroup box, int dist, Runnable setText) {
        if (!on()) { setText.run(); return; }
        box.animate().cancel();
        box.animate().alpha(0f).translationX(-dist * 0.6f).setStartDelay(0).setDuration(110).setInterpolator(EASE_IN).withEndAction(() -> {
            setText.run();
            box.setAlpha(1f); box.setTranslationX(0);
            for (int i = 0; i < box.getChildCount(); i++) {
                View c = box.getChildAt(i);
                c.animate().cancel();
                c.setAlpha(0f); c.setTranslationX(dist);
                c.animate().alpha(1f).translationX(0).setStartDelay(i * 45L).setDuration(300).setInterpolator(EASE_OUT).start();
            }
        }).start();
    }

    static void fade(View v, boolean show) {
        v.animate().cancel();
        if (show) { v.setVisibility(View.VISIBLE); v.setAlpha(0f); v.setTranslationY(v.getHeight() * 0.3f);
            v.animate().alpha(1f).translationY(0).setDuration(260).setInterpolator(EASE_OUT).start(); }
        else v.animate().alpha(0f).translationY(v.getHeight() * 0.3f).setDuration(160).setInterpolator(EASE_IN)
                .withEndAction(() -> { v.setVisibility(View.GONE); v.setTranslationY(0); v.setAlpha(1f); }).start();
    }

    /** LIVE dot breathes three times (the row rebuilds every minute, so it breathes again then). */
    static void pulse(View dot) {
        if (!on()) return;
        ObjectAnimator a = ObjectAnimator.ofFloat(dot, View.ALPHA, 1f, 0.25f);
        a.setDuration(700); a.setRepeatMode(ValueAnimator.REVERSE); a.setRepeatCount(5);
        a.setInterpolator(STANDARD); a.start();
    }

    /** Ken Burns: very slow zoom and drift on a full-screen photo. */
    static ValueAnimator kenBurns(View v, float zoom, long ms) {
        if (!on()) return null;
        v.setPivotX(v.getWidth() > 0 ? v.getWidth() * 0.6f : 1150); v.setPivotY(v.getHeight() > 0 ? v.getHeight() * 0.4f : 430);
        v.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        ValueAnimator a = ValueAnimator.ofFloat(1f, zoom);
        a.setDuration(ms); a.setRepeatMode(ValueAnimator.REVERSE); a.setRepeatCount(ValueAnimator.INFINITE);
        a.setInterpolator(new LinearInterpolator());
        a.addUpdateListener(x -> { float f = (float) x.getAnimatedValue(); v.setScaleX(f); v.setScaleY(f); v.setTranslationX(-(f - 1f) * 400f); });
        a.start();
        return a;
    }

    // ---------- zoom launch ----------
    static View cover; static long coverAt;

    /** OK on a card: a panel in the card's colour grows from the card to fill the screen, then the app opens. */
    static void zoomLaunch(android.app.Activity a, View src, int color, Runnable go) {
        if (!on() || cover != null) { go.run(); return; }
        ViewGroup root = a.findViewById(R.id.root);
        int[] r = new int[2], s = new int[2];
        root.getLocationOnScreen(r); src.getLocationOnScreen(s);
        int w = Math.max(1, Math.round(src.getWidth() * src.getScaleX())), h = Math.max(1, Math.round(src.getHeight() * src.getScaleY()));
        float x = s[0] - r[0], y = s[1] - r[1];
        View v = new View(a);
        android.graphics.drawable.GradientDrawable g = new android.graphics.drawable.GradientDrawable();
        g.setColor((color & 0x00FFFFFF) | 0xFF000000); g.setCornerRadius(Ui.dp(a, 14));
        v.setBackground(g); v.setAlpha(0f);
        root.addView(v, new android.widget.FrameLayout.LayoutParams(w, h));
        v.setX(x); v.setY(y); v.setTranslationZ(Ui.dp(a, 20));
        float sx = root.getWidth() / (float) w * 1.04f, sy = root.getHeight() / (float) h * 1.04f;
        float dx = root.getWidth() / 2f - (x + w / 2f), dy = root.getHeight() / 2f - (y + h / 2f);
        cover = v; coverAt = System.currentTimeMillis();
        v.animate().alpha(1f).setDuration(120).start();
        v.animate().scaleX(sx).scaleY(sy).translationX(dx).translationY(dy).setStartDelay(40).setDuration(340)
                .setInterpolator(new PathInterpolator(0.4f, 0f, 0.2f, 1f)).withEndAction(() -> {
                    go.run();
                    v.postDelayed(() -> { if (cover == v && System.currentTimeMillis() - coverAt > 1800) uncover(); }, 2000);
                }).start();
    }

    /** Back on Home: the panel fades away. */
    static void uncover() {
        View v = cover; if (v == null) return; cover = null;
        v.animate().cancel();
        v.animate().alpha(0f).setStartDelay(80).setDuration(380).setInterpolator(EASE_OUT)
                .withEndAction(() -> { if (v.getParent() != null) ((ViewGroup) v.getParent()).removeView(v); }).start();
    }

    // ---------- gliding tab marker ----------
    static void movePill(View pill, View tab, boolean animate) {
        if (tab.getWidth() == 0) { tab.post(() -> movePill(pill, tab, false)); return; }
        ViewGroup.LayoutParams lp = pill.getLayoutParams();
        int fromW = lp.width > 0 ? lp.width : tab.getWidth();
        int toW = tab.getWidth(); lp.height = tab.getHeight();
        pill.setVisibility(View.VISIBLE);
        if (!animate || !on()) { lp.width = toW; pill.setLayoutParams(lp); pill.setTranslationX(tab.getLeft()); return; }
        pill.animate().cancel();
        pill.animate().translationX(tab.getLeft()).setDuration(340).setInterpolator(new OvershootInterpolator(0.9f)).start();
        ValueAnimator wa = ValueAnimator.ofInt(fromW, toW);
        wa.setDuration(340); wa.setInterpolator(EASE_OUT);
        wa.addUpdateListener(x -> { lp.width = (int) x.getAnimatedValue(); pill.setLayoutParams(lp); });
        wa.start();
    }

    // ---------- language change ----------
    static void curtain(android.app.Activity a, Runnable then) {
        View root = a.findViewById(R.id.root);
        if (!on()) { then.run(); return; }
        root.animate().alpha(0f).setDuration(220).setInterpolator(EASE_IN).withEndAction(then).start();
    }
}
