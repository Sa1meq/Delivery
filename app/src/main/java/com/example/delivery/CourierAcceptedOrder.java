package com.example.delivery;

import static com.example.delivery.MainActivity.REQUEST_LOCATION_PERMISSION;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.delivery.model.CourierLocation;
import com.example.delivery.model.SerializedPoint;
import com.example.delivery.repository.RouteOrderRepository;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.directions.driving.DrivingOptions;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingRouterType;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.directions.driving.VehicleOptions;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.location.FilteringMode;
import com.yandex.mapkit.location.LocationListener;
import com.yandex.mapkit.location.LocationStatus;
import com.yandex.mapkit.location.Purpose;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CourierAcceptedOrder extends AppCompatActivity implements LocationListener {
    private MapView mapView;
    private List<Point> routePoints = new ArrayList<>();
    private RouteOrderRepository routeOrderRepository = new RouteOrderRepository();
    private boolean isRequestInProgress = false;
    private Point userLocation;
    private DrivingRouter drivingRouter;
    private com.yandex.mapkit.location.LocationManager locationManager;
    private boolean isFirstLocationUpdate = true;
    private PlacemarkMapObject userPlacemark;
    private MapObjectCollection pinCollection;
    private final Handler handler = new Handler();
    private boolean isSecondRoute = false;
    private MaterialButton checkReady;
    private MaterialButton chatButton;
    private MaterialButton cancelButton;
    private MaterialButton complaintButton;;
    private DrivingSession drivingSession;
    private DrivingRoute currentRoute;
    private int currentSegmentIndex = 0;
    private boolean isLastSegment = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapKitFactory.initialize(this);
        setContentView(R.layout.activity_courier_accepted_order);
        locationManager = MapKitFactory.getInstance().createLocationManager();
        mapView = findViewById(R.id.mapView);
        checkReady = findViewById(R.id.checkReady);
        pinCollection = mapView.getMap().getMapObjects();
        String orderId = getIntent().getStringExtra("orderId");
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.COMBINED);
        getRouteOrderFromDatabase(orderId);
        chatButton = findViewById(R.id.chatButton);
        cancelButton = findViewById(R.id.cancelButton);
        complaintButton = findViewById(R.id.complaintButton);


        routeOrderRepository = new RouteOrderRepository();

        if (orderId == null) {
            Toast.makeText(this, "Ошибка: ID заказа не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Обработка нажатия на кнопку чата
        chatButton.setOnClickListener(v -> {
            Intent intent = new Intent(CourierAcceptedOrder.this, ChatActivity.class);
            intent.putExtra("orderId", orderId);
            startActivity(intent);
        });

        cancelButton.setOnClickListener(v -> {
            routeOrderRepository.cancelOrder(orderId)
                    .thenAccept(aVoid -> {
                        Toast.makeText(CourierAcceptedOrder.this, "Заказ успешно отменен", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .exceptionally(e -> {
                        Toast.makeText(CourierAcceptedOrder.this, "Ошибка при отмене заказа: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return null;
                    });
        });
        Button yandexNavButton = findViewById(R.id.yandex_nav_button);
        yandexNavButton.setOnClickListener(v -> {
            try {
                if (!isLastSegment){
                    Point destination = routePoints.get(0);
                    String uri = String.format(Locale.ENGLISH, "yandexnavi://build_route_on_map?lat_to=%f&lon_to=%f",
                            destination.getLatitude(),
                            destination.getLongitude());
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(intent);
                }
                else {
                    Point destination = routePoints.get(1);
                    String uri = String.format(Locale.ENGLISH, "yandexnavi://build_route_on_map?lat_to=%f&lon_to=%f",
                            destination.getLatitude(),
                            destination.getLongitude());
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(intent);
                }
            } catch (Exception e) {
                Toast.makeText(this, "Яндекс.Навигатор не установлен", Toast.LENGTH_SHORT).show();
            }
        });
        complaintButton.setOnClickListener(v -> {
            Intent intent = new Intent(CourierAcceptedOrder.this, CreateSupportChatActivity.class);
            intent.putExtra("isComplaintFlow", true);
            startActivity(intent);
            finish();
        });
        checkReady.setOnClickListener(v -> {
            if (currentRoute == null || userLocation == null) return;

            float progress = calculateRouteProgress(currentRoute, userLocation);

            if (progress >= 0.8f) {
                if (isLastSegment) {
                    completeOrder(orderId);
                    Toast.makeText(this, "Заказ выполнен!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    moveToNextSegment();
                }
            } else {
                Toast.makeText(this, "Прогресс: " + (int)(progress * 100) + "%", Toast.LENGTH_SHORT).show();
            }
        });

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentRoute != null && userLocation != null) {
                    float progress = calculateRouteProgress(currentRoute, userLocation);
                    updateProgressUI(progress);

                }
                handler.postDelayed(this, 5000); // Обновление каждые 5 секунд
            }
        }, 5000);
    }


    private void getRouteOrderFromDatabase(String orderId) {
        routeOrderRepository.getRouteOrderById(orderId)
                .thenAccept(routeOrder -> {
                    if (routeOrder != null && routeOrder.routePoints.size() >= 2) {
                        routePoints.clear();
                        routePoints.addAll(routeOrder.routePoints);

                        if (userLocation != null) {
                            requestRoute(userLocation, routePoints.get(0));
                        }
                    }
                });
    }

    private void buildRouteFromUserLocationToFirstPoint() {
        if (userLocation == null || routePoints.size() < 1) {
            Log.e("RouteError", "Недостаточно точек маршрута или ошибка в получении данных.");
            Toast.makeText(CourierAcceptedOrder.this, "Не удалось получить местоположение пользователя или нет точек маршрута", Toast.LENGTH_SHORT).show();
            return;
        }

        requestRoute(userLocation, routePoints.get(0));
        currentSegmentIndex = 0;
        isLastSegment = false;
    }



    private void requestRoute(Point start, Point end) {
        DrivingOptions drivingOptions = new DrivingOptions();
        VehicleOptions vehicleOptions = new VehicleOptions();

        List<RequestPoint> requestPoints = Arrays.asList(
                new RequestPoint(start, RequestPointType.WAYPOINT, null, null),
                new RequestPoint(end, RequestPointType.WAYPOINT, null, null)
        );

        drivingSession = drivingRouter.requestRoutes(
                requestPoints,
                drivingOptions,
                vehicleOptions,
                new DrivingSession.DrivingRouteListener() {
                    @Override
                    public void onDrivingRoutes(@NonNull List<DrivingRoute> routes) {
                        if (!routes.isEmpty()) {
                            currentRoute = routes.get(0);
                            mapView.getMap().getMapObjects().addPolyline(currentRoute.getGeometry());
                            mapView.getMap().move(new CameraPosition(
                                    start, 12f, 0f, 0f
                            ));
                        }
                    }

                    @Override
                    public void onDrivingRoutesError(@NonNull Error error) {
                        Toast.makeText(CourierAcceptedOrder.this,
                                "Ошибка построения маршрута: " + error, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void moveToNextSegment() {
        if (currentSegmentIndex >= routePoints.size() - 2) {
            isLastSegment = true;
            checkReady.setText("Завершить заказ");
            clearRoute();
            buildNextRouteSegment();
            Toast.makeText(this, "Это последний сегмент маршрута", Toast.LENGTH_SHORT).show();
        } else {
            currentSegmentIndex++;
            buildNextRouteSegment();
        }
    }

    private void buildNextRouteSegment() {
        Point start = routePoints.get(currentSegmentIndex);
        Point end = routePoints.get(currentSegmentIndex + 1);
        requestRoute(start, end);
        isLastSegment = true;
    }

    private void completeOrder(String orderId) {
        routeOrderRepository.completeOrder(orderId)
                .thenRun(() -> {
                    Toast.makeText(this, "Заказ завершен", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, CourierProfile.class);
                    startActivity(intent);
                    finish();
                })
                .exceptionally(e -> {
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return null;
                });
    }

    private double calculateDistance(Point point1, Point point2) {
        double lat1 = Math.toRadians(point1.getLatitude());
        double lon1 = Math.toRadians(point1.getLongitude());
        double lat2 = Math.toRadians(point2.getLatitude());
        double lon2 = Math.toRadians(point2.getLongitude());

        double R = 6371000;

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    private float calculateRouteProgress(DrivingRoute route, Point userLocation) {
        if (route == null || userLocation == null) return 0.0f;

        List<Point> routePoints = route.getGeometry().getPoints();
        double totalDistance = 0.0;
        double coveredDistance = 0.0;
        Point nearestPoint = routePoints.get(0);
        double minDistance = Double.MAX_VALUE;

        // Рассчитываем общее расстояние маршрута
        for (int i = 0; i < routePoints.size() - 1; i++) {
            totalDistance += calculateDistance(routePoints.get(i), routePoints.get(i + 1));
        }

        // Находим ближайшую точку на маршруте
        for (int i = 0; i < routePoints.size() - 1; i++) {
            Point a = routePoints.get(i);
            Point b = routePoints.get(i + 1);
            Point projection = projectPointOnSegment(a, b, userLocation);
            double dist = calculateDistance(userLocation, projection);

            if (dist < minDistance) {
                minDistance = dist;
                nearestPoint = projection;
            }
        }

        // Рассчитываем пройденное расстояние до ближайшей точки
        for (int i = 0; i < routePoints.size() - 1; i++) {
            Point a = routePoints.get(i);
            Point b = routePoints.get(i + 1);

            if (b.equals(nearestPoint)) {
                coveredDistance += calculateDistance(a, nearestPoint);
                break;
            } else {
                coveredDistance += calculateDistance(a, b);
            }
        }

        return totalDistance > 0 ? (float) (coveredDistance / totalDistance) : 0;
    }

    private Point projectPointOnSegment(Point a, Point b, Point p) {
        double ax = a.getLongitude();
        double ay = a.getLatitude();
        double bx = b.getLongitude();
        double by = b.getLatitude();
        double px = p.getLongitude();
        double py = p.getLatitude();

        double vectorABx = bx - ax;
        double vectorABy = by - ay;
        double vectorAPx = px - ax;
        double vectorAPy = py - ay;

        double dot = vectorAPx * vectorABx + vectorAPy * vectorABy;
        double lengthSq = vectorABx * vectorABx + vectorABy * vectorABy;

        if (lengthSq == 0) return a;

        double t = Math.max(0, Math.min(1, dot / lengthSq));

        return new Point(
                ay + t * vectorABy,
                ax + t * vectorABx
        );
    }


    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            double distance = 0;
            long time = 5000;
            boolean needAddress = false;
            FilteringMode filteringMode = FilteringMode.ON;
            Purpose purpose = Purpose.GENERAL;

            locationManager.subscribeForLocationUpdates(
                    distance, time, 0, needAddress, filteringMode, purpose, this
            );
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    private void requestUserLocation() {
        if (locationManager == null) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestSingleUpdate(this);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    private void saveCourierLocation(Point location) {
        if (location == null) return;

        String courierId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String orderId = getIntent().getStringExtra("orderId");

        List<SerializedPoint> serializedPoints = new ArrayList<>();
        serializedPoints.add(SerializedPoint.fromMapKitPoint(location));

        CourierLocation courierLocation = new CourierLocation(
                UUID.randomUUID().toString(),
                orderId,
                courierId,
                serializedPoints
        );

        FirebaseFirestore.getInstance().collection("courierLocations")
                .document(courierLocation.getId())
                .set(courierLocation)
                .addOnSuccessListener(aVoid -> Log.d("CourierLocation", "Местоположение сохранено"))
                .addOnFailureListener(e -> Log.e("CourierLocation", "Ошибка сохранения местоположения", e));
    }

    private void updateProgressUI(float progress) {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        int percent = (int) (progress * 100);
        progressBar.setProgress(percent);

        TextView progressText = findViewById(R.id.progressText);
        progressText.setText("Прогресс: " + percent + "%");

        if (percent >= 80) {
            checkReady.setBackgroundColor(ContextCompat.getColor(this, R.color.my_primary));
        } else {
            checkReady.setBackgroundColor(ContextCompat.getColor(this, R.color.my_primary));
        }
    }

    @Override
    public void onLocationUpdated(@NonNull com.yandex.mapkit.location.Location location) {
        userLocation = new Point(location.getPosition().getLatitude(), location.getPosition().getLongitude());

        if (isFirstLocationUpdate) {
            mapView.getMap().move(new CameraPosition(userLocation, 15.0f, 0.0f, 0.0f));
            isFirstLocationUpdate = false;

            if (userPlacemark == null) {
                userPlacemark = pinCollection.addPlacemark(userLocation);
                userPlacemark.setIcon(ImageProvider.fromResource(this, R.drawable.ic_user_location));
            }
        } else {
            if (userPlacemark != null && userPlacemark.isValid()) {
                userPlacemark.setGeometry(userLocation);
            } else {
                userPlacemark = pinCollection.addPlacemark(userLocation);
                userPlacemark.setIcon(ImageProvider.fromResource(this, R.drawable.ic_user_location));
            }
        }

        saveCourierLocation(userLocation);
        if (!isLastSegment){
            buildRouteFromUserLocationToFirstPoint();
        }
    }




    @Override
    public void onLocationStatusUpdated(@NonNull LocationStatus locationStatus) {
        if (locationStatus == LocationStatus.NOT_AVAILABLE) {
            Toast.makeText(this, "Локация недоступна", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestUserLocation();
            } else {
                Toast.makeText(this, "Разрешение на локацию не предоставлено", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void clearRoute() {
        if (mapView != null && mapView.getMap() != null) {
            mapView.getMap().getMapObjects().clear();
            userPlacemark.isValid();
        }
    }

    private void showNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "delivery_channel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Уведомление")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(1, builder.build());
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentSegmentIndex", currentSegmentIndex);
        outState.putBoolean("isLastSegment", isLastSegment);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentSegmentIndex = savedInstanceState.getInt("currentSegmentIndex");
        isLastSegment = savedInstanceState.getBoolean("isLastSegment");
        if (isLastSegment) {
            checkReady.setText("Завершить заказ");
        }
    }

    private void finishRoute() {
        if (userPlacemark != null && userPlacemark.isValid()) {
            userPlacemark = null;
        }

        clearRoute();
    }


    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
        startLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        MapKitFactory.getInstance().onStop();
        mapView.onStop();
        locationManager.unsubscribe(this);
    }
}