package com.example.macbook.mchat;

public class Contact {
    private String name;
    private String phoneNumber;
    private String image;

    public Contact(String name, String phoneNumber, String image) {
        this.name = name;
        this.phoneNumber = phoneNumber;
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
