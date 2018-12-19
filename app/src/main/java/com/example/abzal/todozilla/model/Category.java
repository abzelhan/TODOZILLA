package com.example.abzal.todozilla.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Category {

    private long id;
    private long userId;
    private String title;
    private String description;

    public static List<Category> parseJsonArray(JSONArray array) {
        ArrayList<Category> categories = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                try {
                    categories.add(parseJsonObject(array.getJSONObject(i)));
                } catch (JSONException e) {
                }
            }
        }
        return categories;
    }

    public static Category parseJsonObject(JSONObject object) {
        Category category = new Category();
        try {
            category.setId(object.getLong("TaskCategoryID"));
            category.setTitle(object.getString("CategoryName"));
            category.setDescription(object.getString("Description"));
        } catch (JSONException e) {
        }
        return category;
    }

    public static JSONObject createJsonObject(
            Long id,
            String title,
            String description) {
        JSONObject jsonParams = new JSONObject();

        try {
            if (id != null)
                jsonParams.put("Id", id);

            if (title != null)
                jsonParams.put("Name", title);

            if (description != null)
                jsonParams.put("Description", description);

        } catch (JSONException e) { }

        return jsonParams;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
