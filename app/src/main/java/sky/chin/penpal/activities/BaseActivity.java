package sky.chin.penpal.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import sky.chin.penpal.R;

public class BaseActivity extends AppCompatActivity {

    private static boolean isOffline = true;

    BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            isOffline = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            offlineMessage(isOffline);
            Log.w("Network Listener", "Network Type Changed");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkStateReceiver, filter);
    }

    public boolean isOffline() {
        return isOffline;
    }

    private void offlineMessage(boolean show) {
        View offlineMessage = findViewById(R.id.offlineMessage);
        if (offlineMessage != null) {
            ValueAnimator showOrHideAnim = show ?
                        ObjectAnimator.ofFloat(offlineMessage, "y", -100f, 0f):
                        ObjectAnimator.ofFloat(offlineMessage, "y", 0f, -100f);

            showOrHideAnim.setDuration(250);
            showOrHideAnim.start();
        }
    }



}
