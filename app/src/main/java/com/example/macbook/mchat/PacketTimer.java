package com.example.macbook.mchat;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.util.Queue;
import java.util.TimerTask;
import java.util.Timer;

public class PacketTimeout extends TimerTask {

    private Queue<Packet> m_PacketQueue = TransmissionQueue.getWriteQueue();
    private BluetoothGattCharacteristic m_BluetoothCharacteristic;
    private BluetoothGatt m_BluetoothGatt;
    private final static String TAG = PacketTimeout.class.getSimpleName();
    final static int TIMEOUT_INTERVAL = 1000;

    public PacketTimeout(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt)
    {
        m_BluetoothCharacteristic = characteristic;
        m_BluetoothGatt = gatt;
    }

    private boolean writeCharacteristic(byte[] data)
    {
        boolean success = false;
        try {
            m_BluetoothCharacteristic.setValue(data);
            Log.d(TAG, "WriteCharacteristic(" + m_BluetoothCharacteristic.getUuid() + ") Value: " + data);
            success = m_BluetoothGatt.writeCharacteristic(m_BluetoothCharacteristic);

            if (!success) {
                Log.d(TAG, "WriteCharacteristic failed");
            }
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        }
        return success;
    }

    private void rerunTimer()
    {
        PacketTimeout task = new PacketTimeout(m_BluetoothCharacteristic, m_BluetoothGatt);
        Timer timer = new Timer(true);
        timer.schedule(task, TIMEOUT_INTERVAL);
    }

    private void writePacket(byte[] data)
    {
        if (writeCharacteristic(data)) {
            m_PacketQueue.remove();
            if (m_PacketQueue.size() > 0) {
                rerunTimer();
            }
        } else {
            Log.d(TAG, "Error: Failed to write new packet");
            TransmissionQueue.writingData = false;
        }
    }

    @Override
    public void run(){
        if (TransmissionQueue.writingData == false && m_PacketQueue.size() > 0)
        {
            TransmissionQueue.writingData = true;
            Packet packet = m_PacketQueue.peek();
            Log.d(TAG, "Queue Size: " + m_PacketQueue.size() + " Content: " + new String(packet.getContent()));
            byte[] data = packet.getBytes();
            if (packet.getDataType() == Message.TEXT || packet.getDataType() == Message.PICTURE) {
                if (BluetoothService.NETWORK_REGISTRATION_COMPLETE) {
                    writePacket(data);
                }
                else //Network registration not complete, try again later
                {
                    Log.d(TAG, "Waiting for network registration to complete...");
                    rerunTimer();
                }
            }
            else {
                writePacket(data);
            }
        }
        else
        {
            TransmissionQueue.writingData = false;
            Log.d(TAG, "Error: No more packets detected in queues");
        }
    }
}
