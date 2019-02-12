package com.example.macbook.mchat;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "messages")
public class Message {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "sender_id")
    private String senderId;

    @ColumnInfo(name = "receiver_id")
    private String receiverId;

    @ColumnInfo(name = "message_body")
    private String messageBody;

    public Message() {}

    public Message(String body, String sender, String receiver) {
        messageBody = body;
        senderId = sender;
        receiverId = receiver;
    }

    public int getId() {
        return id;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public String getReceiverId() { return receiverId; }

    public String getSenderId() { return senderId; }

    public void setId(int id) {
        this.id = id;
    }

    public void setMessageBody(String msg) {
        this.messageBody = msg;
    }

    public void setReceiverId(String id) {
        this.receiverId = id;
    }

    public void setSenderId(String id) {
        this.senderId = id;
    }
}
