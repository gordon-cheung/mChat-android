package com.example.macbook.mchat;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "messages")
public class Message {
    public static final int MESSAGE_SENT = 1;
    public static final int MESSAGE_RECEIVED = 2;

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "contact_id")
    private String contactId;

    @ColumnInfo(name = "message_type")
    private int messageType;

//    @ColumnInfo(name = "sender_id")
//    private String senderId;
//
//    @ColumnInfo(name = "receiver_id")
//    private String receiverId;

    @ColumnInfo(name = "message_body")
    private String messageBody;

    @ColumnInfo(name = "timestamp")
    private Date timestamp;

    public Message() {}

    public Message(String body, String contact, final int type) {
        messageBody = body;
        contactId = contact;
        messageType = type;
        timestamp = new Date();
    }

    public int getId() {
        return id;
    }

    public String getMessageBody() {
        return messageBody;
    }

//    public String getReceiverId() { return receiverId; }
//
//    public String getSenderId() { return senderId; }

    public String getContactId() { return contactId; }

    public int getMessageType() { return messageType; }

    public Date getTimestamp() { return timestamp; }

    public void setId(int id) {
        this.id = id;
    }

    public void setContactId(String id) { this.contactId = id; }

    public void setMessageBody(String msg) {
        this.messageBody = msg;
    }

    public void setMessageType(int type) {
        this.messageType = type;
    }

    public void setTimestamp(Date date) {
        this.timestamp = date;
    }





//    public void setReceiverId(String id) {
//        this.receiverId = id;
//    }
//
//    public void setSenderId(String id) {
//        this.senderId = id;
//    }
}
