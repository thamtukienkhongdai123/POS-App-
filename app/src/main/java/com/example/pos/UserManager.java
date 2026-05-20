package com.example.pos;

import android.content.Context;
import android.content.SharedPreferences;

public class UserManager {
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_LOGGED_IN_USER = "logged_in_user";
    private DatabaseHelper dbHelper;
    private SharedPreferences prefs;

    public UserManager(Context context) {
        dbHelper = new DatabaseHelper(context);
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean registerUser(String username, String password) {
        return dbHelper.addUser(username, password);
    }

    public boolean loginUser(String username, String password) {
        boolean success = dbHelper.checkUser(username, password);
        if (success) {
            prefs.edit().putString(KEY_LOGGED_IN_USER, username).apply();
        }
        return success;
    }
    
    public String getLoggedInUser() {
        return prefs.getString(KEY_LOGGED_IN_USER, "Unknown User");
    }

    public void logoutUser() {
        prefs.edit().remove(KEY_LOGGED_IN_USER).apply();
    }
    
    public boolean userExists(String username) {
        return dbHelper.userExists(username);
    }
}