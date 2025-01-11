package com.example.delivery;

import static com.google.android.material.internal.ViewUtils.hideKeyboard;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.model.Card;
import com.example.delivery.model.RouteOrder;
import com.example.delivery.repository.CardRepository;
import com.example.delivery.repository.RouteOrderRepository;
import com.example.delivery.repository.UserRepository;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.yandex.mapkit.GeoObject;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.directions.driving.DrivingOptions;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingRouterType;
import com.yandex.mapkit.directions.driving.DrivingSection;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.directions.driving.VehicleOptions;
import com.yandex.mapkit.geometry.BoundingBox;
import com.yandex.mapkit.geometry.Geometry;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.geometry.PolylinePosition;
import com.yandex.mapkit.geometry.geo.PolylineIndex;
import com.yandex.mapkit.geometry.geo.PolylineUtils;
import com.yandex.mapkit.location.FilteringMode;
import com.yandex.mapkit.location.LocationListener;
import com.yandex.mapkit.location.LocationStatus;
import com.yandex.mapkit.location.Purpose;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.navigation.RoutePosition;
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
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LocationListener {

    public static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int ADDRESS_PICKER_REQUEST = 1;
    private MapView mapView;
    private DrivingRouter drivingRouter;
    private Point userLocation;
    private MapObjectCollection mapObjects;
    private List<Point> routePoints = new ArrayList<>();
    private PlacemarkMapObject userPlacemark;
    private PlacemarkMapObject startPlacemark;
    private PlacemarkMapObject endPlacemark;
    private EditText startAddressEditText, endAddressEditText, estimatedTimeEditText, estimatedCostEditText, orderDescriptionEditText;
    private Button getRouteButton;
    private SearchManager searchManager;
    private SuggestSession suggestSession;
    private boolean isStartFieldActive = true;
    private final Handler handler = new Handler();
    private Runnable suggestionRunnable;
    private RecyclerView suggestionsRecyclerView;
    private AddressSuggestionAdapter suggestionAdapter;
    private List<String> suggestions = new ArrayList<>();
    private com.yandex.mapkit.location.LocationManager locationManager;
    private DrawerLayout drawerLayout;
    private boolean isFirstLocationUpdate = true;
    private MapObjectCollection pinCollection;
    private RouteOrderRepository routeOrderRepository;
    private UserRepository userRepository;
    private boolean isRequestInProgress = false;
    private TextWatcher textWatcher;
    private RadioButton radioButtonPedestrian, radioButtonCar, radioButtonTruck;
    private CardRepository cardRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MapKitFactory.initialize(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.COMBINED);
        mapObjects = mapView.getMap().getMapObjects().addCollection();
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED);
        routeOrderRepository = new RouteOrderRepository();
        userRepository = new UserRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance());
        cardRepository = new CardRepository();

        startAddressEditText = findViewById(R.id.startAddressEditText);
        endAddressEditText = findViewById(R.id.endAddressEditText); // Инициализация EditText
        getRouteButton = findViewById(R.id.getRouteButton);
        radioButtonPedestrian = findViewById(R.id.radioButtonPedestrian);
        radioButtonCar = findViewById(R.id.radioButtonCar);
        radioButtonTruck = findViewById(R.id.radioButtonTruck);

        estimatedTimeEditText = findViewById(R.id.estimatedTimeEditText);
        estimatedCostEditText = findViewById(R.id.estimatedCostEditText);
        orderDescriptionEditText = findViewById(R.id.orderDescriptionEditText);


        suggestionsRecyclerView = findViewById(R.id.suggestionsRecyclerView);
        suggestionAdapter = new AddressSuggestionAdapter(suggestions, this::onSuggestionClick);
        suggestionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        suggestionsRecyclerView.setAdapter(suggestionAdapter);
        suggestionsRecyclerView.setVisibility(View.GONE);


        textWatcher = new AddressTextWatcher();
        startAddressEditText.addTextChangedListener(textWatcher);
        endAddressEditText.addTextChangedListener(textWatcher); // Использование EditText

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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            requestUserLocation();
        }

        pinCollection = mapView.getMap().getMapObjects().addCollection();
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);
        findViewById(R.id.menuIcon).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        locationManager = MapKitFactory.getInstance().createLocationManager();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADDRESS_PICKER_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                String startAddress = data.getStringExtra("startAddress");
                String endAddress = data.getStringExtra("endAddress");

                startAddressEditText.setText(startAddress);
                endAddressEditText.setText(endAddress);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_profile) {
            Intent intent = new Intent(MainActivity.this, UserProfile.class);
            startActivity(intent);
            finish();
        } else if (itemId == R.id.nav_courier) {
            Intent intent = new Intent(MainActivity.this, RegisterCourier.class);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    private void addStartPlacemark(Point point) {
        if (startPlacemark != null) {
            pinCollection.remove(startPlacemark);
        }
        startPlacemark = pinCollection.addPlacemark(point);
        startPlacemark.setIcon(ImageProvider.fromResource(this, R.drawable.ic_routestart));
    }

    private void addFinishPlacemark(Point point) {
        if (endPlacemark != null) {
            pinCollection.remove(endPlacemark);
        }
        endPlacemark = pinCollection.addPlacemark(point);
        endPlacemark.setIcon(ImageProvider.fromResource(this, R.drawable.ic_routefinish));
    }


    private void getSuggestions(String query) {
        SuggestOptions suggestOptions = new SuggestOptions();
        suggestOptions.setSuggestTypes(SuggestType.GEO.value);

        if (userLocation != null) {
            double latitude = userLocation.getLatitude();
            double longitude = userLocation.getLongitude();
            BoundingBox boundingBox = new BoundingBox(
                    new Point(latitude + 0.1, longitude + 0.1), // Меньше смещение, чтобы границы оставались в пределах карты
                    new Point(latitude - 0.1, longitude - 0.1)
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
                Geometry.fromPoint(new Point(53.9, 27.56667)),
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

                                        addStartPlacemark(point);
                                    } else {
                                        if (routePoints.size() > 1) {
                                            routePoints.set(1, point);
                                        } else {
                                            routePoints.add(point);
                                        }
                                        addFinishPlacemark(point);
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
        startAddressEditText.removeTextChangedListener(textWatcher);
        endAddressEditText.removeTextChangedListener(textWatcher);
        if (isStartFieldActive) {
            startAddressEditText.setText(suggestion);
            startAddressEditText.clearFocus();
            searchAddress(suggestion, true);
        } else {
            endAddressEditText.setText(suggestion);
            endAddressEditText.clearFocus();
            searchAddress(suggestion, false);
        }

        hideKeyboard();
        suggestionsRecyclerView.setVisibility(View.GONE);

        startAddressEditText.addTextChangedListener(textWatcher);
        endAddressEditText.addTextChangedListener(textWatcher);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }



    private void requestRoute() {
        String selectedCourierType = getSelectedCourierType();
        if (selectedCourierType.equals("Не указан")) {
            Toast.makeText(MainActivity.this, "Выберите тариф!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getUid();
        CardRepository cardRepository = new CardRepository();

        cardRepository.cardsCollection.whereEqualTo("cardUserID", userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        boolean hasMainCard = false;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Boolean isMain = document.getBoolean("main");
                            if (Boolean.TRUE.equals(isMain)) {
                                hasMainCard = true;
                                break;
                            }
                        }

                        if (!hasMainCard) {
                            showAlertDialog(
                                    "Основная карта не выбрана",
                                    "У вас нет основной карты. Пожалуйста, выберите основную карту.",
                                    () -> {
                                        Intent intent = new Intent(MainActivity.this, CardActivity.class);
                                        startActivity(intent);
                                    }
                            );
                        } else {
                            showConfirmationDialog();
                        }
                    } else {
                        showAlertDialog(
                                "Нет добавленных карт",
                                "У вас нет добавленных карт. Перенаправляем на экран добавления карты.",
                                () -> {
                                    Intent intent = new Intent(MainActivity.this, CardActivity.class);
                                    startActivity(intent);
                                }
                        );
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Ошибка проверки карт: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showAlertDialog(String title, String message, Runnable onOkClicked) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("ОК", (dialog, which) -> onOkClicked.run())
                .setCancelable(false)
                .show();
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Подтверждение заказа")
                .setMessage("Вы уверены, что хотите оформить заказ?")
                .setPositiveButton("ОК", (dialog, which) -> proceedWithOrder())
                .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    private void proceedWithOrder() {
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
                        isRequestInProgress = false;
                        if (!routes.isEmpty()) {
                            DrivingRoute route = routes.get(0);
                            mapObjects.addPolyline(route.getGeometry());
                            if (userLocation != null) {
                                float distanceToEndPoint = distanceBetweenPointsOnRoute(route, routePoints.get(0), routePoints.get(1));
                                float timeToEndPoint = timeTravelToPoint(route, routePoints.get(1));
                                long timeInMinutes = (long) (timeToEndPoint / 60);

                                estimatedTimeEditText.setText(String.valueOf(timeInMinutes));

                                String startAddress = startAddressEditText.getText().toString();
                                String endAddress = endAddressEditText.getText().toString();
                                String courierType = getSelectedCourierType();

                                double orderCost = calculateOrderCost(courierType, distanceToEndPoint / 1000);

                                String formattedCost = String.format(Locale.getDefault(), "%.2f", orderCost).replace(',', '.');
                                estimatedCostEditText.setText(formattedCost);

                                String orderDescription = orderDescriptionEditText.getText().toString();

                                RouteOrder routeOrder = new RouteOrder(
                                        UUID.randomUUID().toString(),
                                        FirebaseAuth.getInstance().getUid(),
                                        null,
                                        routePoints,
                                        distanceToEndPoint,
                                        (long) timeToEndPoint,
                                        startAddress,
                                        endAddress,
                                        courierType,
                                        Double.parseDouble(formattedCost),
                                        parseEstimatedDeliveryTime(),
                                        orderDescription
                                );

                                routeOrderRepository.saveRouteOrder(routeOrder)
                                        .thenAccept(aVoid -> Toast.makeText(MainActivity.this, "Заказ успешно оформлен", Toast.LENGTH_SHORT).show())
                                        .exceptionally(e -> {
                                            Toast.makeText(MainActivity.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            return null;
                                        });
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Маршрут не найден", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onDrivingRoutesError(@NonNull Error error) {
                        isRequestInProgress = false;
                        Toast.makeText(MainActivity.this, "Ошибка построения маршрута", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }





    private float distanceBetweenPointsOnRoute(DrivingRoute route, Point first, Point second) {
        PolylineIndex polylineIndex = PolylineUtils.createPolylineIndex(route.getGeometry());
        if (polylineIndex == null) {
            return 0;
        }
        PolylinePosition firstPosition = polylineIndex.closestPolylinePosition(first, PolylineIndex.Priority.CLOSEST_TO_START, 1000.0);
        PolylinePosition secondPosition = polylineIndex.closestPolylinePosition(second, PolylineIndex.Priority.CLOSEST_TO_RAW_POINT, 1000.0);

        if (firstPosition == null || secondPosition == null) {
            return 0;
        }
        return (float) PolylineUtils.distanceBetweenPolylinePositions(route.getGeometry(), firstPosition, secondPosition);
    }

    private float timeTravelToPoint(DrivingRoute route, Point targetPoint) {
        RoutePosition currentPosition = route.getRoutePosition();
        if (currentPosition == null) {
            return 0;
        }
        float distance = distanceBetweenPointsOnRoute(route, currentPosition.getPoint(), targetPoint);
        if (distance > 0) {
            RoutePosition targetPosition = currentPosition.advance(distance);
            return (float) (currentPosition.timeToFinish() - targetPosition.timeToFinish());
        } else {
            return 0;
        }
    }

    private String getSelectedCourierType() {
        RadioGroup radioGroup = findViewById(R.id.radioGroupCourierType);
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.radioButtonPedestrian) {
            return "Пеший";
        } else if (selectedId == R.id.radioButtonCar) {
            return "Авто";
        } else if (selectedId == R.id.radioButtonTruck) {
            return "Грузовой";
        }
        return "Не указан";
    }


    private double parseEstimatedCost() {
        String costText = estimatedCostEditText.getText().toString();
        try {
            return Double.parseDouble(costText);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Long parseEstimatedDeliveryTime() {
        String timeText = estimatedTimeEditText.getText().toString();
        try {
            return Long.parseLong(timeText);
        } catch (NumberFormatException e) {
            return 0L;
        }
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

            userPlacemark = pinCollection.addPlacemark(userLocation);
            userPlacemark.setIcon(ImageProvider.fromResource(this, R.drawable.ic_user_location));
        } else {
            if (userPlacemark != null) {
                userPlacemark.setGeometry(userLocation);
            }
        }
    }

    @Override
    public void onLocationStatusUpdated(@NonNull LocationStatus locationStatus) {
        if (locationStatus == LocationStatus.NOT_AVAILABLE) {
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
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

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
        public void afterTextChanged(Editable s) {}
    }

    private double calculateOrderCost(String courierType, double distanceInKm) {
        double baseCost = 0.0;
        double costPerKm;

        switch (courierType) {
            case "Пеший":
                costPerKm = 0.9;
                baseCost += 0;
                break;
            case "Авто":
                costPerKm = 1.5;
                baseCost += 2;
                break;
            case "Грузовой":
                costPerKm = 2.0;
                baseCost += 4;
                break;
            default:
                costPerKm = 0.0;
        }

        double courierCost = baseCost + (costPerKm * distanceInKm);

        return courierCost * 1.3;
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