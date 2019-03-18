package com.example.macbook.mchat;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.*;
import android.content.*;
import android.os.Handler;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import de.hdodenhof.circleimageview.CircleImageView;

import java.util.ArrayList;

// TODO Add Scan Button and Clean up Activity
public class SelectDeviceActivity extends MChatActivity {
    private static final String TAG = SelectDeviceActivity.class.getSimpleName();
    private ArrayList<String> mDeviceAddresses = new ArrayList<String>();
    private ArrayList<BluetoothDevice> mDevices = new ArrayList<BluetoothDevice>();

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothScanner;
    private String connectDeviceAddress;

    private RecyclerView mRecyclerView;
    private DevicesRecyclerAdapter mAdapter;

    public boolean isScanning = false;

    protected ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG,"On service connection in SelectDeviceActivity");
            mBluetoothService = ((BluetoothService.LocalBinder) service).getService();
            if (!mBluetoothService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            updateCurrentDevice();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.mServiceConnection = mServiceConnection;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_device);

        setSupportActionBar((Toolbar) findViewById(R.id.app_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        mRecyclerView = findViewById(R.id.devicesRecyclerView);
        mAdapter = new DevicesRecyclerAdapter(this, mDevices);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onAppNotificationReceived(Intent intent) {
        final String action = intent.getAction();
        if (action == AppNotification.ACTION_GATT_DEVICE_SELECTED) {
            String deviceAddress = intent.getStringExtra(AppNotification.ACTION_GATT_DEVICE_SELECTED);
            Log.d(TAG, "Selected Device Address: " + deviceAddress);
            connectDeviceAddress = deviceAddress;

            mBluetoothService.connect(connectDeviceAddress);
        }

        if (action == AppNotification.ACTION_GATT_CONNECTED || action == AppNotification.ACTION_GATT_DISCONNECTED || action == AppNotification.ACTION_GATT_CONNECTING) {
            updateCurrentDevice();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        onCreateOptionsMenu(R.menu.appbar_bluetooth_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan:
                if (!isScanning) {
                    scan();
                }
                else {
                    stopScan();
                }

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private void scan() {
        Log.d(TAG, "Starting scan");
        isScanning = true;
        mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothScanner.startScan(scanCallback);
        updateScanMenuItem();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run() {
            if (isScanning) {
                stopScan();
            }
            }
        }, 10000);

        // TODO
        // java.lang.SecurityException: Need ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission to get scan results
        // occurs if location is not enabled, tell user of this error
    }

    private void stopScan() {
        Log.d(TAG, "Stopping scan");
        isScanning = false;
        mBluetoothScanner.stopScan(scanCallback);
        updateScanMenuItem();
    }
//
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanFailed(int errorCode) {
            // TODO
            // java.lang.SecurityException: Need ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission to get scan results
            // occurs if location is not enabled, tell user of this error
            // is this called for this?
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

    private void updateCurrentDevice() {
        if (mBluetoothService != null) {
            if (mBluetoothService.getConnectionState() == BluetoothService.STATE_CONNECTED) {
                CircleImageView currentDeviceIcon = findViewById(R.id.current_device_icon);
                currentDeviceIcon.setImageResource(R.drawable.ic_bluetooth_connected_black_24dp);

                if (mBluetoothService.getDeviceName() != null && mBluetoothService.getDeviceAddress() != null) {
                    TextView currentDeviceName = (TextView) findViewById(R.id.current_device_name);
                    currentDeviceName.setText(mBluetoothService.getDeviceName());
                    TextView currentDeviceAddress = (TextView) findViewById(R.id.current_device_address);
                    currentDeviceAddress.setText(mBluetoothService.getDeviceAddress());
                }

                findViewById(R.id.current_device_parent_layout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "Current device clicked");
                        showDisconnectDialog();
                    }
                });
            }
            else if (mBluetoothService.getConnectionState() == BluetoothService.STATE_DISCONNECTED) {
                CircleImageView currentDeviceIcon = findViewById(R.id.current_device_icon);
                currentDeviceIcon.setImageResource(R.drawable.ic_bluetooth_disabled_black_24dp);

                findViewById(R.id.current_device_parent_layout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "Current device clicked");
                        if (mBluetoothService.getDeviceName() != null && mBluetoothService.getDeviceAddress() != null) {
                            mBluetoothService.connect(mBluetoothService.getDeviceAddress());
                        }
                    }
                });
            }
            else if (mBluetoothService.getConnectionState() == BluetoothService.STATE_CONNECTING) {
                CircleImageView currentDeviceIcon = findViewById(R.id.current_device_icon);
                currentDeviceIcon.setImageResource(R.drawable.ic_bluetooth_searching_black_24dp);

                if (mBluetoothService.getDeviceName() != null && mBluetoothService.getDeviceAddress() != null) {
                    TextView currentDeviceName = (TextView) findViewById(R.id.current_device_name);
                    currentDeviceName.setText(mBluetoothService.getDeviceName());
                    TextView currentDeviceAddress = (TextView) findViewById(R.id.current_device_address);
                    currentDeviceAddress.setText(mBluetoothService.getDeviceAddress());
                }
            }
        }
    }

    private void updateScanMenuItem() {
        if (isScanning) {
            getOptionsMenu().findItem(R.id.action_scan).setTitle(getResources().getString(R.string.stop_scan));
        } else {
            getOptionsMenu().findItem(R.id.action_scan).setTitle(getResources().getString(R.string.scan));
        }
    }

    private void showDisconnectDialog()  {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String deviceName = mBluetoothService.getDeviceName();
        builder.setMessage("Disconnect from " + deviceName + "?")
                .setPositiveButton("Disconnect", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mBluetoothService.disconnect();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                })
                .show();
    }
}
