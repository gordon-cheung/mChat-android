package com.example.macbook.mchat;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "messages")
public class Message implements Serializable {
    // TYPE
    public static final int IS_SEND = 1;
    public static final int IS_RECEIVE = 2;

    // STATUS
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_SENT = 1;
    public static final int STATUS_RECEIVED = 2;

    // DATA_TYPE
    public static final int TEXT = 1;
    public static final int PICTURE = 2;

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "contact_id")
    private String contactId;

    @ColumnInfo(name = "type")
    private int type;

    @ColumnInfo(name = "data_type")
    private int dataType;

    @ColumnInfo(name = "status")
    private int status;

    @ColumnInfo(name = "body")
    private String body;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    // TODO add read and unread column

    public Message() {}

    // Obsolete
    public Message(String msgBody, String contact, final int msgType) {
        body = msgBody;
        contactId = contact;
        type = msgType;
        dataType = 0; //
        timestamp = System.currentTimeMillis();
        status = STATUS_PENDING;
    }

    public Message(String msgBody, String contact, final int msgType, final int dType) {
        body = msgBody;
        contactId = contact;
        type = msgType;
        dataType = dType;
        timestamp = System.currentTimeMillis();
        status = STATUS_PENDING;
    }

    public Message(String msgBody, String contact, final int msgType, final int dType, final long time) {
        body = msgBody;
        contactId = contact;
        type = msgType;
        dataType = dType;
        timestamp = time;
        status = STATUS_PENDING;
    }

    public int getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    public String getContactId() { return contactId; }

    public int getType() { return type; }

    public long getTimestamp() { return timestamp; }

    public int getDataType() { return dataType; }

    public int getStatus() { return status; }

    public void setId(int id) {
        this.id = id;
    }

    public void setContactId(String id) { this.contactId = id; }

    public void setBody(String msg) {
        this.body = msg;
    }

    public void setType(int type) { this.type = type; }

    public void setTimestamp(long time) {
        this.timestamp = time;
    }

    public void setDataType(int type) {
        this.dataType = type;
    }

    public void setStatus(int status) { this.status = status; }

    public void printMessage() {
        System.out.println("ContactId: " + contactId);
        System.out.println("Type: " + type);
        System.out.println("DataType: " + dataType);
        System.out.println("Body: " + body);
        System.out.println("Timestamp: " + timestamp);
    }

}
