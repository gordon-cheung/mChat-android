package com.example.macbook.mchat;

import android.content.Intent;
import android.util.Log;

import java.nio.ByteBuffer;

public final class ByteUtilities {
    private ByteUtilities() {};
    private final static String TAG = ByteUtilities.class.getSimpleName();

    public static String getByteArrayInHexString(byte[] arr) {
        StringBuilder sb = new StringBuilder();
        for (byte b : arr) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }

    public static String getByteInHexString(byte byt) {
        return String.format("%02X", byt);
    }

}
