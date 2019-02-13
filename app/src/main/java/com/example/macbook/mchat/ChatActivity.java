package com.example.macbook.mchat;

import android.content.*;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.List;

// TODO Cleanup Chat activity
public class ChatActivity extends AppCompatActivity {
    private String TAG = ChatActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private ChatAdapter mAdapter;
    private String contactId;
    private AppDatabase mAppDatabase;

    private BluetoothService mBluetoothService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothService = ((BluetoothService.LocalBinder) service).getService();
            Log.d(TAG, "Currently connected device: " + mBluetoothService.getConnectedDeviceAddress());
            Log.d(TAG, "Connection State: " + mBluetoothService.getConnectionState());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAppDatabase = AppDatabase.getInstance(this);

        contactId = getIntent().getExtras().getString("USER_DATA");
        getSupportActionBar().setTitle(contactId);

        ArrayList<Message> messageList =  new ArrayList<Message>();

        mRecyclerView = findViewById(R.id.reyclerview_message_list);
        mAdapter = new ChatAdapter(messageList, "Gordon");

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        new GetMessagesTask().execute();

        final Button button = findViewById(R.id.button_chatbox_send);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final EditText editText = findViewById(R.id.edittext_chatbox);
                String message = editText.getText().toString();

                // TODO replace with current app user id
                SendMessage(new Message(message, contactId, Message.MESSAGE_SENT));

                if (message.equals("something")) {
                    ReceiveMessage(new Message("I am here", contactId, Message.MESSAGE_RECEIVED));
                }
            }
        });

        Intent gattServiceIntent = new Intent(this, BluetoothService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private boolean SendMessage(final Message msg) {
        int currentSize = mAdapter.getItemCount();

        mAdapter.AddMessage(msg);
        mAdapter.notifyItemInserted(currentSize);

        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);

        mBluetoothService.writeCharcteristic(msg.getMessageBody());

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Inserting stored sent message");
                mAppDatabase.messageDao().insert(msg);
            }
        });

        return true;
    }

    private boolean ReceiveMessage(final Message msg) {
        Log.d(TAG, "Message received: " + msg.getMessageBody() + " from user: " + msg.getContactId());
        int currentSize = mAdapter.getItemCount();

        mAdapter.AddMessage(msg);
        mAdapter.notifyItemInserted(currentSize);

        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Inserting stored received message");
                mAppDatabase.messageDao().insert(msg);
            }
        });

        return true;
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Broadcast received");
        final String action = intent.getAction();
        if (action == "MESSAGE_RECEIVED") {
            ReceiveMessage(new Message(intent.getStringExtra("RECEIVED_MESSAGE"), contactId, Message.MESSAGE_RECEIVED));
        }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
//        if (mBluetoothLeService != null) {
//            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
//            Log.d(TAG, "Connect request result=" + result);
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("MESSAGE_RECEIVED");
        return intentFilter;
    }

    private class GetMessagesTask extends AsyncTask<Void, Void, List<Message>>
    {
        @Override
        protected List<Message> doInBackground(Void... voids) {
            return mAppDatabase.messageDao().getAll(contactId);
        }

        @Override
        protected void onPostExecute(List<Message> messages) {
            for (Message msg: messages) {
                Log.d(TAG, String.format("ContactId %s, MessageBody %s", msg.getContactId(), msg.getMessageBody()));
                int currentSize = mAdapter.getItemCount();

                mAdapter.AddMessage(msg);
                mAdapter.notifyItemInserted(currentSize);

                mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
            }
        }
    }
}
