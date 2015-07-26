package sky.chin.penpal.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import sky.chin.penpal.R;
import sky.chin.penpal.utils.IntentUtils;

public class BaseActivity extends AppCompatActivity {

    private static boolean isOffline = true;

    BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            IntentUtils.printExtras(intent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                NetworkInfo networkInfo = (NetworkInfo)
                        intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
                isOffline = !networkInfo.isConnected();
            } else
                isOffline = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

            offlineMessage(isOffline);
            Log.w("Network Listener", "Network Type Changed");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkStateReceiver, filter);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(networkStateReceiver);

        super.onPause();
    }

    public boolean isOffline() {
        return isOffline;
    }

    private void offlineMessage(boolean show) {
        View offlineMessage = findViewById(R.id.offlineMessage);
        if (offlineMessage != null) {
            ValueAnimator showOrHideAnim = show ?
                        ObjectAnimator.ofFloat(offlineMessage, "y", -150f, 0f):
                        ObjectAnimator.ofFloat(offlineMessage, "y", 0f, -150f);

            showOrHideAnim.setDuration(250);
            showOrHideAnim.start();
        }
    }



}
