package com.example.abzal.todozilla.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.abzal.todozilla.R;
import com.example.abzal.todozilla.cache.SharedPrefsUtils;
import com.example.abzal.todozilla.client.TodozillaRestClient;
import com.example.abzal.todozilla.constant.ErrorProcessor;
import com.example.abzal.todozilla.constant.Mode;
import com.example.abzal.todozilla.constant.TodozillaApi;
import com.example.abzal.todozilla.model.Category;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class CategoryEditorActivity extends AppCompatActivity {

    private EditText titleInput;
    private EditText descriptionInput;
    private FloatingActionButton saveBtn;
    private Mode mode;
    private String token;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_editor);
        initViews();
    }

    private void initViews() {
        titleInput = findViewById(R.id.category_title_input);
        descriptionInput = findViewById(R.id.category_description_input);
        saveBtn = findViewById(R.id.category_save_btn);
        mode = Mode.valueOf(getIntent().getStringExtra("mode"));
        token = SharedPrefsUtils.getStringPreference(CategoryEditorActivity.this, "token");
        if (mode == Mode.CREATE) {
            setTitle("Create new category");
            saveBtn.setOnClickListener(new CreateCategoryClickListener());
        } else if (mode == Mode.UPDATE) {
            setTitle("Update category");
            saveBtn.setImageDrawable(getDrawable(R.drawable.ic_refresh_white));
            long id = getIntent().getLongExtra("id", 0L);

            TodozillaRestClient.get(
                    getApplicationContext(),
                    TodozillaApi.CATEGORIES_ROUTE + "/" + id,
                    new Header[]{TodozillaRestClient.createAuthTokenHeader(token)},
                    null,
                    new JsonHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Category category = Category.parseJsonObject(response);
                            titleInput.setText(category.getTitle());
                            descriptionInput.setText(category.getDescription());
                            saveBtn.setOnClickListener(new UpdateCategoryClickListener(category));
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            ErrorProcessor.handle(getCurrentFocus(), CategoryEditorActivity.this, statusCode);
                            CategoryEditorActivity.this.finish();
                        }
                    });

        }
    }

    private class CreateCategoryClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            String categoryTitle = titleInput.getText().toString();
            String categoryDescription = descriptionInput.getText().toString();

            boolean isValid = true;

            if (categoryTitle == null || categoryTitle.isEmpty()) {
                titleInput.setError("Title cannot be empty!");
                isValid = false;
            }

            if (isValid) {
                StringEntity entity = null;
                try {
                    entity = new StringEntity(Category.createJsonObject(null, categoryTitle, categoryDescription).toString());
                } catch (UnsupportedEncodingException e) {
                }
                TodozillaRestClient.post(
                        getApplicationContext(),
                        TodozillaApi.CATEGORIES_ROUTE,
                        new Header[]{TodozillaRestClient.createAuthTokenHeader(token)},
                        entity,
                        RequestParams.APPLICATION_JSON,
                        new AsyncHttpResponseHandler() {

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                Toast.makeText(CategoryEditorActivity.this, "Category created!", Toast.LENGTH_SHORT).show();
                                CategoryEditorActivity.this.finish();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                ErrorProcessor.handle(getCurrentFocus(), CategoryEditorActivity.this, statusCode);
                            }

                        });
            }
        }
    }

    private class UpdateCategoryClickListener implements View.OnClickListener {

        private Category category;

        public UpdateCategoryClickListener(Category category) {
            this.category = category;
        }

        @Override
        public void onClick(View v) {
            String categoryTitle = titleInput.getText().toString();
            String categoryDescription = descriptionInput.getText().toString();

            boolean isValid = true;

            if (categoryTitle == null || categoryTitle.isEmpty()) {
                titleInput.setError("Title cannot be empty!");
                isValid = false;
            }

            if (isValid) {
                StringEntity entity = null;
                try {
                    entity = new StringEntity(Category.createJsonObject(category.getId(), categoryTitle, categoryDescription).toString());
                } catch (UnsupportedEncodingException e) {
                }
                TodozillaRestClient.put(
                        getApplicationContext(),
                        TodozillaApi.CATEGORIES_ROUTE,
                        new Header[]{TodozillaRestClient.createAuthTokenHeader(token)},
                        entity,
                        RequestParams.APPLICATION_JSON,
                        new AsyncHttpResponseHandler() {

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                Toast.makeText(CategoryEditorActivity.this, "Category Updated!", Toast.LENGTH_SHORT).show();
                                CategoryEditorActivity.this.finish();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                ErrorProcessor.handle(getCurrentFocus(), CategoryEditorActivity.this, statusCode);
                            }

                        });
            }
        }
    }
}
