package com.example.abzal.todozilla.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.abzal.todozilla.R;
import com.example.abzal.todozilla.cache.SharedPrefsUtils;
import com.example.abzal.todozilla.client.TodozillaRestClient;
import com.example.abzal.todozilla.constant.ErrorProcessor;
import com.example.abzal.todozilla.constant.Mode;
import com.example.abzal.todozilla.constant.TodozillaApi;
import com.example.abzal.todozilla.model.Category;
import com.example.abzal.todozilla.model.Task;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class TaskEditorActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private EditText dateEditText;
    private EditText timeEditText;
    private EditText titleInput;
    private EditText descriptionInput;
    private FloatingActionButton saveBtn;
    private Mode mode;
    private String token;
    private Calendar mUserReminderDate;
    private Spinner categorySpinner;
    private List<Category> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_editor);
        initViews();
    }

    private void initViews() {
        titleInput = findViewById(R.id.task_title_input);
        descriptionInput = findViewById(R.id.task_description_input);
        saveBtn = findViewById(R.id.task_save);
        dateEditText = findViewById(R.id.task_date_input);
        timeEditText = findViewById(R.id.task_time_input);
        categorySpinner = findViewById(R.id.category_selector);
        token = SharedPrefsUtils.getStringPreference(this, "token");
        Intent intent = getIntent();
        mode = Mode.valueOf(intent.getStringExtra("mode"));

        if (mode == Mode.CREATE) {
            setTitle("Create new task");
            saveBtn.setOnClickListener(new CreateTaskClickListener());
            mUserReminderDate = Calendar.getInstance();
            boolean time24 = DateFormat.is24HourFormat(getApplicationContext());
            if (time24) {
                mUserReminderDate.set(Calendar.HOUR_OF_DAY, mUserReminderDate.get(Calendar.HOUR_OF_DAY) + 1);
            } else {
                mUserReminderDate.set(Calendar.HOUR, mUserReminderDate.get(Calendar.HOUR) + 1);
            }
            setDateAndTimeEditText();
            initListCategorySpinner(categorySpinner, null);
        } else if (mode == Mode.UPDATE) {
            setTitle("Update task");
            saveBtn.setImageDrawable(getDrawable(R.drawable.ic_refresh_white));
            long id = getIntent().getLongExtra("id", 0L);

            TodozillaRestClient.get(
                    getApplicationContext(),
                    TodozillaApi.TASKS_ROUTE + "/" + id,
                    new Header[]{TodozillaRestClient.createAuthTokenHeader(token)},
                    null,
                    new JsonHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Task task = Task.parseJsonObject(response);
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(task.getExpiredAt());
                            titleInput.setText(task.getTitle());
                            descriptionInput.setText(task.getDescription());
                            mUserReminderDate = calendar;
                            setDateAndTimeEditText();
                            initListCategorySpinner(categorySpinner, task.getCategoryId());
                            categorySpinner.setVisibility(View.GONE);
                            findViewById(R.id.select_category_text).setVisibility(View.GONE);
                            saveBtn.setOnClickListener(new TaskEditorActivity.UpdateTaskClickListener(task));
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            ErrorProcessor.handle(getCurrentFocus(), TaskEditorActivity.this, statusCode);
                            TaskEditorActivity.this.finish();
                        }
                    });
        }


        dateEditText.setOnClickListener(v -> {

            Calendar calendar = mUserReminderDate;
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(TaskEditorActivity.this, year, month, day);

            datePickerDialog.show(getFragmentManager(), "DateFragment");

        });

        timeEditText.setOnClickListener(v -> {

            Calendar calendar = mUserReminderDate;
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(TaskEditorActivity.this, hour, minute, DateFormat.is24HourFormat(TaskEditorActivity.this));

            timePickerDialog.show(getFragmentManager(), "TimeFragment");
        });

    }

    private void initListCategorySpinner(Spinner spinner, Long categoryId) {
        TodozillaRestClient.get(getApplicationContext(), TodozillaApi.CATEGORIES_ROUTE, new Header[]{TodozillaRestClient.createAuthTokenHeader(token)}, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                categories = Category.parseJsonArray(response);
                if (!categories.isEmpty()) {
                    List<String> titles = categories.stream().map(category -> category.getTitle()).collect(Collectors.toList());
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(TaskEditorActivity.this, android.R.layout.simple_spinner_item, titles.toArray(new String[titles.size()]));
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                    if (categoryId != null) {
                        for (int i = 0; i < categories.size(); i++) {
                            if (categoryId.equals(categories.get(i).getId())) {
                                spinner.setSelection(i);
                            }
                        }
                    } else {
                        spinner.setSelection(0);
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                ErrorProcessor.handle(getCurrentFocus(), TaskEditorActivity.this, statusCode);
            }
        });
    }

    public static String formatDate(String formatString, Date dateToFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatString);
        return simpleDateFormat.format(dateToFormat);
    }

    private void setDateAndTimeEditText() {

        if (mUserReminderDate != null) {
            String userDate = formatDate("d MMM, yyyy", mUserReminderDate.getTime());
            String formatToUse;
            if (DateFormat.is24HourFormat(getApplicationContext())) {
                formatToUse = "k:mm";
            } else {
                formatToUse = "h:mm a";

            }
            String userTime = formatDate(formatToUse, mUserReminderDate.getTime());
            timeEditText.setText(userTime);
            dateEditText.setText(userDate);

        } else {
            boolean time24 = DateFormat.is24HourFormat(getApplicationContext());
            Calendar cal = Calendar.getInstance();
            if (time24) {
                cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) + 1);
            } else {
                cal.set(Calendar.HOUR, cal.get(Calendar.HOUR) + 1);
            }
            mUserReminderDate = cal;
            String userDate = formatDate("d MMM, yyyy", mUserReminderDate.getTime());

            String timeString;
            if (time24) {
                timeString = formatDate("k:mm", mUserReminderDate.getTime());
            } else {
                timeString = formatDate("h:mm a", mUserReminderDate.getTime());
            }
            timeEditText.setText(timeString);
            dateEditText.setText(userDate);
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Log.d("Task", "onDateSet: " + year + " " + monthOfYear + " " + dayOfMonth);
        mUserReminderDate.set(Calendar.YEAR, year);
        mUserReminderDate.set(Calendar.MONTH, monthOfYear);
        mUserReminderDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        setDateAndTimeEditText();
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        Log.d("Task", "onTimeSet: " + hourOfDay + " " + minute);
        mUserReminderDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mUserReminderDate.set(Calendar.MINUTE, minute);
        setDateAndTimeEditText();

    }

    private class CreateTaskClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            String taskTitle = titleInput.getText().toString();
            String taskDescription = descriptionInput.getText().toString();
            boolean isValid = true;

            if (taskTitle == null || taskTitle.isEmpty()) {
                titleInput.setError("Title cannot be empty!");
                isValid = false;
            }

            if (isValid) {
                StringEntity entity = null;
                try {
                    int itemPosition = categorySpinner.getSelectedItemPosition();
                    Category category = categories.get(itemPosition);
                    entity = new StringEntity(Task.createJsonObject(null, taskTitle, taskDescription, System.currentTimeMillis(), mUserReminderDate.getTimeInMillis(), false, category.getId()).toString());
                    TodozillaRestClient.post(
                            getApplicationContext(),
                            TodozillaApi.TASKS_ROUTE,
                            new Header[]{TodozillaRestClient.createAuthTokenHeader(token)},
                            entity,
                            RequestParams.APPLICATION_JSON,
                            new AsyncHttpResponseHandler() {

                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    Toast.makeText(TaskEditorActivity.this, "Category created!", Toast.LENGTH_SHORT).show();
                                    TaskEditorActivity.this.finish();
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                    ErrorProcessor.handle(getCurrentFocus(), TaskEditorActivity.this, statusCode);
                                }

                            });
                } catch (UnsupportedEncodingException e) {
                }
            }

        }
    }

    private class UpdateTaskClickListener implements View.OnClickListener {

        private Task task;

        public UpdateTaskClickListener(Task task) {
            this.task = task;
        }

        @Override
        public void onClick(View v) {
            String taskTitle = titleInput.getText().toString();
            String taskDescription = descriptionInput.getText().toString();

            boolean isValid = true;

            if (taskTitle == null || taskTitle.isEmpty()) {
                titleInput.setError("Title cannot be empty!");
                isValid = false;
            }

            if (isValid) {
                StringEntity entity = null;
                try {
                    entity = new StringEntity(Task.createJsonObject(task.getId(), taskTitle, taskDescription, task.getCreatedAt(), mUserReminderDate.getTimeInMillis(), task.isDone(), task.getCategoryId()).toString());
                } catch (UnsupportedEncodingException e) {
                }
                TodozillaRestClient.put(
                        getApplicationContext(),
                        TodozillaApi.TASKS_ROUTE + "/" + task.getId(),
                        new Header[]{TodozillaRestClient.createAuthTokenHeader(token)},
                        entity,
                        RequestParams.APPLICATION_JSON,
                        new AsyncHttpResponseHandler() {

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                Toast.makeText(TaskEditorActivity.this, "Task Updated!", Toast.LENGTH_SHORT).show();
                                TaskEditorActivity.this.finish();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                ErrorProcessor.handle(getCurrentFocus(), TaskEditorActivity.this, statusCode);
                            }

                        });
            }
        }
    }


}
