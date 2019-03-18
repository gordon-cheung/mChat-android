package com.example.macbook.mchat;

import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.MenuRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public abstract class MChatActivity extends AppCompatActivity {
    private String TAG = MChatActivity.class.getSimpleName();

    protected BluetoothService mBluetoothService;
    private Menu mOptionsMenu;

    // Manage service lifecycle
    protected ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothService = ((BluetoothService.LocalBinder) service).getService();
            if (!mBluetoothService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent gattServiceIntent = new Intent(getApplicationContext(), BluetoothService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private final BroadcastReceiver appNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "Broadcast received: " + action);
            if (action == AppNotification.ACTION_GATT_CONNECTED) {
                Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
                updateConnectionState(BluetoothService.STATE_CONNECTED);
            }
            else if (action == AppNotification.ACTION_GATT_DISCONNECTED) {
                Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show();
                updateConnectionState(BluetoothService.STATE_DISCONNECTED);
            }
            else if (action == AppNotification.ACTION_GATT_CONNECTING) {
                Toast.makeText(context, "Connecting to " + mBluetoothService.getDeviceAddress(), Toast.LENGTH_SHORT).show();
                updateConnectionState(BluetoothService.STATE_CONNECTING);
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
        intentFilter.addAction(AppNotification.ACTION_GATT_CONNECTING);
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
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mOptionsMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.appbar_main_menu, menu);
        return true;
    }

    public boolean onCreateOptionsMenu(@MenuRes int menuRes, Menu menu) {
        mOptionsMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(menuRes, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mBluetoothService != null) {
            updateConnectionState(mBluetoothService.getConnectionState());
        }
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

    private void updateConnectionState(final int connectionState) {
        if (mOptionsMenu != null && mOptionsMenu.findItem(R.id.action_bluetooth) != null) {
            if (connectionState == BluetoothService.STATE_CONNECTED) {
                mOptionsMenu.findItem(R.id.action_bluetooth).setIcon(R.drawable.ic_bluetooth_connected_black_24dp);
            }
            else if (connectionState == BluetoothService.STATE_DISCONNECTED) {
                mOptionsMenu.findItem(R.id.action_bluetooth).setIcon(R.drawable.ic_bluetooth_disabled_black_24dp);
            }
            else if (connectionState == BluetoothService.STATE_CONNECTING) {
                mOptionsMenu.findItem(R.id.action_bluetooth).setIcon(R.drawable.ic_bluetooth_searching_black_24dp);
            }
        }
    }

    public Menu getOptionsMenu() {
        return mOptionsMenu;
    }

    protected abstract void onAppNotificationReceived(Intent intent);
}
