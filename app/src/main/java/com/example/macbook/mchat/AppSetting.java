package com.example.macbook.mchat;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "app_settings")
public class AppSetting {
    @NonNull
    @PrimaryKey
    private String name;

    @ColumnInfo
    private String value;

    public AppSetting() {}

    public AppSetting(String k, String v) {
        this.name = k;
        this.value = v;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}