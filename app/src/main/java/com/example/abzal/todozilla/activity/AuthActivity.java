package com.example.abzal.todozilla.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.abzal.todozilla.R;
import com.example.abzal.todozilla.cache.SharedPrefsUtils;
import com.example.abzal.todozilla.client.TodozillaRestClient;
import com.example.abzal.todozilla.constant.SharedPrefs;
import com.example.abzal.todozilla.constant.TodozillaApi;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.entity.StringEntity;

public class AuthActivity extends AppCompatActivity {

    private Button registrationBtn;
    private Button loginBtn;
    private EditText emailInput;
    private EditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        setTitle("Authorization");

        loginBtn = findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener((v) -> {
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();
            doLogin(email, password);
        });

        registrationBtn = findViewById(R.id.registrationBtn);
        registrationBtn.setOnClickListener((v) -> {
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();
            doRegistration(email, password);
        });

        emailInput = findViewById(R.id.emailTextInput);
        passwordInput = findViewById(R.id.passwordTextInput);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkAuthentication();
    }

    private void doRegistration(String email, String password) {
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("username", email);
            jsonParams.put("password", password);
            StringEntity entity = new StringEntity(jsonParams.toString());
            TodozillaRestClient.post(
                    AuthActivity.this.getApplicationContext(),
                    TodozillaApi.REGISTRATION_ROUTE,
                    entity,
                    RequestParams.APPLICATION_JSON,
                    new AsyncHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            if (statusCode == HttpStatus.SC_OK) {
                                Toast.makeText(AuthActivity.this, "Registration success!", Toast.LENGTH_SHORT).show();
                                doLogin(email, password);
                            } else {
                                Log.d(AuthActivity.class.getName(), String.valueOf(statusCode));
                                Toast.makeText(AuthActivity.this, "The email is busy!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.d(AuthActivity.class.getName(), String.valueOf(statusCode));
                            if (statusCode == HttpStatus.SC_BAD_REQUEST) {
                                Toast.makeText(AuthActivity.this, "The email is busy!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AuthActivity.this, "Error code: " + statusCode, Toast.LENGTH_SHORT).show();
                            }
                        }

                    });
        } catch (Exception e) {
            Log.e(AuthActivity.class.getName(), e.getMessage());
            Toast.makeText(AuthActivity.this, "Exception message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void doLogin(String email, String password) {
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("username", email);
            jsonParams.put("password", password);
            StringEntity entity = new StringEntity(jsonParams.toString());
            TodozillaRestClient.post(
                    AuthActivity.this.getApplicationContext(),
                    TodozillaApi.LOGIN_ROUTE,
                    entity,
                    RequestParams.APPLICATION_JSON,
                    new JsonHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            if (statusCode == HttpStatus.SC_OK) {
                                //shared preference
                                Toast.makeText(AuthActivity.this, "Hello " + email + "!", Toast.LENGTH_SHORT).show();
                                try {
                                    String token = (String) response.get(SharedPrefs.TOKEN_KEY);
                                    SharedPrefsUtils.setStringPreference(AuthActivity.this, SharedPrefs.TOKEN_KEY, String.format(SharedPrefs.TOKEN_HEADER_FORMAT, token));
                                    Log.d(AuthActivity.class.getName(), "Success. Status code: " + statusCode);
                                    Log.d(AuthActivity.class.getName(), "Auth Token: " + token);
                                    redirectToMainActivity(AuthActivity.this);
                                } catch (JSONException e) {
                                    onFailure(statusCode, headers, e, response);
                                }
                            } else {
                                Log.d(AuthActivity.class.getName(), String.valueOf(statusCode));
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.d(AuthActivity.class.getName(), String.valueOf(statusCode));
                            Toast.makeText(AuthActivity.this, "Error code: " + statusCode, Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Log.e(AuthActivity.class.getName(), e.getMessage());
            Toast.makeText(AuthActivity.this, "Exception message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAuthentication() {
        String token = SharedPrefsUtils.getStringPreference(AuthActivity.this, SharedPrefs.TOKEN_KEY);
        if (token != null) {
            redirectToMainActivity(AuthActivity.this);
        }
    }

    private void redirectToMainActivity(Context context) {
        Intent myIntent = new Intent(context, MainActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(myIntent);
    }

}