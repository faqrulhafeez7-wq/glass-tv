package com.gathin.tvhome;

import android.app.Activity;
import android.content.SharedPreferences;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Waktu solat JAKIM, zon SGR01 (Hulu Langat dll.). */
final class PrayerTimes {
    private static final String ZONE = "SGR01";
    private static final String API =
            "https://www.e-solat.gov.my/index.php?r=esolatApi/takwimsolat&period=today&zone=";
    private static boolean fetching;

    private PrayerTimes() {}

    static void load(final Activity act, final TextView view) {
        if (view == null) return;
        final SharedPreferences p = act.getSharedPreferences("prayer", Activity.MODE_PRIVATE);
        final String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        String cached = p.getString("text", null);
        if (cached != null) view.setText(cached);
        if (today.equals(p.getString("day", "")) || fetching) return;
        fetching = true;
        new Thread(() -> {
            try {
                final String text = fetch();
                if (text != null) {
                    p.edit().putString("text", text).putString("day", today).apply();
                    act.runOnUiThread(() -> view.setText(text));
                }
            } catch (Exception ignored) {
            } finally {
                fetching = false;
            }
        }).start();
    }

    private static String fetch() throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(API + ZONE).openConnection();
        c.setConnectTimeout(10000);
        c.setReadTimeout(10000);
        c.setRequestProperty("User-Agent", "GlassTV-prayer/1.0");
        try {
            if (c.getResponseCode() != 200) return null;
            StringBuilder sb = new StringBuilder();
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(c.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = r.readLine()) != null) sb.append(line);
            }
            JSONArray a = new JSONObject(sb.toString()).getJSONArray("prayerTime");
            JSONObject t = a.getJSONObject(0);
            return "Subuh " + hm(t.getString("fajr"))
                    + "   Zohor " + hm(t.getString("dhuhr"))
                    + "   Asar " + hm(t.getString("asr"))
                    + "   Maghrib " + hm(t.getString("maghrib"))
                    + "   Isyak " + hm(t.getString("isha"));
        } finally {
            c.disconnect();
        }
    }

    private static String hm(String s) {
        return s.length() >= 5 ? s.substring(0, 5) : s;
    }
}
