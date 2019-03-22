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

    private static final int PACKET_LENGTH_INDEX = 0;
    private static final int PACKET_ADDRESS_INDEX = 1;
    private static final int PACKET_ADDRESS_LENGTH = 10;
    private static final int PACKET_DATA_TYPE_INDEX = 11;
    private static final int PACKET_MSG_ID_INDEX = 12;
    private static final int PACKET_MSG_ID_LENGTH = 2;
    private static final int PACKET_TIMESTAMP_INDEX = 14;
    private static final int PACKET_TIMESTAMP_LENGTH = 4;

    private static final int PACKET_HEADER_LENGTH = 18;

    private static final int PACKET_MAX_SIZE = 244;
    public static final int PACKET_MAX_CONTENT_SIZE = PACKET_MAX_SIZE - PACKET_HEADER_LENGTH;


    private byte length;
    private byte[] address;
    private byte dataType;
    private byte[] msgId;
    private byte[] timestamp;
    private byte[] content;

    public Packet(byte[] pkt) {
        this.length = pkt[PACKET_LENGTH_INDEX];
        this.address = new byte[PACKET_ADDRESS_LENGTH];
        System.arraycopy(pkt, PACKET_ADDRESS_INDEX, this.address, 0, PACKET_ADDRESS_LENGTH);
        this.dataType = pkt[PACKET_DATA_TYPE_INDEX];
        this.msgId = new byte[PACKET_MSG_ID_LENGTH];
        System.arraycopy(pkt, PACKET_MSG_ID_INDEX, this.msgId, 0, PACKET_MSG_ID_LENGTH);
        this.timestamp = new byte[PACKET_TIMESTAMP_LENGTH];
        System.arraycopy(pkt, PACKET_TIMESTAMP_INDEX, this.timestamp, 0, PACKET_TIMESTAMP_LENGTH);

        int packetLength = PACKET_HEADER_LENGTH + (int)this.length;

        try {
            ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
            for (int i = PACKET_HEADER_LENGTH; i < packetLength; i++) {
                contentStream.write(pkt[i]);
            }
            this.content = contentStream.toByteArray();
        } catch (ArrayIndexOutOfBoundsException ex) {
            Log.e(TAG, "Packet byte size does not match total length (18 " + this.length);
        }
    }

    public Packet(Message msg) {
        String formattedNumber = Contact.formatPhoneNumber(msg.getContactId());

        this.address = formattedNumber.getBytes();
        this.dataType = (byte) msg.getDataType();
        this.msgId = new byte[2];
        this.msgId[1] =  (byte)(msg.getMsgId() & 0xFF);
        this.msgId[0] = (byte)((msg.getMsgId() >> 8) & 0xFF);
        int dateInSec = (int) (msg.getTimestamp() / 1000);
        this.timestamp = ByteBuffer.allocate(4).putInt(dateInSec).array();
        setContent(msg.getBody().getBytes());
    }

    public Packet(Message msg, byte[] content) {
        this(msg);

        setContent(content);
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
        } catch(IOException ex) {
            Log.e(TAG, ex.getMessage());
        }
        return outputStream.toByteArray();
    }

    // TODO throw exception if packet length is too large
    public void setContent(byte[] content) {
        this.content = content;
        this.length = (byte)content.length;
        if (this.length > PACKET_MAX_CONTENT_SIZE) {
            Log.e("TAG", "Content length is too large");
        }
    }

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
        for (int i = 0; i < size; i += PACKET_MAX_CONTENT_SIZE) {
            if (i + Packet.PACKET_MAX_CONTENT_SIZE < size) {
                byte[] buffer = Arrays.copyOfRange(image, i, i + Packet.PACKET_MAX_CONTENT_SIZE);
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
