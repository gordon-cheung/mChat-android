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

fun List<BluetoothGattCharacteristic>.getCharacteristic(characteristic: String): BluetoothGattCharacteristic? {
    for (gattCharacteristic in this) {
        if(gattCharacteristic.getUuid().toString() == characteristic) {
            return gattCharacteristic;
        }
    }
    return null;
}

fun List<BluetoothGattDescriptor>.getDescriptor(descriptor: String): BluetoothGattDescriptor? {
    for (gattDescriptor in this) {
        if(gattDescriptor.getUuid().toString() == descriptor) {
            return gattDescriptor;
        }
    }
    return null;
}

