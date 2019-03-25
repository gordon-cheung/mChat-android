package com.example.macbook.mchat;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.util.Queue;
import java.util.LinkedList;
import java.util.Timer;

public class TransmissionQueue {

    private static Queue<Packet> m_PendingQueue = new LinkedList<>();
    private static Queue<Packet> m_SendingQueue = new LinkedList<>();
    private final static String TAG = PacketTimer.class.getSimpleName();
    static boolean waitingAck = false;
    static Timer m_Timer;

    public static Queue<Packet> getWriteQueue()
    {
        return m_PendingQueue;
    }

    public static void queuedWrite(Packet packet, BluetoothGattCharacteristic characteristic, BluetoothGatt gatt)
    {
        m_PendingQueue.add(packet);
        write(characteristic, gatt);
    }

    public static void ackReceived() {
        if (m_SendingQueue.size() == 0) {
            Log.d(TAG, "Error! Ack received but data queue is empty!");
        }
        else {
            for (PacketStatus packet : m_SendingQueue) {
                if (packet.getAckStatus() == false) {
                    packet.setAckStatus(true);
                    break;
                }
            }
        }
    }

    public static void nackReceived() {
        if (m_SendingQueue.size() == 0) {
            Log.d(TAG, "Error! Ack received but data queue is empty!");
        }
        else {
            for (int i = 0; i < m_SendingQueue.size(); i++)
            {
                PacketStatus temp = m_SendingQueue.get(i);
                if (temp.getAckStatus() == false) {
                    m_SendingQueue.remove(i);
                }
                m_PendingQueue.add(temp.getPacket());
                m_SendingQueue.add(temp);
            }
        }
    }

    public static void txSuccess() {
        if (m_SendingQueue.size() == 0) {
            Log.d(TAG, "Error! TX received but data queue is empty!");
        }
        else {
            for (int i = 0; i < m_SendingQueue.size(); i++)
            {
                PacketStatus temp = m_SendingQueue.get(i);
                if (temp.getAckStatus() == true) {
                    m_SendingQueue.remove(i);
                }
            }
        }
    }

    public static void txFailure() {
        if (m_SendingQueue.size() == 0) {
            Log.d(TAG, "Error! TX received but data queue is empty!");
        }
        else {
            for (int i = 0; i < m_SendingQueue.size(); i++)
            {
                PacketStatus temp = m_SendingQueue.get(i);
                if (temp.getAckStatus() == true) {
                    m_SendingQueue.remove(i);
                }
                m_PendingQueue.add(temp.getPacket());
                temp.setAckStatus(false);
                m_SendingQueue.add(temp);
            }
        }
    }

    static boolean writeCharacteristic(byte[] data, BluetoothGattCharacteristic characteristic, BluetoothGatt gatt)
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

    static void write(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt)
    {
        if ((waitingAck == false) && (m_PendingQueue.size() > 0))
        {
            waitingAck = true;
            writeCharacteristic(m_PendingQueue.peek().getBytes(), characteristic, gatt);
            PacketTimer task = new PacketTimer(characteristic, gatt);
            m_Timer = new Timer(true);
            m_Timer.schedule(task, PacketTimer.TIMEOUT_INTERVAL);
        }
    }
}
