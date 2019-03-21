package com.example.macbook.mchat;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

public final class AppNotification {

    // Intent name and intent extra key name for messages received over BLE
    public static String MESSAGE_RECEIVED_NOTIFICATION = "MESSAGE_RECEIVED";

    // BLUETOOTH STATUS
    public final static String ACTION_GATT_DEVICE_SELECTED = "ACTION_GATT_DEVICE_SELECTED";
    public final static String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_CONNECTING = "ACTION_GATT_CONNECTING";
    public final static String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";

    //
    public final static String IMAGE_DATA = "IMAGE_DATA";
    public final static String CONTACT_DATA = "CONTACT_DATA";
    private AppNotification() {

    }
}
