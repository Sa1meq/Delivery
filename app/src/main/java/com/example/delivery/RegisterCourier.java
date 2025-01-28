package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
    private TextView errorTextView;

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
        errorTextView = findViewById(R.id.errorTextView); // Инициализация errorTextView
        courierRepository = new CourierRepository(FirebaseFirestore.getInstance());

        ImageView closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterCourier.this, UserProfile.class);
            startActivity(intent);
            finish();
        });

        phoneEditText.setText("+375");
        phoneEditText.setSelection(4);

        findViewById(R.id.cardPedestrian).setOnClickListener(view -> {
            typeOfCourier = "Пеший";
            setSelectedCard(view);
        });
        findViewById(R.id.cardCar).setOnClickListener(view -> {
            typeOfCourier = "Авто";
            setSelectedCard(view);
        });
        findViewById(R.id.cardTruck).setOnClickListener(view -> {
            typeOfCourier = "Грузовой";
            setSelectedCard(view);
        });

        registerButton.setOnClickListener(v -> registerCourier());

        textViewLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterCourier.this, LoginCourier.class);
            startActivity(intent);
        });

        phoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // Этот метод не используется
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int after) {
                String currentText = phoneEditText.getText().toString();
                if (!currentText.startsWith("+375")) {
                    phoneEditText.setText("+375");
                    phoneEditText.setSelection(4);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void registerCourier() {
        String firstName = firstNameEditText.getText().toString().trim();
        String surName = surNameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        errorTextView.setVisibility(View.GONE);

        if (validateInputs(firstName, surName, phone, typeOfCourier)) {
            courierRepository.getCourierByPhone(phone).thenAccept(existingCourier -> {
                if (existingCourier != null) {
                    showErrorMessage("Курьер с таким номером уже существует.");
                } else {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    FirebaseUser currentUser = auth.getCurrentUser();

                    if (currentUser != null) {
                        String userId = currentUser.getUid();
                        Courier courier = new Courier(userId, firstName, surName, phone, typeOfCourier, 0.00F, "0", "0.00", 0, false, 0, "active", 0);

                        courierRepository.addCourier(courier, userId).thenAccept(aVoid -> {
                            showErrorMessage("Ожидайте верификации админом.");
                        }).exceptionally(e -> {
                            showErrorMessage("Ошибка: " + e.getMessage());
                            return null;
                        });
                    } else {
                        showErrorMessage("Ошибка аутентификации");
                    }
                }
            }).exceptionally(e -> {
                showErrorMessage("Ошибка проверки курьера: " + e.getMessage());
                return null;
            });
        }
    }


    private boolean validateInputs(String firstName, String surName, String phone, String typeOfCourier) {
        if (TextUtils.isEmpty(firstName)) {
            showErrorMessage("Имя обязательно");
            return false;
        }
        if (TextUtils.isEmpty(surName)) {
            showErrorMessage("Фамилия обязательна");
            return false;
        }
        if (TextUtils.isEmpty(phone)) {
            showErrorMessage("Телефон обязателен");
            return false;
        }
        if (!isValidPhone(phone)) {
            showErrorMessage("Неверный формат телефона. Используйте: +375 (XX) XXXXXXX");
            return false;
        }
        if (TextUtils.isEmpty(typeOfCourier)) {
            showErrorMessage("Тип курьера обязателен");
            return false;
        }
        return true;
    }

    private boolean isValidPhone(String phone) {
        // Проверка на соответствие формату +375 (XX) XXXXXXX
        String phonePattern = "^\\+375\\d{2}\\d{7}$"; // Регулярное выражение для проверки формата
        return phone.matches(phonePattern);
    }

    private void showErrorMessage(String message) {
        errorTextView.setVisibility(View.VISIBLE);
        errorTextView.setText(message);
    }

    private void setSelectedCard(View selectedCard) {
        findViewById(R.id.cardPedestrian).setBackgroundColor(getResources().getColor(R.color.default_card_background));
        findViewById(R.id.cardCar).setBackgroundColor(getResources().getColor(R.color.default_card_background));
        findViewById(R.id.cardTruck).setBackgroundColor(getResources().getColor(R.color.default_card_background));

        selectedCard.setBackgroundColor(getResources().getColor(R.color.selected_card_background));
    }
}
