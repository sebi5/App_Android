package sky.chin.penpal.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by sky on 31/07/2015.
 */
public class ToastUtils {

    public static void show(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

}
