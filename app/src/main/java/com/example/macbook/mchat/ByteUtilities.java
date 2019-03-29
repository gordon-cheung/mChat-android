package com.example.macbook.mchat;

public final class ByteUtilities {
    private ByteUtilities() {};

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
