package com.example.delivery;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.delivery.repository.CourierRepository;
import com.example.delivery.repository.UserRepository;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.api.Distribution;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DetailActivity extends AppCompatActivity {

    private ShapeableImageView avatarImageView;
    private TextView nameTextView, emailTextView, balanceTextView, ratingTextView, typeTextView, phoneTextView, bonusPoints;
    private Button deleteUserButton, makeAdminButton, blockCourierButton, updateBonusButton, updateEarningsButton, setRatingButton, sendEnterCode;
    private LinearLayout phoneLayout, ratingLayout, typeLayout, balanceLayout, bonusLayout;

    private UserRepository userRepository;
    private CourierRepository courierRepository;
    private String userId;
    private String email;

    private static final String PREFS_NAME = "CourierPrefs";
    private static final String KEY_BLOCKED_UNTIL = "blocked_until_";

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        userRepository = new UserRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance());
        courierRepository = new CourierRepository(FirebaseFirestore.getInstance());

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
        sendEnterCode = findViewById(R.id.sendEnterCode);
        bonusPoints = findViewById(R.id.bonusTextView); // Исправлено
        phoneLayout = findViewById(R.id.phoneLayout); // Исправлено
        ratingLayout = findViewById(R.id.ratingLayout); // Исправлено
        typeLayout = findViewById(R.id.typeLayout); // Исправлено
        balanceLayout = findViewById(R.id.balanceLayout); // Исправлено
        bonusLayout = findViewById(R.id.bonusPoints); // Исправлено



        userId = getIntent().getStringExtra("userId");
        email = getIntent().getStringExtra("email");
        String type = getIntent().getStringExtra("type");
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (type != null && type.equals("user")) {
            loadUserData();
        } else if (type != null && type.equals("courier")) {
            loadCourierData();
        } else {
            Toast.makeText(this, "Неизвестный тип данных", Toast.LENGTH_SHORT).show();
            finish();
        }

        deleteUserButton.setOnClickListener(v -> showDeleteUserDialog());
        makeAdminButton.setOnClickListener(v -> makeUserAdmin());
        blockCourierButton.setOnClickListener(v -> showBlockCourierDialog());
        updateBonusButton.setOnClickListener(v -> updateBonusPoints());
        updateEarningsButton.setOnClickListener(v -> updateEarnings());
        setRatingButton.setOnClickListener(v -> showSetRatingDialog());
        sendEnterCode.setOnClickListener(view -> sendEnterCode());
    }

    private void loadUserData() {
        String avatarUrl = getIntent().getStringExtra("avatarUrl");
        String name = getIntent().getStringExtra("name");
        String balance = getIntent().getStringExtra("balance");

        Glide.with(this).load(avatarUrl).placeholder(R.drawable.ic_avatar).into(avatarImageView);
        nameTextView.setText(name);
        emailTextView.setText("Email: " + email);

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
        String email = getIntent().getStringExtra("email");
        String secondName = getIntent().getStringExtra("secondName");
        int bonusPointsValue = getIntent().getIntExtra("bonusPoints", 0);
        String avatarUrl = getIntent().getStringExtra("Url");
        Glide.with(this).load(avatarUrl).placeholder(R.drawable.ic_avatar).into(avatarImageView);
        nameTextView.setText(name + " " + secondName);
        phoneTextView.setText("Телефон: " + phone);
        balanceTextView.setText("Баланс: " + balance + " BYN");
        ratingTextView.setText("Рейтинг: " + rating);
        typeTextView.setText("Тип курьера: " + typeOfCourier);
        emailTextView.setText("Адрес электронной почты: " + email);
        bonusPoints.setText("Бонусные баллы: " + bonusPointsValue);

        phoneLayout.setVisibility(View.VISIBLE);
        balanceLayout.setVisibility(View.VISIBLE);
        ratingLayout.setVisibility(View.VISIBLE);
        typeLayout.setVisibility(View.VISIBLE);
        bonusLayout.setVisibility(View.VISIBLE);
        blockCourierButton.setVisibility(View.VISIBLE);
        updateBonusButton.setVisibility(View.VISIBLE);
        updateEarningsButton.setVisibility(View.VISIBLE);
        setRatingButton.setVisibility(View.VISIBLE);
        sendEnterCode.setVisibility(View.VISIBLE);
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
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_BLOCKED_UNTIL + userId);
        editor.apply();

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
        courierRepository.blockCourier(userId, blockDuration)
                .thenAccept(success -> runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(this, "Курьер заблокирован", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Ошибка блокировки", Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private void saveBlockedUntil(String courierId, long blockedUntil) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_BLOCKED_UNTIL + courierId, blockedUntil);
        editor.apply();
    }

    private long getBlockedUntil(String courierId) {
        return sharedPreferences.getLong(KEY_BLOCKED_UNTIL + courierId, 0);
    }

    private void unblockCourier() {
        if (userId == null) {
            Toast.makeText(this, "Ошибка: ID курьера не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        saveBlockedUntil(userId, 0);

        courierRepository.unblockCourier(userId)
                .thenAccept(success -> runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(this, "Курьер успешно разблокирован", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Ошибка разблокировки курьера", Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private void checkAndUnblockCourier() {
        long blockedUntil = getBlockedUntil(userId);
        if (blockedUntil > 0 && System.currentTimeMillis() >= blockedUntil) {
            unblockCourier();
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        checkAndUnblockCourier();
    }

    private void updateBonusPoints() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Введите бонусные баллы");

        new AlertDialog.Builder(this)
                .setTitle("Обновление бонусных баллов")
                .setView(input)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String value = input.getText().toString();
                    if (!value.isEmpty()) {
                        int bonus = Integer.parseInt(value);
                        courierRepository.updateCourierBonusPoints(userId, bonus)
                                .thenAccept(success -> runOnUiThread(() -> {
                                    if (success) {
                                        Toast.makeText(this, "Бонусные баллы обновлены", Toast.LENGTH_SHORT).show();
                                        loadCourierData();
                                    } else {
                                        Toast.makeText(this, "Ошибка обновления", Toast.LENGTH_SHORT).show();
                                    }
                                }));
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void updateEarnings() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Введите сумму заработка");

        new AlertDialog.Builder(this)
                .setTitle("Обновление заработка")
                .setView(input)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String value = input.getText().toString();
                    if (!value.isEmpty()) {
                        double earnings = Double.parseDouble(value);
                        courierRepository.updateCourierEarnings(userId, earnings)
                                .thenAccept(success -> runOnUiThread(() -> {
                                    if (success) {
                                        Toast.makeText(this, "Заработок обновлен", Toast.LENGTH_SHORT).show();
                                        loadCourierData();
                                    } else {
                                        Toast.makeText(this, "Ошибка обновления", Toast.LENGTH_SHORT).show();
                                    }
                                }));
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void sendEnterCode() {
        new AlertDialog.Builder(this)
                .setTitle("Отправка кода доступа")
                .setMessage("Сгенерировать и отправить новый код доступа курьеру?")
                .setPositiveButton("Отправить", (dialog, which) -> {
                    String enterCode = generateEnterCode();
                    courierRepository.updateCourierEnterCode(userId, enterCode)
                            .thenAccept(success -> runOnUiThread(() -> {
                                if (success) {
                                    sendCodeNotification(enterCode);
                                    sendCodeEmail(enterCode);
                                    Toast.makeText(this, "Код доступа отправлен", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Ошибка отправки кода", Toast.LENGTH_SHORT).show();
                                }
                            }))
                            .exceptionally(e -> {
                                runOnUiThread(() ->
                                        Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                return null;
                            });
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private String generateEnterCode() {
        int codeLength = 6;
        String characters = "0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            int index = (int) (Math.random() * characters.length());
            code.append(characters.charAt(index));
        }
        return code.toString();
    }

    private void sendCodeNotification(String enterCode) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference notificationsRef = database.getReference("notifications").child(userId);

        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("title", "Ваш код доступа");
        notificationData.put("body", "Новый код для входа в аккаунт: " + enterCode);
        notificationData.put("timestamp", System.currentTimeMillis() / 1000);

        notificationsRef.setValue(notificationData)
                .addOnSuccessListener(aVoid -> Log.d("EnterCode", "Уведомление отправлено"))
                .addOnFailureListener(e -> Log.e("EnterCode", "Ошибка уведомления", e));
    }

    private void sendCodeEmail(String enterCode) {
        String email = getIntent().getStringExtra("email");
        if (email != null && !email.isEmpty()) {
            String subject = "Код доступа в аккаунт";
            String body = "Здравствуйте!\n\n"
                    + "Ваш код для входа в аккаунт курьера: " + enterCode + "\n\n"
                    + "Используйте этот код для авторизации в мобильном приложении.\n\n"
                    + "С уважением,\nКоманда OnTheWay Delivery Service.";

            EmailSender.sendEmail(email, subject, body);
        } else {
            Log.e("EnterCode", "Email курьера отсутствует");
            Toast.makeText(this, "Ошибка: email курьера не найден", Toast.LENGTH_SHORT).show();
        }
    }

}
