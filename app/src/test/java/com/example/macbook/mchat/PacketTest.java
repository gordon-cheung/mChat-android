package com.example.macbook.mchat;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class PacketTest {
    @Test
    public void messageToPacketTest() {
        Message msg = new Message("I am going to see coffee girl", "555-555-5555", 1, 1);
        Packet packet = new Packet(msg);
        packet.printPacket();
    }

    @Test
    public void bytesToPacketTest() {
        String content = "Say Hello to My Little Friend";
        int currentTime = (int) (System.currentTimeMillis() / 1000);
        String phoneNumber = "5555555555";

        byte[] init = new byte[] {(byte)0xA0, (byte)0xA1};
        byte[] address = phoneNumber.getBytes();
        byte[] dataType = new byte[] {0x01};
        byte[] msgId = new byte[] {0x01};
        byte[] timestampByte = ByteBuffer.allocate(4).putInt(currentTime).array();
        byte[] contentByte = content.getBytes();
        byte[] checksum = new byte[] {(byte)0xFF, (byte)0xFF};
        byte[] sig = new byte[] {(byte)0xB0, (byte)0xB1};

        int size = init.length + address.length + dataType.length + msgId.length + timestampByte.length + contentByte.length + checksum.length + sig.length;
        byte[] sizeByte = new byte[] {(byte)size};

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(init);
            outputStream.write(sizeByte);
            outputStream.write(address);
            outputStream.write(dataType);
            outputStream.write(msgId);
            outputStream.write(timestampByte);
            outputStream.write(contentByte);
            outputStream.write(checksum);
            outputStream.write(sig);
        } catch(Exception ex) {

        }

        Packet packet = new Packet(outputStream.toByteArray());
        packet.printPacket();
    }
}
