package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.delivery.OrderAdapter;
import com.example.delivery.model.RouteOrder;
import com.example.delivery.repository.RouteOrderRepository;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class UserOrdersHistory extends AppCompatActivity {
    private RecyclerView ordersRecyclerView;
    private OrderAdapter orderAdapter;
    private List<RouteOrder> orderList = new ArrayList<>();
    private RouteOrderRepository orderRepository = new RouteOrderRepository();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_orders_history);

        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        orderAdapter = new OrderAdapter(orderList, this);
        ordersRecyclerView.setAdapter(orderAdapter);

        ImageView backImageView = findViewById(R.id.backImageView);
        backImageView.setOnClickListener(v -> {
            Intent intent = new Intent(UserOrdersHistory.this, UserProfile.class);
            startActivity(intent);
            finish();
        });

        loadUserOrders();
    }

    private void loadUserOrders() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        orderRepository.getOrdersByUserId(userId).thenAccept(orders -> {
            orderList.clear();
            orderList.addAll(orders);
            orderAdapter.notifyDataSetChanged();
        }).exceptionally(e -> {
            return null;
        });
    }
}
