package com.example.delivery;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.directions.driving.DrivingOptions;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.directions.driving.VehicleOptions;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.PlacemarkMapObject;

import com.yandex.mapkit.geometry.Point;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MapView mapView;
    private DrivingRouter drivingRouter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MapKitFactory.initialize();
        mapView = findViewById(R.id.mapView);
        drivingRouter = MapKitFactory.getInstance().getDrivingRouter();

        Button getRouteButton = findViewById(R.id.getRouteButton);
        getRouteButton.setOnClickListener(v -> requestRoute());

        // Установка начального положения камеры
        mapView.getMap().move(
                new CameraPosition(new Point(55.278543, 25.196141), 10f, 0f, 0f)
        );
    }

    private void requestRoute() {
        DrivingOptions drivingOptions = new DrivingOptions();
        drivingOptions.setRoutesCount(3); // Запрос на 3 маршрута

        VehicleOptions vehicleOptions = new VehicleOptions(); // Можно задать параметры транспорта

        List<RequestPoint> points = List.of(
                new RequestPoint(new Point(25.196141, 55.278543), RequestPointType.WAYPOINT, null),
                new RequestPoint(new Point(25.171148, 55.238034), RequestPointType.WAYPOINT, null)
        );

        drivingRouter.requestRoutes(points, drivingOptions, vehicleOptions, new DrivingSession.DrivingRouteListener() {
            @Override
            public void onDrivingRoutes(List<DrivingRoute> drivingRoutes) {
                // Обработка успешного запроса маршрутов
                for (DrivingRoute route : drivingRoutes) {
                    // Здесь можно добавить маркер для каждого маршрута
                    mapView.getMap().getMapObjects().addPolyline(route.getGeometry());
                }
            }

            @Override
            public void onDrivingRoutesError(Error error) {
                // Обработка ошибок при построении маршрута
                // Вы можете вывести сообщение пользователю
            }
        });
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }
}
