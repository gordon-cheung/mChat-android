package com.example.macbook.mchat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.*;
import android.content.*;
import android.os.Handler;
import android.os.IBinder;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import java.util.ArrayList;

// TODO Add Scan Button and Clean up Activity
public class SelectDeviceActivity extends MChatActivity {
    private static final String TAG = SelectDeviceActivity.class.getSimpleName();
    private ArrayList<String> mDeviceAddresses = new ArrayList<String>();
    private ArrayList<BluetoothDevice> mDevices = new ArrayList<BluetoothDevice>();

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothScanner;
    private BluetoothService mBluetoothService;
    private String connectDeviceAddress;

    private RecyclerView mRecyclerView;
    private DevicesRecyclerAdapter mAdapter;

    // Manage service lifecycle
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothService = ((BluetoothService.LocalBinder) service).getService();
            if (!mBluetoothService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            mBluetoothService.connect(connectDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_device);
        setSupportActionBar((Toolbar) findViewById(R.id.app_toolbar));

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
//        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
//            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
//            finish();
//        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Error with bluetooth");
        }

        getDeviceList();

        mRecyclerView = findViewById(R.id.devicesRecyclerView);

        mAdapter = new DevicesRecyclerAdapter(this, mDevices);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onAppNotificationReceived(Intent intent) {
        final String action = intent.getAction();
        if (action == AppNotification.ACTION_GATT_DEVICE_SELECTED) {
            String deviceAddress = intent.getStringExtra(AppNotification.ACTION_GATT_DEVICE_SELECTED);
            Log.d(TAG, "Selected Device Address: " + deviceAddress);
            connectDeviceAddress = deviceAddress;

            Intent bluetoothServiceIntent = new Intent(getApplicationContext(), BluetoothService.class);
            startService(bluetoothServiceIntent);

            Intent gattServiceIntent = new Intent(getApplicationContext(), BluetoothService.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO add service is connected flag
        //unbindService(mServiceConnection);
        mBluetoothService = null;
    }

    private void getDeviceList() {
        Log.d(TAG, "Retrieving devices...");
        scan();
    }

    private void scan() {
        mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();

        Log.d(TAG, "Starting scan");
        //mBluetoothScanner.startScan(scanFilters, scanSetting, scanCallback);
        mBluetoothScanner.startScan(scanCallback);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run() {
                Log.d(TAG, "Scan stopped");
                mBluetoothScanner.stopScan(scanCallback);
            }
        }, 10000);

        // TODO
        // java.lang.SecurityException: Need ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission to get scan results
        // occurs if location is not enabled, tell user of this error
    }
//
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanFailed(int errorCode) {

        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "Logging scan results");
            Log.d(TAG, result.toString());
            if (!mDeviceAddresses.contains(result.getDevice().getAddress())) {
                mDeviceAddresses.add(result.getDevice().getAddress());
                mAdapter.addDevice(result.getDevice());
            }
        }
    };
}
