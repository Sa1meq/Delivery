package com.example.delivery;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.delivery.model.RouteOrder;
import com.example.delivery.repository.RouteOrderRepository;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;
import com.example.delivery.adapter.OrdersAdapter;

public class CourierOrdersList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrdersAdapter ordersAdapter;
    private RouteOrderRepository routeOrderRepository;
    private String courierId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier_orders_list);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        routeOrderRepository = new RouteOrderRepository();
        courierId = FirebaseAuth.getInstance().getUid();

        loadCourierOrders();
    }

    private void loadCourierOrders() {
        routeOrderRepository.getAllPendingRouteOrdersForCourier(courierId)
                .thenAccept(this::updateOrdersList)
                .exceptionally(e -> {
                    // Обработка ошибки
                    e.printStackTrace();
                    return null;
                });
    }

    private void updateOrdersList(List<RouteOrder> routeOrders) {
        ordersAdapter = new OrdersAdapter(routeOrders);
        recyclerView.setAdapter(ordersAdapter);
    }
}
