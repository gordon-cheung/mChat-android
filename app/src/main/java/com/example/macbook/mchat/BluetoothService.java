package com.example.macbook.mchat;

import android.app.Service;
import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;

// TODO refactor and clean up class
public class BluetoothService extends Service {
    private final static String TAG = BluetoothService.class.getSimpleName();
    private final static String NORDIC_UART_GATT_SERVICE_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    private final static String NORDIC_UART_GATT_CHARACTERISTIC_TX_UUID = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    private final static String NORDIC_UART_GATT_CHARACTERISTIC_RX_UUID = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";

    private BluetoothGattCharacteristic nordicUARTGattCharacteristicTX;

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
        Log.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;

    private BluetoothGatt mBluetoothGatt;

    private String mDeviceAddress;

    // Bluetooth Callback to know if connection is successful
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // Bluetooth is connected, discover services
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());

                mBluetoothGatt.discoverServices();
                mConnectionState = STATE_CONNECTED;
//                BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic();
//
//                try {
//                    String data = "Connection established";
//                    characteristic.setValue(URLEncoder.encode(data, "utf-8"));
//                    mBluetoothGatt.writeCharacteristic(characteristic);
//                } catch(Exception ex) {
//
//                }

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
                mConnectionState = STATE_DISCONNECTED;
            }
        }

        @Override
        // New services discovered
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Services are discovered");
                displayGattServices(getSupportedGattServices());
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,  int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "On Charcteristic Read:" + characteristic.getUuid().toString());
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "Notification received from " + characteristic.getUuid().toString());
            Log.d(TAG, "value: " + new String(characteristic.getValue()));
            broadcast(characteristic);
        }
    };

    public void broadcast(final BluetoothGattCharacteristic characteristic) {
        Log.d("TAG", "Attempting to broadcast with charctierstic: " + characteristic.getUuid().toString());
        if (characteristic.getUuid().toString().equals(NORDIC_UART_GATT_CHARACTERISTIC_RX_UUID)) {
            Log.d(TAG, "Handling received message notification");
            receive(characteristic.getValue());
        }
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        Log.d(TAG, "Displaying Gatt Services");

        for (BluetoothGattService gattService : gattServices) {
            Log.d(TAG, "Gatt Service UUID: " + gattService.getUuid().toString());
//
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();

            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                Log.d(TAG, "Gatt Characteristic UUID: " + gattCharacteristic.getUuid().toString());

                List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
                for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
                    Log.d(TAG, "Gatt Descriptor UUID: " + gattDescriptor.getUuid().toString());
                }
                // THIS WORKS OMG
                try {
                    String data = "Connection established";
                    // Need to wait for requests see see
                    // https://stackoverflow.com/questions/47097298/android-ble-bluetoothgatt-writedescriptor-return-sometimes-false
                    //6e400001-b5a3-f393-e0a9-e50e24dcca9e UART UUID service
//                        if (gattCharacteristic.getUuid().toString().equals("6e400002-b5a3-f393-e0a9-e50e24dcca9e")) {
//                            gattCharacteristic.setValue(URLEncoder.encode(data, "utf-8"));
//                            Log.d(TAG, "WriteCharcteristic(" + gattCharacteristic.getUuid() + ") Value: " + data);
//                            mBluetoothGatt.writeCharacteristic(gattCharacteristic);
//                        }
                    if (gattCharacteristic.getUuid().toString().equals("6e400002-b5a3-f393-e0a9-e50e24dcca9e")) {
                        nordicUARTGattCharacteristicTX = gattCharacteristic;
                    }
                    if (gattCharacteristic.getUuid().toString().equals("6e400003-b5a3-f393-e0a9-e50e24dcca9e")) {
                        setCharacteristicNotification(gattCharacteristic, true);
                    }
                } catch(Exception ex) {

                }
            }
        }
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        Log.d(TAG, "gatt.SetCharacteristicNotification " + characteristic.getUuid().toString());
        boolean success1 = mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        Log.d(TAG, "Set Characteristic Notification: " + (success1 ? "success" : "fail"));

        // This is specific to Heart Rate Measurement.
        if ("6e400003-b5a3-f393-e0a9-e50e24dcca9e".equals(characteristic.getUuid().toString())) {
            Log.d(TAG, "Setting up descriptor");
            Log.d(TAG, "Enabling Notifications for " + characteristic.getUuid().toString());
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));

            if (descriptor == null) {
                Log.d(TAG, "Descriptor not found");
            }
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            //descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            Log.d(TAG, "gattWriteDescriptor " + descriptor.getUuid().toString());
            boolean success = mBluetoothGatt.writeDescriptor(descriptor);
            Log.d(TAG, "Write Descriptor: " + (success ? "success" : "fail"));
            Log.d(TAG, "Data written to descriptor: " + descriptor.getUuid().toString() + "value: " + BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE.toString());
        }

        Log.d(TAG, "Notification enabled");
    }

    public boolean send(Message message) {
        Packet packet = new Packet(message);
        return writeCharacteristic(packet.getBytes());
    }

    public void receive(byte[] data) {
        Packet packet = new Packet(data);
        final Message message = packet.getMessage();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Inserting stored received message");
                AppDatabase.getInstance().messageDao().insert(message);
            }
        });

        // TODO create static broadcast ids
        final Intent intent = new Intent(AppNotification.MESSAGE_RECEIVED_NOTIFICATION);
        intent.putExtra(AppNotification.MESSAGE_RECEIVED_NOTIFICATION, message);
        Log.d(TAG, "Broadcasting intent: " + "MESSAGE_RECEIVED");
        sendBroadcast(intent);
    }

    public boolean writeCharacteristic(String data) {
        boolean success = false;
        try {
            nordicUARTGattCharacteristicTX.setValue(URLEncoder.encode(data, "utf-8"));
            Log.d(TAG, "WriteCharcteristic(" + nordicUARTGattCharacteristicTX.getUuid() + ") Value: " + data);
            success = mBluetoothGatt.writeCharacteristic(nordicUARTGattCharacteristicTX);

            if (!success) {
                Log.d(TAG, "WriteCharacteristic failed");
            }
        } catch (Exception ex) {

        }

        return success;
    }

    public boolean writeCharacteristic(byte[] data) {
        boolean success = false;
        try {
            nordicUARTGattCharacteristicTX.setValue(data);
            Log.d(TAG, "WriteCharcteristic(" + nordicUARTGattCharacteristicTX.getUuid() + ") Value: " + data);
            success = mBluetoothGatt.writeCharacteristic(nordicUARTGattCharacteristicTX);

            if (!success) {
                Log.d(TAG, "WriteCharacteristic failed");
            }
        } catch (Exception ex) {

        }

        return success;
    }

//    // Initialize bluetooth
    public boolean initialize() {
        Log.d(TAG, "Initialize Bluetooth Service");
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize Bluetooth Manager");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Unable to initialize Bluetooth Adapter or Bluetooth Adapter is disabled");
        }

        return true;
    }
//
//    // Connect to a GATT server
//    // Takes the MAC address
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.e(TAG, "Bluetooth adapter not initialized or unspecified address");
            return false;
        }

        // Use existing bluetooth device to connect
        if (mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.e(TAG, "Device not found.  Unable to connect.");
            return false;
        }

        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mDeviceAddress = address;
        return mBluetoothGatt.connect();
    }

    public String getConnectedDeviceAddress() {
        return mDeviceAddress;
    }

    private int mConnectionState = STATE_DISCONNECTED;

    // TODO update connection states
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public int getConnectionState() {
        Log.d(TAG, "Connection state is " + mConnectionState);
        return mConnectionState;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
