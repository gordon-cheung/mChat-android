package com.example.macbook.mchat;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface AppSettingDao {
    @Query("SELECT * FROM App_Settings")
    List<AppSetting> getAll();

    @Insert
    void insert(AppSetting setting);

    @Query ("UPDATE App_Settings SET value=:value WHERE name==:key")
    void updateSetting(String key, String value);



}
