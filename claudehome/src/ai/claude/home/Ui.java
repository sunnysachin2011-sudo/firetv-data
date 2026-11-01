package ai.claude.home;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/** Bundled Outfit font, applied to whole view trees. */
public class Ui {
    static Typeface regular, medium, semibold;

    static void init(Context c) {
        if (regular != null) return;
        try {
            Typeface base = c.getResources().getFont(R.font.outfit);
            regular = Typeface.create(base, 400, false);
            medium = Typeface.create(base, 500, false);
            semibold = Typeface.create(base, 600, false);
        } catch (Exception e) { regular = medium = semibold = Typeface.DEFAULT; }
    }

    static void apply(View v) {
        if (v instanceof TextView) {
            TextView t = (TextView) v;
            boolean bold = t.getTypeface() != null && t.getTypeface().isBold();
            t.setTypeface(bold ? semibold : regular);
        } else if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) apply(g.getChildAt(i));
        }
    }

    static int dp(Context c, float v) { return Math.round(v * c.getResources().getDisplayMetrics().density); }
}
