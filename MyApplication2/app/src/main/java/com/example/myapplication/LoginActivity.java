package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {
    private EditText etServerUrl;
    private EditText etDatabase;
    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;

    // URL permitida
    private static final String ALLOWED_URL = "192.168.231.253";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar vistas
        etServerUrl = findViewById(R.id.etServerUrl);
        etDatabase = findViewById(R.id.etDatabase);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void login() {
        String serverUrl = etServerUrl.getText().toString().trim();
        String database = etDatabase.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();

        // Verificar que todos los campos estén completados
        if (serverUrl.isEmpty() || database.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar la URL (puede ser con o sin protocolo)
        if (!isValidServerUrl(serverUrl)) {
            Toast.makeText(this, "La URL del servidor no es válida. Debe ser: " + ALLOWED_URL, Toast.LENGTH_LONG).show();
            return;
        }

        // Normalizar la URL para asegurar que tenga formato http://
        String normalizedUrl = normalizeUrl(serverUrl);

        // Mostrar progreso
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        // Crear cliente Odoo para esta URL específica
        OdooApiService apiService = createApiService(normalizedUrl);

        // Crear solicitud de autenticación
        JsonObject authRequest = createAuthRequest(database, username, password);

        // Realizar la llamada de autenticación
        apiService.authenticate(authRequest).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                if (response.isSuccessful()) {
                    JsonObject responseJson = response.body();
                    JsonObject result = null;

                    if (responseJson != null && responseJson.has("result")) {
                        result = responseJson.getAsJsonObject("result");
                    }

                    if (result != null && !result.has("error")) {
                        // Extraer información de sesión
                        int userId = -1;
                        if (result.has("uid") && !result.get("uid").isJsonNull()) {
                            userId = result.get("uid").getAsInt();
                        }

                        String sessionId = "";
                        if (response.headers().get("set-cookie") != null) {
                            sessionId = response.headers().get("set-cookie");
                        }

                        if (userId > 0) {
                            // Login exitoso
                            Toast.makeText(LoginActivity.this,
                                    "Inicio de sesión exitoso! Usuario ID: " + userId,
                                    Toast.LENGTH_SHORT).show();

                            // Aquí podrías guardar la sesión y navegar a otra actividad
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Credenciales inválidas", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String errorMsg = "Error de autenticación";
                        if (result != null && result.has("error")) {
                            JsonObject error = result.getAsJsonObject("error");
                            if (error.has("message")) {
                                errorMsg = error.get("message").getAsString();
                            }
                        }
                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this,
                        "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Verifica si la URL es permitida
    private boolean isValidServerUrl(String url) {
        // Eliminar protocolo si existe
        String host = url.replaceAll("^https?://", "");

        // Eliminar puerto si existe
        host = host.replaceAll(":\\d+$", "");

        // Eliminar cualquier ruta adicional
        host = host.replaceAll("/.*$", "");

        return host.equals(ALLOWED_URL);
    }

    // Normaliza la URL para asegurar formato http://
    private String normalizeUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "http://" + url;
        }
        return url;
    }

    // Crea el servicio API para una URL específica
    private OdooApiService createApiService(String baseUrl) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(OdooApiService.class);
    }

    // Crea la solicitud de autenticación
    private JsonObject createAuthRequest(String db, String username, String password) {
        JsonObject params = new JsonObject();
        params.addProperty("db", db);
        params.addProperty("login", username);
        params.addProperty("password", password);

        JsonObject request = new JsonObject();
        request.add("params", params);

        return request;
    }

    // Interfaz API de Odoo
    public interface OdooApiService {
        @POST("/web/session/authenticate")
        Call<JsonObject> authenticate(@Body JsonObject loginRequest);
    }
}