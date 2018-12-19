package com.example.abzal.todozilla.model;

import org.json.JSONException;
import org.json.JSONObject;

public class UserProfile {

    private String name;
    private String surname;

    public UserProfile() {
    }

    public UserProfile(String name, String surname) {
        this.name = name;
        this.surname = surname;
    }

    public static UserProfile parseJsonObject(JSONObject object) {
        UserProfile userProfile = new UserProfile();
        try {
            if(!object.isNull("Name")){
                userProfile.setName(object.getString("Name"));
            }
            if (!object.isNull("Surname")) {
                userProfile.setSurname(object.getString("Surname"));
            }
        } catch (JSONException e) {
        }
        return userProfile;
    }

    public static JSONObject createJsonObject(
            String name,
            String surname

    ) {
        JSONObject jsonParams = new JSONObject();

        try {
            if (name != null) {
                jsonParams.put("Name", name);
            }
            if (surname != null) {
                jsonParams.put("Surname", surname);
            }
        } catch (JSONException e) {

        }
        return jsonParams;
    }

    public String getName() {
        return name;
    }

    public UserProfile setName(String name) {
        this.name = name;
        return this;
    }

    public String getSurname() {
        return surname;
    }

    public UserProfile setSurname(String surname) {
        this.surname = surname;
        return this;
    }
}
