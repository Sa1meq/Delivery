package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

import com.example.delivery.model.RouteOrder;
import com.example.delivery.repository.CourierRepository;
import com.example.delivery.repository.RouteOrderRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.example.delivery.adapter.OrdersAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class CourierOrdersList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrdersAdapter ordersAdapter;
    private RouteOrderRepository routeOrderRepository;
    private String courierId;
    private ImageView backImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier_orders_list);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        routeOrderRepository = new RouteOrderRepository();
        courierId = FirebaseAuth.getInstance().getUid();
        backImageView = findViewById(R.id.backImageView);
        loadCourierOrders();
        backImageView.setOnClickListener(v -> {
            Intent intent = new Intent(CourierOrdersList.this, CourierProfile.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadCourierOrders() {
        routeOrderRepository.getAllPendingRouteOrdersForCourier(courierId)
                .thenAccept(this::filterAndUpdateOrdersList)
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    private void filterAndUpdateOrdersList(List<RouteOrder> routeOrders) {
        String courierUid = FirebaseAuth.getInstance().getUid();
        CourierRepository courierRepository = new CourierRepository(FirebaseFirestore.getInstance());


        courierRepository.getCourierTypeByUid(courierUid)
                .thenAccept(courierType -> {
                    if (courierType == null) {
                        runOnUiThread(() -> Toast.makeText(this, "Тип курьера не найден", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    List<RouteOrder> filteredOrders = new ArrayList<>();
                    for (RouteOrder routeOrder : routeOrders) {
                        if (routeOrder.getCourierType().equals(courierType)) {
                            filteredOrders.add(routeOrder);
                        }
                    }
                    updateOrdersList(filteredOrders);
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> Toast.makeText(this, "Ошибка при получении типа курьера", Toast.LENGTH_SHORT).show());
                    return null;
                });
    }




    private void updateOrdersList(List<RouteOrder> routeOrders) {
        ordersAdapter = new OrdersAdapter(routeOrders, this::showAcceptOrderDialog, this);
        recyclerView.setAdapter(ordersAdapter);
    }

    private void showAcceptOrderDialog(RouteOrder routeOrder) {
        new AlertDialog.Builder(this)
                .setTitle("Принять заказ")
                .setMessage("Вы хотите принять этот заказ?")
                .setPositiveButton("Да", (dialog, which) -> {
                    acceptOrder(routeOrder);
                })
                .setNegativeButton("Нет", null)
                .show();
    }

    private void acceptOrder(RouteOrder routeOrder) {
        routeOrderRepository.updateCourierForOrder(routeOrder.orderId, courierId)
                .thenAccept(aVoid -> runOnUiThread(() -> {
                    Toast.makeText(this, "Заказ принят", Toast.LENGTH_SHORT).show();
                    loadCourierOrders();
                    openAcceptedOrderScreen(routeOrder.orderId);
                }))
                .exceptionally(e -> {
                    runOnUiThread(() -> Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    return null;
                });
    }
    private void openAcceptedOrderScreen(String orderId) {
        Intent intent = new Intent(this, CourierAcceptedOrder.class);
        intent.putExtra("orderId", orderId);
        startActivity(intent);
    }
}
