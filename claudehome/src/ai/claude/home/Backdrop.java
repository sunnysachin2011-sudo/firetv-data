package ai.claude.home;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.ImageView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/** Full-screen backdrop: photo of the day (Bing, no key) plus a colour tint that crossfades with the focused card. */
public class Backdrop {
    static final String BING = "https://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1&mkt=en-GB";
    final Activity a; final ImageView backA, backB; final View tintA, tintB;
    boolean tintOnA = true; int tint = 0;
    static Bitmap photo; static String credit = ""; static String photoDay = "";

    Backdrop(Activity a) {
        this.a = a;
        backA = a.findViewById(R.id.backA); backB = a.findViewById(R.id.backB);
        tintA = a.findViewById(R.id.tintA); tintB = a.findViewById(R.id.tintB);
    }

    /** style: "photo" or "gradient" */
    void loadWallpaper(String style, Runnable onPhoto) {
        if (!"photo".equals(style)) { backA.animate().alpha(0f).setDuration(400).start(); return; }
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        if (photo != null && today.equals(photoDay)) { show(photo); if (onPhoto != null) onPhoto.run(); return; }
        new Thread(() -> {
            try {
                File f = new File(a.getCacheDir(), "wall_" + today + ".jpg");
                File cr = new File(a.getCacheDir(), "wall_" + today + ".txt");
                if (!f.exists()) {
                    String json = Net.text(a, BING, "bing.json");
                    JSONObject img = new JSONObject(json).getJSONArray("images").getJSONObject(0);
                    String url = "https://www.bing.com" + img.getString("urlbase") + "_1920x1080.jpg";
                    download(url, f);
                    try (FileOutputStream o = new FileOutputStream(cr)) { o.write(img.optString("copyright").getBytes("UTF-8")); }
                    for (File old : a.getCacheDir().listFiles()) if (old.getName().startsWith("wall_") && !old.getName().contains(today)) old.delete();
                }
                BitmapFactory.Options op = new BitmapFactory.Options(); op.inPreferredConfig = Bitmap.Config.RGB_565;
                Bitmap bm = BitmapFactory.decodeFile(f.getPath(), op);
                String c = ""; try { c = new String(java.nio.file.Files.readAllBytes(cr.toPath()), "UTF-8"); } catch (Exception ignored) {}
                if (bm != null) {
                    photo = bm; photoDay = today; credit = c;
                    a.runOnUiThread(() -> { show(bm); if (onPhoto != null) onPhoto.run(); });
                }
            } catch (Exception ignored) {}
        }).start();
    }

    static void download(String url, File into) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(8000); c.setReadTimeout(15000);
        File tmp = new File(into.getPath() + ".part");
        try (InputStream in = c.getInputStream(); FileOutputStream out = new FileOutputStream(tmp)) {
            byte[] b = new byte[16384]; int k; while ((k = in.read(b)) > 0) out.write(b, 0, k);
        }
        tmp.renameTo(into);
    }

    void show(Bitmap bm) {
        backA.setImageBitmap(bm);
        backA.animate().alpha(0.9f).setDuration(900).start();
    }

    /** Poster art for the focused card (or null to go back to the wallpaper). */
    void showImage(Bitmap bm) {
        if (bm == null) { backB.animate().alpha(0f).setDuration(500).start(); return; }
        backB.setImageBitmap(bm);
        backB.animate().alpha(1f).setDuration(500).start();
    }

    void tint(int color) {
        if (color == tint) return;
        tint = color;
        View in = tintOnA ? tintB : tintA, out = tintOnA ? tintA : tintB;
        tintOnA = !tintOnA;
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{ (color & 0x00FFFFFF) | 0x8C000000, (color & 0x00FFFFFF) | 0x26000000, 0x00000000 });
        g.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        g.setGradientCenter(0.78f, 0.28f);
        g.setGradientRadius(Ui.dp(a, 620));
        in.setBackground(g);
        in.animate().alpha(1f).setDuration(600).start();
        out.animate().alpha(0f).setDuration(600).start();
    }
}
