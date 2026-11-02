package ai.claude.home;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.provider.Settings;

/** WireGuard tunnels (Mumbai, Delhi) switched by intent; app fixes for blocked streaming apps. */
public class Vpn {
    static final String WG = "com.wireguard.android";
    static final String[] TUNNELS = { "Mumbai", "Delhi" };
    static final String[] STREAMING = { "in.startv.hotstar", "com.onemainstream.sonyliv.android", "com.fancode.tv", "com.zee5.amazon", "com.netflix.ninja", "com.amazon.firebat" };

    static boolean installed(Context c) { return c.getPackageManager().getLaunchIntentForPackage(WG) != null || c.getPackageManager().getLeanbackLaunchIntentForPackage(WG) != null; }

    static boolean isStreaming(String pkg) { for (String s : STREAMING) if (s.equals(pkg)) return true; return false; }

    static SharedPreferences prefs(Context c) { return c.getSharedPreferences("vpn", Context.MODE_PRIVATE); }

    static String current(Context c) { return prefs(c).getString("tunnel", "Mumbai"); }

    static void send(Context c, String action, String tunnel) {
        Intent i = new Intent("com.wireguard.android.action." + action).setPackage(WG).putExtra("tunnel", tunnel);
        c.sendBroadcast(i);
    }

    /** Switch to one location: the other goes down first, then this one comes up. */
    static void switchTo(Context c, String tunnel) {
        for (String t : TUNNELS) if (!t.equals(tunnel)) send(c, "SET_TUNNEL_DOWN", t);
        send(c, "SET_TUNNEL_UP", tunnel);
        prefs(c).edit().putString("tunnel", tunnel).apply();
    }

    static long lastKick;

    /** Watchdog. Android blocks WireGuard from starting its VPN service while WireGuard is in the background
     *  ("Background start not allowed"), so a plain broadcast fails after a reboot. Bring WireGuard to the front
     *  for ~2 s, send the start signal, then run "then" (come back, or open the app). At most once per 5 min.
     *  Returns true when it took over (the caller must not continue). */
    static boolean ensure(android.app.Activity a, Runnable then) {
        if (!installed(a) || vpnUp(a)) return false;
        long now = System.currentTimeMillis();
        if (now - lastKick < 5 * 60000L) return false;
        lastKick = now;
        Intent w = a.getPackageManager().getLeanbackLaunchIntentForPackage(WG);
        if (w == null) w = a.getPackageManager().getLaunchIntentForPackage(WG);
        if (w == null) { send(a, "SET_TUNNEL_UP", current(a)); return false; }
        android.os.Handler h = new android.os.Handler(android.os.Looper.getMainLooper());
        try { a.startActivity(w.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION)); }
        catch (Exception e) { send(a, "SET_TUNNEL_UP", current(a)); return false; }
        h.postDelayed(() -> send(a, "SET_TUNNEL_UP", current(a)), 900);
        h.postDelayed(then, 2600);
        return true;
    }

    /** At boot: bring the last used location back up. */
    static void restore(Context c) { if (installed(c)) send(c, "SET_TUNNEL_UP", current(c)); }

    static boolean vpnUp(Context c) {
        try {
            ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            for (Network n : cm.getAllNetworks()) {
                NetworkCapabilities nc = cm.getNetworkCapabilities(n);
                if (nc != null && nc.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    /** Stop the app if it is in the background (it is, while Claude Home is on screen). */
    static void stopApp(Context c, String pkg) {
        try { ((ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE)).killBackgroundProcesses(pkg); } catch (Exception ignored) {}
    }

    /** Opens the app's info screen, where "Clear cache" is one press away. */
    static void openAppInfo(Context c, String pkg) {
        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + pkg)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try { c.startActivity(i); } catch (Exception ignored) {}
    }
}
