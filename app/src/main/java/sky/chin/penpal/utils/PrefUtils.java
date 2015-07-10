package sky.chin.penpal.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefUtils {

    public static final String PREFS_USER_ID = "pref_user_id";
    public static final String PREFS_USER_PASSWORD = "pref_user_password";

    public static void saveLogIn(final Context context, String userId, String userPassword) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit()
                .putString(PREFS_USER_ID, userId)
                .putString(PREFS_USER_PASSWORD, userPassword)
                .commit();
    }

    public static void removeLogIn(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit()
                .remove(PREFS_USER_ID)
                .remove(PREFS_USER_PASSWORD)
                .commit();
    }

    public static String getPrefsUserId(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREFS_USER_ID, "");
    }

    public static String getPrefsUserPassword(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREFS_USER_PASSWORD, "");
    }

    public static void registerOnSharedPreferenceChangeListener(final Context context,
                                                                SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterOnSharedPreferenceChangeListener(final Context context,
                                                                  SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
