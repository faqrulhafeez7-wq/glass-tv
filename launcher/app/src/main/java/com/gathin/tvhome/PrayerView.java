package com.gathin.tvhome;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/** Baris waktu solat JAKIM di bawah jam. Kemas kini sendiri. */
public class PrayerView extends TextView {
    private final Runnable tick = new Runnable() {
        @Override public void run() {
            refresh();
            postDelayed(this, 30 * 60 * 1000L);
        }
    };

    public PrayerView(Context c) { super(c); }
    public PrayerView(Context c, AttributeSet a) { super(c, a); }
    public PrayerView(Context c, AttributeSet a, int s) { super(c, a, s); }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        removeCallbacks(tick);
        post(tick);
    }

    @Override protected void onDetachedFromWindow() {
        removeCallbacks(tick);
        super.onDetachedFromWindow();
    }

    @Override protected void onWindowVisibilityChanged(int v) {
        super.onWindowVisibilityChanged(v);
        if (v == View.VISIBLE) refresh();
    }

    private void refresh() {
        Context c = getContext();
        while (c instanceof ContextWrapper && !(c instanceof Activity)) {
            c = ((ContextWrapper) c).getBaseContext();
        }
        if (c instanceof Activity) PrayerTimes.load((Activity) c, this);
    }
}
