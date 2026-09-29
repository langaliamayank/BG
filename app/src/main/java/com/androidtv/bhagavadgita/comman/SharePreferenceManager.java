package com.androidtv.bhagavadgita.comman;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class SharePreferenceManager {
    private static final SharePreferenceManager INSTANCE = new SharePreferenceManager();
    private static final String PREF_NAME = "BhagavadGita";
    private final SharedPreferences mSharedPreferences;
    private final Gson gson = new Gson();

    public SharePreferenceManager() {
        mSharedPreferences = MyApplication.getInstance().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static SharePreferenceManager getInstance() {
        return INSTANCE;
    }

    public static void save(String title, Object obj) {
        SharedPreferences.Editor editor = SharePreferenceManager.getInstance().getEditor();
        if (obj instanceof String)
            editor.putString(title, (String) obj);
        else if (obj instanceof Boolean)
            editor.putBoolean(title, (Boolean) obj);
        else if (obj instanceof Float)
            editor.putFloat(title, (Float) obj);
        else if (obj instanceof Integer)
            editor.putInt(title, (Integer) obj);
        else if (obj instanceof Long)
            editor.putLong(title, (Long) obj);
        else if (obj != null) {
            // Converts custom objects / ArrayList to JSON String
            editor.putString(title, SharePreferenceManager.getInstance().gson.toJson(obj));
        }

        editor.apply();
    }

    // Generic deserializer for ArrayLists
    public static <T> ArrayList<T> getList(String title, Type type) {
        String json = getString(title);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        return SharePreferenceManager.getInstance().gson.fromJson(json, type);
    }

    public static String getString(String title) {
        return getPreferences().getString(title, "");
    }

    public static boolean getBoolean(String title) {
        return getPreferences().getBoolean(title, false);
    }

    public static boolean getBoolean(String title, boolean defVal) {
        return getPreferences().getBoolean(title, defVal);
    }

    public static float getFloat(String title) {
        return getPreferences().getFloat(title, 0f);
    }

    public static int getInt(String title) {
        return getPreferences().getInt(title, 0);
    }

    public static long getLong(String title) {
        return getPreferences().getLong(title, 0L);
    }

    public static void clearDB() {
        SharePreferenceManager.getInstance().getEditor().clear().apply();
    }

    private static SharedPreferences getPreferences() {
        return SharePreferenceManager.getInstance().getPreference();
    }

    public SharedPreferences getPreference() {
        return mSharedPreferences;
    }

    public SharedPreferences.Editor getEditor() {
        return mSharedPreferences.edit();
    }
}