package com.example.macbook.mchat;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Date;

// TODO create inherited class for ack messages or chat messages?
@Entity(tableName = "messages")
public class Message implements Serializable {
    // TYPE
    public static final int IS_SYSTEM = 0; // RENAME this to something else
    public static final int IS_SEND = 1;
    public static final int IS_RECEIVE = 2;

    // STATUS
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_SENT = 1;
    public static final int STATUS_RECEIVED = 2;

    // DATA_TYPE
    //    #define MLINK_STATE_INIT 0
    //    #define MLINK_STATE_TEXT 1
    //    #define MLINK_STATE_PICTURE 2
    //    #define MLINK_STATE_STARTUP_COMPLETE 3
    //    #define MLINK_STATE_NOT_READY 4
    //    #define MLINK_STATE_IN_PROGRESS 5
    //    #define MLINK_STATE_SENT 6
    //    #define MLINK_STATE_ERROR 7

    public static final int STATE_INIT = 0;
    public static final int TEXT = 1;
    public static final int PICTURE = 2;
    public static final int STATE_IN_PROGRESS = 5;

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

    // For System Packets
    public Message(String contact, int dType) {
        this.contactId = contact;
        this.type = 0;
        this.dataType = dType;
        this.status = 0;
        this.body = "";
        this.timestamp = System.currentTimeMillis();
    }

    // Obsolete
    public Message(String msgBody, String contact, final int msgType) {
        body = msgBody;
        contactId = contact;
        type = msgType;
        dataType = 1; //
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
