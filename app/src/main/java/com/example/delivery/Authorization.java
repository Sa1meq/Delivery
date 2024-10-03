package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.delivery.auth.Authentication;
import com.example.delivery.repository.UserRepository;
import com.google.firebase.firestore.FirebaseFirestore;

public class Authorization extends AppCompatActivity {

    public UserRepository userRepository;
    public EditText emailEditText, passwordEditText;
    public TextView errorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);

        userRepository = new UserRepository(FirebaseFirestore.getInstance());

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        errorTextView = findViewById(R.id.errorTextView);  // Добавьте TextView для отображения ошибок
    }

    public void onClickLogin(View view) {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            errorTextView.setVisibility(View.VISIBLE);
            errorTextView.setText("Все поля обязательны");
            return;
        }

        userRepository.getUserByEmail(email)
                .thenAccept(user -> {
                    if (user == null || !user.getPassword().equals(password)) {
                        errorTextView.setVisibility(View.VISIBLE);
                        runOnUiThread(() -> errorTextView.setText("Неправильный email или пароль"));
                        return;
                    }

                    Authentication.setUser(user);

                    Intent intent = new Intent(Authorization.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> errorTextView.setText("Ошибка авторизации: " + e.getMessage()));
                    return null;
                });
    }

    public void onClickGoToRegistration(View view) {
        Intent intent = new Intent(Authorization.this, Registration.class);
        startActivity(intent);
        finish();
    }
}