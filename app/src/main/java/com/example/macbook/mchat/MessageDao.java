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

    @Query("SELECT * FROM Messages WHERE receiver_id == :id OR sender_id == :id")
    List<Message> getAll(String id);

    @Insert
    void insert(Message msg);
}
