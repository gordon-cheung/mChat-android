package com.example.macbook.mchat;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.util.Queue;
import java.util.TimerTask;
import java.util.Timer;

public class WriteTimer extends TimerTask {

    private Queue<Packet> m_PacketQueue = PacketQueue.getNewPacketQueue();
    private BluetoothGattCharacteristic m_BluetoothCharacteristic;
    private BluetoothGatt m_BluetoothGatt;
    private final static String TAG = WriteTimer.class.getSimpleName();
    final static int CurrentInterval = 1000;

    public WriteTimer(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt)
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
        WriteTimer task = new WriteTimer(m_BluetoothCharacteristic, m_BluetoothGatt);
        Timer timer = new Timer(true);
        timer.schedule(task, CurrentInterval);
    }

    private void writePacket(byte[] data)
    {
        if (writeCharacteristic(data)) {
            m_PacketQueue.remove();
            if (m_PacketQueue.size() > 0) {
                rerunTimer();
            }
            else {
                PacketQueue.writingData = false;
            }
        } else {
            Log.d(TAG, "Error: Failed to write new packet");
            PacketQueue.writingData = false;
        }
    }

    @Override
    public void run(){
        if (PacketQueue.writingData == false && m_PacketQueue.size() > 0)
        {
            PacketQueue.writingData = true;
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
            PacketQueue.writingData = false;
            Log.d(TAG, "Error: No more packets detected in queues");
        }
    }
}
