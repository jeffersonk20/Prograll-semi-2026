package com.example.miprimeraapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

public class LoginActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "welcome_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        createNotificationChannel();

        EditText etEmail = findViewById(R.id.etEmail);
        Button btnSignIn = findViewById(R.id.btnSignIn);

        btnSignIn.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim().toLowerCase();
            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa tu correo", Toast.LENGTH_SHORT).show();
            } else {
                // Guardar la sesión como "Logueado"
                SharedPreferences globalPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                globalPrefs.edit()
                        .putBoolean("isLoggedIn", true)
                        .putString("currentUserEmail", email)
                        .apply();

                sendWelcomeNotification(email);
                
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("isGuest", false);
                intent.putExtra("userEmail", email);
                startActivity(intent);
                finish();
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Welcome Channel";
            String description = "Channel for welcome notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void sendWelcomeNotification(String email) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("¡Bienvenido a JEFFPHONE!")
                .setContentText("Hola " + email + ", gracias por volver.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }
    }
}
