package com.example.macbook.mchat;

public class PacketStatus {
    private boolean m_AckStatus;
    private Packet m_Packet;

    public void setAckStatus(boolean status) {
        m_AckStatus = status;
    }

    public boolean getAckStatus() {
        return m_AckStatus;
    }

    public Packet getPacket() {
        return m_Packet;
    }

    public PacketStatus(Packet packet) {
        m_AckStatus = false;
        m_Packet = packet;
    }
}
