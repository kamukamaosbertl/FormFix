package com.example.smartfit.maps;

import android.content.Context;
import android.content.SharedPreferences;

public class SavedLocationRepository {

    private static final String PREFS_NAME = "smartfit_map_prefs";
    private static final String KEY_LAT = "saved_lat";
    private static final String KEY_LNG = "saved_lng";
    private static final String KEY_TITLE = "saved_title";

    private final SharedPreferences preferences;

    public SavedLocationRepository(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveLocation(double lat, double lng, String title) {
        preferences.edit()
                .putLong(KEY_LAT, Double.doubleToRawLongBits(lat))
                .putLong(KEY_LNG, Double.doubleToRawLongBits(lng))
                .putString(KEY_TITLE, title)
                .apply();
    }

    public boolean hasSavedLocation() {
        return preferences.contains(KEY_LAT) && preferences.contains(KEY_LNG);
    }

    public double getSavedLat() {
        return Double.longBitsToDouble(preferences.getLong(KEY_LAT, 0L));
    }

    public double getSavedLng() {
        return Double.longBitsToDouble(preferences.getLong(KEY_LNG, 0L));
    }

    public String getSavedTitle() {
        return preferences.getString(KEY_TITLE, "Saved Workout Spot");
    }

    public void clear() {
        preferences.edit().clear().apply();
    }
}