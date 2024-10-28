package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
    private RadioGroup radioGroupCourierType;
    private Button registerButton;

    private CourierRepository courierRepository;

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
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String userId = auth.getCurrentUser().getUid();
            Courier courier = new Courier(userId, firstName, surName, phone, typeOfCourier, "0.00", "0");
            courierRepository.addCourier(courier).thenAccept(aVoid -> {
                Toast.makeText(RegisterCourier.this, "Курьер успешно зарегистрирован!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterCourier.this, CourierProfile.class);
                startActivity(intent);
                finish();
            }).exceptionally(e -> {
                Toast.makeText(RegisterCourier.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return null;
            });
        }
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