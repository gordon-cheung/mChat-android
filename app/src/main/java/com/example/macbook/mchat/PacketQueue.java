package com.example.macbook.mchat;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import java.util.Queue;
import java.util.LinkedList;
import java.util.Timer;

public class PacketQueue {

    private static Queue<Packet> m_NewMsgQueue = new LinkedList<>();
    static boolean writingData = false;

    public static Queue<Packet> getNewPacketQueue()
    {
        return m_NewMsgQueue;
    }

    public static void writeNewPacket(Packet packet)
    {
        m_NewMsgQueue.add(packet);
    }

    public static void write(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt)
    {
        if (writingData == false && (m_NewMsgQueue.size() > 0))
        {
            writingData = true;
            WriteTimer task = new WriteTimer(characteristic, gatt);
            Timer timer = new Timer(true);
            timer.schedule(task, 0);
        }
    }

}
