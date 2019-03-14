package com.example.macbook.mchat;

import android.app.Service;
import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
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

    // *** Service *** //
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

    // ** Bluetooth **//
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic nordicUARTGattCharacteristicTX;
    private String mDeviceAddress;

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");
                broadcast(AppNotification.ACTION_GATT_CONNECTED);

                Log.i(TAG, "Attempting to start service discovery");
                mBluetoothGatt.discoverServices();
                mConnectionState = STATE_CONNECTED;
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
                broadcast(AppNotification.ACTION_GATT_DISCONNECTED);
                mConnectionState = STATE_DISCONNECTED;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Services are discovered");
                broadcast(AppNotification.ACTION_GATT_SERVICES_DISCOVERED);
                setupServices(getSupportedGattServices());
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,  int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "On Characteristic Read:" + characteristic.getUuid().toString());
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "Notification received from " + characteristic.getUuid().toString());
            Log.d(TAG, "value: " + new String(characteristic.getValue()));
            broadcast(characteristic);
        }

        @Override
        public void onDescriptorWrite (BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d("TAG", "Descriptor write finished");
            startNetworkRegistration();
        }
    };

    private void setupServices(List<BluetoothGattService> gattServices) {
        Log.d(TAG, "Initializing Gatt Services");

        try {
            BluetoothGattService uartService = BluetoothServiceHelperKt.getService(gattServices, GattAttributes.NORDIC_UART_GATT_SERVICE_UUID);
            nordicUARTGattCharacteristicTX = uartService.getCharacteristic(UUID.fromString(GattAttributes.NORDIC_UART_GATT_CHARACTERISTIC_TX_UUID));
            setCharacteristicNotification(uartService.getCharacteristic(UUID.fromString(GattAttributes.NORDIC_UART_GATT_CHARACTERISTIC_RX_UUID)), true);
        } catch (NullPointerException ex) {
            Log.e(TAG, ex.toString());
        }
    }

    private void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        Log.d(TAG, "Setting Characteristic Notification");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        boolean success = mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        Log.d(TAG, "Set Characteristic Notification: " + (success ? "success" : "fail"));

        if (characteristic.getUuid().toString().equals(GattAttributes.NORDIC_UART_GATT_CHARACTERISTIC_RX_UUID)) {
            Log.d(TAG, "Enabling Notifications for " + characteristic.getUuid().toString());
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));

            if (descriptor == null) {
                Log.d(TAG, "Descriptor not found");
            }

            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            boolean writeSuccess = mBluetoothGatt.writeDescriptor(descriptor);
            Log.d(TAG, "Write Descriptor: " + (writeSuccess ? "success" : "fail"));
            Log.d(TAG, "Notification enabled");
        }
    }

    // TODO
    private void startNetworkRegistration() {
        Log.d(TAG, "Sending network registration packet");
        Message networkRegMsg = new Message("5551234567", Message.STATE_INIT);
        //TODO Read ACK and show it isconnece doto the base
        send(networkRegMsg);
    }

    private void broadcast(final String action) {
        Log.d(TAG, "Attempting to broadcast action: " + action);
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcast(final BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "Attempting to broadcast with characteristic: " + characteristic.getUuid().toString());
        if (characteristic.getUuid().toString().equals(GattAttributes.NORDIC_UART_GATT_CHARACTERISTIC_RX_UUID)) {
            Log.d(TAG, "Handling received message notification");
            receive(characteristic.getValue());
        }
    }

    private boolean writeCharacteristic(String data) {
        boolean success = false;
        try {
            nordicUARTGattCharacteristicTX.setValue(URLEncoder.encode(data, "utf-8"));
            Log.d(TAG, "WriteCharacteristic(" + nordicUARTGattCharacteristicTX.getUuid() + ") Value: " + data);
            success = mBluetoothGatt.writeCharacteristic(nordicUARTGattCharacteristicTX);

            if (!success) {
                Log.d(TAG, "WriteCharacteristic failed");
            }
        } catch (Exception ex) {

        }

        return success;
    }

    private boolean writeCharacteristic(byte[] data) {
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

    public boolean send(Message message) {
        Packet packet = new Packet(message);
        Log.d("TAG", "Sending packet over BLE " + ByteUtilities.getByteArrayInHexString(packet.getBytes()));
        return writeCharacteristic(packet.getBytes());
    }

    public void receive(byte[] data) {
        Log.d(TAG, "RECEIVED RAW BYTES: " + ByteUtilities.getByteArrayInHexString(data));
        Packet packet = new Packet(data);

        Log.d(TAG, "Encoded RAW BYTES to PACKET");
        packet.printPacket();

//        Message msg = packet.getMessage();
//        if (msg.getDataType() == Message.STATE_IN_PROGRESS) {
//            // Broadcast Notification
//            Toast.makeText(mContext, mContacts.get(position).getName(), Toast.LENGTH_SHORT).show();
//        }

        //final Message message = packet.getMessage();

        // TODO need to handle ACKs not just messages
//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
//                Log.d(TAG, "Inserting stored received message");
//                AppDatabase.getInstance().messageDao().insert(message);
//            }
//        });

        // TODO create static broadcast ids
//        final Intent intent = new Intent(AppNotification.MESSAGE_RECEIVED_NOTIFICATION);
//        intent.putExtra(AppNotification.MESSAGE_RECEIVED_NOTIFICATION, message);
//        Log.d(TAG, "Broadcasting intent: " + "MESSAGE_RECEIVED");
//        sendBroadcast(intent);
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

    public boolean connect(final String address) {
        Log.d(TAG, "Connecting to " + address);
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
            Log.e(TAG, "Device not found. Unable to connect.");
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
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public int getConnectionState() {
        return mConnectionState;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    public void displayGattServices(List<BluetoothGattService> gattServices) {
        for (BluetoothGattService gattService : gattServices) {
            Log.d(TAG, "Gatt Service UUID: " + gattService.getUuid().toString());

            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                Log.d(TAG, "Gatt Characteristic UUID: " + gattCharacteristic.getUuid().toString());

                List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
                for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
                    Log.d(TAG, "Gatt Descriptor UUID: " + gattDescriptor.getUuid().toString());
                }
            }
        }
    }
}
