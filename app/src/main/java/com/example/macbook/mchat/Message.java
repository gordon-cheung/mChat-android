package com.example.macbook.mchat;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import java.io.Serializable;
import java.nio.ByteBuffer;

// TODO create inherited class for ack messages or chat messages?
@Entity(tableName = "messages")
public class Message implements Serializable {
    // TYPE
    public static final int IS_SEND = 0;
    public static final int IS_RECEIVE = 1;

    // STATUS
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_SENT = 1;
    public static final int STATUS_RECEIVED = 2;
    public static final int STATUS_FAILED = 3;

    // DATA_TYPE
    public static final int STATE_INIT = 0;
    public static final int TEXT = 1;
    public static final int PICTURE = 2;
    public static final int STARTUP_COMPLETE = 3;
    public static final int NOT_READY = 4;
    public static final int ACK = 5;
    public static final int SENT = 6;
    public static final int ERROR = 7;
    public static final int BUFFER_FULL = 8;
    public static final int TIMEOUT = 9;
    public static final int PICTURE_START = 10;
    public static final int PICTURE_END = 11;
    public static final int NACK = 12;

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

    @ColumnInfo(name = "msg_id")
    private int msgId;

    @ColumnInfo(name = "msg_ack_id")
    private int msgAckId;

    @ColumnInfo(name = "body")
    private String body;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    // TODO add read and unread column
    public Message() {}

    public Message(String msgBody, String contact, final int msgType, final int dType, final long time) {
        body = msgBody;
        contactId = contact;
        type = msgType;
        dataType = dType;
        timestamp = time;
        status = STATUS_PENDING;
        msgId = 0;
        msgAckId = -1;
    }
    public Message(String msgBody, String contact, final int msgType, final int dType, final int messageId) {
        body = msgBody;
        contactId = contact;
        type = msgType;
        dataType = dType;
        timestamp = System.currentTimeMillis();
        status = STATUS_PENDING;
        msgId = messageId;
        msgAckId = -1;
    }

    public Message(String msgBody, String contact, final int msgType, final int dType, final long time, final int stat) {
        body = msgBody;
        contactId = contact;
        type = msgType;
        dataType = dType;
        timestamp = time;
        status = stat;
        msgAckId = -1;
    }

    public Message(Packet packet, int messageType, int msgStatus) {
        body = new String(packet.getContent());
        contactId = new String(packet.getAddress());
        dataType = packet.getDataType();
        timestamp = (long)ByteBuffer.wrap(packet.getTimestamp()).getInt() * 1000;
        type = messageType;
        status = msgStatus;
        msgId = packet.getMsgId();
        msgAckId = -1;
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

    public int getMsgId() { return msgId; }

    public int getMsgAckId() { return msgAckId; }

    public void setId(int id) {
        this.id = id;
    }

    public void setContactId(String id) { this.contactId = id; }

    public void setBody(String msg) {
        this.body = msg;
    }

    public void setType(int type) { this.type = type; }

    public void setTimestamp(long time) { this.timestamp = time; }

    public void setDataType(int type) {
        this.dataType = type;
    }

    public void setStatus(int status) { this.status = status; }

    public void setMsgId(int messsageId) { this.msgId = messsageId; }

    public void setMsgAckId(int ackId) { this.msgAckId = ackId; }

    public void printMessage() {
        System.out.println("ContactId: " + contactId);
        System.out.println("Type: " + type);
        System.out.println("DataType: " + dataType);
        System.out.println("Body: " + body);
        System.out.println("Timestamp: " + timestamp);
        System.out.println("MessageId: " + msgId);
        System.out.println("MessageAckId: " + msgId);
    }

    public Message deepClone()  {
        Message msg = new Message(this.body, this.contactId, this.type, this.dataType, this.msgId);
        msg.setTimestamp(this.timestamp);
        msg.setStatus(this.status);
        msg.setMsgAckId(this.msgAckId);

        return msg;
    }
}
