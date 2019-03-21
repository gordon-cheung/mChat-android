package com.example.macbook.mchat;

import android.app.Service;
import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.os.AsyncTask;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
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
        Log.d(TAG, "onUnbind");
        close();
        return super.onUnbind(intent);
    }

    // ** Bluetooth **//
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic nordicUARTGattCharacteristicTX;
    private BluetoothDevice mCurrentDevice = null;

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
        Message networkRegMsg = new Message("", "5551234567", Message.IS_SEND, Message.STATE_INIT, ChatActivity.incrementMessageId());
        //TODO Read ACK and show it is connected to the base
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

    public boolean send(Message message) {
        if (message.getDataType() == Message.TEXT || message.getDataType() == Message.STATE_INIT) {
            Packet packet = new Packet(message);
            PacketQueue.writeNewPacket(packet);
            Log.d("TAG", "Sending packet over BLE " + ByteUtilities.getByteArrayInHexString(packet.getBytes()));
            PacketQueue.write(nordicUARTGattCharacteristicTX, mBluetoothGatt);
            return true;
        } else if (message.getDataType() == Message.PICTURE) {
            try {
                ArrayList<Packet> packets = Packet.constructPackets(message);
                for (Packet pkt : packets) {
                    PacketQueue.writeNewPacket(pkt);
                    Log.d("TAG", "Sending packet over BLE " + ByteUtilities.getByteArrayInHexString(pkt.getBytes()));
                    PacketQueue.write(nordicUARTGattCharacteristicTX, mBluetoothGatt);
                }
            } catch (IOException ex) {
                Log.e(TAG, ex.getMessage());
                return false;
            }
            return true;
        }
        return false;
    }

    public void receive(byte[] data) {
        Log.d(TAG, "RECEIVED RAW BYTES: " + ByteUtilities.getByteArrayInHexString(data));
        Packet packet = new Packet(data);

        Log.d(TAG, "Encoded RAW BYTES to PACKET");
        packet.printPacket();
        Message msg = new Message(packet, Message.IS_RECEIVE, Message.STATUS_RECEIVED);

        // Data received
        if (msg.getDataType() == Message.TEXT)
        {
            saveMsg(msg);
            broadcastMsg(msg, AppNotification.MESSAGE_RECEIVED_NOTIFICATION);
        }
        else if (msg.getDataType() == Message.PICTURE) //TODO
        {
        }
        // Handle NACKs
        else if (msg.getDataType() == Message.BUFFER_FULL || msg.getDataType() == Message.TIMEOUT)
        {
            processNACK(msg);
        }
        else if (msg.getDataType() == Message.IN_PROGRESS) {
            updateMessageStatus(msg, Message.IN_PROGRESS);
        }
        // Handle ACK
        else if (msg.getDataType() == Message.SENT) {
            // Broadcast Notification
            updateMessageStatus(msg, Message.STATUS_SENT);
            broadcastMsg(msg, AppNotification.ACK_RECEIVED_NOTIFICATION);
        }
        else { // (msg.getDataType() == Message.ERROR) {
            updateMessageStatus(msg, Message.STATUS_FAILED);
            broadcastMsg(msg, AppNotification.MESSAGE_FAILED_NOTIFICATION);
        }
    }

    private void broadcastMsg(final Message msg, final String notificationId) {
        final Intent intent = new Intent(notificationId);
        intent.putExtra(notificationId, msg);
        Log.d(TAG, "Broadcasting intent: " + notificationId);
        sendBroadcast(intent);
    }

    private void saveMsg(final Message msg) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Inserting stored received message");
                AppDatabase.getInstance().messageDao().insert(msg);
            }
        });
    }

    private void processNACK(final Message msg) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                int msgId = msg.getMsgId();
                String phoneNum = msg.getContactId();
                List<Message> content = AppDatabase.getInstance().messageDao().getAll(phoneNum, msgId, Message.STATUS_PENDING);
                if (content.size() == 0) {
                    Log.d(TAG, "Error, not found! MsgId:" + msgId + " PhoneNum: " + phoneNum);
                }
                else {
                    Packet failedPacket = new Packet(content.get(0));
                    PacketQueue.writeFailedPacket(failedPacket);
                    PacketQueue.write(nordicUARTGattCharacteristicTX, mBluetoothGatt);
                }
            }
        });
    }

    private void updateMessageStatus(final Message msg, final int status) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Updating message to status: " + status);
                AppDatabase.getInstance().messageDao().updateStatus(msg.getContactId(), msg.getMsgId(), status);
            }
        });
    }

//  Initialize bluetooth
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
        if (mCurrentDevice  != null) {
            if (mBluetoothGatt != null && mCurrentDevice.getAddress().equals(address)) {
                Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
                if (mBluetoothGatt.connect()) {
                    broadcast(AppNotification.ACTION_GATT_CONNECTING);
                    mConnectionState = STATE_CONNECTING;
                    return true;
                } else {
                    return false;
                }
            }
            else if (mBluetoothManager.getConnectionState(mCurrentDevice, BluetoothProfile.GATT) == BluetoothProfile.STATE_CONNECTED) {
                mBluetoothGatt.disconnect();
            }
        }

        mCurrentDevice = mBluetoothAdapter.getRemoteDevice(address);
        if (mCurrentDevice == null) {
            Log.e(TAG, "Device not found. Unable to connect.");
            return false;
        }

        mBluetoothGatt = mCurrentDevice.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        broadcast(AppNotification.ACTION_GATT_CONNECTING);
        mConnectionState = STATE_CONNECTING;
        return mBluetoothGatt.connect();
    }

    public void disconnect()  {
        Log.d(TAG, "Disconnecting from " + getDeviceAddress());
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    private void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    private int mConnectionState = STATE_DISCONNECTED;
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

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

    public String getDeviceAddress() {
        return mCurrentDevice != null ? mCurrentDevice.getAddress() : null;
    }

    public String getDeviceName() {
        if (mCurrentDevice != null) {
            return mCurrentDevice.getName() != null ? mCurrentDevice.getName() : getResources().getString(R.string.unknown_device);
        }

        return null;
    }
}
