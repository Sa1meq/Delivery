package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.delivery.repository.CourierRepository;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginCourier extends AppCompatActivity {

    private EditText enterCodeEditText; // Поле для ввода кода входа
    private Button loginButton;
    private TextView errorTextView;

    private CourierRepository courierRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_courier);

        enterCodeEditText = findViewById(R.id.editTextPhone); // Инициализация поля для кода входа
        loginButton = findViewById(R.id.loginButton);
        errorTextView = findViewById(R.id.errorTextView);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        courierRepository = new CourierRepository(db);

        // Обработка нажатия на кнопку входа
        loginButton.setOnClickListener(v -> loginCourier());

    }

    private void loginCourier() {
        String enterCode = enterCodeEditText.getText().toString().trim();

        errorTextView.setVisibility(View.GONE);

        if (validateInputs(enterCode)) {
            // Ищем курьера по коду входа
            courierRepository.getCourierByEnterCode(enterCode).thenAccept(existingCourier -> {
                if (existingCourier != null) {
                    if (existingCourier.isVerified()) { // Проверяем, верифицирован ли курьер
                        Intent intent = new Intent(LoginCourier.this, CourierProfile.class);
                        intent.putExtra("COURIER_ID", existingCourier.getId()); // Передаем ID курьера
                        startActivity(intent);
                        finish();
                    } else {
                        showErrorMessage("Верификация не пройдена.");
                    }
                } else {
                    showErrorMessage("Курьер с таким кодом не найден!");
                }
            }).exceptionally(e -> {
                showErrorMessage("Ошибка при проверке кода: " + e.getMessage());
                return null;
            });
        }
    }

    private boolean validateInputs(String enterCode) {
        if (TextUtils.isEmpty(enterCode)) {
            showErrorMessage("Код входа обязателен.");
            return false;
        }
        if (enterCode.length() != 6) {
            showErrorMessage("Код входа должен состоять из 6 символов.");
            return false;
        }
        return true;
    }

    // Отображение сообщения об ошибке
    private void showErrorMessage(String message) {
        runOnUiThread(() -> {
            errorTextView.setText(message);
            errorTextView.setVisibility(View.VISIBLE);
        });
    }
}