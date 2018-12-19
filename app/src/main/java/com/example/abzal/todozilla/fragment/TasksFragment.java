package com.example.abzal.todozilla.fragment;


import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.abzal.todozilla.R;
import com.example.abzal.todozilla.activity.CategoryEditorActivity;
import com.example.abzal.todozilla.activity.TaskEditorActivity;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class TasksFragment extends Fragment {

    private RecyclerView taskRecyclerView;
    private FloatingActionButton createTaskBtn;
    private LinearLayout emptyListView;
    private LinearLayout loadingView;
    private TaskAdapter taskAdapter;
    private Mode mode;
    private String token;

    public TasksFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getAllTasks();
    }

    private void initViews(View view) {
        Bundle bundle = getArguments();
        mode = Mode.valueOf(bundle.getString("mode"));

        createTaskBtn = view.findViewById(R.id.create_task_floating_button);
        createTaskBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), TaskEditorActivity.class);
            intent.putExtra("mode", Mode.CREATE.toString());
            getContext().startActivity(intent);
        });
        token = SharedPrefsUtils.getStringPreference(getActivity(), "token");

        taskRecyclerView = view.findViewById(R.id.task_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        taskRecyclerView.setLayoutManager(layoutManager);
        taskAdapter = new TaskAdapter();
        taskRecyclerView.setAdapter(taskAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(taskRecyclerView.getContext(),
                layoutManager.getOrientation());
        taskRecyclerView.addItemDecoration(dividerItemDecoration);

        emptyListView = view.findViewById(R.id.task_list_empty);
        loadingView = view.findViewById(R.id.loading_screen);

        if (mode == Mode.FINISHED) {
            createTaskBtn.hide();
        } else {
            createTaskBtn.show();
            ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return true;
                }

                @Override
                public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    super.clearView(recyclerView, viewHolder);
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    List<Task> tasks = taskAdapter.getItems();
                    Task task = tasks.get(viewHolder.getAdapterPosition());
                    if (direction == ItemTouchHelper.LEFT) {

                        TodozillaRestClient.delete(getContext(),
                                TodozillaApi.TASKS_ROUTE + "/" + task.getId(),
                                new Header[]{TodozillaRestClient.createAuthTokenHeader(token)},
                                null,
                                new AsyncHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                        Toast.makeText(getActivity(), "Task deleted!", Toast.LENGTH_SHORT).show();
                                        getAllTasks();
                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                        Toast.makeText(getActivity(), "Delete Task. Error Code:" + statusCode, Toast.LENGTH_SHORT).show();
                                        taskAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                                    }
                                });
                    }

                    if (direction == ItemTouchHelper.RIGHT) {
                        StringEntity entity = null;
                        try {
                            entity = new StringEntity(Task.createJsonObject(task.getId(), null, null, null, task.getExpiredAt(), true, null).toString());
                        } catch (UnsupportedEncodingException e) {
                        }
                        TodozillaRestClient.put(
                                getActivity(),
                                TodozillaApi.TASKS_ROUTE + "/" + task.getId(),
                                new Header[]{TodozillaRestClient.createAuthTokenHeader(token)},
                                entity,
                                RequestParams.APPLICATION_JSON,
                                new AsyncHttpResponseHandler() {

                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                        Toast.makeText(getActivity(), "Task finished!", Toast.LENGTH_SHORT).show();
                                        getAllTasks();
                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                        Toast.makeText(getActivity(), "Done Task. Error Code:" + statusCode, Toast.LENGTH_SHORT).show();
                                        taskAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                                    }

                                });
                    }
                }
            };
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
            itemTouchHelper.attachToRecyclerView(taskRecyclerView);
        }
    }

    private void getAllTasks() {
        enableLoadingView();

        String route = mode == Mode.IN_PROGRESS ? TodozillaApi.TASKS_ROUTE + "/current" : TodozillaApi.TASKS_ROUTE + "/finished";

        TodozillaRestClient.get(getActivity(), route, new Header[]{TodozillaRestClient.createAuthTokenHeader(token)}, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                if (response.length() == 0) {
                    enableEmptyListView();
                } else {
                    List<Task> tasks = Task.parseJsonArray(response);
                    if (!tasks.isEmpty()) {
                        enableTaskListView();
                        taskAdapter.setItems(tasks);
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                enableEmptyListView();
                ErrorProcessor.handle(getView(), getContext(), statusCode);
            }
        });
    }

    private void enableEmptyListView() {
        emptyListView.setVisibility(View.VISIBLE);
        taskRecyclerView.setVisibility(View.GONE);
        loadingView.setVisibility(View.GONE);
    }


    private void enableTaskListView() {
        emptyListView.setVisibility(View.GONE);
        loadingView.setVisibility(View.GONE);
        taskRecyclerView.setVisibility(View.VISIBLE);
    }

    private void enableLoadingView() {
        loadingView.setVisibility(View.VISIBLE);
        emptyListView.setVisibility(View.GONE);
        taskRecyclerView.setVisibility(View.GONE);
    }

    private class TaskAdapter extends RecyclerView.Adapter<TasksFragment.TaskAdapter.TaskViewHolder> {

        private List<Task> taskList = new ArrayList<>();

        private SimpleDateFormat deadlineFormat = new SimpleDateFormat("dd MMMM YYYY,hh:mm");


        public void setItems(Collection<Task> tasks) {
            taskList.clear();
            taskList.addAll(tasks);
            notifyDataSetChanged();
        }

        public List<Task> getItems() {
            return taskList;
        }

        public void clearItems() {
            taskList.clear();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            int resource;
            if (mode == Mode.IN_PROGRESS) {
                resource = R.layout.item_task_in_progress;
            } else {
                resource = R.layout.item_task_finished;
            }
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(resource, viewGroup, false);
            return new TaskAdapter.TaskViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TaskAdapter.TaskViewHolder taskViewHolder, int i) {
            Task task = taskList.get(i);
            taskViewHolder.title.setText(task.getTitle());
            taskViewHolder.categoryName.setVisibility(View.GONE);
            TodozillaRestClient.get(
                    getContext(),
                    TodozillaApi.CATEGORIES_ROUTE + "/" + task.getCategoryId(),
                    new Header[]{TodozillaRestClient.createAuthTokenHeader(token)},
                    null,
                    new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Category category = Category.parseJsonObject(response);
                            taskViewHolder.categoryName.setText(category.getTitle());
                            taskViewHolder.categoryName.setVisibility(View.VISIBLE);
                        }
                    });
            Calendar deadline = Calendar.getInstance();
            deadline.setTimeInMillis(task.getExpiredAt());
            taskViewHolder.deadline.setText(deadlineFormat.format(deadline.getTime()));
            taskViewHolder.infoImg.setOnClickListener((v) -> {

                TodozillaRestClient.get(
                        getContext(),
                        TodozillaApi.CATEGORIES_ROUTE + "/" + task.getCategoryId(),
                        new Header[]{TodozillaRestClient.createAuthTokenHeader(token)},
                        null,
                        new JsonHttpResponseHandler() {

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                Log.d("TaskFragment", "onSuccess: wtf");
                                final Dialog dialog = new Dialog(getContext());
                                dialog.setCancelable(true);
                                dialog.setContentView(R.layout.dialog_task);
                                Category category = Category.parseJsonObject(response);
                                TextView title = dialog.findViewById(R.id.dialog_task_title);
                                title.setText(task.getTitle());
                                TextView description = dialog.findViewById(R.id.dialog_task_description);
                                description.setText(task.getDescription());
                                TextView categoryTitle = dialog.findViewById(R.id.dialog_task_category);
                                categoryTitle.setText(category.getTitle());
                                TextView expiredAt = dialog.findViewById(R.id.dialog_task_expired_at);
                                expiredAt.setText(deadlineFormat.format(deadline.getTime()));
                                Calendar createdAtCalendar = Calendar.getInstance();
                                createdAtCalendar.setTimeInMillis(task.getCreatedAt());
                                TextView createdAt = dialog.findViewById(R.id.dialog_task_created_at);
                                createdAt.setText(deadlineFormat.format(createdAtCalendar.getTime()));
                                taskViewHolder.categoryName.setText(category.getTitle());
                                taskViewHolder.categoryName.setVisibility(View.VISIBLE);
                                dialog.show();
                            }

                        });

            });
            if (mode == Mode.IN_PROGRESS) {
                taskViewHolder.itemView.setOnLongClickListener(v -> {
                    Intent intent = new Intent(getContext(), TaskEditorActivity.class);
                    intent.putExtra("id", task.getId());
                    intent.putExtra("mode", Mode.UPDATE.toString());
                    getContext().startActivity(intent);
                    return true;
                });
            }

        }

        @Override
        public int getItemCount() {
            return taskList.size();
        }

        class TaskViewHolder extends RecyclerView.ViewHolder {

            private TextView title;
            private TextView deadline;
            private TextView categoryName;
            private ImageView infoImg;

            public TaskViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.task_item_title);
                deadline = itemView.findViewById(R.id.task_item_deadline);
                infoImg = itemView.findViewById(R.id.task_item_info_img);
                categoryName = itemView.findViewById(R.id.task_item_category);
            }
        }

    }

}
