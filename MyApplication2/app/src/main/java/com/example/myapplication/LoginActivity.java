package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.OdooClient;
import com.example.myapplication.OdooSessionManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText etServerUrl;
    private EditText etDatabase;
    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private OdooSessionManager sessionManager;

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

        // Inicializar session manager
        sessionManager = new OdooSessionManager(this);

        // Verificar si ya hay sesión activa
        if (sessionManager.isLoggedIn()) {
            // Navegar a la actividad principal
            navigateToMainActivity();
        }

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

        if (serverUrl.isEmpty() || database.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar progreso
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        // Crear solicitud de autenticación
        JsonObject authRequest = OdooClient.createAuthRequest(database, username, password);

        // Realizar la llamada de autenticación
        OdooClient.getApiService().authenticate(authRequest).enqueue(new Callback<JsonObject>() {
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
                            // Guardar información de sesión
                            sessionManager.saveSession(sessionId, userId, username);

                            // Navegar a la actividad principal
                            navigateToMainActivity();
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

    private void navigateToMainActivity() {

        Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
    }
}