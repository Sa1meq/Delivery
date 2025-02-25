package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.delivery.model.Courier;
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
            courierRepository.getCourierByEnterCode(enterCode).thenAccept(existingCourier -> {
                if (existingCourier != null) {
                    // Проверка на верификацию
                    if (!existingCourier.isVerified()) {
                        showErrorMessage("Верификация не пройдена.");
                        return;
                    }

                    // Проверка на блокировку
                    if (existingCourier.getStatus().equals("blocked")) {
                        long currentTime = System.currentTimeMillis();
                        long blockedUntil = existingCourier.getBlockedUntil();

                        if (blockedUntil > currentTime) {
                            long remainingTime = (blockedUntil - currentTime) / 1000; // Оставшееся время в секундах
                            String message = String.format("Курьер заблокирован. До разблокировки осталось %d секунд.", remainingTime);
                            showErrorMessage(message);
                        } else {
                            // Время блокировки истекло, разблокируем курьера
                            courierRepository.unblockCourier(existingCourier.getId())
                                    .thenAccept(success -> {
                                        if (success) {
                                            // Разблокировка успешна, разрешаем вход
                                            proceedToCourierProfile(existingCourier);
                                        } else {
                                            showErrorMessage("Ошибка при разблокировке курьера.");
                                        }
                                    });
                        }
                    } else {
                        // Курьер не заблокирован, разрешаем вход
                        proceedToCourierProfile(existingCourier);
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

    private void proceedToCourierProfile(Courier courier) {
        Intent intent = new Intent(LoginCourier.this, CourierProfile.class);
        intent.putExtra("COURIER_ID", courier.getId());
        startActivity(intent);
        finish();
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