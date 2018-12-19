package com.example.abzal.todozilla.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Task {

    private long id;
    private long userId;
    private long categoryId;
    private String title;
    private String description;
    private long createdAt;
    private long expiredAt;
    private boolean done;

    public static List<Task> parseJsonArray(JSONArray array) {
        ArrayList<Task> tasks = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                try {
                    tasks.add(parseJsonObject(array.getJSONObject(i)));
                } catch (JSONException e) {
                }
            }
        }
        return tasks;
    }

    public static Task parseJsonObject(JSONObject object) {
        Task task = new Task();
        try {
            task.setId(object.getLong("Id"));
            task.setTitle(object.getString("Name"));
            task.setDescription(object.getString("Description"));
            task.setCreatedAt(object.getLong("CreatedAt"));
            task.setExpiredAt(object.getLong("ExpiredAt"));
            task.setDone(object.getBoolean("isDone"));
            task.setCategoryId(object.getLong("CategoryID"));
        } catch (JSONException e) {
        }
        return task;
    }

    public static JSONObject createJsonObject(
            Long id,
            String title,
            String description,
            Long createdAt,
            Long expiredAt,
            Boolean done,
            Long categoryId

    ) {
        JSONObject jsonParams = new JSONObject();

        try {
            if (id != null)
                jsonParams.put("Id", id);

            if (title != null)
                jsonParams.put("Name", title);

            if (description != null)
                jsonParams.put("Description", description);

            if (createdAt != null)
                jsonParams.put("CreatedAt", createdAt);

            if (expiredAt != null)
                jsonParams.put("ExpiredAt", expiredAt);

            if (done != null)
                jsonParams.put("isDone", done);

            if (categoryId != null)
                jsonParams.put("CategoryID", categoryId);

        } catch (JSONException e) { }

        return jsonParams;
    }


    public long getId() {
        return id;
    }

    public Task setId(long id) {
        this.id = id;
        return this;
    }

    public long getUserId() {
        return userId;
    }

    public Task setUserId(long userId) {
        this.userId = userId;
        return this;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public Task setCategoryId(long categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Task setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Task setDescription(String description) {
        this.description = description;
        return this;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Task setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public long getExpiredAt() {
        return expiredAt;
    }

    public Task setExpiredAt(long expiredAt) {
        this.expiredAt = expiredAt;
        return this;
    }

    public boolean isDone() {
        return done;
    }

    public Task setDone(boolean done) {
        this.done = done;
        return this;
    }

}
