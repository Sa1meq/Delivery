package com.example.delivery;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.delivery.auth.Authentication;
import com.example.delivery.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Authorization extends AppCompatActivity {

    public UserRepository userRepository;
    public EditText emailEditText, passwordEditText;
    public TextView errorTextView;
    private boolean isPasswordVisible = false;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private CheckBox rememberMeCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);

        userRepository = new UserRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance());

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        errorTextView = findViewById(R.id.errorTextView);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);

        passwordEditText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_END = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (passwordEditText.getRight() - passwordEditText.getCompoundDrawables()[DRAWABLE_END].getBounds().width())) {
                    togglePasswordVisibility();
                    return true;
                }
            }
            return false;
        });

        rememberMeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
//                saveUserCredentials();
            } else {
                clearUserCredentials();
            }
        });
    }

    private void authenticateUser(String email, String password) {
        // Здесь можно добавить код для проверки пользователя в базе данных
        userRepository.getUserByEmail(email)
                .thenAccept(user -> {
                    if (user == null || !user.getPassword().equals(password)) {
                        Log.d("Authorization", "Неправильный email или пароль при автоматическом входе.");
                        return;
                    }

                    // Если пользователь найден и пароль совпадает
                    Authentication.setUser(user);
                    Log.d("Authorization", "Пользователь автоматически авторизован: " + user.getEmail());

                    // Переход на MainActivity
                    Intent intent = new Intent(Authorization.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .exceptionally(e -> {
                    Log.e("Authorization", "Ошибка при автоматическом входе: " + e.getMessage());
                    return null;
                });
    }

//    private void saveUserCredentials() {
//        String email = emailEditText.getText().toString().trim();
//        String password = passwordEditText.getText().toString().trim();
//
//        if (!email.isEmpty() && !password.isEmpty()) {
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putString(KEY_EMAIL, email);
//            editor.putString(KEY_PASSWORD, password);
//            editor.putBoolean("rememberMe", true); // Сохранение состояния чекбокса
//            editor.apply();
//        }
//    }

    private void clearUserCredentials() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_PASSWORD);
        editor.remove("rememberMe"); // Удаление состояния чекбокса
        editor.apply();
    }

    private void togglePasswordVisibility() {
        Typeface currentTypeface = passwordEditText.getTypeface();
        int selection = passwordEditText.getSelectionEnd();

        if (isPasswordVisible) {
            passwordEditText.setTransformationMethod(new PasswordTransformationMethod());
        } else {
            passwordEditText.setTransformationMethod(null);
        }

        isPasswordVisible = !isPasswordVisible;

        passwordEditText.setTypeface(currentTypeface);
        passwordEditText.setSelection(selection);
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
                        runOnUiThread(() -> {
                            errorTextView.setVisibility(View.VISIBLE);
                            errorTextView.setText("Неправильный email или пароль");
                        });
                        return;
                    }

                    Authentication.setUser(user);
//                    saveUserCredentials(); // Сохранить данные при успешной авторизации

                    runOnUiThread(() -> {
                        Intent intent = new Intent(Authorization.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    });
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> {
                        errorTextView.setVisibility(View.VISIBLE);
                        errorTextView.setText("Ошибка авторизации: " + e.getMessage());
                    });
                    return null;
                });
    }

    public void onClickGoToRegistration(View view) {
        Intent intent = new Intent(Authorization.this, Registration.class);
        startActivity(intent);
        finish();
    }
}
