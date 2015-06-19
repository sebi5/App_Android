package sky.chin.penpal.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import sky.chin.penpal.R;


public class MainActivity extends SuperActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnLogin).setOnClickListener(this);
        findViewById(R.id.btnSignUp).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String userId = settings.getString(PREFS_USER_ID, "");
        String userPassword = settings.getString(PREFS_USER_PASSWORD, "");

        if (!"".equals(userId) && !"".equals(userPassword))
            startActivity(new Intent(this, HomeActivity.class));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnLogin:
                startActivity(new Intent(this, LoginActivity.class));
                break;
            case R.id.btnSignUp:
                startActivity(new Intent(this, RegisterActivity.class));
                break;
        }
    }
}
