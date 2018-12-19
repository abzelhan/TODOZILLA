package com.example.abzal.todozilla.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.abzal.todozilla.R;
import com.example.abzal.todozilla.activity.AuthActivity;
import com.example.abzal.todozilla.cache.SharedPrefsUtils;
import com.example.abzal.todozilla.client.TodozillaRestClient;
import com.example.abzal.todozilla.constant.ErrorProcessor;
import com.example.abzal.todozilla.constant.TodozillaApi;
import com.example.abzal.todozilla.model.UserProfile;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;


public class SettingsFragment extends Fragment {

    private Button button;
    private Button saveBtn;
    private EditText nameInput;
    private EditText surnameInput;
    private String token;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        initViews(view);
        return view;
    }

    private void updateUiByRequest(){
        TodozillaRestClient.get(getActivity(), TodozillaApi.USER_RPOFILE_ROUTE, new Header[]{TodozillaRestClient.createAuthTokenHeader(token)}, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                UserProfile userProfile = UserProfile.parseJsonObject(response);
                String name = userProfile.getName();
                String surname = userProfile.getSurname();
                if (name == null) {
                    nameInput.setText("Not exist");
                } else {
                    nameInput.setText(name);
                }
                if (surname == null) {
                    surnameInput.setText("Not exist");
                } else {
                    surnameInput.setText(surname);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                ErrorProcessor.handle(getView(), getContext(), statusCode);
            }
        });
    }

    private void initViews(View view) {
        button = view.findViewById(R.id.logoutBtn);
        saveBtn = view.findViewById(R.id.saveBtn);
        nameInput = view.findViewById(R.id.name_input);
        surnameInput = view.findViewById(R.id.surname_input);

        token = SharedPrefsUtils.getStringPreference(getActivity(), "token");

        updateUiByRequest();

        saveBtn.setOnClickListener(v -> {
            String name = nameInput.getText().toString();
            String surname = surnameInput.getText().toString();

            boolean isValid = true;

            if (name.equals("") || name.equals("Not exist")) {
                isValid = false;
                nameInput.setError("Specify the name!");
            }

            if (surname.equals("") || surname.equals("Not exist")) {
                isValid = false;
                surnameInput.setError("Specify the surname!");
            }

            if (isValid) {
                StringEntity entity = null;
                try {
                    entity = new StringEntity(UserProfile.createJsonObject(name, surname).toString());
                } catch (UnsupportedEncodingException e) {
                }
                TodozillaRestClient.put(
                        getContext(),
                        TodozillaApi.USER_RPOFILE_ROUTE,
                        new Header[]{TodozillaRestClient.createAuthTokenHeader(token)},
                        entity,
                        RequestParams.APPLICATION_JSON,
                        new AsyncHttpResponseHandler() {

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                Snackbar.make(view, "Information was updated", Snackbar.LENGTH_LONG)
                                        .show();
                                updateUiByRequest();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                ErrorProcessor.handle(view, getActivity(), statusCode);
                            }

                        });
            }

        });

        button.setOnClickListener(v -> {
            SharedPrefsUtils.setStringPreference(getContext(), "token", null);
            Intent myIntent = new Intent(getContext(), AuthActivity.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(myIntent);
            return;
        });
    }

}
