package com.example.macbook.mchat;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
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

                Toast.makeText(mContext, "Connecting to: " + deviceAddress, Toast.LENGTH_SHORT).show();

                Log.d(TAG, "onClick: clicked on: " + deviceName);
                Log.d(TAG, "onClick: clicked on: " + deviceAddress);

                final Intent intent = new Intent(AppNotification.ACTION_GATT_DEVICE_SELECTED);
                intent.putExtra(AppNotification.ACTION_GATT_DEVICE_SELECTED, deviceAddress);
                Log.d(TAG, "Broadcasting bluetooth device selected");
                mContext.sendBroadcast(intent);
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
            parentLayout = itemView.findViewById(R.id.parent_layout);
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
