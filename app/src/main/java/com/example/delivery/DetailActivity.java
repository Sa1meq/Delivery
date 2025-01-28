package com.example.delivery;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.delivery.repository.CourierRepository;
import com.example.delivery.repository.UserRepository;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DetailActivity extends AppCompatActivity {

    private ShapeableImageView avatarImageView;
    private TextView nameTextView, emailTextView, balanceTextView, ratingTextView, typeTextView, phoneTextView;
    private Button deleteUserButton, makeAdminButton, blockCourierButton, updateBonusButton, updateEarningsButton, setRatingButton;

    private UserRepository userRepository;
    private CourierRepository courierRepository;
    private String userId;
    private String email;

    // Планировщик для блокировки
    private ScheduledExecutorService scheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        userRepository = new UserRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance());
        courierRepository = new CourierRepository(FirebaseFirestore.getInstance());
        scheduler = Executors.newScheduledThreadPool(1);

        avatarImageView = findViewById(R.id.avatarImageView);
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        balanceTextView = findViewById(R.id.balanceTextView);
        ratingTextView = findViewById(R.id.ratingTextView);
        typeTextView = findViewById(R.id.typeTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        deleteUserButton = findViewById(R.id.deleteUserButton);
        makeAdminButton = findViewById(R.id.makeAdminButton);
        blockCourierButton = findViewById(R.id.blockCourierButton);
        updateBonusButton = findViewById(R.id.updateBonusButton);
        updateEarningsButton = findViewById(R.id.updateEarningsButton);
        setRatingButton = findViewById(R.id.updateRatingButton);

        userId = getIntent().getStringExtra("userId");
        email = getIntent().getStringExtra("email");
        String type = getIntent().getStringExtra("type");

        if (type != null && type.equals("user")) {
            loadUserData();
        } else if (type != null && type.equals("courier")) {
            loadCourierData();
        } else {
            Toast.makeText(this, "Неизвестный тип данных", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Установка обработчиков кнопок
        deleteUserButton.setOnClickListener(v -> showDeleteUserDialog());
        makeAdminButton.setOnClickListener(v -> makeUserAdmin());
        blockCourierButton.setOnClickListener(v -> showBlockCourierDialog());
        updateBonusButton.setOnClickListener(v -> updateBonusPoints());
        updateEarningsButton.setOnClickListener(v -> updateEarnings());
        setRatingButton.setOnClickListener(v -> showSetRatingDialog());
    }

    private void loadUserData() {
        String avatarUrl = getIntent().getStringExtra("avatarUrl");
        String name = getIntent().getStringExtra("name");
        String balance = getIntent().getStringExtra("balance");

        Glide.with(this).load(avatarUrl).placeholder(R.drawable.ic_avatar).into(avatarImageView);
        nameTextView.setText(name);
        emailTextView.setText("Email: " + email);
        balanceTextView.setText("Баланс: " + balance);

        // Показать кнопки только для пользователя
        deleteUserButton.setVisibility(View.VISIBLE);
        makeAdminButton.setVisibility(View.VISIBLE);
    }

    private void loadCourierData() {
        String name = getIntent().getStringExtra("name");
        String phone = getIntent().getStringExtra("phone");
        String balance = getIntent().getStringExtra("balance");
        float rating = getIntent().getFloatExtra("rating", 0.0f);
        String typeOfCourier = getIntent().getStringExtra("typeOfCourier");

        nameTextView.setText(name);
        phoneTextView.setVisibility(View.VISIBLE);
        phoneTextView.setText("Телефон: " + phone);
        balanceTextView.setText("Баланс: " + balance);
        ratingTextView.setVisibility(View.VISIBLE);
        ratingTextView.setText("Рейтинг: " + rating);
        typeTextView.setVisibility(View.VISIBLE);
        typeTextView.setText("Тип курьера: " + typeOfCourier);
        blockCourierButton.setVisibility(View.VISIBLE);
        updateBonusButton.setVisibility(View.VISIBLE);
        updateEarningsButton.setVisibility(View.VISIBLE);
        setRatingButton.setVisibility(View.VISIBLE);
    }

    private void showDeleteUserDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Удалить пользователя")
                .setMessage("Вы уверены, что хотите удалить этого пользователя?")
                .setPositiveButton("Да", (dialog, which) -> deleteUser())
                .setNegativeButton("Нет", null)
                .show();
    }

    private void deleteUser() {
        if (userId == null) {
            Toast.makeText(this, "Ошибка: ID пользователя не найден", Toast.LENGTH_SHORT).show();
            return;
        }
        userRepository.deleteUserById(userId, CloudinaryManager.getCloudinary())
                .thenAccept(success -> runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(this, "Пользователь успешно удален", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Ошибка при удалении пользователя", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                }));
    }

    private void makeUserAdmin() {
        if (userId == null) {
            Toast.makeText(this, "Ошибка: ID пользователя не найден", Toast.LENGTH_SHORT).show();
            return;
        }
        userRepository.makeUserAdmin(userId)
                .thenAccept(success -> runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(this, "Пользователь наделен правами администратора", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Ошибка изменения прав пользователя", Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private void showBlockCourierDialog() {
        final EditText timeInput = new EditText(this);
        timeInput.setHint("Введите время блокировки (в часах)");

        new AlertDialog.Builder(this)
                .setTitle("Блокировка курьера")
                .setMessage("Введите время блокировки курьера")
                .setView(timeInput)
                .setPositiveButton("Заблокировать", (dialog, which) -> {
                    String timeText = timeInput.getText().toString();
                    if (!timeText.isEmpty()) {
                        long blockDuration = Long.parseLong(timeText) * 3600000; // конвертируем в миллисекунды
                        blockCourier(blockDuration);
                    } else {
                        Toast.makeText(this, "Пожалуйста, введите время блокировки", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void blockCourier(long blockDuration) {
        if (userId == null) {
            Toast.makeText(this, "Ошибка: ID курьера не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        long blockEndTime = System.currentTimeMillis() + blockDuration;
        courierRepository.blockCourier(userId, blockEndTime)
                .thenAccept(success -> runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(this, "Курьер заблокирован на " + blockDuration / 3600000 + " час(ов)", Toast.LENGTH_SHORT).show();

                        scheduler.schedule(() -> unblockCourier(), blockDuration, TimeUnit.MILLISECONDS);
                    } else {
                        Toast.makeText(this, "Ошибка блокировки курьера", Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private void unblockCourier() {
        if (userId == null) {
            Toast.makeText(this, "Ошибка: ID курьера не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        // Разблокируем курьера
        courierRepository.unblockCourier(userId)
                .thenAccept(success -> runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(this, "Курьер успешно разблокирован", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Ошибка разблокировки курьера", Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private void showSetRatingDialog() {
        final EditText ratingInput = new EditText(this);
        ratingInput.setHint("Введите новый рейтинг");

        new AlertDialog.Builder(this)
                .setTitle("Изменить рейтинг курьера")
                .setMessage("Введите новый рейтинг курьера")
                .setView(ratingInput)
                .setPositiveButton("Установить", (dialog, which) -> {
                    String ratingText = ratingInput.getText().toString();
                    if (!ratingText.isEmpty()) {
                        float rating = Float.parseFloat(ratingText);
                        setCourierRating(rating);
                    } else {
                        Toast.makeText(this, "Пожалуйста, введите рейтинг", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void setCourierRating(float rating) {
        if (userId == null) {
            Toast.makeText(this, "Ошибка: ID курьера не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        courierRepository.updateCourierRating(userId, rating)
                .thenAccept(success -> runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(this, "Рейтинг курьера обновлен", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Ошибка обновления рейтинга", Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private void updateBonusPoints() {
        // Реализуйте логику для обновления бонусных очков
        Toast.makeText(this, "Бонусные очки обновлены", Toast.LENGTH_SHORT).show();
    }

    private void updateEarnings() {
        // Реализуйте логику для обновления заработка
        Toast.makeText(this, "Заработок обновлен", Toast.LENGTH_SHORT).show();
    }
}
