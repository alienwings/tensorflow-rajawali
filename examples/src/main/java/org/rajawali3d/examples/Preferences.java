package org.rajawali3d.examples;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class Preferences implements OnSharedPreferenceChangeListener {

    private static final String WALLPAPER_RENDERER_KEY = "org.rajawali3d.examples.Preferences.wallpaper_renderer";

    private static Preferences instance;

    private final SharedPreferences preferences;

    private String wallpaperRendererPreference;

    public static Preferences getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (Preferences.class) {
                if (instance == null) {
                    instance = new Preferences(context);
                }
            }
        }
        return instance;
    }

    private Preferences(@NonNull Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.registerOnSharedPreferenceChangeListener(this);

        updatePreferences();
    }

    private void updatePreferences() {
        wallpaperRendererPreference = preferences.getString(WALLPAPER_RENDERER_KEY, "");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // This has the potential to get heavy, but we will consider it ok for this demonstration
        updatePreferences();
    }

    @NonNull
    public String getWallpaperRendererPreference() {
        return wallpaperRendererPreference;
    }
}
