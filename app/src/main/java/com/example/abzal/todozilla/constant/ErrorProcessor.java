package com.example.abzal.todozilla.constant;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.example.abzal.todozilla.activity.AuthActivity;

public class ErrorProcessor {

    public static void handle(View view, Context context, int statusCode) {
        String errorMessage;

        if (statusCode == 0) {
            errorMessage = "Server unavailable";
        } else if (statusCode == 403) {
            Intent myIntent = new Intent(context, AuthActivity.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(myIntent);
            return;
        } else {
            errorMessage = "Server error";
        }

        Snackbar.make(view, errorMessage, Snackbar.LENGTH_LONG)
                .show();
    }

}
