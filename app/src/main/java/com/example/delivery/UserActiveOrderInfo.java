package com.example.delivery;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.delivery.R;
import com.example.delivery.model.CourierLocation;
import com.example.delivery.model.SerializedPoint;
import com.example.delivery.repository.CourierLocationRepository;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.mapview.MapView;

import java.util.List;

public class UserActiveOrderInfo extends AppCompatActivity {

    private MapView mapView;
    private TextView orderIdTextView;
    private TextView courierIdTextView;
    private CourierLocationRepository courierLocationRepository;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapKitFactory.initialize(this);
        setContentView(R.layout.activity_user_active_order_info);

        mapView = findViewById(R.id.mapView);
        orderIdTextView = findViewById(R.id.orderIdTextView);
        courierIdTextView = findViewById(R.id.courierIdTextView);

        courierLocationRepository = new CourierLocationRepository();
        orderId = getIntent().getStringExtra("orderId");

        if (orderId != null) {
            orderIdTextView.setText("Заказ №: " + orderId);
            fetchCourierLocation(orderId);
        } else {
            Toast.makeText(this, "Ошибка: ID заказа не найден", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchCourierLocation(String orderId) {
        courierLocationRepository.getCourierLocationByOrderId(orderId)
                .thenAccept(courierLocation -> {
                    if (courierLocation != null) {
                        updateUIWithCourierData(courierLocation);
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

    private void updateUIWithCourierData(CourierLocation courierLocation) {
        courierIdTextView.setText("Курьер ID: " + courierLocation.getCourierId());
    }

    private void showCourierOnMap(@NonNull List<SerializedPoint> courierPoints) {
        if (courierPoints == null || courierPoints.isEmpty()) {
            Toast.makeText(this, "Маршрут курьера не найден", Toast.LENGTH_SHORT).show();
            return;
        }
        SerializedPoint firstPoint = courierPoints.get(0);
        Point startPoint = new Point(firstPoint.getLatitude(), firstPoint.getLongitude());

        mapView.getMap().move(new CameraPosition(startPoint, 15.0f, 0.0f, 0.0f));
        for (SerializedPoint serializedPoint : courierPoints) {
            Point point = new Point(serializedPoint.getLatitude(), serializedPoint.getLongitude());
            mapView.getMap().getMapObjects().addPlacemark(point);
        }
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
