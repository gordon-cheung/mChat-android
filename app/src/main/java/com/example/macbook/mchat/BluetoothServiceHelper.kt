package com.example.macbook.mchat

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import java.lang.NullPointerException

fun List<BluetoothGattService>.getService(service: String): BluetoothGattService? {
    for (gattService in this) {
        if(gattService.getUuid().toString() == service) {
            return gattService;
        }
    }
    throw NullPointerException("Service not found: " + service)
}

