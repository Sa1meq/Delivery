package com.example.delivery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.delivery.model.Card;
import com.example.delivery.repository.CourierRepository;
import com.example.delivery.repository.RouteOrderRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CourierProfile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView avatarImageView;
    private TextView nameTextView;
    private TextView phoneTextView;
    private TextView ratingTextView;
    private TextView earningsTextView;
    private TextView bonusTextView;
    private CourierRepository courierRepository;
    private RouteOrderRepository routeOrderRepository;
    private CloudinaryUploader cloudinaryUploader;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier);

        // Инициализация UI элементов
        avatarImageView = findViewById(R.id.avatarImageView);
        nameTextView = findViewById(R.id.nameTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        ratingTextView = findViewById(R.id.ratingTextView);
        earningsTextView = findViewById(R.id.earningsTextView);
        bonusTextView = findViewById(R.id.bonusTextView);
        CardView ordersCardView = findViewById(R.id.ordersCardView);
        CardView acceptedOrdersCardView = findViewById(R.id.acceptedOrdersCardView);
        CardView historyOrdersCardView = findViewById(R.id.historyOrdersCardView);
        CardView logoutCardView = findViewById(R.id.logoutCardView);
        CardView bonusActivateCardView = findViewById(R.id.bonusActivateCardView);

        courierRepository = new CourierRepository(FirebaseFirestore.getInstance());
        routeOrderRepository = new RouteOrderRepository();
        cloudinaryUploader = new CloudinaryUploader(this);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadCourierData(userId);

        avatarImageView.setOnClickListener(v -> openImagePicker());
        bonusActivateCardView.setOnClickListener(view -> showTariffDialog());
        ordersCardView.setOnClickListener(v -> navigateToOrdersList());
        logoutCardView.setOnClickListener(v -> logoutUser());
        historyOrdersCardView.setOnClickListener(view -> navigateToOrdersHistoryList());
        acceptedOrdersCardView.setVisibility(View.GONE);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                uploadImageToCloudinary(imageUri);
            }
        }
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        cloudinaryUploader.uploadImage(imageUri, userId, new CloudinaryUploader.UploadCallback() {
            @Override
            public void onUploadComplete(String imageUrl) {
                runOnUiThread(() -> {
                    if (imageUrl != null) {
                        courierRepository.updateCourierAvatar(userId, imageUrl).thenAccept(success -> {
                            if (success) {
                                Glide.with(CourierProfile.this)
                                        .load(imageUrl)
                                        .circleCrop()
                                        .into(avatarImageView);
                                Toast.makeText(CourierProfile.this, "Аватар обновлен", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(CourierProfile.this, "Ошибка сохранения", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(CourierProfile.this, "Ошибка загрузки", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void loadCourierData(String userId) {
        courierRepository.checkAndResetTariff(userId).thenCompose(isReset -> {
            return courierRepository.getCourierById(userId);
        }).thenAccept(courier -> {
            runOnUiThread(() -> {
                if (courier != null) {
                    nameTextView.setText(String.format("%s %s", courier.getFirstName(), courier.getSurName()));
                    phoneTextView.setText(String.format("Телефон: %s", courier.getPhone()));
                    ratingTextView.setText(String.format("Рейтинг: %.1f", courier.getRating()));
                    earningsTextView.setText(String.format("Заработок: %.2f BYN", Double.parseDouble(courier.getBalance())));

                    String tariffStatus = courier.getTariffEndTime() > System.currentTimeMillis()
                            ? "Тариф активен до: " + new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(new Date(courier.getTariffEndTime()))
                            : "Повышенный тариф не активен";

                    bonusTextView.setText(String.format(
                            "Бонусы: %d WCoins\n%s",
                            courier.getBonusPoints(),
                            tariffStatus
                    ));

                    // Загрузка аватарки
                    if (courier.getAvatarUrl() != null && !courier.getAvatarUrl().isEmpty()) {
                        Glide.with(CourierProfile.this)
                                .load(courier.getAvatarUrl())
                                .circleCrop()
                                .placeholder(R.drawable.ic_avatar)
                                .into(avatarImageView);
                    } else {
                        avatarImageView.setImageResource(R.drawable.ic_avatar);
                    }
                } else {
                    Toast.makeText(this, "Данные курьера не найдены", Toast.LENGTH_SHORT).show();
                }
            });
        }).exceptionally(e -> {
            runOnUiThread(() ->
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
            return null;
        });
    }

    private void showTariffDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Повышение тарифа")
                .setMessage("Активация повышенного тарифа (+30%) на 24 часа. Стоимость: 600 WCoins")
                .setPositiveButton("Активировать", (dialog, which) -> checkAndActivateTariff())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void checkAndActivateTariff() {
        courierRepository.getCourierById(userId).thenAccept(courier -> {
            if (courier != null && courier.getBonusPoints() >= 600 && courier.getTariffMultiplier() != 1.3) {
                long endTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1);

                courierRepository.updateCourierTariff(userId, 1.3, endTime, 600)
                        .thenAccept(success -> {
                            runOnUiThread(() -> {
                                if (success) {
                                    loadCourierData(userId);
                                    Toast.makeText(this, "Тариф активирован!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Ошибка активации", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
            } else {
                runOnUiThread(() ->
                        Toast.makeText(this, "Недостаточно бонусных баллов || Тариф уже активирован", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }



    private void navigateToOrdersList() {
        startActivity(new Intent(this, CourierOrdersList.class));
        finish();
    }

    private void navigateToOrdersHistoryList(){
        startActivity(new Intent(this, CourierOrdersHistory.class));
        finish();
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, Registration.class));
        finish();
    }
}