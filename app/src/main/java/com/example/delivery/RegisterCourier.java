package com.example.delivery;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.delivery.model.Courier;
import com.example.delivery.repository.CourierRepository;
import com.example.delivery.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.CompletableFuture;

public class RegisterCourier extends AppCompatActivity {

    private EditText firstNameEditText;
    private EditText surNameEditText;
    private EditText phoneEditText;
    private RadioGroup radioGroupCourierType;
    private Button registerButton;

    private CourierRepository courierRepository;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_courier);

        firstNameEditText = findViewById(R.id.editTextFirstName);
        surNameEditText = findViewById(R.id.editTextSurName);
        phoneEditText = findViewById(R.id.editTextPhone);
        radioGroupCourierType = findViewById(R.id.radioGroupCourierType);
        registerButton = findViewById(R.id.registerButton);

        courierRepository = new CourierRepository(FirebaseFirestore.getInstance());
        userRepository = new UserRepository(FirebaseFirestore.getInstance());

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerCourier();
            }
        });
    }

    private void registerCourier() {
        String firstName = firstNameEditText.getText().toString().trim();
        String surName = surNameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String typeOfCourier = getSelectedCourierType();

        if (validateInputs(firstName, surName, phone, typeOfCourier)) {
            CompletableFuture<String> emailFuture = getCurrentUserEmail();

            emailFuture.thenAccept(email -> {
                if (email != null) {
                    courierRepository.getCourierByPhone(phone).thenAccept(existingCourier -> {
                        if (existingCourier != null) {
                            runOnUiThread(() -> Toast.makeText(RegisterCourier.this, "Этот номер телефона уже используется", Toast.LENGTH_SHORT).show());
                        } else {
                            CompletableFuture<Courier> courierFuture = courierRepository.addCourier(firstName, surName, phone, typeOfCourier, "0.00", "0");

                            courierFuture.thenAccept(courier -> runOnUiThread(() -> {
                                if (courier != null) {
                                    Toast.makeText(RegisterCourier.this, "Курьер успешно зарегистрирован!", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(RegisterCourier.this, "Не удалось зарегистрировать курьера", Toast.LENGTH_SHORT).show();
                                }
                            })).exceptionally(e -> {
                                runOnUiThread(() -> Toast.makeText(RegisterCourier.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                return null;
                            });
                        }
                    }).exceptionally(e -> {
                        runOnUiThread(() -> Toast.makeText(RegisterCourier.this, "Ошибка проверки телефона: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        return null;
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(RegisterCourier.this, "Не удалось получить почту пользователя", Toast.LENGTH_SHORT).show());
                }
            });
        }
    }

    private CompletableFuture<String> getCurrentUserEmail() {
        CompletableFuture<String> emailFuture = new CompletableFuture<>();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Проверяем, авторизован ли пользователь
        if (currentUser != null) {
            String userId = currentUser.getUid();

            userRepository.getUserById(userId).thenAccept(user -> {
                if (user != null) {
                    emailFuture.complete(user.email);
                } else {
                    emailFuture.complete(null);
                }
            }).exceptionally(e -> {
                emailFuture.completeExceptionally(e);
                return null;
            });
        } else {
            runOnUiThread(() -> Toast.makeText(RegisterCourier.this, "Пользователь не авторизован. Пожалуйста, войдите в систему.", Toast.LENGTH_SHORT).show());
            emailFuture.complete(null);
        }

        return emailFuture;
    }


    private String getSelectedCourierType() {
        int selectedId = radioGroupCourierType.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedId);
        return selectedRadioButton != null ? selectedRadioButton.getText().toString() : null;
    }

    private boolean validateInputs(String firstName, String surName, String phone, String typeOfCourier) {
        if (TextUtils.isEmpty(firstName)) {
            Toast.makeText(this, "Имя обязательно", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(surName)) {
            Toast.makeText(this, "Фамилия обязательна", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Телефон обязателен", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(typeOfCourier)) {
            Toast.makeText(this, "Тип курьера обязателен", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
