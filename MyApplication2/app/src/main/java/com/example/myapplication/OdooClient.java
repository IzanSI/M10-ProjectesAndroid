package com.example.myapplication;

import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class OdooClient {
    private static final String BASE_URL = "http://192.168.1.129";
    private static OkHttpClient okHttpClient;
    private static Retrofit retrofit;
    private static OdooApiService apiService;

    private static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
        }
        return okHttpClient;
    }

    private static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(getOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static OdooApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofit().create(OdooApiService.class);
        }
        return apiService;
    }

    public static JsonObject createAuthRequest(String db, String username, String password) {
        JsonObject params = new JsonObject();
        params.addProperty("db", db);
        params.addProperty("login", username);
        params.addProperty("password", password);

        JsonObject request = new JsonObject();
        request.add("params", params);

        return request;
    }
}