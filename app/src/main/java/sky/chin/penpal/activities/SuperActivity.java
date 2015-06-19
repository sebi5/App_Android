package sky.chin.penpal.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class SuperActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "InterLocalPrefs";
    public static final String PREFS_USER_ID = "userId";
    public static final String PREFS_USER_PASSWORD = "userPassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void saveLoginProfile(String userId, String userPassword) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREFS_USER_ID, userId);
        editor.putString(PREFS_USER_PASSWORD, userPassword);

        Log.d("Login", "Saving settings: " + PREFS_USER_ID + " = " + userId + ", " +
                PREFS_USER_PASSWORD + " = " + userPassword);

        editor.commit();
    }

    protected void clearLoginProfile() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(PREFS_USER_ID);
        editor.remove(PREFS_USER_PASSWORD);

        Log.d("Login", "Removing settings");

        editor.commit();
    }
}
