package com.example.macbook.mchat;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "messages")
public class Message implements Serializable {
    public static final int MESSAGE_SENT = 1;
    public static final int MESSAGE_RECEIVED = 2;

    public static final int TEXT = 1;
    public static final int PICTURE = 2;

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "contact_id")
    private String contactId;

    @ColumnInfo(name = "message_type")
    private int messageType;

    @ColumnInfo(name = "data_type")
    private int dataType;

    @ColumnInfo(name = "message_body")
    private String messageBody;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    // TODO add read and unread column

    public Message() {}

    // Obsolete
    public Message(String body, String contact, final int msgType) {
        messageBody = body;
        contactId = contact;
        messageType = msgType;
        dataType = 0; //
        timestamp = System.currentTimeMillis();
    }

    public Message(String body, String contact, final int msgType, final int dType) {
        messageBody = body;
        contactId = contact;
        messageType = msgType;
        dataType = dType;
        timestamp = System.currentTimeMillis();
    }

    public Message(String body, String contact, final int msgType, final int dType, final long time) {
        messageBody = body;
        contactId = contact;
        messageType = msgType;
        dataType = dType;
        timestamp = time;
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

    public long getTimestamp() { return timestamp; }

    public int getDataType() { return dataType; }

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

    public void setTimestamp(long time) {
        this.timestamp = time;
    }

    public void setDataType(int type) {
        this.dataType = type;
    }

    public void printMessage() {
        System.out.println("ContactId: " + contactId);
        System.out.println("MessageType: " + messageType);
        System.out.println("DataType: " + dataType);
        System.out.println("MessageBody: " + messageBody);
        System.out.println("Timestamp: " + timestamp);
    }

}
