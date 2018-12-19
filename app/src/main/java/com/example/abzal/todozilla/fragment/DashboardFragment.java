package com.example.abzal.todozilla.fragment;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.abzal.todozilla.R;
import com.example.abzal.todozilla.constant.Mode;

public class DashboardFragment extends Fragment {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        Bundle bundle = new Bundle();
        TasksFragment fragment = new TasksFragment();
        fragment.setArguments(bundle);
        switch (item.getItemId()) {
            case R.id.navigation_in_progress:{
                bundle.putString("mode",Mode.IN_PROGRESS.toString());
                fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.dashboard_frame, fragment).commit();
                return true;
            }
            case R.id.navigation_finished:
                bundle.putString("mode",Mode.FINISHED.toString());
                fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.dashboard_frame, fragment).commit();
                return true;
        }
        return false;
    };

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        BottomNavigationView navigation = view.findViewById(R.id.navigation_dashboard);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_in_progress);
        return view;
    }


}
