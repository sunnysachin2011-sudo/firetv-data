package ai.claude.home;

import android.content.Context;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/** Small HTTPS text fetch with a cache file fallback (used by Football and Posters). */
public class Net {
    static String text(Context ctx, String url, String cacheName) {
        File cache = new File(ctx.getCacheDir(), cacheName);
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(url + (url.contains("?") ? "&t=" : "?t=") + System.currentTimeMillis()).openConnection();
            c.setConnectTimeout(6000); c.setReadTimeout(6000);
            try (InputStream in = c.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] b = new byte[4096]; int n; while ((n = in.read(b)) > 0) out.write(b, 0, n);
                String s = out.toString("UTF-8");
                try (FileOutputStream f = new FileOutputStream(cache)) { f.write(s.getBytes("UTF-8")); }
                return s;
            }
        } catch (Exception e) {
            try (FileInputStream f = new FileInputStream(cache); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] b = new byte[4096]; int n; while ((n = f.read(b)) > 0) out.write(b, 0, n);
                return out.toString("UTF-8");
            } catch (Exception ignored) { return null; }
        }
    }
}
