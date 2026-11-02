package ai.claude.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.View;

/** Full-screen test patterns, drawn in code. One pattern pixel is one screen pixel. Same geometry as the HTML preview. */
public class Patterns extends View {
    static final String[] ALL = { "pluge", "white", "ramp", "bars", "sharp", "size", "black", "full", "red", "green", "blue", "grey" };
    static final int[] PLUGE = { 4, 8, 12, 16, 24, 32 };
    static final int[] WHITE = { 230, 235, 240, 245, 250, 255 };
    static final int[] BARS = { rgb(191, 191, 191), rgb(191, 191, 0), rgb(0, 191, 191), rgb(0, 191, 0), rgb(191, 0, 191), rgb(191, 0, 0), rgb(0, 0, 191) };
    static final int[] SKIN = { rgb(255, 224, 196), rgb(241, 194, 167), rgb(224, 172, 105), rgb(198, 134, 66), rgb(141, 85, 36), rgb(92, 56, 30) };

    String id = "pluge";
    final Paint fill = new Paint(), line = new Paint(), text = new Paint(Paint.ANTI_ALIAS_FLAG);

    public Patterns(Context c) {
        super(c);
        fill.setStyle(Paint.Style.FILL);
        line.setStyle(Paint.Style.STROKE);
        text.setTypeface(Ui.medium);
        text.setTextAlign(Paint.Align.CENTER);
    }

    void show(String p) { id = p; invalidate(); }

    static int rgb(int r, int g, int b) { return Color.rgb(r, g, b); }
    static int gray(int v) { return Color.rgb(v, v, v); }

    void rect(Canvas c, int color, float x, float y, float w, float h) { fill.setShader(null); fill.setColor(color); c.drawRect(x, y, x + w, y + h, fill); }

    void label(Canvas c, String s, float x, float top, int color, float size) {
        text.setColor(color); text.setTextSize(size);
        c.drawText(s, x, top - text.ascent(), text);
    }

    @Override protected void onDraw(Canvas c) {
        int W = getWidth(), H = getHeight();
        switch (id) {
            case "pluge": {
                rect(c, Color.BLACK, 0, 0, W, H);
                int bw = Math.round(W * .09f), gap = Math.round(W * .02f), top = Math.round(H * .14f), bh = Math.round(H * .42f);
                int x0 = Math.round((W - (6 * bw + 5 * gap)) / 2f);
                for (int i = 0; i < 6; i++) {
                    int x = x0 + i * (bw + gap);
                    rect(c, gray(PLUGE[i]), x, top, bw, bh);
                    label(c, String.valueOf(i + 1), x + bw / 2f, top + bh + H * .02f, gray(70), Math.round(H * .035f));
                }
                break;
            }
            case "white": {
                rect(c, gray(128), 0, 0, W, H);
                int bw = Math.round(W * .1f), top = Math.round(H * .14f), bh = Math.round(H * .42f), x0 = Math.round((W - 6 * bw) / 2f);
                for (int i = 0; i < 6; i++) {
                    int x = x0 + i * bw;
                    rect(c, gray(WHITE[i]), x, top, bw, bh);
                    label(c, String.valueOf(i + 1), x + bw / 2f, top + bh + H * .02f, gray(210), Math.round(H * .035f));
                }
                break;
            }
            case "ramp": {
                rect(c, Color.BLACK, 0, 0, W, H);
                int h1 = Math.round(H * .34f), h2 = Math.round(H * .34f);
                fill.setShader(new LinearGradient(0, 0, W, 0, Color.BLACK, Color.WHITE, Shader.TileMode.CLAMP));
                c.drawRect(0, 0, W, h1, fill);
                for (int i = 0; i < 16; i++) {
                    int x = Math.round(i * W / 16f), x2 = Math.round((i + 1) * W / 16f);
                    rect(c, gray(i * 17), x, h1, x2 - x, h2);
                }
                break;
            }
            case "bars": {
                rect(c, Color.BLACK, 0, 0, W, H);
                int h1 = Math.round(H * .48f);
                for (int i = 0; i < 7; i++) {
                    int x = Math.round(i * W / 7f), x2 = Math.round((i + 1) * W / 7f);
                    rect(c, BARS[i], x, 0, x2 - x, h1);
                }
                int top = h1 + Math.round(H * .04f), sh = Math.round(H * .16f), sw = Math.round(W * .1f), gap = Math.round(W * .015f);
                int x0 = Math.round((W - (6 * sw + 5 * gap)) / 2f);
                for (int i = 0; i < 6; i++) rect(c, SKIN[i], x0 + i * (sw + gap), top, sw, sh);
                break;
            }
            case "sharp": {
                rect(c, gray(128), 0, 0, W, H);
                for (int x = 0; x < W; x += 80) rect(c, Color.BLACK, x, 0, 1, H);
                for (int y = 0; y < H; y += 80) rect(c, Color.BLACK, 0, y, W, 1);
                line.setColor(Color.BLACK); line.setStrokeWidth(2); line.setAntiAlias(true);
                c.drawCircle(W / 2f, H * .36f, H * .28f, line);
                int s = Math.round(H * .2f), by = Math.round(H * .1f);
                for (int i = 0; i < 3; i++) {
                    int p = i + 1, bx = Math.round(W * .14f + i * W * .27f);
                    for (int x = 0; x < s; x += 2 * p) rect(c, Color.BLACK, bx + x, by, p, s);
                    for (int y = 0; y < s; y += 2 * p) rect(c, Color.BLACK, bx + s + 20, by + y, s, p);
                }
                text.setColor(Color.BLACK); text.setTextSize(Math.round(H * .05f));
                c.drawText("Aa Bb 0123 – Claude Home", W / 2f, H * .5f - (text.ascent() + text.descent()) / 2, text);
                break;
            }
            case "size": {
                rect(c, Color.BLACK, 0, 0, W, H);
                line.setColor(gray(60)); line.setStrokeWidth(2); line.setAntiAlias(true);
                c.drawLine(0, 0, W, H, line); c.drawLine(W, 0, 0, H, line);
                line.setAntiAlias(false);
                float[] f = { .05f, .025f }; int[] v = { 100, 170 }; String[] s = { "5%", "2.5%" };
                for (int i = 0; i < 2; i++) {
                    int ix = Math.round(W * f[i]), iy = Math.round(H * f[i]);
                    line.setColor(gray(v[i]));
                    c.drawRect(ix + 1, iy + 1, W - ix - 1, H - iy - 1, line);
                    label(c, s[i], W / 2f, iy + 8, gray(v[i]), Math.round(H * .025f));
                }
                rect(c, Color.WHITE, 0, 0, W, 1); rect(c, Color.WHITE, 0, H - 1, W, 1);
                rect(c, Color.WHITE, 0, 0, 1, H); rect(c, Color.WHITE, W - 1, 0, 1, H);
                break;
            }
            case "full": rect(c, Color.WHITE, 0, 0, W, H); break;
            case "red": rect(c, Color.RED, 0, 0, W, H); break;
            case "green": rect(c, Color.GREEN, 0, 0, W, H); break;
            case "blue": rect(c, Color.BLUE, 0, 0, W, H); break;
            case "grey": rect(c, gray(128), 0, 0, W, H); break;
            default: rect(c, Color.BLACK, 0, 0, W, H);
        }
    }
}
