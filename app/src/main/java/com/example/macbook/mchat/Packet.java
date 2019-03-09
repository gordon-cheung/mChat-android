package com.example.macbook.mchat;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Calendar;

public class Packet {
    private static final String TAG = SelectDeviceActivity.class.getSimpleName();
    private byte length;
    private byte[] address;
    private byte dataType;
    private byte msgId;
    private byte[] timestamp;
    private byte[] content;

    public Packet(byte[] pkt) {
        this.length = pkt[0];
        this.address = new byte[] {pkt[1], pkt[2], pkt[3], pkt[4], pkt[5], pkt[6], pkt[7], pkt[8], pkt[9], pkt[10]};
        this.dataType = pkt[11];
        this.msgId = pkt[12];
        this.timestamp = new byte[] {pkt[13], pkt[14], pkt[15], pkt[16]};

        int headerLength = 17;
        int packetLength = headerLength + (int)this.length;

        ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
        for (int i = headerLength; i < packetLength; i++) {
            contentStream.write(pkt[i]);
        }
        this.content = contentStream.toByteArray();
    }

    public Packet(Message msg) {
        String formattedNumber = msg.getContactId().replaceAll("[^\\d.]", "");
        if (formattedNumber.length() > 10) {
            Log.d(TAG, "Phone number is too long: " + formattedNumber);
            formattedNumber = formattedNumber.substring(formattedNumber.length() - 10);
        }

        this.address = formattedNumber.getBytes();
        this.dataType = (byte)msg.getDataType();
        this.msgId = 0x00;

        // TODO  change Timestamp in  message class to use System.currentTimeMillis, store as long
        int dateInSec = (int) (msg.getTimestamp() / 1000);

        this.timestamp = ByteBuffer.allocate(4).putInt(dateInSec).array();
        this.content = msg.getBody().getBytes();

        this.length = (byte)(content.length);

        // Get Epoch Time
        int dateInSec2 = ByteBuffer.wrap(this.timestamp).getInt();
        System.out.println("EPOCH: " + dateInSec2);
    }

    public void printPacket() {
        System.out.println("Packet: " + ByteUtilities.getByteArrayInHexString(getBytes()));

        System.out.println("Length: " + ByteUtilities.getByteInHexString(length));
        System.out.println("Address: " + ByteUtilities.getByteArrayInHexString((address)));
        System.out.println("DataType: " + ByteUtilities.getByteInHexString(dataType));
        System.out.println("MsgId: " + ByteUtilities.getByteInHexString(msgId));
        System.out.println("Timestamp: " + ByteUtilities.getByteArrayInHexString((timestamp)));
        System.out.println("Content: " + ByteUtilities.getByteArrayInHexString((content)));
    }

    public byte getLength() {
        return this.length;
    }
    public byte[] getAddress() {
        return this.address;
    }
    public byte getDataType() {
        return this.dataType;
    }
    public byte getMsgId() { return this.msgId; }
    public byte[] getContent() {
        return this.content;
    }
    public byte[] getTimestamp() {
        return this.timestamp;
    }

    public byte[] getBytes() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(length);
            outputStream.write(address);
            outputStream.write(dataType);
            outputStream.write(msgId);
            outputStream.write(timestamp);
            outputStream.write(content);
        } catch(Exception ex) {

        }

        return outputStream.toByteArray();
    }

    public Message getMessage() {
        String messageBody = new String(getContent());
        String contactId = new String(getAddress());
        int dataType = (int)getDataType();
        long timestamp = ByteBuffer.wrap(getTimestamp()).getInt();
        // TODO: Message.MESSAGE_RECEIVED depends
        Message message = new Message(messageBody, contactId, Message.IS_RECEIVE, dataType, timestamp);
        System.out.println("\nMessage");
        message.printMessage();

        return message;
    }
}
