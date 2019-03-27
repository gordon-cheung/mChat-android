package com.example.macbook.mchat;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class PacketTest {
    @Test
    public void messageToPacketTest() {
        Message msg = new Message("I am going to see coffee girl", "555-555-5555", Message.IS_SEND,Message.TEXT, 0);
        Packet packet = new Packet(msg);
        packet.printPacket();
    }

    @Test
    public void bytesToPacketTest() {
        String content = "Say Hello to My Little Friend";
        int currentTime = (int) (System.currentTimeMillis() / 1000);
        String phoneNumber = "5555555555";

        byte[] address = phoneNumber.getBytes();
        byte[] dataType = new byte[] {0x01};
        byte[] msgId = new byte[] {0x00, 0x01};
        byte[] timestampByte = ByteBuffer.allocate(4).putInt(currentTime).array();
        byte[] contentByte = content.getBytes();

        int size = contentByte.length;
        byte[] sizeByte = new byte[] {(byte)size};

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(sizeByte);
            outputStream.write(address);
            outputStream.write(dataType);
            outputStream.write(msgId);
            outputStream.write(timestampByte);
            outputStream.write(contentByte);
        } catch(Exception ex) {

        }

        Packet packet = new Packet(outputStream.toByteArray());
        packet.printPacket();
    }

    @Test
    public void PictureMessageToPacketsTest() {
        Message msg = new Message("/storage/emulated/0/DCIM/Camera/20190318_224056.jpg", "5551234567", Message.IS_SEND, Message.PICTURE, 0);

        int size = 732;
        byte[] arr = new byte[size];
        for (int i=0; i<732;i++) {
            arr[i] = (byte)i;
        }

        ArrayList<Packet> pkts = Packet.encodeImage(msg, arr);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            for (Packet p : pkts) {
                System.out.println("");
                p.printPacket();
                System.out.println("Length of content: " + p.getContent().length);
                System.out.println("Length of packet: " + p.getBytes().length);
                stream.write(p.getContent());
            }
        } catch(Exception ex) {

        }

        byte[] finalArr = stream.toByteArray();
        System.out.println("");
        System.out.println("Original byte array");
        System.out.println(ByteUtilities.getByteArrayInHexString(arr));
        System.out.println("Reconstructed byte array");
        System.out.println(ByteUtilities.getByteArrayInHexString(finalArr));

        boolean result = Arrays.equals(finalArr, arr);
        System.out.println(result ? "Matches" : "Does not match");
    }
}
