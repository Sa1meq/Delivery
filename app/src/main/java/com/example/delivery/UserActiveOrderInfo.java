package com.example.delivery;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.delivery.model.CourierLocation;
import com.example.delivery.model.SerializedPoint;
import com.example.delivery.repository.CourierLocationRepository;
import com.example.delivery.repository.RouteOrderRepository;
import com.google.android.material.button.MaterialButton;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.geometry.Polyline;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.PolylineMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.image.ImageProvider;

import java.util.ArrayList;
import java.util.List;

public class UserActiveOrderInfo extends AppCompatActivity {

    private MapView mapView;
    private MaterialButton chatButton;
    private MaterialButton cancelButton;
    private MaterialButton complaintButton;
    private CourierLocationRepository courierLocationRepository;
    private RouteOrderRepository routeOrderRepository;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapKitFactory.initialize(this);
        setContentView(R.layout.activity_user_active_order_info);

        mapView = findViewById(R.id.mapView);
        chatButton = findViewById(R.id.chatButton);
        cancelButton = findViewById(R.id.cancelButton);
        complaintButton = findViewById(R.id.complaintButton);

        courierLocationRepository = new CourierLocationRepository();
        routeOrderRepository = new RouteOrderRepository();
        orderId = getIntent().getStringExtra("orderId");

        if (orderId != null) {
            fetchCourierLocation(orderId);
        } else {
            Toast.makeText(this, "Ошибка: ID заказа не найден", Toast.LENGTH_SHORT).show();
            finish();
        }

        chatButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserActiveOrderInfo.this, ChatActivity.class);
            intent.putExtra("orderId", orderId); // Убедитесь, что orderId не null
            startActivity(intent);
        });

        cancelButton.setOnClickListener(v -> {
            routeOrderRepository.cancelOrder(orderId)
                    .thenAccept(aVoid -> {
                        Toast.makeText(UserActiveOrderInfo.this, "Заказ успешно отменен", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .exceptionally(e -> {
                        Toast.makeText(UserActiveOrderInfo.this, "Ошибка при отмене заказа: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return null;
                    });
        });

        complaintButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserActiveOrderInfo.this, CreateSupportChatActivity.class);
            intent.putExtra("isComplaintFlow", true);
            startActivity(intent);
            finish();
        });

    }

    private void fetchCourierLocation(String orderId) {
        courierLocationRepository.getCourierLocationByOrderId(orderId)
                .thenAccept(courierLocation -> {
                    if (courierLocation != null) {
                        showCourierOnMap(courierLocation.getCourierLocation());
                    } else {
                        Toast.makeText(UserActiveOrderInfo.this, "Данные о курьере не найдены", Toast.LENGTH_SHORT).show();
                    }
                })
                .exceptionally(e -> {
                    Toast.makeText(UserActiveOrderInfo.this, "Ошибка получения данных: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return null;
                });
    }

    private void showCourierOnMap(@NonNull List<SerializedPoint> courierPoints) {
        if (courierPoints == null || courierPoints.isEmpty()) {
            Toast.makeText(this, "Маршрут курьера не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        // Очищаем предыдущие объекты на карте
        mapView.getMap().getMapObjects().clear();

        // Получаем последнюю точку как текущее местоположение курьера
        SerializedPoint lastPoint = courierPoints.get(courierPoints.size() - 1);
        Point courierPosition = new Point(lastPoint.getLatitude(), lastPoint.getLongitude());

        // Перемещаем камеру к текущей позиции курьера
        mapView.getMap().move(new CameraPosition(courierPosition, 15.0f, 0.0f, 0.0f));

        // Добавляем метку с кастомной иконкой курьера
        PlacemarkMapObject courierMarker = mapView.getMap().getMapObjects().addPlacemark(courierPosition);
        courierMarker.setIcon(ImageProvider.fromResource(this, R.drawable.ic_courier_min));


    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }
}