package com.example.macbook.mchat;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.util.Queue;
import java.util.LinkedList;
import java.util.Timer;

public class TransmissionManager {

    private static Queue<Packet> m_PendingQueue = new LinkedList<>();
    private static Queue<Packet> m_SendingQueue = new LinkedList<>();
    private final static String TAG = PacketTimer.class.getSimpleName();
    static boolean waitingAck = false;
    static Timer m_Timer;

    public static void queuedWrite(Packet packet, BluetoothGattCharacteristic characteristic, BluetoothGatt gatt)
    {
        m_PendingQueue.add(packet);
        write(characteristic, gatt);
    }

    public static void ackReceived(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt) {
        if (m_PendingQueue.size() == 0) {
            Log.d(TAG, "Error! ACK received but pending queue is empty!");
        }
        else {
            Packet packet = m_PendingQueue.remove();
            m_SendingQueue.add(packet);
            waitingAck = false;
            write(characteristic, gatt);
        }
    }

    public static void nackReceived(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt) {
        if (m_PendingQueue.size() == 0) {
            Log.d(TAG, "Error! NACK received but pending queue is empty!");
        }
        else {
            write(characteristic, gatt);
        }
    }

    public static void txSuccess() {
        if (m_SendingQueue.size() == 0) {
            Log.d(TAG, "Error! TX received but sending queue is empty!");
        }
        else {
            m_SendingQueue.remove();
        }
    }

    public static void txFailure() {
        if (m_SendingQueue.size() == 0) {
            Log.d(TAG, "Error! TX received but sending queue is empty!");
        }
        else {
            Packet packet = m_SendingQueue.remove();
            m_PendingQueue.add(packet);
        }
    }

    static boolean writeCharacteristic(byte[] data, BluetoothGattCharacteristic characteristic, BluetoothGatt gatt)
    {
        boolean success = false;
        try {
            characteristic.setValue(data);
            Log.d(TAG, "WriteCharacteristic(" + characteristic.getUuid() + ") Value: " + data);
            success = gatt.writeCharacteristic(characteristic);

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
