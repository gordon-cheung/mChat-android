package com.example.macbook.mchat;
import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.lang.reflect.Array;

public class MChatApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        MChatApplication.context = getApplicationContext();

        Intent bluetoothServiceIntent = new Intent(getApplicationContext(), BluetoothService.class);
        startService(bluetoothServiceIntent);
    }

    public static Context getAppContext() {
        return MChatApplication.context;
    }
}
