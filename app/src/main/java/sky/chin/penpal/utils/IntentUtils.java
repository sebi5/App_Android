package sky.chin.penpal.utils;

import android.content.Intent;
import android.util.Log;

public class IntentUtils {

    public static void printExtras(Intent intent) {
        for (String key : intent.getExtras().keySet()) {
            Object value = intent.getExtras().get(key);
            Log.d("IntentUtils", String.format("%s %s (%s)", key,
                    value.toString(), value.getClass().getName()));
        }
    }
}
