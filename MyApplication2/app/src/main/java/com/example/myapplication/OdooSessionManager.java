package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

public class OdooSessionManager {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private static final String PREF_NAME = "OdooPrefs";
    private static final String KEY_SESSION_ID = "session_id";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    public OdooSessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveSession(String sessionId, int userId, String username) {
        editor.putString(KEY_SESSION_ID, sessionId);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getSessionId() {
        return sharedPreferences.getString(KEY_SESSION_ID, null);
    }

    public int getUserId() {
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, null);
    }
}
