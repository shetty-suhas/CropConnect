package com.crop.cropconnect;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class CropConnectApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}