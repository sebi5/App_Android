package sky.chin.penpal.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import sky.chin.penpal.R;
import sky.chin.penpal.utils.VolleySingleton;


public class LoginActivity extends ActionBarActivity {

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

                String url = "http://45.55.157.207/python/login.py";

                hideErrorMessage();
                btnLogin.setText(getResources().getString(R.string.logging_in));
                btnLogin.setEnabled(false);

                StringRequest jsObjRequest = new StringRequest
                        (Request.Method.POST, url, new Response.Listener<String>() {

                            @Override
                            public void onResponse(String response) {
                                Log.d("Login", "Response: " + response.toString());

                                JSONObject jsonResp = null;
                                try {
                                    jsonResp = new JSONObject(response.toString());
                                    JSONArray dataArray = jsonResp.getJSONArray("data");

                                    JSONObject data;
                                    for (int i = 0; i < dataArray.length(); i++) {
                                        data = dataArray.getJSONObject(i);
                                        String code = data.getString("code");
                                        if ("0".equals(code)) {
                                            Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                                        } else {
                                            showErrorMessage(data.getString("message"));
                                        }
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                btnLogin.setText(getResources().getString(R.string.log_in));
                                btnLogin.setEnabled(true);
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("Login", "Error: " + error.getMessage());
                                btnLogin.setText(getResources().getString(R.string.log_in));
                                btnLogin.setEnabled(true);
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("user", username);
                        params.put("password", password);
                        params.put("p_chk", "key");

                        return params;
                    }
                };

                VolleySingleton.getInstance(LoginActivity.this).addToRequestQueue(jsObjRequest);
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
}
