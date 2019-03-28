package com.example.macbook.mchat;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

@Database(entities = {Message.class, AppSetting.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase sInstance;

    public abstract MessageDao messageDao();
    public abstract AppSettingDao appSettingDao();

    // TODO add logging whenever a database action is performed
    public static AppDatabase getInstance() {
        if (sInstance == null) {
            sInstance = getInstance(MChatApplication.getAppContext());
        }

        return sInstance;
    }

    public static AppDatabase getInstance(Context context) {
        if (sInstance == null) {
            sInstance = Room.databaseBuilder(context.getApplicationContext(),
                AppDatabase.class, "mchat-ms").build();
        }

        return sInstance;
    }
}
