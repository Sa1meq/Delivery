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
import com.google.firebase.firestore.FirebaseFirestore;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class RegisterCourier extends AppCompatActivity {

    public static final String ACCOUNT_SID = "ACb44d5bfb439b84c474d07e997219e32e";
    public static final String AUTH_TOKEN = "bf36b27d48340d11e848aa179f1a8757";
    public static final String TWILIO_PHONE_NUMBER = "+15046085363"; // Измените формат, если нужно

    private EditText firstNameEditText;
    private EditText surNameEditText;
    private EditText phoneEditText;
    private RadioGroup radioGroupCourierType;
    private Button registerButton;

    private CourierRepository courierRepository;

    private String generatedCode;

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

        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = phoneEditText.getText().toString().trim();
                sendVerificationCode(phone);
            }
        });
    }

    private void sendVerificationCode(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber) || !phoneNumber.startsWith("+")) {
            Toast.makeText(this, "Введите корректный номер телефона с кодом страны", Toast.LENGTH_SHORT).show();
            return;
        }

        generatedCode = String.valueOf(new Random().nextInt(899999) + 100000);

        try {
            Message message = Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(TWILIO_PHONE_NUMBER),
                    "Ваш код подтверждения: " + generatedCode
            ).create();

            Toast.makeText(RegisterCourier.this, "Код подтверждения отправлен!", Toast.LENGTH_SHORT).show();

            // Показать диалог для ввода кода
            showCodeInputDialog();
        } catch (Exception e) {
            Toast.makeText(RegisterCourier.this, "Не удалось отправить SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showCodeInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Введите код подтверждения");

        final EditText codeInput = new EditText(this);
        builder.setView(codeInput);

        builder.setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String enteredCode = codeInput.getText().toString().trim();
                if (!TextUtils.isEmpty(enteredCode)) {
                    verifyCode(enteredCode);
                } else {
                    Toast.makeText(RegisterCourier.this, "Введите код", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void verifyCode(String enteredCode) {
        if (enteredCode.equals(generatedCode)) {
            registerCourier();
        } else {
            Toast.makeText(this, "Неверный код подтверждения", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerCourier() {
        String firstName = firstNameEditText.getText().toString().trim();
        String surName = surNameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String typeOfCourier = getSelectedCourierType();

        if (validateInputs(firstName, surName, phone, typeOfCourier)) {
            courierRepository.getCourierByPhone(phone).thenAccept(existingCourier -> {
                if (existingCourier != null) {
                    runOnUiThread(() -> Toast.makeText(RegisterCourier.this, "Этот номер телефона уже используется", Toast.LENGTH_SHORT).show());
                } else {
                    // Добавляем курьера в базу данных
                    CompletableFuture<Courier> courierFuture = courierRepository.addCourier(
                            firstName, surName, phone, typeOfCourier, "0.00", "0");

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
