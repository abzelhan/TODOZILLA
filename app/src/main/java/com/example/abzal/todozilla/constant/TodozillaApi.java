package com.example.abzal.todozilla.constant;

public interface TodozillaApi {

    String PROTOCOL = "http";
    String IP = "10.0.2.2";
    String PORT = "5080";

    String BASE_URL = PROTOCOL + "://" + IP + ":" + PORT + "/api";

    String LOGIN_ROUTE = "/users/auth";
    String REGISTRATION_ROUTE = "/users/register";
    String CATEGORIES_ROUTE = "/categories";
    String TASKS_ROUTE = "/tasks";
    String USER_RPOFILE_ROUTE = "/userprofile";

}
