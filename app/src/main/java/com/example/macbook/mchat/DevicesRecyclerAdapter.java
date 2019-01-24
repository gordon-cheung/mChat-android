package com.example.macbook.mchat;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class DevicesRecyclerAdapter extends RecyclerView.Adapter {
    private static final String TAG = DevicesRecyclerAdapter.class.getSimpleName();
    private ArrayList<BluetoothDevice> mDeviceList;
    private Context mContext;

    public DevicesRecyclerAdapter(Context context, ArrayList<BluetoothDevice> devices) {
        mContext = context;
        mDeviceList = devices;
    }

    public void addDevice(BluetoothDevice device) {
        int currentSize = getItemCount();
        mDeviceList.add(device);
        notifyItemInserted(currentSize);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_listitem, parent, false);
        DeviceViewHolder holder = new DeviceViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        BluetoothDevice currentDevice = mDeviceList.get(position);
        ((DeviceViewHolder) holder).bind(currentDevice);
        ((DeviceViewHolder) holder).parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String deviceName = mDeviceList.get(position).getName();
                String deviceAddress = mDeviceList.get(position).getAddress();

                Log.d(TAG, "onClick: clicked on: " + deviceName);
                Log.d(TAG, "onClick: clicked on: " + deviceAddress);

//                BluetoothService btService = new BluetoothService(mContext);
//                btService.initialize();
//                btService.connect(deviceAddress);
//
//                Intent gattServiceIntent = new Intent(mContext, BluetoothService.class);
//                bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }

    public class DeviceViewHolder extends RecyclerView.ViewHolder {
        private TextView mDeviceName;
        private TextView mDeviceAddress;
        private LinearLayout parentLayout;

        public DeviceViewHolder(View itemView) {
            super(itemView);
            mDeviceName = itemView.findViewById(R.id.device_name);
            mDeviceAddress = itemView.findViewById(R.id.device_address);
            parentLayout =itemView.findViewById(R.id.parent_layout);
        }

        public void bind(BluetoothDevice device) {
            String deviceName = device.getName();
            if (deviceName == null || deviceName.isEmpty()) {
                deviceName = "(Unknown)";
            }
            mDeviceName.setText(deviceName);
            mDeviceAddress.setText(device.getAddress());
        }
    }
}
