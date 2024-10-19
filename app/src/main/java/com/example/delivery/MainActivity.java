package com.example.delivery;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;


import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
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
import com.yandex.mapkit.geometry.BoundingBox;
import com.yandex.mapkit.geometry.Geometry;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.location.FilteringMode;
import com.yandex.mapkit.location.LocationListener;
import com.yandex.mapkit.location.LocationStatus;
import com.yandex.mapkit.location.Purpose;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.TextStyle;
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
import com.yandex.mapkit.search.SuggestType;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LocationListener {

    private static final int REQUEST_LOCATION_PERMISSION = 1; // Код запроса разрешения на местоположение
    private MapView mapView;
    private DrivingRouter drivingRouter;
    private Point userLocation;
    private MapObjectCollection mapObjects;
    private List<Point> routePoints = new ArrayList<>();
    private PlacemarkMapObject userPlacemark;
    private EditText startAddressEditText, endAddressEditText;
    private Button getRouteButton;
    private SearchManager searchManager;
    private SuggestSession suggestSession;
    private boolean isStartFieldActive = true;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private final Handler handler = new Handler();
    private Runnable suggestionRunnable;
    private RecyclerView suggestionsRecyclerView;
    private AddressSuggestionAdapter suggestionAdapter;
    private List<String> suggestions = new ArrayList<>();
    private com.yandex.mapkit.location.LocationManager locationManager;
    private DrawerLayout drawerLayout;
    private boolean isFirstLocationUpdate = true;
    private MapObjectCollection pinCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MapKitFactory.setApiKey("37174936-b5e1-4db7-86b0-9a3a32e1ff5d");
        MapKitFactory.initialize(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.COMBINED);
        mapObjects = mapView.getMap().getMapObjects().addCollection();
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED);

        startAddressEditText = findViewById(R.id.startAddressEditText);
        endAddressEditText = findViewById(R.id.endAddressEditText);
        getRouteButton = findViewById(R.id.getRouteButton);

        suggestionsRecyclerView = findViewById(R.id.suggestionsRecyclerView);
        suggestionAdapter = new AddressSuggestionAdapter(suggestions, this::onSuggestionClick);
        suggestionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        suggestionsRecyclerView.setAdapter(suggestionAdapter);
        suggestionsRecyclerView.setVisibility(View.GONE);

        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);


        getRouteButton.setOnClickListener(v -> {
            String startAddress = startAddressEditText.getText().toString();
            String endAddress = endAddressEditText.getText().toString();

            if (!startAddress.isEmpty() && !endAddress.isEmpty()) {
                if (routePoints.size() == 2) {
                    requestRoute();
                } else {
                    Toast.makeText(MainActivity.this, "Сначала выберите оба адреса", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Введите оба адреса", Toast.LENGTH_SHORT).show();
            }
        });

        startAddressEditText.setOnFocusChangeListener((v, hasFocus) -> isStartFieldActive = hasFocus);
        endAddressEditText.setOnFocusChangeListener((v, hasFocus) -> isStartFieldActive = !hasFocus);

        startAddressEditText.addTextChangedListener(new AddressTextWatcher());
        endAddressEditText.addTextChangedListener(new AddressTextWatcher());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            requestUserLocation();
        }

        startAddressEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        endAddressEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        findViewById(R.id.sheet_drag_handle).setOnClickListener(v -> {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        pinCollection = mapView.getMap().getMapObjects().addCollection();
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);
        findViewById(R.id.menuIcon).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        locationManager = MapKitFactory.getInstance().createLocationManager();
        Log.d("MainActivity", "LocationManager initialized: " + locationManager);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_profile) {
            Toast.makeText(this, "Сосал? Не работает", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_courier) {
            Intent intent = new Intent(MainActivity.this, RegisterCourier.class);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void clearPlacemarks() {
        pinCollection.clear();
    }

    private void addPlacemark(Point point) {

        PlacemarkMapObject placemark = pinCollection.addPlacemark(point);
        placemark.setIcon(ImageProvider.fromResource(this, R.drawable.ic_user_location));
    }

    private void getSuggestions(String query) {
        SuggestOptions suggestOptions = new SuggestOptions();
        suggestOptions.setSuggestTypes(SuggestType.GEO.value);

        if (userLocation != null) {
            double latitude = userLocation.getLatitude();
            double longitude = userLocation.getLongitude();
            BoundingBox boundingBox = new BoundingBox(
                    new Point(latitude + 0.1, longitude + 0.1),  // Северо-Западный угол
                    new Point(latitude - 0.1, longitude - 0.1)   // Южно-Восточный угол
            );

            suggestSession = searchManager.createSuggestSession();
            suggestSession.suggest(query, boundingBox, suggestOptions, new SuggestSession.SuggestListener() {
                @Override
                public void onResponse(@NonNull SuggestResponse suggestResponse) {
                    suggestions.clear();
                    for (SuggestItem item : suggestResponse.getItems()) {
                        suggestions.add(item.getDisplayText());
                    }
                    suggestionAdapter.notifyDataSetChanged();
                    suggestionsRecyclerView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError(@NonNull Error error) {
                    Toast.makeText(MainActivity.this, "Ошибка получения подсказок", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(MainActivity.this, "Не удалось получить текущее местоположение", Toast.LENGTH_SHORT).show();
        }
    }

    private void searchAddress(String address, boolean isStart) {
        SearchOptions searchOptions = new SearchOptions();
        searchOptions.setSearchTypes(SearchType.GEO.value);

        searchManager.submit(
                address,
                Geometry.fromPoint(new Point(53.9, 27.56667)), // Минск
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
                                        addPlacemark(point);
                                    }
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

    private void onSuggestionClick(String suggestion) {
        if (isStartFieldActive) {
            startAddressEditText.setText(suggestion);
            startAddressEditText.clearFocus();
            searchAddress(suggestion, true);
        } else {
            endAddressEditText.setText(suggestion);
            endAddressEditText.clearFocus();
            searchAddress(suggestion, false);
        }
        suggestionsRecyclerView.setVisibility(View.GONE);
    }

    private void requestRoute() {
        if (routePoints.size() < 2) {
            Toast.makeText(MainActivity.this, "Выберите минимум две точки", Toast.LENGTH_SHORT).show();
            return;
        }

        DrivingOptions drivingOptions = new DrivingOptions();
        VehicleOptions vehicleOptions = new VehicleOptions();

        List<RequestPoint> requestPoints = new ArrayList<>();
        requestPoints.add(new RequestPoint(routePoints.get(0), RequestPointType.WAYPOINT, "", ""));
        requestPoints.add(new RequestPoint(routePoints.get(1), RequestPointType.WAYPOINT, "", ""));

        DrivingSession drivingSession = drivingRouter.requestRoutes(
                requestPoints,
                drivingOptions,
                vehicleOptions,
                new DrivingSession.DrivingRouteListener() {
                    @Override
                    public void onDrivingRoutes(@NonNull List<DrivingRoute> routes) {
                        if (!routes.isEmpty()) {
                            mapObjects.addPolyline(routes.get(0).getGeometry());
                        }
                    }

                    @Override
                    public void onDrivingRoutesError(@NonNull Error error) {
                        Toast.makeText(MainActivity.this, "Ошибка построения маршрута", Toast.LENGTH_SHORT).show();
                    }
                }
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


    private Runnable locationUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            requestUserLocation();
            handler.postDelayed(this, 5000);
        }
    };

    private void requestUserLocation() {
        if (locationManager == null) {
            Log.e("MainActivity", "LocationManager is null!");
            return; // Прерывание, если locationManager не инициализирован
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
        Log.d("MainActivity", "User location: " + userLocation);

        if (isFirstLocationUpdate) {
            mapView.getMap().move(new CameraPosition(userLocation, 14.0f, 0.0f, 0.0f));
            isFirstLocationUpdate = false;

            userPlacemark = pinCollection.addPlacemark(userLocation);
            userPlacemark.setIcon(ImageProvider.fromResource(this, R.drawable.ic_user_location)); // Укажите свой ресурс маркера
        } else {
            if (userPlacemark != null) {
                userPlacemark.setGeometry(userLocation);
            }
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

    private class AddressTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String query = s.toString();
            handler.removeCallbacks(suggestionRunnable);
            if (!query.isEmpty()) {
                suggestionRunnable = () -> getSuggestions(query);
                handler.postDelayed(suggestionRunnable, 500);
            } else {
                suggestions.clear();
                suggestionAdapter.notifyDataSetChanged();
                suggestionsRecyclerView.setVisibility(View.GONE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
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
        handler.removeCallbacks(locationUpdateRunnable);
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }
}