package com.example.macbook.mchat;

import android.app.Service;
import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class BluetoothService extends Service {
    private final static String TAG = BluetoothService.class.getSimpleName();

    public class LocalBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        //close();
        return super.onUnbind(intent);
    }



//    private Context mContext;
//    private BluetoothAdapter mBluetoothAdapter;
//    private BluetoothManager mBluetoothManager;
//
//    private BluetoothGatt mBluetoothGatt;
//
//    public BluetoothService(Context context) {
//        mContext = context;
//    }
//
//    // Bluetooth Callback to know if connection is successful
//    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
//        @Override
//        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//            if (newState == BluetoothProfile.STATE_CONNECTED) {
//                // Bluetooth is connected, discover services
//                Log.i(TAG, "Connected to GATT server.");
//                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
//
//            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                Log.i(TAG, "Disconnected from GATT server.");
//            }
//        }
//
//        @Override
//        // New services discovered
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                Log.i(TAG, "Services are discovered");
//            }
//        }
//    };
//
//    // Initialize bluetooth
    public boolean initialize() {
        Log.d(TAG, "Initialize Bluetooth Service");
//        if (mBluetoothManager == null) {
//            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//
//            if (mBluetoothManager == null) {
//                Log.e(TAG, "Unable to initialize Bluetooth Manager");
//                return false;
//            }
//        }
//
//        mBluetoothAdapter = mBluetoothManager.getAdapter();
//        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
//            Log.e(TAG, "Unable to initialize Bluetooth Adapter or Bluetooth Adapter is disabled");
//        }
//
        return true;
    }
//
//    // Connect to a GATT server
//    // Takes the MAC address
//    public boolean connect(final String address) {
//        if (mBluetoothAdapter == null || address == null) {
//            Log.e(TAG, "Bluetooth adapter not initialized or unspecified address");
//            return false;
//        }
//
//        // Use existing bluetooth device to connect
//        if (mBluetoothGatt != null) {
//            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
//            if (mBluetoothGatt.connect()) {
//                return true;
//            } else {
//                return false;
//            }
//        }
//
//        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
//        if (device == null) {
//            Log.e(TAG, "Device not found.  Unable to connect.");
//            return false;
//        }
//
//        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
//        Log.d(TAG, "Trying to create a new connection.");
//        return mBluetoothGatt.connect();
//    }
}
