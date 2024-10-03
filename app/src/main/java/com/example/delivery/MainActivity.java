package com.example.delivery;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.yandex.mapkit.GeoObject;
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
import com.yandex.mapkit.geometry.Geometry;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.search.SearchFactory;
import com.yandex.mapkit.search.SearchManager;
import com.yandex.mapkit.search.SearchManagerType;
import com.yandex.mapkit.search.SearchOptions;
import com.yandex.mapkit.search.Response;
import com.yandex.mapkit.search.SearchType;
import com.yandex.mapkit.search.Session;
import com.yandex.mapkit.search.SuggestItem;
import com.yandex.mapkit.search.SuggestResponse;
import com.yandex.mapkit.search.SuggestSession;
import com.yandex.mapkit.search.SuggestOptions;
import com.yandex.runtime.Error;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private MapView mapView;
    private DrivingRouter drivingRouter;
    private Point userLocation;
    private MapObjectCollection mapObjects;
    private List<Point> routePoints = new ArrayList<>();
    private PlacemarkMapObject userPlacemark;
    private EditText startAddressEditText, endAddressEditText;
    private Button searchButton, getRouteButton;
    private SearchManager searchManager;
    private SuggestSession suggestSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MapKitFactory.setApiKey("307886ac-d49c-4f2f-b74d-1eb3fb10141c");
        super.onCreate(savedInstanceState);
        MapKitFactory.initialize(this);
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.COMBINED);
        mapObjects = mapView.getMap().getMapObjects().addCollection();
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED);

        startAddressEditText = findViewById(R.id.startAddressEditText);
        endAddressEditText = findViewById(R.id.endAddressEditText);
        searchButton = findViewById(R.id.searchButton);
        getRouteButton = findViewById(R.id.getRouteButton);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startAddress = startAddressEditText.getText().toString();
                String endAddress = endAddressEditText.getText().toString();
                if (!startAddress.isEmpty() && !endAddress.isEmpty()) {
                    searchAddress(startAddress, true);
                    searchAddress(endAddress, false);
                } else {
                    Toast.makeText(MainActivity.this, "Введите оба адреса", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            displayUserLocation();
        }

        mapView.getMap().addInputListener(new InputListener() {
            @Override
            public void onMapTap(@NonNull com.yandex.mapkit.map.Map map, @NonNull Point point) {
                handleMapTap(point);
            }

            @Override
            public void onMapLongTap(@NonNull com.yandex.mapkit.map.Map map, @NonNull Point point) {
                handleMapTap(point);
            }
        });

        getRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestRoute();
            }
        });
    }

    private void searchAddress(String address, boolean isStart) {
        SearchOptions searchOptions = new SearchOptions();
        searchOptions.setSearchTypes(SearchType.GEO.value);

        searchManager.submit(
                address,
                Geometry.fromPoint(new Point(0, 0)),
                searchOptions,
                new Session.SearchListener() {
                    @Override
                    public void onSearchResponse(@NonNull Response searchResponse) {
                        if (!searchResponse.getCollection().getChildren().isEmpty()) {
                            GeoObject geoObject = searchResponse.getCollection().getChildren().get(0).getObj();

                            if (geoObject != null && !geoObject.getGeometry().isEmpty()) {
                                Geometry geometry = geoObject.getGeometry().get(0);
                                Point point = geometry.getPoint();

                                if (point != null) {
                                    if (isStart) {
                                        if (routePoints.size() > 0) {
                                            routePoints.set(0, point);
                                        } else {
                                            routePoints.add(point);
                                        }
                                    } else {
                                        if (routePoints.size() > 1) {
                                            routePoints.set(1, point);
                                        } else {
                                            routePoints.add(point);
                                        }
                                    }
                                    mapObjects.addPlacemark(point);
                                } else {
                                    Toast.makeText(MainActivity.this, "Не удалось получить координаты", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "Гео-объект не содержит геометрию", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Адрес не найден", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onSearchError(@NonNull Error error) {
                        Toast.makeText(MainActivity.this, "Ошибка поиска: " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void handleMapTap(Point point) {
        if (routePoints.size() == 2) {
            mapObjects.clear();
            routePoints.clear();
        }

        routePoints.add(point);
        mapObjects.addPlacemark(point);
    }

    private void requestRoute() {
        if (routePoints.size() < 2) {
            Toast.makeText(MainActivity.this, "Выберите минимум две точки", Toast.LENGTH_SHORT).show();
            return;
        }

        DrivingOptions drivingOptions = new DrivingOptions();
        VehicleOptions vehicleOptions = new VehicleOptions();

        List<RequestPoint> requestPoints = new ArrayList<>();
        RequestPoint startPoint = new RequestPoint(routePoints.get(0), RequestPointType.WAYPOINT, "", "");
        RequestPoint endPoint = new RequestPoint(routePoints.get(1), RequestPointType.WAYPOINT, "", "");
        requestPoints.add(startPoint);
        requestPoints.add(endPoint);

        drivingRouter.requestRoutes(requestPoints, drivingOptions, vehicleOptions, new DrivingSession.DrivingRouteListener() {
            @Override
            public void onDrivingRoutes(@NonNull List<DrivingRoute> drivingRoutes) {
                if (drivingRoutes.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Маршрут не найден", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Очищаем предыдущие маршруты
                mapObjects.clear();

                for (DrivingRoute route : drivingRoutes) {
                    mapObjects.addPolyline(route.getGeometry());
                }
            }

            @Override
            public void onDrivingRoutesError(@NonNull Error error) {
                Toast.makeText(MainActivity.this, "Ошибка построения маршрута: " + error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void displayUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                userLocation = new Point(location.getLatitude(), location.getLongitude());
            } else {
                // Если не удается найти местоположение, используем Минск
                userLocation = new Point(53.9045, 27.5590);
            }

            mapView.getMap().move(new CameraPosition(userLocation, 15.0f, 0.0f, 0.0f));

            // Добавляем метку пользователя
            if (userPlacemark != null) {
                mapObjects.remove(userPlacemark);
            }
            userPlacemark = mapObjects.addPlacemark(userLocation);
        }
    }

    @Override
    protected void onStop() {
        if (mapView != null) {
            mapView.onStop();
        }
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mapView != null) {
            mapView.onStart();
        }
        MapKitFactory.getInstance().onStart();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displayUserLocation();
            } else {
                Toast.makeText(this, "Необходимо разрешение на доступ к местоположению", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
