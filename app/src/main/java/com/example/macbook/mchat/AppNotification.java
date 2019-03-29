package com.example.macbook.mchat;
public final class AppNotification {

    // Intent name and intent extra key name for messages received over BLE
    public static String MESSAGE_RECEIVED_NOTIFICATION = "MESSAGE_RECEIVED";
    public static String ACK_RECEIVED_NOTIFICATION = "ACK_RECEIVED";
    public static String MESSAGE_FAILED_NOTIFICATION = "MESSAGED_FAILED";
    public static String NETWORK_REGISTRATION_NOTIFICATION = "NETWORK_REGISTRATION_NOTIFICATION";

    // Bluetooth Status
    public final static String ACTION_GATT_DEVICE_SELECTED = "ACTION_GATT_DEVICE_SELECTED";
    public final static String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_CONNECTING = "ACTION_GATT_CONNECTING";
    public final static String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";

    // Data passed through changing activities
    public final static String IMAGE_DATA = "IMAGE_DATA";
    public final static String CONTACT_DATA = "CONTACT_DATA";
    private AppNotification() {

    }
}
