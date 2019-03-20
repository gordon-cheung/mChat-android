package com.example.macbook.mchat;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import java.util.Queue;
import java.util.LinkedList;
import java.util.Timer;

public class PacketQueue {

    private static Queue<Packet> m_NewMsgQueue = new LinkedList<>();
    private static Queue<Packet> m_FailedMsgQueue = new LinkedList<>();
    static boolean writingData = false;

    public static Queue<Packet> getNewPacketQueue()
    {
        return m_NewMsgQueue;
    }

    public static Queue<Packet> getFailedPacketQueue()
    {
        return m_FailedMsgQueue;
    }

    public static void writeNewPacket(Packet packet)
    {
        m_NewMsgQueue.add(packet);
    }

    public static void writeFailedPacket(Packet packet)
    {
        m_FailedMsgQueue.add(packet);
    }

    public static void write(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt)
    {
        if (writingData == false && m_NewMsgQueue.size() > 0 && m_FailedMsgQueue.size() > 0)
        {
            writingData = true;
            WriteTimer task = new WriteTimer(characteristic, gatt, m_FailedMsgQueue.size());
            Timer timer = new Timer(true);
            timer.schedule(task, WriteTimer.CurrentInterval);
        }
    }

}
