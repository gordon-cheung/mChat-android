package com.example.macbook.mchat;
import android.app.Application;
import android.content.Context;

public class MChatApplication extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        MChatApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MChatApplication.context;
    }
}
