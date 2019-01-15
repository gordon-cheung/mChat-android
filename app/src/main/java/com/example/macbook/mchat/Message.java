package com.example.macbook.mchat;

public class Message {
    private String messageBody;
    private String userName;

    public Message(String body, String user) {
        messageBody = body;
        userName = user;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public String getUser() { return userName; }
}
