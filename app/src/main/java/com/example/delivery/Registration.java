package com.example.delivery;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.delivery.model.User;
import com.example.delivery.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Pattern;

public class Registration extends AppCompatActivity {
    private UserRepository userRepository;
    private EditText editName;
    private EditText editEmail;
    private EditText editPassword;
    private EditText repeatPassword;
    private Button registerButton;
    private TextView loginTextView;
    private TextView errorTextView;
    private boolean isPasswordVisible = false;
    private boolean isRepeatPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);

        userRepository = new UserRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance());
        editName = findViewById(R.id.nicknameEditText);
        editEmail = findViewById(R.id.emailEditText);
        editPassword = findViewById(R.id.passwordEditText);
        repeatPassword = findViewById(R.id.repeatEditTextPassword);
        registerButton = findViewById(R.id.registerButton);
        loginTextView = findViewById(R.id.loginTextView);
        errorTextView = findViewById(R.id.errorTextView);
        errorTextView.setVisibility(View.GONE);

        registerButton.setOnClickListener(this::onClickRegistration);
        loginTextView.setOnClickListener(v -> {
            Intent intent = new Intent(Registration.this, Authorization.class);
            startActivity(intent);
        });

        editPassword.setOnClickListener(v -> togglePasswordVisibility());
        repeatPassword.setOnClickListener(v -> toggleRepeatPasswordVisibility());
    }

    public void onClickRegistration(View view) {
        String name = editName.getText().toString();
        String email = editEmail.getText().toString();
        String password = editPassword.getText().toString();
        String repeat = repeatPassword.getText().toString();
        String balance = "0";

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || repeat.isEmpty()) {
            showError("Заполните все поля");
            return;
        }
        if (!password.equals(repeat)) {
            showError("Пароли не совпадают");
            return;
        }

        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        if (!Pattern.compile(emailPattern).matcher(email).matches()) {
            showError("Неправильный формат электронной почты");
            return;
        }

        userRepository.addUser(name, email, password, balance).thenAccept(user -> {
            if (user != null) {
                Intent intent = new Intent(Registration.this, UserProfile.class);
                startActivity(intent);
                finish();
            } else {
                showError("Ошибка регистрации");
            }
        }).exceptionally(e -> {
            showError("Ошибка: " + e.getMessage());
            return null;
        });
    }

    private void togglePasswordVisibility() {
        Typeface currentTypeface = editPassword.getTypeface();
        int selection = editPassword.getSelectionEnd();

        if (isPasswordVisible) {
            editPassword.setTransformationMethod(new PasswordTransformationMethod());
            editPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.visibility_icon, 0);
        } else {
            editPassword.setTransformationMethod(null);
            editPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_visibility_off_24, 0);
        }

        isPasswordVisible = !isPasswordVisible;
        editPassword.setTypeface(currentTypeface);
        editPassword.setSelection(selection);
    }

    private void toggleRepeatPasswordVisibility() {
        Typeface currentTypeface = repeatPassword.getTypeface();
        int selection = repeatPassword.getSelectionEnd();

        if (isRepeatPasswordVisible) {
            repeatPassword.setTransformationMethod(new PasswordTransformationMethod());
            repeatPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.visibility_icon, 0);
        } else {
            repeatPassword.setTransformationMethod(null);
            repeatPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_visibility_off_24, 0);
        }

        isRepeatPasswordVisible = !isRepeatPasswordVisible;
        repeatPassword.setTypeface(currentTypeface);
        repeatPassword.setSelection(selection);
    }

    private void showError(String errorMessage) {
        errorTextView.setText(errorMessage);
        errorTextView.setVisibility(View.VISIBLE);
    }
}
