package com.example.macbook.mchat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.*;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
public class BluetoothServiceTest {
    // Put this code in activity test?? or maybe use androidTest
    @Test
    public void ImageConstructionTest2() {
//        BluetoothService bs = new BluetoothService();
//        try {
//
//
////            InputStream in = this.getClass().getClassLoader().getResourceAsStream("test_image1.jpg");
////            String contentType = URLConnection.guessContentTypeFromStream(in);
////            System.out.println(contentType);
////
////            ByteArrayOutputStream os = new ByteArrayOutputStream();
////
////            byte[] buffer = new byte[1024];
////            int len;
////
////            // read bytes from the input stream and store them in buffer
////            while ((len = in.read(buffer)) != -1) {
////                // write bytes from the buffer into output stream
////                os.write(buffer, 0, len);
////            }
//
//            // TODO add code below to t est function to test
//            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test_image1);
//            ByteArrayOutputStream os = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
//
//            Message message = new Message("Some Image", "5551234567", Message.IS_SEND, Message.PICTURE, 0);
//            ArrayList<Packet> sentImagePackets = Packet.encodeImage(message, os.toByteArray());
//            for (Packet p : sentImagePackets) {
//                bs.storeImagePacket(p);
//                ArrayList<Packet> image = bs.detectImageReceived();
//                if (image != null) {
//                    System.out.println("Image detected");
//                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//                    for (Packet p2: image) {
//                        try {
//                            outputStream.write(p2.getContent());
//                        } catch (IOException ex) {
//
//                        }
//                    }
//
//                    try {
//                        Bitmap bitmap = BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.toByteArray().length);
//                        String contentType2 = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(outputStream.toByteArray()));
//                        System.out.println(contentType2);
//                    } catch(IOException ex) {
//                        System.out.println(ex.getMessage());
//                    }
//                }
//            }
//        } catch (IOException ex) {
//
//        }


//        Bitmap bitmap = BitmapFactory.decodeByteArray(inputStream.)
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
//
//        MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "test_image1.jpg", "Test");
    }

    @Test
    public void ImageConstructionTest() {
        int currentTime = (int) (System.currentTimeMillis() / 1000);
        String phoneNumber = "5555555555";
        byte[] address = phoneNumber.getBytes();
        byte[] dataType = new byte[] {0x02};
        byte[] dataTypeImageStart = new byte[] {0xA};
        byte[] dataTypeImageEnd = new byte[] { 0xB };
        byte[] timestampByte = ByteBuffer.allocate(4).putInt(currentTime).array();

        // *** IMAGE 1 ****
        // Packet 0
        byte[] msgId = new byte[] {0x00, 0x00};
        byte[] contentByte = { 0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0x10 };
        int size = contentByte.length;
        byte[] sizeByte = new byte[] {(byte)size};
        byte[] packet0 = getByteArray(sizeByte, address, dataTypeImageStart, msgId, timestampByte, contentByte);

        // Packet 1
        byte[] msgId1 = new byte[] {0x00, 0x01};
        byte[] contentByte1 = { 0xA, 0xB, 0xC, 0xD, 0xE, 0xF, 0x10, 0x11, 0x12, 0x13, 0x14 };
        int size1 = contentByte.length;
        byte[] sizeByte1 = new byte[] {(byte)size1};
        byte[] packet1 = getByteArray(sizeByte1, address, dataType, msgId1, timestampByte, contentByte1);

        // Packet 2
        byte[] msgId2 = new byte[] {0x00, 0x02};
        byte[] contentByte2 = { 0xA, 0xB, 0xC, 0xD, 0xE, 0xF, 0x10, 0x11, 0x12, 0x13, 0x14 };
        int size2 = contentByte.length;
        byte[] sizeByte2 = new byte[] {(byte)size2};
        byte[] packet2 = getByteArray(sizeByte2, address, dataTypeImageEnd, msgId2, timestampByte, contentByte2);

        // *** Image 2 ***
        // Packet 3
        byte[] msgId3 = new byte[] {0x00, 0x03};
        byte[] contentByte3 = { 0xA, 0xB, 0xC, 0xD, 0xE, 0xF, 0x10, 0x11, 0x12, 0x13, 0x14 };
        int size3 = contentByte.length;
        byte[] sizeByte3 = new byte[] {(byte)size3};
        byte[] packet3 = getByteArray(sizeByte3, address, dataTypeImageStart, msgId3, timestampByte, contentByte3);

        // Packet 4
        byte[] msgId4 = new byte[] {0x00, 0x04};
        byte[] contentByte4 = { 0xA, 0xB, 0xC, 0xD, 0xE, 0xF, 0x10, 0x11, 0x12, 0x13, 0x14 };
        int size4 = contentByte.length;
        byte[] sizeByte4 = new byte[] {(byte)size4};
        byte[] packet4 = getByteArray(sizeByte4, address, dataTypeImageEnd, msgId4, timestampByte, contentByte4);


        BluetoothService bs = new BluetoothService();
        bs.receive(packet0);
        bs.receive(packet2);
        bs.receive(packet1);
        bs.receive(packet4);
        bs.receive(packet3);

        ArrayList<Packet> buffer = bs.getImageBuffer();
        for (Packet p : buffer) {
            System.out.println(p.getMsgId());
        }

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("test_image1.jpg");
        try {
            String contentType = URLConnection.guessContentTypeFromStream(inputStream);
            System.out.println(contentType);
        } catch(IOException ex) {

        }

        InputStream inputStream2 = this.getClass().getClassLoader().getResourceAsStream("test_image2.png");
        try {
            String contentType = URLConnection.guessContentTypeFromStream(inputStream2);
            System.out.println(contentType);
        } catch(IOException ex) {

        }
    }

    private byte[] getByteArray(byte[] length, byte[] address, byte[] dataType, byte[] msgId, byte[] timestamp, byte[] content) {
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
}
