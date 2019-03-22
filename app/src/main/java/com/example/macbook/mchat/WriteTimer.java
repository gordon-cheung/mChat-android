package com.example.macbook.mchat;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.util.Queue;
import java.util.TimerTask;
import java.util.Timer;

public class WriteTimer extends TimerTask {

    private Queue<Packet> m_FailedPacketQueue = PacketQueue.getFailedPacketQueue();
    private Queue<Packet> m_NewPacketQueue = PacketQueue.getNewPacketQueue();
    private BluetoothGattCharacteristic m_BluetoothCharacteristic;
    private BluetoothGatt m_BluetoothGatt;
    private final static String TAG = WriteTimer.class.getSimpleName();
    private final int MIN_INTERVAL = 1000;
    private final int MAX_INTERVAL = 64000;
    static int CurrentInterval = 1000;
    private int m_NumFailedPackets;

    public WriteTimer(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt, int numFailedPackets)
    {
        m_BluetoothCharacteristic = characteristic;
        m_BluetoothGatt = gatt;
        m_NumFailedPackets = numFailedPackets;
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

    @Override
    public void run(){
        if (m_FailedPacketQueue.size() > 0)
        {
            Log.d(TAG, "Failed Packet Q Size: " + m_FailedPacketQueue.size() + " Content: " + new String(m_FailedPacketQueue.peek().getContent()));
            byte[] data = m_FailedPacketQueue.peek().getBytes();
            if (writeCharacteristic(data))
            {
                m_FailedPacketQueue.remove();
            }
            else
            {
                Log.d(TAG, "Failed to write failed packet");
            }
        }
        else
        {
            Log.d(TAG, "New Packet Q Size: " + m_NewPacketQueue.size() + " Content: " + new String(m_NewPacketQueue.peek().getContent()));
            byte[] data = m_NewPacketQueue.peek().getBytes();
            if (writeCharacteristic(data))
            {
                m_NewPacketQueue.remove();
            }
            else
            {
                Log.d(TAG, "Failed to write new packet");
            }
        }
        if (m_FailedPacketQueue.size() > 0 || m_NewPacketQueue.size() > 0)
        {
            int numFailedPackets = m_FailedPacketQueue.size();
            WriteTimer task = new WriteTimer(m_BluetoothCharacteristic, m_BluetoothGatt, numFailedPackets);
            Timer timer = new Timer(true);
            if (numFailedPackets > m_NumFailedPackets)
            {
                CurrentInterval = Math.min((CurrentInterval * 2), MAX_INTERVAL);
                Log.d(TAG, "More failed packets detected, new interval: " + CurrentInterval);

            }
            else
            {
                CurrentInterval = Math.max((CurrentInterval / 2), MIN_INTERVAL);
                Log.d(TAG, "Less failed packets detected, new interval: " + CurrentInterval);
            }
            timer.schedule(task, CurrentInterval);
        }
        else
        {
            PacketQueue.writingData = false;
            Log.d(TAG, "No more packets detected in queues");
        }
    }
}
