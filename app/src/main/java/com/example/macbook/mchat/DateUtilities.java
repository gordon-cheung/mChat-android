package com.example.macbook.mchat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class DateUtilities {
    private DateUtilities() {}
    private final static String TAG = DateUtilities.class.getSimpleName();

    public static String getDateString(long timestamp) {
        DateFormat dateFormat = new SimpleDateFormat("MMM dd h:mm a");
        Date timestampDate = new Date(timestamp);
        return dateFormat.format(timestampDate);
    }

}
