package ai.claude.home;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/** "Tonight" poster row. Data: posters.json in the firetv-data repo. */
public class Posters {
    static final String URL_JSON = "https://raw.githubusercontent.com/sunnysachin2011-sudo/firetv-data/main/posters.json";
    static final int[] FALLBACK = { 0xFF8A4B3C, 0xFF3C5A8A, 0xFF4B7A55, 0xFF7A5B3C, 0xFF5A3C7A, 0xFF3C6E7A, 0xFF7A3C56 };

    static class Item { String title, sub, image, pkg, url; }

    static void load(Activity a, LinearLayout row, TextView title) {
        new Thread(() -> {
            List<Item> items = new ArrayList<>();
            String heading = "Tonight";
            try {
                String json = Net.text(a, URL_JSON, "posters.json");
                JSONObject o = new JSONObject(json);
                heading = o.optString("title", heading);
                JSONArray arr = o.optJSONArray("items");
                for (int i = 0; arr != null && i < arr.length(); i++) {
                    JSONObject j = arr.getJSONObject(i); Item it = new Item();
                    it.title = j.optString("title"); it.sub = j.optString("subtitle"); it.image = j.optString("image");
                    it.pkg = j.optString("package"); it.url = j.optString("url"); items.add(it);
                }
            } catch (Exception ignored) {}
            final String h = heading; final List<Item> list = items;
            a.runOnUiThread(() -> render(a, row, title, h, list));
        }).start();
    }

    static void render(Activity a, LinearLayout row, TextView title, String heading, List<Item> items) {
        title.setText(heading);
        row.removeAllViews();
        if (items.isEmpty()) { Item i = new Item(); i.title = "Your picks appear here"; i.sub = "The morning task fills this row"; items.add(i); }
        int n = 0;
        for (Item it : items) {
            View v = LayoutInflater.from(a).inflate(R.layout.poster_card, row, false);
            ImageView img = v.findViewById(R.id.pImage);
            img.setBackgroundColor(FALLBACK[n++ % FALLBACK.length]);
            ((TextView) v.findViewById(R.id.pTitle)).setText(it.title);
            ((TextView) v.findViewById(R.id.pSub)).setText(it.sub);
            if (it.image != null && !it.image.isEmpty()) loadImage(a, it.image, img);
            v.setOnClickListener(x -> open(a, it));
            v.setOnFocusChangeListener((x, f) -> x.animate().scaleX(f ? 1.06f : 1f).scaleY(f ? 1.06f : 1f).setDuration(140).start());
            row.addView(v);
        }
    }

    static void loadImage(Activity a, String url, ImageView into) {
        new Thread(() -> {
            try {
                File f = new File(a.getCacheDir(), "img_" + Integer.toHexString(url.hashCode()));
                if (!f.exists()) {
                    HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
                    c.setConnectTimeout(8000); c.setReadTimeout(8000);
                    try (InputStream in = c.getInputStream(); FileOutputStream out = new FileOutputStream(f)) {
                        byte[] b = new byte[8192]; int k; while ((k = in.read(b)) > 0) out.write(b, 0, k);
                    }
                }
                BitmapFactory.Options op = new BitmapFactory.Options(); op.inSampleSize = 2;
                Bitmap bm = BitmapFactory.decodeFile(f.getPath(), op);
                if (bm != null) a.runOnUiThread(() -> into.setImageBitmap(bm));
            } catch (Exception ignored) {}
        }).start();
    }

    static void open(Activity a, Item it) {
        try {
            if (it.url != null && !it.url.isEmpty()) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(it.url));
                if (it.pkg != null && !it.pkg.isEmpty()) i.setPackage(it.pkg.equals("com.amazon.cloud9") && a.getPackageManager().getLaunchIntentForPackage("com.phlox.tvwebbrowser") != null ? "com.phlox.tvwebbrowser" : it.pkg);
                if (a.getPackageManager().resolveActivity(i, 0) != null) { a.startActivity(i); return; }
            }
            if (it.pkg != null && !it.pkg.isEmpty()) {
                Intent li = a.getPackageManager().getLeanbackLaunchIntentForPackage(it.pkg);
                if (li == null) li = a.getPackageManager().getLaunchIntentForPackage(it.pkg);
                if (li != null) { a.startActivity(li); return; }
            }
            Toast.makeText(a, "Nothing to open yet", Toast.LENGTH_SHORT).show();
        } catch (Exception e) { Toast.makeText(a, "Cannot open", Toast.LENGTH_SHORT).show(); }
    }
}
