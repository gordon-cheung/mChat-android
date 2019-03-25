package com.example.macbook.mchat;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.util.TimerTask;

public class PacketTimer extends TimerTask {

    private BluetoothGattCharacteristic m_BluetoothCharacteristic;
    private BluetoothGatt m_BluetoothGatt;
    private final static String TAG = PacketTimer.class.getSimpleName();
    final static int TIMEOUT_INTERVAL = 3000;

    public PacketTimer(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt)
    {
        m_BluetoothCharacteristic = characteristic;
        m_BluetoothGatt = gatt;
    }

    @Override
    public void run() {
        TransmissionManager.waitingAck = false;
        TransmissionManager.write(m_BluetoothCharacteristic, m_BluetoothGatt);
    }
}
