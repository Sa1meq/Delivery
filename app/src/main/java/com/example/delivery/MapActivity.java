package com.example.delivery;

import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.location.FilteringMode;
import com.yandex.mapkit.location.Location;
import com.yandex.mapkit.location.LocationManager;
import com.yandex.mapkit.location.LocationStatus;
import com.yandex.mapkit.location.LocationListener;
import com.yandex.mapkit.location.Purpose;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.image.ImageProvider;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MapActivity extends AppCompatActivity implements LocationListener {

    private MapView mapView;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private MapObjectCollection mapObjects;
    private Point userLocation;
    private static final String TAG = "MapActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapKitFactory.setApiKey("37174936-b5e1-4db7-86b0-9a3a32e1ff5d");
        MapKitFactory.initialize(this);
        setContentView(R.layout.activity_map);
        mapView = findViewById(R.id.mapview);
        mapObjects = mapView.getMap().getMapObjects().addCollection();

        Button locationButton = findViewById(R.id.button_location);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userLocation != null) {
                    moveCameraToUserLocation(userLocation);
                } else {
                }
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            setCameraToMinsk();
            displayUserLocation();
        }
    }

    private void setCameraToMinsk() {
        double latitude = 53.9045;
        double longitude = 27.5590;
        float zoom = 12.0f;
        mapView.getMap().move(new CameraPosition(new Point(latitude, longitude), zoom, 0, 0));
    }

    private void displayUserLocation() {
        LocationManager locationManager = MapKitFactory.getInstance().createLocationManager();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.subscribeForLocationUpdates(
                    0.0,
                    0,
                    1,
                    false,
                    FilteringMode.OFF,
                    Purpose.GENERAL,
                    this
            );
        }
    }


    private void moveCameraToUserLocation(Point userLocation) {
        mapView.getMap().move(
                new CameraPosition(userLocation, 15.0f, 0.0f, 0.0f),
                new com.yandex.mapkit.Animation(
                        com.yandex.mapkit.Animation.Type.SMOOTH,
                        1.5f
                ),
                null
        );
    }

    @Override
    public void onLocationUpdated(@NonNull Location location) {
        if (location != null) {
            userLocation = new Point(
                    location.getPosition().getLatitude(),
                    location.getPosition().getLongitude()
            );
            mapObjects.clear();
            PlacemarkMapObject userPlacemark = mapObjects.addPlacemark(userLocation);
            userPlacemark.setIcon(ImageProvider.fromResource(this, R.drawable.ic_launcher_background));

            moveCameraToUserLocation(userLocation);
        }

    }

    @Override
    public void onLocationStatusUpdated(LocationStatus locationStatus) {
        // Здесь можно обрабатывать изменения статуса местоположения
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        if (mapView != null) {
            mapView.onStart();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setCameraToMinsk();
                displayUserLocation();
            }
        }
    }
}
