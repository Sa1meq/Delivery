package com.example.delivery;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.delivery.model.Courier;
import com.example.delivery.repository.CourierRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CourierActivity extends AppCompatActivity {

    private ImageView avatarImageView;
    private TextView nameTextView;
    private TextView phoneTextView;
    private TextView ratingTextView;
    private Button ordersButton;
    private Button mapsButton;
    private Button historyButton;
    private Button logoutButton;

    private CourierRepository courierRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier);

        avatarImageView = findViewById(R.id.avatarImageView);
        nameTextView = findViewById(R.id.nameTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        ratingTextView = findViewById(R.id.ratingTextView);
        ordersButton = findViewById(R.id.ordersButton);
        mapsButton = findViewById(R.id.mapsButton);
        historyButton = findViewById(R.id.historyButton);
        logoutButton = findViewById(R.id.logoutButton);

        courierRepository = new CourierRepository(FirebaseFirestore.getInstance());

        // Получаем текущего пользователя
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        // Загружаем данные курьера из Firestore
        loadCourierData(userId);

        // Обработчики кнопок
//        ordersButton.setOnClickListener(v -> {
//            // Переход к заказам
//            startActivity(new Intent(CourierActivity.this, OrdersActivity.class));
//        });
//
//        mapsButton.setOnClickListener(v -> {
//            // Переход к картам
//            startActivity(new Intent(CourierActivity.this, MapsActivity.class));
//        });
//
//        historyButton.setOnClickListener(v -> {
//            // Переход к истории заказов
//            startActivity(new Intent(CourierActivity.this, OrderHistoryActivity.class));
//        });
//
//        logoutButton.setOnClickListener(v -> {
//            // Логика выхода
//            FirebaseAuth.getInstance().signOut();
//            startActivity(new Intent(CourierActivity.this, LoginActivity.class));
//            finish();
//        });
    }

    private void loadCourierData(String userId) {
        courierRepository.getCourierById(userId).thenAccept(courier -> {
            if (courier != null) {
                nameTextView.setText(courier.getFirstName() + " " + courier.getSurName());
                phoneTextView.setText(courier.getPhone());
                ratingTextView.setText("Рейтинг: " + courier.getRating());
            } else {
                Toast.makeText(CourierActivity.this, "Не удалось загрузить данные курьера", Toast.LENGTH_SHORT).show();
            }
        }).exceptionally(e -> {
            Toast.makeText(CourierActivity.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        });
    }
}