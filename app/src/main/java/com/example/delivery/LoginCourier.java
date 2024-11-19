package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.delivery.repository.CourierRepository;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginCourier extends AppCompatActivity {

    private EditText phoneEditText;
    private Button loginButton;
    private TextView textViewRegister;
    private TextView errorTextView;

    private CourierRepository courierRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_courier);

        phoneEditText = findViewById(R.id.editTextPhone);
        loginButton = findViewById(R.id.loginButton);
        textViewRegister = findViewById(R.id.textViewRegister);
        errorTextView = findViewById(R.id.errorTextView);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        courierRepository = new CourierRepository(db);

        if (TextUtils.isEmpty(phoneEditText.getText())) {
            phoneEditText.setText("+375");
        }

        phoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int after) {
                String phone = charSequence.toString();
                if (!phone.startsWith("+375")) {
                    phoneEditText.setText("+375");
                    phoneEditText.setSelection(4);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        loginButton.setOnClickListener(v -> loginCourier());

        textViewRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginCourier.this, RegisterCourier.class);
            startActivity(intent);
        });
    }

    private void loginCourier() {
        String phone = phoneEditText.getText().toString().trim();

        errorTextView.setVisibility(View.GONE);

        if (validateInputs(phone)) {
            courierRepository.getCourierByPhone(phone).thenAccept(existingCourier -> {
                if (existingCourier != null) {
                    if (existingCourier.isVerified()) {
                        Intent intent = new Intent(LoginCourier.this, CourierProfile.class);
                        startActivity(intent);
                        finish();
                    } else {
                        showErrorMessage("Верификация не пройдена.");
                    }
                } else {
                    showErrorMessage("Курьер не найден!");
                }
            }).exceptionally(e -> {
                showErrorMessage("Ошибка при проверке телефона: " + e.getMessage());
                return null;
            });
        }
    }

    private boolean validateInputs(String phone) {
        if (TextUtils.isEmpty(phone)) {
            showErrorMessage("Телефон обязателен.");
            return false;
        }
        return true;
    }

    private void showErrorMessage(String message) {
        runOnUiThread(() -> {
            errorTextView.setText(message);
            errorTextView.setVisibility(View.VISIBLE);
        });
    }
}
