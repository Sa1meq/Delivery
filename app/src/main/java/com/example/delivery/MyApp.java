package com.example.delivery;

import android.app.Application;
import com.yandex.mapkit.MapKitFactory;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MapKitFactory.setApiKey("37174936-b5e1-4db7-86b0-9a3a32e1ff5d");
        MapKitFactory.initialize(this);
    }
}
