package com.example.macbook.mchat;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Calendar;

public class Packet {
    private static final String TAG = SelectDeviceActivity.class.getSimpleName();

    // TODO update packet constructor to fit this
    private static final int LENGTH_INDEX = 0;
    //private static final int ADDRESS_INDEX = 1, 2, 3, 4, 5, 6, 7, 8, 9, 10;
    private static final int DATA_TYPE_INDEX = 11;
    private static final int MSG_ID_INDEX =12;
    //private static final int TIMESTAMP_INDEX = 13,14,15,16;

    public static final int HEADER_LENGTH = 17;

    public static final int MAX_CONTENT_SIZE = 244;


    private byte length;
    private byte[] address;
    private byte dataType;
    private byte[] msgId;
    private byte[] timestamp;
    private byte[] content;

    public Packet(byte[] pkt) {
        this.length = pkt[0];
        this.address = new byte[] {pkt[1], pkt[2], pkt[3], pkt[4], pkt[5], pkt[6], pkt[7], pkt[8], pkt[9], pkt[10]};
        this.dataType = pkt[11];
        this.msgId = new byte[] {pkt[12], pkt[13]};
        this.timestamp = new byte[] {pkt[14], pkt[15], pkt[16], pkt[17]};

        int headerLength = 18;
        int packetLength = headerLength + (int)this.length;

        try {
            ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
            for (int i = headerLength; i < packetLength; i++) {
                contentStream.write(pkt[i]);
            }
            this.content = contentStream.toByteArray();
        } catch (ArrayIndexOutOfBoundsException ex) {
            Log.e(TAG, "Packet byte size does not match total length (18 " + this.length);
        }
    }

    public Packet(Message msg) {
        String formattedNumber = msg.getContactId().replaceAll("[^\\d.]", "");
        if (formattedNumber.length() > 10) {
            Log.d(TAG, "Phone number is too long: " + formattedNumber);
            formattedNumber = formattedNumber.substring(formattedNumber.length() - 10);
        }

        this.address = formattedNumber.getBytes();
        this.dataType = (byte) msg.getDataType();
        this.msgId = new byte[2];
        this.msgId[1] =  (byte)(msg.getMsgId() & 0xFF);
        this.msgId[0] = (byte)((msg.getMsgId() >> 8) & 0xFF);

        // TODO  change Timestamp in  message class to use System.currentTimeMillis, store as long
        int dateInSec = (int) (msg.getTimestamp() / 1000);

        this.timestamp = ByteBuffer.allocate(4).putInt(dateInSec).array();
        this.content = msg.getBody().getBytes();

        this.length = (byte)(content.length);

        // Get Epoch Time
        int dateInSec2 = ByteBuffer.wrap(this.timestamp).getInt();
        System.out.println("EPOCH: " + dateInSec2);
    }

    public Packet(Message msg, byte[] content) {
        this(msg);

        this.content = content;
        this.length = (byte)content.length;
    }

    public void printPacket() {
        System.out.println("Packet: " + ByteUtilities.getByteArrayInHexString(getBytes()));

        System.out.println("Length: " + ByteUtilities.getByteInHexString(length));
        System.out.println("Address: " + ByteUtilities.getByteArrayInHexString((address)));
        System.out.println("DataType: " + ByteUtilities.getByteInHexString(dataType));
        System.out.println("MsgId: " + ByteUtilities.getByteArrayInHexString(msgId));
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
    public int getMsgId() {
        return ((this.msgId[0] & 0xff) << 8) | (this.msgId[1] & 0xff);
    }
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

    public void setDataType(byte dataType) {
        this.dataType = dataType;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
