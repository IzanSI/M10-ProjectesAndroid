package com.example.myapplication;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface OdooApiService {
    @POST("/web/session/authenticate")
    Call<JsonObject> authenticate(@Body JsonObject loginRequest);
}
