package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.model.Courier;
import com.example.delivery.repository.CourierRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdminPanel extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CourierRepository courierRepository;
    private CourierAdapter courierAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        recyclerView = findViewById(R.id.recyclerViewVerificationRequests);
        courierRepository = new CourierRepository(FirebaseFirestore.getInstance());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadPendingCouriers();
        ImageView backImageView = findViewById(R.id.backImageView);
        backImageView.setOnClickListener(v -> {
            Intent intent = new Intent(AdminPanel.this, UserProfile.class);
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
            Toast.makeText(AdminPanel.this, "Ошибка загрузки курьеров", Toast.LENGTH_SHORT).show();
            return null;
        });

    }

    private void verifyCourier(Courier courier) {
        courierRepository.updateCourierVerification(courier.getId(), true).thenAccept(success -> {
            if (success) {
                Toast.makeText(AdminPanel.this, "Курьер верифицирован", Toast.LENGTH_SHORT).show();
                loadPendingCouriers();
            } else {
                Toast.makeText(AdminPanel.this, "Ошибка верификации", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rejectCourier(Courier courier) {
        courierRepository.updateCourierVerification(courier.getId(), false).thenAccept(success -> {
            if (success) {
                Toast.makeText(AdminPanel.this, "Курьер отклонен", Toast.LENGTH_SHORT).show();
                loadPendingCouriers();
            } else {
                Toast.makeText(AdminPanel.this, "Ошибка отклонения", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
