package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.delivery.repository.CourierRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CourierProfile extends AppCompatActivity {

    private ImageView avatarImageView;
    private TextView nameTextView;
    private TextView phoneTextView;
    private TextView ratingTextView;
    private TextView earningsTextView;
    private Button ordersButton;
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
        earningsTextView = findViewById(R.id.earningsTextView);
        ordersButton = findViewById(R.id.ordersButton);
        logoutButton = findViewById(R.id.logoutButton);

        courierRepository = new CourierRepository(FirebaseFirestore.getInstance());
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        loadCourierData(userId);

        ordersButton.setOnClickListener(v -> {
            Intent intent = new Intent(CourierProfile.this, CourierOrdersList.class);
            startActivity(intent);
            finish();
        });

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(CourierProfile.this, UserProfile.class));
            finish();
        });
    }
    private void loadCourierData(String userId) {
        courierRepository.getCourierById(userId).thenAccept(courier -> {
            if (courier != null) {
                nameTextView.setText(courier.getFirstName() + " " + courier.getSurName());
                phoneTextView.setText("Телефон: " + courier.getPhone());
                ratingTextView.setText("Рейтинг: " + courier.getRating());
                earningsTextView.setText("Заработок: " + courier.getBalance() + " BYN");
            } else {
                Toast.makeText(CourierProfile.this, "Не удалось загрузить данные курьера", Toast.LENGTH_SHORT).show();
            }
        }).exceptionally(e -> {
            Toast.makeText(CourierProfile.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        });
    }
}
