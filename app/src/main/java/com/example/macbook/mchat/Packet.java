package com.example.macbook.mchat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
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

    public static final int MAX_PACKET_SIZE = 244;
    public static final int MAX_CONTENT_SIZE = MAX_PACKET_SIZE - HEADER_LENGTH;


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

        try {
            ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
            for (int i = headerLength; i < packetLength; i++) {
                contentStream.write(pkt[i]);
            }
            this.content = contentStream.toByteArray();
        } catch (ArrayIndexOutOfBoundsException ex) {
            Log.e(TAG, "Packet byte size does not match total length (17 " + this.length);
        }
    }

    public Packet(Message msg) {
        String formattedNumber = msg.getContactId().replaceAll("[^\\d.]", "");
        if (formattedNumber.length() > 10) {
            Log.d(TAG, "Phone number is too long: " + formattedNumber);
            formattedNumber = formattedNumber.substring(formattedNumber.length() - 10);
        }

        this.address = formattedNumber.getBytes();
        this.dataType = (byte)msg.getDataType();

        // TODO  update msg id
        this.msgId = 0x00;

        // TODO  change Timestamp in  message class to use System.currentTimeMillis, store as long
        int dateInSec = (int) (msg.getTimestamp() / 1000);

        this.timestamp = ByteBuffer.allocate(4).putInt(dateInSec).array();

        // Get Epoch Time
        int dateInSec2 = ByteBuffer.wrap(this.timestamp).getInt();
        System.out.println("EPOCH: " + dateInSec2);

        this.content = msg.getBody().getBytes();
        this.length = (byte)(content.length);
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

    // TODO test this
    public static ArrayList<Packet> constructPackets(Message msg) throws IOException {
        ArrayList<Packet> packets = new ArrayList<>();
        if (msg.getDataType() == Message.PICTURE) {
            try {
                Uri imageUri = Uri.fromFile(new File(msg.getBody()));
                Bitmap image = MediaStore.Images.Media.getBitmap(MChatApplication.getAppContext().getContentResolver(), imageUri);

                // Apply image compression algorithm here

                int size = image.getByteCount();
                ByteBuffer byteBuffer = ByteBuffer.allocate(size);
                image.copyPixelsToBuffer(byteBuffer);
                byte[] byteArray = byteBuffer.array();

                packets.addAll(encodeImage(msg, byteArray));

            } catch (IOException ex) {
                Log.e(TAG, ex.getMessage());
                throw ex;
            }
        }

        // TODO remove this after testing
        for (Packet p : packets) {
            p.printPacket();
        }

        return packets;
    }

    public static ArrayList<Packet> encodeImage(Message msg, byte[] image) {
        ArrayList<Packet> packets = new ArrayList<>();
        int size = image.length;
        for (int i = 0; i < size; i += MAX_CONTENT_SIZE) {
            if (i + Packet.MAX_CONTENT_SIZE < size) {
                byte[] buffer = Arrays.copyOfRange(image, i, i + Packet.MAX_CONTENT_SIZE);
                packets.add(new Packet(msg, buffer));
            }
            else {
                byte[] buffer = Arrays.copyOfRange(image, i, size);
                packets.add(new Packet(msg, buffer));
            }
        }

        return packets;
    }
}
