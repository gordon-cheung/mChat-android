package com.example.macbook.mchat;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import java.util.HashMap;
import java.util.List;

public class MChatApplication extends Application {
    public static String APP_MSG_ID = "APP_MSG_ID";

    private static Context context;
    private static HashMap<String, String> settings = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        MChatApplication.context = getApplicationContext();

        initAppSettings();

        Intent bluetoothServiceIntent = new Intent(getApplicationContext(), BluetoothService.class);
        startService(bluetoothServiceIntent);
    }

    public void initAppSettings()  {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                List<AppSetting> appSettings = AppDatabase.getInstance().appSettingDao().getAll();
                HashMap<String, String> hashMapAppSettings = new HashMap<>();

                for (AppSetting setting : appSettings) {
                    hashMapAppSettings.put(setting.getName(), setting.getValue());
                }

                if (!hashMapAppSettings.containsKey(APP_MSG_ID)) {
                    AppDatabase.getInstance().appSettingDao().insert(new AppSetting(APP_MSG_ID, String.valueOf(0)));
                }

                new GetSettingsTask().execute();
            }
        });
    }

    public static Context getAppContext() {
        return MChatApplication.context;
    }


    private class GetSettingsTask extends AsyncTask<Void, Void, List<AppSetting>>
    {
        @Override
        protected List<AppSetting> doInBackground(Void... voids) {
            return AppDatabase.getInstance().appSettingDao().getAll();
        }

        @Override
        protected void onPostExecute(List<AppSetting> appSettings) {
            for (AppSetting setting : appSettings) {
                settings.put(setting.getName(), setting.getValue());
            }
        }
    }

    public static int getAppMsgId()  {
        if (settings.containsKey(APP_MSG_ID)) {
            incrementAppMsgId();
            return Integer.parseInt(settings.get(APP_MSG_ID));
        }

        return 0;
    }

    private static void incrementAppMsgId()  {
        int incrementedAppMsgId = Integer.parseInt(settings.get(APP_MSG_ID)) + 1;
        if (incrementedAppMsgId >= 65536) {
            incrementedAppMsgId = 0;
        }
        final String msgId = String.valueOf(incrementedAppMsgId);
        if (settings.containsKey(APP_MSG_ID)) {
            settings.put(APP_MSG_ID, msgId);
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                AppDatabase.getInstance().appSettingDao().updateSetting(MChatApplication.APP_MSG_ID, String.valueOf(msgId));
            }
        });
    }
}
