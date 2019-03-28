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
    private final static String TAG = TransmissionManager.class.getSimpleName();
    static volatile boolean waitingAck = false;
    static Timer m_Timer = null;

    public static void queuedWrite(Packet packet, BluetoothGattCharacteristic characteristic, BluetoothGatt gatt)
    {
        Log.d(TAG, "Queued Write, msgId:" + new String(packet.getContent()));
        m_PendingQueue.add(packet);
        write(characteristic, gatt);
    }

    public static void ackReceived(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt) {
        Log.d(TAG, "ACK Received, removing msgId from pending q: " + new String(m_PendingQueue.peek().getContent()));
        if (m_Timer != null)
            m_Timer.cancel();
        PacketTimer.TIMEOUT_INTERVAL = Math.max(PacketTimer.MIN_TIMEOUT_INTERVAL, (PacketTimer.TIMEOUT_INTERVAL / 2));
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
        Log.d(TAG, "NACK Received, retrying msgId from pending q: " + new String(m_PendingQueue.peek().getContent()));
        if (m_Timer != null)
            m_Timer.cancel();
        PacketTimer.TIMEOUT_INTERVAL = Math.min(PacketTimer.MAX_TIMEOUT_INTERVAL, (PacketTimer.TIMEOUT_INTERVAL * 2));
        if (m_PendingQueue.size() == 0) {
            Log.d(TAG, "Error! NACK received but pending queue is empty!");
        }
        else {
//            waitingAck = false;
//            write(characteristic, gatt);
            PacketTimer task = new PacketTimer(characteristic, gatt);
            m_Timer = new Timer(true);
            m_Timer.schedule(task, PacketTimer.TIMEOUT_INTERVAL);
        }
    }

    public static void txSuccess() {
        if (m_SendingQueue.size() == 0) {
            Log.d(TAG, "Error! TX received but sending queue is empty!");
        }
        else {
            Log.d(TAG, "TX SUCCESS, removing msgId from sending q: " + new String(m_SendingQueue.peek().getContent()));
            m_SendingQueue.remove();
        }
    }

    public static void txFailure() {
        Log.d(TAG, "TX FAILURE, removing msgId from sending q: " + new String(m_SendingQueue.peek().getContent()));
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
            //Log.d(TAG, "WriteCharacteristic(" + characteristic.getUuid() + ") Value: " + data);
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
            if (m_Timer != null)
            {
                m_Timer.cancel();
            }
            m_Timer = new Timer(true);
            m_Timer.schedule(task, PacketTimer.TIMEOUT_INTERVAL);
        }
    }
}
