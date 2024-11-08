package com.example.delivery;

import static com.example.delivery.MainActivity.REQUEST_LOCATION_PERMISSION;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.delivery.repository.RouteOrderRepository;
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
import java.util.List;

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
    private Button checkReady;
    private DrivingSession drivingSession;

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

        checkReady.setOnClickListener(v -> {
            if (drivingSession != null) {
                drivingSession.cancel();
            }

            clearRoute();
            buildRouteFromFirstToSecondPoint();
            startLocationUpdates();
        });
    }

    private void getRouteOrderFromDatabase(String orderId) {
        routeOrderRepository.getRouteOrderById(orderId)
                .thenAccept(routeOrder -> {
                    if (routeOrder != null && routeOrder.routePoints != null && routeOrder.routePoints.size() >= 2) {
                        routePoints.clear();
                        routePoints.addAll(routeOrder.routePoints);
                        Log.d("RoutePoints", "Точки маршрута получены: " + routePoints);

                        buildRouteFromUserLocationToFirstPoint();
                    } else {
                        Toast.makeText(CourierAcceptedOrder.this, "Недостаточно точек маршрута", Toast.LENGTH_SHORT).show();
                    }
                })
                .exceptionally(e -> {
                    Toast.makeText(CourierAcceptedOrder.this, "Ошибка загрузки маршрута: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return null;
                });
    }

    private void buildRouteFromUserLocationToFirstPoint() {
        if (userLocation == null || routePoints.size() < 1) {
            Log.e("RouteError", "Недостаточно точек маршрута или ошибка в получении данных.");
            Toast.makeText(CourierAcceptedOrder.this, "Не удалось получить местоположение пользователя или нет точек маршрута", Toast.LENGTH_SHORT).show();
            return;
        }

        requestRouteFromUserLocationToPoint(userLocation, routePoints.get(0), true);
    }

    private void buildRouteFromFirstToSecondPoint() {
        if (routePoints.size() < 2) {
            Toast.makeText(CourierAcceptedOrder.this, "Недостаточно точек для второго маршрута", Toast.LENGTH_SHORT).show();
            return;
        }

        requestRouteFromUserLocationToPoint(routePoints.get(0), routePoints.get(1), false);
    }

    private void requestRouteFromUserLocationToPoint(Point startPoint, Point endPoint, boolean isStart) {
        if (isRequestInProgress) {
            Log.d("RouteRequest", "Запрос маршрута уже выполняется.");
            return;
        }

        isRequestInProgress = true;

        DrivingOptions drivingOptions = new DrivingOptions();
        VehicleOptions vehicleOptions = new VehicleOptions();

        List<RequestPoint> requestPoints = new ArrayList<>();
        requestPoints.add(new RequestPoint(startPoint, RequestPointType.WAYPOINT, "", ""));
        requestPoints.add(new RequestPoint(endPoint, RequestPointType.WAYPOINT, "", ""));

        drivingSession = drivingRouter.requestRoutes(
                requestPoints,
                drivingOptions,
                vehicleOptions,
                new DrivingSession.DrivingRouteListener() {
                    @Override
                    public void onDrivingRoutes(@NonNull List<DrivingRoute> routes) {
                        if (!routes.isEmpty()) {
                            mapView.getMap().getMapObjects().addPolyline(routes.get(0).getGeometry());
                            DrivingRoute route = routes.get(0);

                            float progress = calculateRouteProgress(route, userLocation);
                            Log.d("RouteProgress", "Прогресс по маршруту: " + progress * 100 + "%");
                            if (userLocation != null) {
                                Log.d("Route", "Маршрут от точки " + routePoints.get(0) + " к " + routePoints.get(1));
                            }
                        } else {
                            Toast.makeText(CourierAcceptedOrder.this, "Маршрут не найден", Toast.LENGTH_SHORT).show();
                        }
                        isRequestInProgress = false;
                    }

                    @Override
                    public void onDrivingRoutesError(@NonNull Error error) {
                        Toast.makeText(CourierAcceptedOrder.this, "Ошибка построения маршрута", Toast.LENGTH_SHORT).show();
                        isRequestInProgress = false;
                    }
                }
        );
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
        List<Point> routePoints = route.getGeometry().getPoints();
        double totalDistance = 0.0;
        double distanceCovered = 0.0;

        for (int i = 1; i < routePoints.size(); i++) {
            totalDistance += calculateDistance(routePoints.get(i - 1), routePoints.get(i));
        }

        if (userLocation != null) {
            for (int i = 1; i < routePoints.size(); i++) {
                Point segmentStart = routePoints.get(i - 1);
                Point segmentEnd = routePoints.get(i);
                double distanceToSegmentStart = calculateDistance(segmentStart, userLocation);
                double distanceToSegmentEnd = calculateDistance(segmentEnd, userLocation);

                if (distanceToSegmentStart < distanceToSegmentEnd) {
                    distanceCovered += distanceToSegmentStart;
                    break;
                } else {
                    distanceCovered += calculateDistance(segmentStart, segmentEnd);
                }
            }
        }

        return (float) (distanceCovered / totalDistance);
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
            if (userPlacemark != null) {
                userPlacemark.setGeometry(userLocation);
            }
        }

        buildRouteFromUserLocationToFirstPoint();
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