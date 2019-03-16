package com.example.macbook.mchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

public abstract class MChatActivity extends AppCompatActivity {
    private String TAG = MChatActivity.class.getSimpleName();

    private final BroadcastReceiver appNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "Broadcast received: " + action);
            if (action == AppNotification.ACTION_GATT_CONNECTED) {
                Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
            }
            else if (action == AppNotification.ACTION_GATT_DISCONNECTED) {
                Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show();
            }
            onAppNotificationReceived(intent);
        }
    };

    private static IntentFilter appNotificationIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppNotification.MESSAGE_RECEIVED_NOTIFICATION);
        intentFilter.addAction(AppNotification.ACTION_GATT_CONNECTED);
        intentFilter.addAction(AppNotification.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(AppNotification.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(AppNotification.ACTION_GATT_DEVICE_SELECTED);
        return intentFilter;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(appNotificationReceiver, appNotificationIntentFilter());
//        if (mBluetoothLeService != null) {
//            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
//            Log.d(TAG, "Connect request result=" + result);
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // TODO does this mean it is not receiving messages when it is paused?
        unregisterReceiver(appNotificationReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.appbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bluetooth:
                Intent intent = new Intent(this, SelectDeviceActivity.class);
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public abstract void onAppNotificationReceived(Intent intent);
}
