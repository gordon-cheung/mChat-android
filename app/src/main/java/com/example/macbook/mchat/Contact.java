package com.example.macbook.mchat;

import android.util.Log;

import java.io.Serializable;

public class Contact implements Serializable {
    private String TAG = Contact.class.getSimpleName();
    private String name;
    private String phoneNumber;
    private String image;

    public Contact(String name, String phoneNumber, String image) {
        this.name = name;
        String formattedNumber = phoneNumber.replaceAll("[^\\d.]", "");
        if (formattedNumber.length() > 10) {
            Log.d(TAG, "Phone number is too long: " + formattedNumber);
            formattedNumber = formattedNumber.substring(formattedNumber.length() - 10);
        }
        this.phoneNumber = formattedNumber;
        this.image = image;
    }

    public String getName() {
        return this.name;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public String getImage() {
        return this.image;
    }

    public static String formatPhoneNumber(String contact) {
        if (contact != null) {
            contact = contact.replaceAll("[+\\- \\(\\)]", "");
        }
        return contact;
    }
}
