package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.model.Courier;
import com.example.delivery.repository.CourierRepository;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class VerificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CourierRepository courierRepository;
    private CourierAdapter courierAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        recyclerView = findViewById(R.id.recyclerViewVerificationRequests);
        courierRepository = new CourierRepository(FirebaseFirestore.getInstance());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadPendingCouriers();
        ImageView backImageView = findViewById(R.id.backImageView);
        backImageView.setOnClickListener(v -> {
            Intent intent = new Intent(VerificationActivity.this, AdminPanel.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadPendingCouriers() {
        courierRepository.getPendingCouriers().thenAccept(couriers -> {
            Log.d("AdminPanel", "Loaded couriers: " + couriers.size());
            courierAdapter = new CourierAdapter(couriers, this::verifyCourier, this::rejectCourier);
            recyclerView.setAdapter(courierAdapter);
        }).exceptionally(e -> {
            Log.e("AdminPanel", "Ошибка загрузки курьеров", e);
            Toast.makeText(VerificationActivity.this, "Ошибка загрузки курьеров", Toast.LENGTH_SHORT).show();
            return null;
        });

    }

    private void verifyCourier(Courier courier) {
        courierRepository.updateCourierVerification(courier.getId(), true).thenAccept(success -> {
            if (success) {
                Toast.makeText(VerificationActivity.this, "Курьер верифицирован", Toast.LENGTH_SHORT).show();
                loadPendingCouriers();

                // Добавление уведомления о верификации
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference notificationsRef = database.getReference("notifications").child(courier.getId());

                Map<String, Object> notificationData = new HashMap<>();
                notificationData.put("title", "Аккаунт верифицирован!");
                notificationData.put("body", "Ваш аккаунт курьера подтверждён.");
                notificationData.put("timestamp", System.currentTimeMillis() / 1000);

                notificationsRef.setValue(notificationData)
                        .addOnSuccessListener(aVoid -> Log.d("Notification", "Уведомление добавлено"))
                        .addOnFailureListener(error -> Log.e("Notification", "Ошибка: " + error.getMessage()));
            } else {
                Toast.makeText(VerificationActivity.this, "Ошибка верификации", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void rejectCourier(Courier courier) {
        courierRepository.updateCourierVerification(courier.getId(), false).thenAccept(success -> {
            if (success) {
                Toast.makeText(VerificationActivity.this, "Курьер отклонен", Toast.LENGTH_SHORT).show();
                loadPendingCouriers();
            } else {
                Toast.makeText(VerificationActivity.this, "Ошибка отклонения", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
