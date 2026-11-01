package ai.claude.home;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.util.*;

/** "Tonight" items. Data: posters.json in the firetv-data repo. */
public class Posters {
    static final String URL_JSON = "https://raw.githubusercontent.com/sunnysachin2011-sudo/firetv-data/main/posters.json";

    static class Item { String title, sub, image, pkg, url; Bitmap bitmap; }

    static List<Item> items = new ArrayList<>();
    static String heading = "Tonight";

    static void load(Activity a, Runnable done) {
        new Thread(() -> {
            List<Item> list = new ArrayList<>();
            String h = "Tonight";
            try {
                JSONObject o = new JSONObject(Net.text(a, URL_JSON, "posters.json"));
                h = o.optString("title", h);
                JSONArray arr = o.optJSONArray("items");
                for (int i = 0; arr != null && i < arr.length(); i++) {
                    JSONObject j = arr.getJSONObject(i); Item it = new Item();
                    it.title = j.optString("title"); it.sub = j.optString("subtitle"); it.image = j.optString("image");
                    it.pkg = j.optString("package"); it.url = j.optString("url"); list.add(it);
                }
            } catch (Exception ignored) {}
            for (Item it : list) if (it.image != null && !it.image.isEmpty()) it.bitmap = image(a, it.image);
            heading = h; items = list;
            a.runOnUiThread(done);
        }).start();
    }

    static Bitmap image(Activity a, String url) {
        try {
            File f = new File(a.getCacheDir(), "img_" + Integer.toHexString(url.hashCode()));
            if (!f.exists()) Backdrop.download(url, f);
            BitmapFactory.Options op = new BitmapFactory.Options(); op.inPreferredConfig = Bitmap.Config.RGB_565;
            return BitmapFactory.decodeFile(f.getPath(), op);
        } catch (Exception e) { return null; }
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
