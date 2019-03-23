package com.example.macbook.mchat;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.util.Queue;
import java.util.LinkedList;
import java.util.Timer;

public class TransmissionQueue {

    private static Queue<Packet> m_WriteQueue = new LinkedList<>();
    private static LinkedList<PacketStatus> m_DataQueue = new LinkedList<>();
    private final static String TAG = WriteTimer.class.getSimpleName();
    static boolean writingData = false;

    public static Queue<Packet> getWriteQueue()
    {
        return m_WriteQueue;
    }

    public static void queuedWrite(Packet packet, BluetoothGattCharacteristic characteristic, BluetoothGatt gatt)
    {
        m_WriteQueue.add(packet);
        m_DataQueue.add(new PacketStatus(packet));
        write(characteristic, gatt);
    }

    public static void ackReceived() {
        if (m_DataQueue.size() == 0) {
            Log.d(TAG, "Error! Ack received but data queue is empty!");
        }
        else {
            for (PacketStatus packet : m_DataQueue) {
                if (packet.getAckStatus() == false) {
                    packet.setAckStatus(true);
                    break;
                }
            }
        }
    }

    public static void nackReceived() {
        if (m_DataQueue.size() == 0) {
            Log.d(TAG, "Error! Ack received but data queue is empty!");
        }
        else {
            for (int i = 0; i < m_DataQueue.size(); i++)
            {
                PacketStatus temp = m_DataQueue.get(i);
                if (temp.getAckStatus() == false) {
                    m_DataQueue.remove(i);
                }
                m_WriteQueue.add(temp.getPacket());
                m_DataQueue.add(temp);
            }
        }
    }

    public static void txSuccess() {
        if (m_DataQueue.size() == 0) {
            Log.d(TAG, "Error! TX received but data queue is empty!");
        }
        else {
            for (int i = 0; i < m_DataQueue.size(); i++)
            {
                PacketStatus temp = m_DataQueue.get(i);
                if (temp.getAckStatus() == true) {
                    m_DataQueue.remove(i);
                }
            }
        }
    }

    public static void txFailure() {
        if (m_DataQueue.size() == 0) {
            Log.d(TAG, "Error! TX received but data queue is empty!");
        }
        else {
            for (int i = 0; i < m_DataQueue.size(); i++)
            {
                PacketStatus temp = m_DataQueue.get(i);
                if (temp.getAckStatus() == true) {
                    m_DataQueue.remove(i);
                }
                m_WriteQueue.add(temp.getPacket());
                temp.setAckStatus(false);
                m_DataQueue.add(temp);
            }
        }
    }

    public static void write(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt)
    {
        if (writingData == false && (m_WriteQueue.size() > 0))
        {
            writingData = true;
            WriteTimer task = new WriteTimer(characteristic, gatt);
            Timer timer = new Timer(true);
            timer.schedule(task, 0);
        }
    }
}
