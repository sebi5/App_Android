package sky.chin.penpal.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import sky.chin.penpal.configs.ConfigPreferences;

public class AuthManager {

    private String userId;
    private String userPassword;

    private static AuthManager instance;
    private static Context context;

    public AuthManager(Context context) {
        this.context = context;
        SharedPreferences settings = context.getSharedPreferences(ConfigPreferences.PREFS_NAME, 0);
        userId = settings.getString(ConfigPreferences.PREFS_USER_ID, "");
        userPassword = settings.getString(ConfigPreferences.PREFS_USER_PASSWORD, "");

        Log.d("Auth", "userId: " + userId + ", userPassword: " + userPassword);
    }

    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context);
        }
        return instance;
    }

    public boolean isAuthorised() {
        return !"".equals(userId) && !"".equals(userPassword);
    }

    public void setLogin(String userId, String userPassword) {
        this.userId = userId;
        this.userPassword = userPassword;

        // Save into SharedPreferences
        SharedPreferences settings = context.getSharedPreferences(ConfigPreferences.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(ConfigPreferences.PREFS_USER_ID, userId);
        editor.putString(ConfigPreferences.PREFS_USER_PASSWORD, userPassword);

        Log.d("Login", "Saving settings: " + ConfigPreferences.PREFS_USER_ID + " = " + userId + ", " +
                ConfigPreferences.PREFS_USER_PASSWORD + " = " + userPassword);

        editor.commit();
    }

    public void removeLogin() {
        this.userId = "";
        this.userPassword = "";

        SharedPreferences settings = context.getSharedPreferences(ConfigPreferences.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(ConfigPreferences.PREFS_USER_ID);
        editor.remove(ConfigPreferences.PREFS_USER_PASSWORD);

        Log.d("Login", "Removing settings");

        editor.commit();
    }

    public String getUserId() {
        return userId;
    }

    public String getUserPassword() {
        return userPassword;
    }
}
