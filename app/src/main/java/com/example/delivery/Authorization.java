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
import com.google.firebase.auth.FirebaseUser;

public class Authorization extends AppCompatActivity {

    private UserRepository userRepository;
    private EditText emailEditText, passwordEditText;
    private TextView errorTextView;
    private boolean isPasswordVisible = false;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER_ME = "rememberMe";
    private CheckBox rememberMeCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        userRepository = new UserRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance());

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        errorTextView = findViewById(R.id.errorTextView);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);


        loadUserCredentials();

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
                saveUserCredentials(); // Сохранение данных пользователя
            } else {
                clearUserCredentials(); // Очистка данных
            }
        });
    }

    private void loadUserCredentials() {
        String savedEmail = sharedPreferences.getString(KEY_EMAIL, "");
        String savedPassword = sharedPreferences.getString(KEY_PASSWORD, "");
        boolean rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);

        if (rememberMe && !savedEmail.isEmpty() && !savedPassword.isEmpty()) {
            emailEditText.setText(savedEmail);
            passwordEditText.setText(savedPassword);
            rememberMeCheckBox.setChecked(true);
            authenticateUser(savedEmail, savedPassword);
        } else {
            emailEditText.setText("");
            passwordEditText.setText("");
            rememberMeCheckBox.setChecked(false);
        }
    }


    private void saveUserCredentials() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!email.isEmpty() && !password.isEmpty()) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_EMAIL, email);
            editor.putString(KEY_PASSWORD, password);
            editor.putBoolean(KEY_REMEMBER_ME, true);
            editor.apply();
        }
    }

    private void clearUserCredentials() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_PASSWORD);
        editor.putBoolean(KEY_REMEMBER_ME, false);
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


        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            Intent intent = new Intent(Authorization.this, UserProfile.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        errorTextView.setVisibility(View.VISIBLE);
                        errorTextView.setText("Неправильный email или пароль");
                    }
                });
    }

    public void onClickGoToRegistration(View view) {
        Intent intent = new Intent(Authorization.this, Registration.class);
        startActivity(intent);
        finish();
    }

    private void authenticateUser(String email, String password) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        Intent intent = new Intent(Authorization.this, UserProfile.class);
                        startActivity(intent);
                        finish();
                    } else {
                    }
                });
    }
}
