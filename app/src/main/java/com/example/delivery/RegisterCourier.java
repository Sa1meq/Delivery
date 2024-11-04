package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.delivery.model.Courier;
import com.example.delivery.repository.CourierRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterCourier extends AppCompatActivity {

    private EditText firstNameEditText;
    private EditText surNameEditText;
    private EditText phoneEditText;
    private Button registerButton;
    private TextView textViewLogin;

    private String typeOfCourier = null;
    private CourierRepository courierRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_courier);

        firstNameEditText = findViewById(R.id.editTextFirstName);
        surNameEditText = findViewById(R.id.editTextSurName);
        phoneEditText = findViewById(R.id.editTextPhone);
        registerButton = findViewById(R.id.registerButton);
        textViewLogin = findViewById(R.id.textViewLogin);

        courierRepository = new CourierRepository(FirebaseFirestore.getInstance());

        findViewById(R.id.radioButtonPedestrian).setOnClickListener(view -> typeOfCourier = "Pedestrian");
        findViewById(R.id.radioButtonCar).setOnClickListener(view -> typeOfCourier = "Car");
        findViewById(R.id.radioButtonTruck).setOnClickListener(view -> typeOfCourier = "Truck");

        registerButton.setOnClickListener(v -> registerCourier());

        textViewLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterCourier.this, LoginCourier.class);
            startActivity(intent);
        });
    }

    private void registerCourier() {
        String firstName = firstNameEditText.getText().toString().trim();
        String surName = surNameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        if (validateInputs(firstName, surName, phone, typeOfCourier)) {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = auth.getCurrentUser();

            if (currentUser != null) {
                String userId = currentUser.getUid();
                Courier courier = new Courier(userId, firstName, surName, phone, typeOfCourier, "0.00", "0");

                courierRepository.addCourier(courier, userId).thenAccept(aVoid -> {
                    Toast.makeText(RegisterCourier.this, "Курьер успешно зарегистрирован!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterCourier.this, CourierProfile.class);
                    startActivity(intent);
                    finish();
                }).exceptionally(e -> {
                    Toast.makeText(RegisterCourier.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return null;
                });
            } else {
                Toast.makeText(this, "Ошибка аутентификации", Toast.LENGTH_SHORT).show();
            }
        }
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
