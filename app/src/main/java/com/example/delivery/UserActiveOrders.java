package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.delivery.model.RouteOrder;
import com.example.delivery.repository.RouteOrderRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UserActiveOrders extends AppCompatActivity {
    private RecyclerView ordersRecyclerView;
    private OrderAdapter orderAdapter;
    private List<RouteOrder> activeOrderList = new ArrayList<>();
    private RouteOrderRepository orderRepository = new RouteOrderRepository();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_active_orders);

        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        orderAdapter = new OrderAdapter(activeOrderList);
        ordersRecyclerView.setAdapter(orderAdapter);

        loadActiveOrders();

        ImageView backImageView = findViewById(R.id.backImageView);
        backImageView.setOnClickListener(v -> {
            Intent intent = new Intent(UserActiveOrders.this, UserProfile.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadActiveOrders() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        orderRepository.getActiveOrdersByUserId(userId).thenAccept(orders -> {
            activeOrderList.clear();
            activeOrderList.addAll(orders);
            orderAdapter.notifyDataSetChanged();
        }).exceptionally(e -> {
            // Обработать ошибку
            return null;
        });
    }
}
