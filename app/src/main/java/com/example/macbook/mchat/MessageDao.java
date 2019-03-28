package com.example.macbook.mchat;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;
//TODO organize into folders
@Dao
public interface MessageDao {
    @Query("SELECT * FROM Messages")
    List<Message> getAll();

    @Query("SELECT * FROM Messages WHERE contact_id == :id")
    List<Message> getAll(String id);

    @Query("SELECT m1.* FROM Messages m1 INNER JOIN " +
            "( SELECT max(timestamp) max_timestamp, contact_id FROM Messages Group By contact_id) m2 " +
            "on m1.contact_id = m2.contact_id and m1.timestamp = m2.max_timestamp Order By timestamp desc")
    List<Message> getLatestUniqueMessages();

    @Query("SELECT * FROM Messages WHERE contact_id ==:id AND msg_id ==:msgId AND status ==:status")
    List<Message> getAll(String id, int msgId, int status);

    @Insert
    void insert(Message msg);

    @Query ("UPDATE Messages SET status=:status WHERE contact_id ==:id AND msg_id ==:msgId AND type ==:type")
    void updateStatus(String id, int msgId, int status, int type);
}
