package sky.chin.penpal.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import sky.chin.penpal.R;
import sky.chin.penpal.server.interfaces.ServerResponseListener;
import sky.chin.penpal.server.requests.LoginRequest;
import sky.chin.penpal.utils.AuthManager;
import sky.chin.penpal.server.Server;


public class LoginActivity extends SuperActivity implements ServerResponseListener {

    private LinearLayout errorContainer;
    private TextView txtError;
    private EditText loginUsername, loginPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        errorContainer = (LinearLayout) findViewById(R.id.errorContainer);
        txtError = (TextView) findViewById(R.id.txtError);
        loginUsername = (EditText) findViewById(R.id.loginUsername);
        loginPassword = (EditText) findViewById(R.id.loginPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String username = loginUsername.getText().toString();
                if ("".equals(username)) {
                    loginUsername.setError(getResources().getString(R.string.error_login_username_required));
                    return;
                }

                final String password = loginPassword.getText().toString();
                if ("".equals(password)) {
                    loginPassword.setError(getResources().getString(R.string.error_password_required));
                    return;
                }

                hideErrorMessage();
                btnLogin.setText(getResources().getString(R.string.logging_in));
                btnLogin.setEnabled(false);

                Map<String, String> params = new HashMap<>();
                params.put("user", username);
                params.put("password", password);
                params.put("p_chk", "key");

                Server.getInstance(LoginActivity.this).sendRequest(
                        new LoginRequest.Builder().user(username).password(password).build(),
                        LoginActivity.this);
            }
        });
    }

    private void hideErrorMessage() {
        txtError.setText("");
        errorContainer.setVisibility(View.GONE);
    }

    private void showErrorMessage(String message) {
        txtError.setText(message);
        errorContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSuccess(JSONObject data) {
        try {
            String userId = data.getString("user_id");
            String userPassword = data.getString("password");

            AuthManager.getInstance(LoginActivity.this)
                    .setLogin(userId, userPassword);

            finish();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        btnLogin.setText(getResources().getString(R.string.log_in));
        btnLogin.setEnabled(true);
    }

    @Override
    public void onError(String content) {
        showErrorMessage(content);
        btnLogin.setText(getResources().getString(R.string.log_in));
        btnLogin.setEnabled(true);
    }
}
