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
    static volatile boolean waitingTX = false;
    static Timer m_TimeoutTimer = null;

    public static void queuedWrite(Packet packet, BluetoothGattCharacteristic characteristic, BluetoothGatt gatt)
    {
        Log.d(TAG, "Queued Write, msgId: " + packet.getMsgId());
        m_PendingQueue.add(packet);
        write(characteristic, gatt);
    }

    public static void ackReceived(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt) {
        Log.d(TAG, "ACK Received, removing msgId from pending q, msgId: " + m_PendingQueue.peek().getMsgId());
        if (m_TimeoutTimer != null)
            m_TimeoutTimer.cancel();
        PacketTimer.TIMEOUT_INTERVAL = Math.max(PacketTimer.MIN_TIMEOUT_INTERVAL, (PacketTimer.TIMEOUT_INTERVAL / 2));
        if (m_PendingQueue.size() == 0) {
            Log.d(TAG, "Error! ACK received but pending queue is empty!");
        }
        else {
            Packet packet = m_PendingQueue.remove();
            m_SendingQueue.add(packet);
        }
    }

    public static void nackReceived(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt) {
        Log.d(TAG, "NACK Received, retrying msgId from pending q, msgId: " + m_PendingQueue.peek().getMsgId());
        if (m_TimeoutTimer != null)
            m_TimeoutTimer.cancel();
        PacketTimer.TIMEOUT_INTERVAL = Math.min(PacketTimer.MAX_TIMEOUT_INTERVAL, (PacketTimer.TIMEOUT_INTERVAL * 2));
        if (m_PendingQueue.size() == 0) {
            Log.d(TAG, "Error! NACK received but pending queue is empty!");
        }
        else {
            //waitingTX = false;
            //write(characteristic, gatt);
            PacketTimer task = new PacketTimer(characteristic, gatt);
            m_TimeoutTimer = new Timer(true);
            m_TimeoutTimer.schedule(task, PacketTimer.TIMEOUT_INTERVAL);
        }
    }

    public static Packet txSuccess(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt) {
        if (m_SendingQueue.size() == 0) {
            Log.d(TAG, "Error! TX received but sending queue is empty!");
            return null;
        }
        else {
            Log.d(TAG, "TX SUCCESS, removing msgId removing msgId from sending q, msgId: " + m_SendingQueue.peek().getMsgId());
            Packet packet = m_SendingQueue.remove();
            waitingTX = false;
            write(characteristic, gatt);
            return packet;
        }
    }

    public static void txFailure(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt) {
        Log.d(TAG, "TX FAILURE, removing msgId from sending q, msgId: " + m_SendingQueue.peek().getMsgId());
        if (m_SendingQueue.size() == 0) {
            Log.d(TAG, "Error! TX received but sending queue is empty!");
        }
        else {
            Packet packet = m_SendingQueue.remove();
            m_PendingQueue.add(packet);
            waitingTX = false;
            write(characteristic, gatt);
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
        if ((waitingTX == false) && (m_PendingQueue.size() > 0))
        {
            waitingTX = true;
            writeCharacteristic(m_PendingQueue.peek().getBytes(), characteristic, gatt);
            PacketTimer task = new PacketTimer(characteristic, gatt);
            if (m_TimeoutTimer != null)
            {
                m_TimeoutTimer.cancel();
            }
            m_TimeoutTimer = new Timer(true);
            m_TimeoutTimer.schedule(task, PacketTimer.TIMEOUT_INTERVAL);
        }
    }
}
