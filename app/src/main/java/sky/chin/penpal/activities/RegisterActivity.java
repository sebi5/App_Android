package sky.chin.penpal.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import sky.chin.penpal.R;
import sky.chin.penpal.configs.Url;
import sky.chin.penpal.utils.AuthManager;
import sky.chin.penpal.utils.VolleySingleton;

public class RegisterActivity extends SuperActivity {

    private Button regBirthDate, regGender, regCountry, regRegion, btnSignUp;
    private EditText regUsername, regName, regPassword, regEmail;
    private DatePickerFragment datePickerFragment;
    private GenderPickerFragment genderPickerFragment;
    private CountryPickerFragment countryPickerFragment;
    private RegionPickerFragment regionPickerFragment;
    private LinearLayout errorContainer;
    private TextView txtError;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        scrollView = (ScrollView) findViewById(R.id.scrollView);
        errorContainer = (LinearLayout) findViewById(R.id.errorContainer);
        txtError = (TextView) findViewById(R.id.txtError);
        regUsername = (EditText) findViewById(R.id.regUsername);
        regName = (EditText) findViewById(R.id.regName);
        regPassword = (EditText) findViewById(R.id.regPassword);
        regEmail = (EditText) findViewById(R.id.regEmail);

        // Select Birth Date
        regBirthDate = (Button) findViewById(R.id.regBirthDate);
        datePickerFragment = new DatePickerFragment();
        datePickerFragment.setOutput(regBirthDate);

        regBirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                builder.setMessage(getResources().getString(R.string.msg_register_cannot_changed))
                        .setTitle(getResources().getString(R.string.remarks))
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                datePickerFragment.show(getSupportFragmentManager(), "datePicker");
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        // Select Gender
        regGender = (Button) findViewById(R.id.regGender);
        genderPickerFragment = new GenderPickerFragment();
        genderPickerFragment.setOutput(regGender);

        regGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                builder.setMessage(getResources().getString(R.string.msg_register_cannot_changed))
                        .setTitle(getResources().getString(R.string.remarks))
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                genderPickerFragment.show(getSupportFragmentManager(), "datePicker");
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        // Select Country
        regCountry = (Button) findViewById(R.id.regCountry);
        countryPickerFragment = new CountryPickerFragment();
        countryPickerFragment.setOutput(regCountry);

        regCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countryPickerFragment.show(getSupportFragmentManager(), "countryPicker");
            }
        });

        // Select Region
        regRegion = (Button) findViewById(R.id.regRegion);
        regionPickerFragment = new RegionPickerFragment();
        regionPickerFragment.setOutput(regRegion);

        regRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (regCountry.getTag() == null) {
                    showToast(getResources().getString(R.string.error_register_country_required));
                    return;
                }

                String url = Url.REGIONS + "?country=" + regCountry.getTag();

                Log.d("Register", "Fetch Region Url: " + url);

                regRegion.setEnabled(false);
                regRegion.setText(getResources().getString(R.string.fetching_region));

                StringRequest jsObjRequest = new StringRequest
                        (Request.Method.GET, url, new Response.Listener<String>() {

                            @Override
                            public void onResponse(String response) {
                                Log.d("Login", "Response: " + response);

                                try {
                                    JSONObject jsonResp = new JSONObject(response);
                                    JSONArray dataArray = jsonResp.getJSONArray("data");

                                    JSONObject data;
                                    for (int i = 0; i < dataArray.length(); i++) {
                                        data = dataArray.getJSONObject(i);
                                        String code = data.getString("code");
                                        if ("0".equals(code)) {
                                            JSONArray regions = data.getJSONArray("regions");
                                            regionPickerFragment.setArray(regions.join(",").replaceAll("\"", "").split(","));
                                            regionPickerFragment.show(getSupportFragmentManager(), "regionPicker");
                                        } else {
                                            showErrorMessage(data.getString("message"));
                                        }
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                regRegion.setText(getResources().getString(R.string.select_region));
                                regRegion.setEnabled(true);
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("Login", "Error: " + error.getMessage());
                                showErrorMessage(error.getMessage());
                                regRegion.setText(getResources().getString(R.string.select_region));
                                regRegion.setEnabled(true);
                            }
                        });
                VolleySingleton.getInstance(RegisterActivity.this).addToRequestQueue(jsObjRequest);
            }
        });

        btnSignUp = (Button) findViewById(R.id.btnSignUp);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String username = regUsername.getText().toString();
                if ("".equals(username)) {
                    regUsername.setError(getResources().getString(R.string.error_username_required));
                    scrollToView(regUsername);
                    return;
                }

                final String name = regName.getText().toString();
                if ("".equals(name)) {
                    regName.setError(getResources().getString(R.string.error_register_name_required));
                    scrollToView(regName);
                    return;
                }

                final String password = regPassword.getText().toString();
                if ("".equals(password)) {
                    regPassword.setError(getResources().getString(R.string.error_password_required));
                    scrollToView(regPassword);
                    return;
                }

                final String email = regEmail.getText().toString();
                if ("".equals(email)) {
                    regEmail.setError(getResources().getString(R.string.error_register_email_required));
                    scrollToView(regEmail);
                    return;
                }

                final String birthDate = regBirthDate.getText().toString();
                if ("Select Birth Date".equals(birthDate)) {
                    regBirthDate.setError(getResources().getString(R.string.error_register_birth_date_required));
                    showToast(getResources().getString(R.string.error_register_birth_date_required));
                    scrollToView(regBirthDate);
                    return;
                }

                final String gender = regGender.getText().toString();
                if (getResources().getString(R.string.select_gender).equals(gender)) {
                    regGender.setError(getResources().getString(R.string.error_register_gender_required));
                    showToast(getResources().getString(R.string.error_register_gender_required));
                    return;
                }

                final String country = (String) regCountry.getTag();
                if (country == null) {
                    regCountry.setError(getResources().getString(R.string.error_register_country_required));
                    showToast(getResources().getString(R.string.error_register_country_required));
                    return;
                }

                if (regRegion.getTag() == null) {
                    regRegion.setError(getResources().getString(R.string.error_register_region_required));
                    showToast(getResources().getString(R.string.error_register_region_required));
                    return;
                }

                final int region = (int) regRegion.getTag();

                hideErrorMessage();
                btnSignUp.setText(getResources().getString(R.string.signing_up));
                btnSignUp.setEnabled(false);

                StringRequest jsObjRequest = new StringRequest
                        (Request.Method.POST, Url.SIGNUP, new Response.Listener<String>() {

                            @Override
                            public void onResponse(String response) {
                                Log.d("Login", "Response: " + response);

                                try {
                                    JSONObject jsonResp = new JSONObject(response);
                                    JSONArray dataArray = jsonResp.getJSONArray("data");

                                    JSONObject data;
                                    for (int i = 0; i < dataArray.length(); i++) {
                                        data = dataArray.getJSONObject(i);
                                        String code = data.getString("code");
                                        if ("0".equals(code)) {
                                            String userId = data.getString("user_id");
                                            String userPassword = data.getString("password");
                                            Log.d("Login", "Registered user_id = " + userId +
                                                    ", password = " + userPassword);
                                            AuthManager.getInstance(RegisterActivity.this)
                                                    .setLogin(userId, userPassword);
                                            finish();
                                        } else {
                                            showErrorMessage(data.getString("message"));
                                        }
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                btnSignUp.setText(getResources().getString(R.string.sign_up));
                                btnSignUp.setEnabled(true);
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("Login", "Error: " + error.getMessage());
                                btnSignUp.setText(getResources().getString(R.string.sign_up));
                                btnSignUp.setEnabled(true);
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("username", username);
                        params.put("fullname", name);
                        params.put("password", password);
                        params.put("user_email", email);
                        params.put("gender", (gender.equals("Male") ? "0" : "1"));
                        params.put("birthdate", birthDate);
                        params.put("country", country);
                        params.put("region", region+"");
                        params.put("p_chk", "key");
                        params.put("device", "2");

                        return params;
                    }
                };

                VolleySingleton.getInstance(RegisterActivity.this).addToRequestQueue(jsObjRequest);
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void hideErrorMessage() {
        txtError.setText("");
        errorContainer.setVisibility(View.GONE);
    }

    private void showErrorMessage(String message) {
        txtError.setText(message);
        errorContainer.setVisibility(View.VISIBLE);

        // Scroll to errors
        scrollToView(errorContainer);
    }

    private void scrollToView(final View view) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                scrollView.scrollTo(0, view.getTop());
            }
        });
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private Button output;
        private int selectedYear, selectedMonth, selectedDay;

        public DatePickerFragment(){
            selectedYear = -1;
            selectedMonth = -1;
            selectedDay = -1;
        }

        public void setOutput(Button output) {
            this.output = output;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = selectedYear > -1 ? selectedYear : c.get(Calendar.YEAR);
            int month = selectedMonth > -1 ? selectedMonth : c.get(Calendar.MONTH);
            int day =selectedDay > -1 ? selectedDay :  c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            selectedYear = year;
            selectedMonth = month;
            selectedDay = day;

            output.setText(formatLeadingZero(month+1) + "-" + formatLeadingZero(day) + "-" + year);
            output.setError(null);
        }

        private String formatLeadingZero(int digit) {
            return (digit < 10 ? "0" : "") + digit;
        }
    }

    public static class GenderPickerFragment extends DialogFragment {

        private Button output;

        public void setOutput(Button output) {
            this.output = output;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.select_gender)
                    .setItems(R.array.gender, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String[] gender = getActivity().getResources().getStringArray(R.array.gender);
                            output.setText(gender[which]);
                            output.setError(null);
                        }
                    });
            return builder.create();
        }
    }

    public static class CountryPickerFragment extends DialogFragment {

        private Button output;

        public void setOutput(Button output) {
            this.output = output;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.select_country)
                    .setItems(R.array.countries, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String[] countries = getActivity().getResources().getStringArray(R.array.countries);
                            output.setText(countries[which]);
                            output.setError(null);

                            String[] countryCodes = getActivity().getResources().getStringArray(R.array.country_codes);
                            String selectedCountryCode = countryCodes[which];

                            output.setTag(selectedCountryCode);
                        }
                    });
            return builder.create();
        }
    }

    public static class RegionPickerFragment extends DialogFragment {

        private Button output;
        private String[] array;

        public void setOutput(Button output) {
            this.output = output;
        }

        public void setArray(String[] array) {
            this.array = array;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.select_region)
                    .setItems(array, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            output.setText(array[which]);
                            output.setTag(which);
                            output.setError(null);
                        }
                    });
            return builder.create();
        }
    }
}
