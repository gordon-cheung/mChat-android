package com.example.macbook.mchat;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Calendar;

public class Packet {
    private static final String TAG = SelectDeviceActivity.class.getSimpleName();
    private final static byte[] header = new byte[] {(byte)0xA0, (byte)0xA1};
    private byte length;
    private byte[] address;
    private byte dataType;
    private byte msgId;
    private byte[] timestamp;
    private byte[] content;
    private byte[] checksum;
    private final static byte[] end =  new byte[] {(byte)0xB0, (byte) 0xB1};

    public Packet(byte[] pkt) {
        this.length = pkt[2];
        this.address = new byte[] {pkt[3], pkt[4], pkt[5], pkt[6], pkt[7], pkt[8], pkt[9], pkt[10], pkt[11], pkt[12]};
        this.dataType = pkt[13];
        this.msgId = pkt[14];
        this.timestamp = new byte[] {pkt[15], pkt[16], pkt[17], pkt[18]};

        int headerLength = 19;
        int packetLength = (int)length;
        int endLength = 4;

        ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
        for (int i = headerLength; i <= packetLength - endLength; i++) {
            contentStream.write(pkt[i]);
        }

        this.content = contentStream.toByteArray();

        this.checksum = new byte[] { pkt[packetLength - 4], pkt[packetLength - 3]};
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

        this.timestamp = ByteBuffer.allocate(4).putInt(dateInSec).array();;
        this.content = msg.getMessageBody().getBytes();
        this.checksum = new byte[] { (byte)0xFF, (byte)0xFF};

        this.length = (byte)(header.length + address.length + 1 + 1 + 1 + timestamp.length + content.length + checksum.length + end.length);

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
        System.out.println("Checksum: " + ByteUtilities.getByteArrayInHexString((checksum)));
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
    public byte[] getChecksum() {
        return this.checksum;
    }
    public byte[] getTimestamp() {
        return this.timestamp;
    }

    public byte[] getBytes() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(header);
            outputStream.write(length);
            outputStream.write(address);
            outputStream.write(dataType);
            outputStream.write(msgId);
            outputStream.write(timestamp);
            outputStream.write(content);
            outputStream.write(checksum);
            outputStream.write(end);
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
        Message message = new Message(messageBody, contactId, Message.MESSAGE_RECEIVED, dataType, timestamp);
        System.out.println("\nMessage");
        message.printMessage();

        return message;
    }
}
