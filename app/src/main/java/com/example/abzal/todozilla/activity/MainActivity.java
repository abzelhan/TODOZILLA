package com.example.abzal.todozilla.activity;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;

import com.example.abzal.todozilla.R;
import com.example.abzal.todozilla.fragment.CategoriesFragment;
import com.example.abzal.todozilla.fragment.DashboardFragment;
import com.example.abzal.todozilla.fragment.SettingsFragment;
import com.example.abzal.todozilla.fragment.TasksFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_dashboard:
                getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame, new DashboardFragment()).commit();
                return true;
            case R.id.navigation_category:
                getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame, new CategoriesFragment()).commit();
                return true;
            case R.id.navigation_settings:
                getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame, new SettingsFragment()).commit();
                return true;

        }
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_dashboard);
    }

}
