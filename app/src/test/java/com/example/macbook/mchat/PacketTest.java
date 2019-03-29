package com.example.macbook.mchat;

import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class PacketTest {
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

    @Test
    public void MessageToMessageTest() {
        Message msg = new Message();
        msg.setStatus(Message.STATUS_PENDING);
        msg.setBody("Hellos");
        msg.setType(Message.IS_SEND);
        msg.setDataType(Message.TEXT);
        msg.setTimestamp(System.currentTimeMillis());
        msg.setContactId("5878880963");
        msg.setMsgId(1);
        msg.setMsgAckId(1);

        System.out.println("\nSent Message");
        msg.printMessage();

        Packet packet = new Packet(msg);
        System.out.println("\nSent Packet");
        packet.printPacket();

        byte[] bytes = packet.getBytes();

        Packet receivedPacket = new Packet(bytes);
        System.out.println("\nReceived Packet");
        receivedPacket.printPacket();

        Message receivedMessage = new Message(receivedPacket, Message.IS_RECEIVE, Message.STATUS_RECEIVED);
        System.out.println("\nReceived Message");
        receivedMessage.printMessage();

    }
}
