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
        String formattedNumber = formatPhoneNumber(phoneNumber);
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
            contact = contact.replaceAll("[^\\d.]", "");
        }
        if (contact.length() > 10) {
            return contact.substring(contact.length() - 10);
        }
        return contact;
    }
}
