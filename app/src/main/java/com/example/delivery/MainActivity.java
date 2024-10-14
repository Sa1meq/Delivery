package com.example.delivery;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.yandex.mapkit.location.LocationListener;
import com.yandex.mapkit.location.LocationStatus;
import com.yandex.mapkit.map.CameraPosition;
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
import com.yandex.mapkit.search.SuggestType;


import com.yandex.runtime.Error;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LocationListener {
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
    private boolean isStartFieldActive = true;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private final Handler handler = new Handler();
    private Runnable suggestionRunnable;

    private RecyclerView suggestionsRecyclerView;
    private AddressSuggestionAdapter suggestionAdapter;
    private List<String> suggestions = new ArrayList<>();

    private DrawerLayout drawerLayout;

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
        searchButton = findViewById(R.id.searchButton);
        getRouteButton = findViewById(R.id.getRouteButton);

        suggestionsRecyclerView = findViewById(R.id.suggestionsRecyclerView);
        suggestionAdapter = new AddressSuggestionAdapter(suggestions, this::onSuggestionClick);
        suggestionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        suggestionsRecyclerView.setAdapter(suggestionAdapter);



        suggestionsRecyclerView.setVisibility(View.GONE); // Initialize as hidden

        searchButton.setOnClickListener(v -> {
            String startAddress = startAddressEditText.getText().toString();
            String endAddress = endAddressEditText.getText().toString();
            if (!startAddress.isEmpty() && !endAddress.isEmpty()) {
                searchAddress(startAddress, true);
                searchAddress(endAddress, false);
                suggestionsRecyclerView.setVisibility(View.GONE);
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
            displayUserLocation();
        }



        getRouteButton.setOnClickListener(v -> requestRoute());

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);
        findViewById(R.id.menuIcon).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));


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
                private void onSuggestionClick(String suggestion) {
                    if (isStartFieldActive) {
                        startAddressEditText.setText(suggestion);
                        startAddressEditText.clearFocus(); // Remove focus from the field
                    } else {
                        endAddressEditText.setText(suggestion);
                        endAddressEditText.clearFocus(); // Remove focus from the field
                    }
                    suggestionsRecyclerView.setVisibility(View.GONE);
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

    private void onSuggestionClick(String suggestion) {
        if (isStartFieldActive) {
            startAddressEditText.setText(suggestion);
            startAddressEditText.clearFocus(); // Снимаем фокус с поля
        } else {
            endAddressEditText.setText(suggestion);
            endAddressEditText.clearFocus(); // Снимаем фокус с поля
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

    private void displayUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                userLocation = new Point(location.getLatitude(), location.getLongitude());
                mapView.getMap().move(new CameraPosition(userLocation, 14.0f, 0.0f, 0.0f));
                if (userPlacemark == null) {
                    userPlacemark = mapObjects.addPlacemark(userLocation);
                } else {
                    userPlacemark.setGeometry(userLocation);
                }
            }
        } else {
            Toast.makeText(this, "Нет разрешения на использование геолокации", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationUpdated(@NonNull com.yandex.mapkit.location.Location location) {

    }

    @Override
    public void onLocationStatusUpdated(@NonNull LocationStatus locationStatus) {

    }

    // Реализация текстового слушателя для обработки ввода
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
            // Не требуется для нашей задачи
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
