package com.example.macbook.mchat;

import android.app.Service;
import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.os.AsyncTask;
import java.io.*;
import java.net.URLConnection;
import java.util.ArrayList;
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
    static boolean NETWORK_REGISTRATION_COMPLETE = false;

    private ArrayList<Packet> imageBuffer = new ArrayList<Packet>();

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
            Log.d(TAG, "Descriptor write finished");
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

    private void startNetworkRegistration() {
        Log.d(TAG, "Sending network registration packet");
        Message networkRegMsg = new Message("", MChatActivity.PHONE_NUMBER, Message.IS_SEND, Message.STATE_INIT, MChatApplication.getAppMsgId());
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

    public boolean isConnected() {
        return NETWORK_REGISTRATION_COMPLETE;
    }

    public int send(final Message message) {
        if (isConnected()) {
            if (message.getDataType() == Message.TEXT) {
//                String content = "";
//                for (int i = 0; i < Packet.PACKET_MAX_CONTENT_SIZE - 4; i++)
//                {
//                    content = content + "a";
//                }
//                content = "  " + content;
//                for (int i = 0; i < 100; i ++)
//                {
//                    Message msg = new Message((Integer.toString(i) + content), message.getContactId(), message.getType(), message.getDataType(), i);
//                    Packet packet = new Packet(msg);
//                    TransmissionManager.queuedWrite(packet, nordicUARTGattCharacteristicTX, mBluetoothGatt);
//                    Log.d(TAG, "Queued text packet write to BLE, msgId: " + packet.getMsgId());
//                }
                    Packet packet = new Packet(message);
                    TransmissionManager.queuedWrite(packet, nordicUARTGattCharacteristicTX, mBluetoothGatt);
                    Log.d(TAG, "Queued text packet write to BLE, msgId: " + packet.getMsgId());
                return message.getMsgId();
            } else if (message.getDataType() == Message.PICTURE) {
                try {
                    ArrayList<Packet> packets = Packet.constructPackets(message);
                    for (Packet pkt : packets) {
                        TransmissionManager.queuedWrite(pkt, nordicUARTGattCharacteristicTX, mBluetoothGatt);
                        Log.d(TAG, "Queuing picture packet write to BLE, msgId: " + pkt.getMsgId() + " content: " + ByteUtilities.getByteArrayInHexString(pkt.getBytes()));
                    }
                    return packets.get(packets.size() - 1).getMsgId();
                } catch (IOException ex) {
                    Log.e(TAG, ex.getMessage());
                    return message.getMsgId();
                }
            }
        } else if (message.getDataType() == Message.STATE_INIT) { //Don't queue as we don't expect an ACK for non-data messages
            Packet packet = new Packet(message);
            TransmissionManager.writeCharacteristic(packet.getBytes(), nordicUARTGattCharacteristicTX, mBluetoothGatt);
            return message.getMsgId();
        }
        return message.getMsgId();
    }

    public void receive(byte[] data) {
        Log.d(TAG, "RECEIVED RAW BYTES: " + ByteUtilities.getByteArrayInHexString(data));
        Packet packet = new Packet(data);

        packet.printPacket();
        Message msg = new Message(packet, Message.IS_RECEIVE, Message.STATUS_RECEIVED);

        int type = msg.getDataType();
        switch(type){
            case Message.TEXT:
                saveMsg(msg);
                broadcastMsg(msg, AppNotification.MESSAGE_RECEIVED_NOTIFICATION);
                break;
            case Message.PICTURE_START:
            case Message.PICTURE_END:
            case Message.PICTURE: //TODO
                // store packet
                storeImagePacket(packet);

                // Detect if a full image was received
                ArrayList<Packet> img = detectImageReceived();

                if (img != null) {
                    Log.d(TAG, "Full image received");
                    Bitmap bitmap = constructImage(img);

                    if (bitmap != null) {
                        String url = saveImage(bitmap, msg);

                        msg.setDataType(Message.PICTURE);
                        msg.setBody(url);

                        saveMsg(msg);
                        broadcastMsg(msg, AppNotification.MESSAGE_RECEIVED_NOTIFICATION);
                    }
                }
                break;
            case Message.NACK:
                TransmissionManager.nackReceived(nordicUARTGattCharacteristicTX, mBluetoothGatt);
                Log.d(TAG, "NACK received");
                break;
            case Message.BUFFER_FULL:
            case Message.TIMEOUT:
                TransmissionManager.txFailure(nordicUARTGattCharacteristicTX, mBluetoothGatt);
                Log.d(TAG, "Buffer full or timeout");
                break;
            case Message.STARTUP_COMPLETE:
                NETWORK_REGISTRATION_COMPLETE = true;
                Log.d(TAG, "MLINK startup complete");
                break;
            case Message.ACK:
                TransmissionManager.ackReceived(nordicUARTGattCharacteristicTX, mBluetoothGatt);
                Log.d(TAG, "ACK received");
                break;
            case Message.SENT:
                TransmissionManager.txSuccess(nordicUARTGattCharacteristicTX, mBluetoothGatt);
                updateMessageStatus(msg, Message.STATUS_SENT);
                broadcastMsg(msg, AppNotification.ACK_RECEIVED_NOTIFICATION);
                Log.d(TAG, "TX success notification");
                break;
            case Message.ERROR:
                updateMessageStatus(msg, Message.STATUS_FAILED);
                TransmissionManager.txFailure(nordicUARTGattCharacteristicTX, mBluetoothGatt);
                broadcastMsg(msg, AppNotification.MESSAGE_FAILED_NOTIFICATION);
                Log.d(TAG, "TX error notification");
            default:
                Log.d(TAG, "Invalid packet type received, contactId: " + msg.getContactId() + "MsgId: " + msg.getMsgId() + " Type: " + msg.getDataType());
                break;
        }
    }

    public void storeImagePacket(Packet pkt) {
        for (int i = 0 ; i <= imageBuffer.size(); i++) {
            if (i == imageBuffer.size()) {
                imageBuffer.add(pkt);
                break;
            }
            else if (imageBuffer.get(i).getMsgId() > pkt.getMsgId()) {
                imageBuffer.add(i, pkt);
                break;
            }
        }
    }

    public ArrayList<Packet> detectImageReceived() {
        int startCountIndex = -1;
        int currentMsgIdCounter = -1;
        int endCountIndex = -1;
        ArrayList<Packet> image = null;
        for (int i = 0; i < imageBuffer.size(); i++) {
            Packet currentPacket = imageBuffer.get(i);
            if (currentPacket.getMsgId() == currentMsgIdCounter + 1 && startCountIndex != -1) {
                currentMsgIdCounter++;
            }
            else {
                currentMsgIdCounter = -1;
                startCountIndex = -1;
            }

            if ((int)currentPacket.getDataType() == Message.PICTURE_START) {
                startCountIndex = i;
                currentMsgIdCounter = currentPacket.getMsgId();
            }

            if ((int)currentPacket.getDataType() == Message.PICTURE_END) {
                if (startCountIndex != -1) {
                    endCountIndex = i;
                    image = new ArrayList<Packet>(imageBuffer.subList(startCountIndex, endCountIndex + 1));
                    imageBuffer.subList(startCountIndex, endCountIndex + 1).clear();
                    return image;
                }
            }
        }

        return null;
    }

    public Bitmap constructImage(ArrayList<Packet> imgPkts) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (Packet p: imgPkts) {
            try {
                outputStream.write(p.getContent());
            } catch (IOException ex) {
                Log.e(TAG, ex.getMessage());
            }
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.toByteArray().length);
            String contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(outputStream.toByteArray()));
            Log.d(TAG, "Constructed image of content type: " + contentType);
            return bitmap;
        } catch(IOException ex) {
            Log.e(TAG, ex.getMessage());
            return null;
        }
    }

    // TODO testing png and jpg
    public String saveImage(Bitmap bitmap, Message msg) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + "/mchat");
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }

        try {
            String title = String.valueOf(System.currentTimeMillis()) + ".jpg";
            File f = new File(wallpaperDirectory, title);
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
            Log.d("TAG", "File Saved::---&gt;" + f.getAbsolutePath());

            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
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

    private void updateMessageStatus(final Message msg, final int status) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Updating message to status: " + status);
                AppDatabase.getInstance().messageDao().updateStatus(msg.getContactId(), msg.getMsgId(), status, Message.IS_SEND);
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
