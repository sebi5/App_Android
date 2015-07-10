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
        userId = PrefUtils.getPrefsUserId(this.context);
        userPassword = PrefUtils.getPrefsUserPassword(this.context);

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
        PrefUtils.saveLogIn(context, userId, userPassword);
    }

    public void removeLogin() {
        this.userId = "";
        this.userPassword = "";

        PrefUtils.removeLogIn(context);
    }

    public String getUserId() {
        return userId;
    }

    public String getUserPassword() {
        return userPassword;
    }
}
