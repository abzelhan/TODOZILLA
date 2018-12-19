package com.example.abzal.todozilla.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.abzal.todozilla.R;
import com.example.abzal.todozilla.activity.AuthActivity;
import com.example.abzal.todozilla.cache.SharedPrefsUtils;


public class SettingsFragment extends Fragment {

    private Button button;
    private Button saveBtn;
    private EditText nameInput;
    private EditText surnameInput;

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

    private void initViews(View view) {
        button = view.findViewById(R.id.logoutBtn);
        saveBtn = view.findViewById(R.id.saveBtn);
        nameInput = view.findViewById(R.id.name_input);
        surnameInput = view.findViewById(R.id.surname_input);

        saveBtn.setOnClickListener(v->{
            String name = nameInput.getText().toString();
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
