package com.example.macbook.mchat;

import android.content.*;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.List;

// TODO Cleanup Chat activity
public class ChatActivity extends MChatActivity {
    private String TAG = ChatActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private ChatAdapter mAdapter;
    private String contactId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setSupportActionBar((Toolbar) findViewById(R.id.app_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // TODO aggregate Contact with ChatActivity
        contactId = getIntent().getExtras().getString(AppNotification.CONTACT_DATA);
        getSupportActionBar().setTitle(contactId);

        // TODO remove initial messageList
        ArrayList<Message> messageList =  new ArrayList<Message>();

        mRecyclerView = findViewById(R.id.reyclerview_message_list);
        // TODO update user list
        mAdapter = new ChatAdapter(messageList, "Gordon");

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        new GetMessagesTask().execute();

        final Button button = findViewById(R.id.button_chatbox_send);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final EditText editText = findViewById(R.id.edittext_chatbox);
                String message = editText.getText().toString();

                if (!message.isEmpty()) {
                    SendMessage(new Message(message, contactId, Message.IS_SEND, Message.TEXT));
                    editText.setText("");
                }
            }
        });

        // TODO: Remove
        final Button testButton = findViewById(R.id.button_chatbox_test);
        testButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "TEST BUTTON CLICKED");

                // Enable this to test startNetworkRegistration
                //mBluetoothService.startNetworkRegistration();

                // Enable this to test broadcast notification
                Intent broadcastTestIntent = new Intent(AppNotification.MESSAGE_RECEIVED_NOTIFICATION);
                Message message = new Message("TESTING 123", contactId, Message.IS_RECEIVE, Message.TEXT, System.currentTimeMillis());
                broadcastTestIntent.putExtra(AppNotification.MESSAGE_RECEIVED_NOTIFICATION, message);
                sendBroadcast(broadcastTestIntent);
            }
        });
    }

    private boolean SendMessage(final Message msg) {
        int currentSize = mAdapter.getItemCount();

        // Update UI
        mAdapter.addMessage(msg);
        mAdapter.notifyItemInserted(currentSize);
        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);

        // Store message in database
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
            Log.d(TAG, "Inserting stored sent message");
            AppDatabase.getInstance().messageDao().insert(msg);
            }
        });

        // TODO handle messages failed to send
        if (mBluetoothService.send(msg)) {
            Log.d(TAG, "Message successfully sent");
        } else {
            Log.e(TAG, "Messaage failed to send");
        }
        return true;
    }

    private boolean ReceiveMessage(final Message msg) {
        Log.d(TAG, "Message received: " + msg.getBody() + " from user: " + msg.getContactId());
        int currentSize = mAdapter.getItemCount();

        mAdapter.addMessage(msg);
        mAdapter.notifyItemInserted(currentSize);

        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);

//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
//                Log.d(TAG, "Inserting stored received message");
//                mAppDatabase.messageDao().insert(msg);
//            }
//        });

        return true;
    }

    @Override
    public void onAppNotificationReceived(Intent intent) {
        final String action = intent.getAction();
        if (action == AppNotification.MESSAGE_RECEIVED_NOTIFICATION) {
            Log.d(TAG, "Message Received");
            Message msg = (Message)intent.getSerializableExtra(AppNotification.MESSAGE_RECEIVED_NOTIFICATION);
            if (msg.getContactId().equals(contactId)) {
                ReceiveMessage(msg);
            }
        }
    }

    private class GetMessagesTask extends AsyncTask<Void, Void, List<Message>>
    {
        @Override
        protected List<Message> doInBackground(Void... voids) {
            return AppDatabase.getInstance().messageDao().getAll(contactId);
        }

        @Override
        protected void onPostExecute(List<Message> messages) {
            for (Message msg: messages) {
                Log.d(TAG, String.format("ContactId %s, MessageBody %s", msg.getContactId(), msg.getBody()));
                int currentSize = mAdapter.getItemCount();

                mAdapter.addMessage(msg);
                mAdapter.notifyItemInserted(currentSize);

                mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
            }
        }
    }
}
