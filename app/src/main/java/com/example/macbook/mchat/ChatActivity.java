package com.example.macbook.mchat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private ChatAdapter mAdapter;
    private String userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        userData = getIntent().getExtras().getString("USER_DATA");
        getSupportActionBar().setTitle(userData);

        ArrayList<Message> messageList =  new ArrayList<Message>();

        mRecyclerView = findViewById(R.id.reyclerview_message_list);
        mAdapter = new ChatAdapter(messageList, userData);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        final Button button = findViewById(R.id.button_chatbox_send);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final EditText editText = findViewById(R.id.edittext_chatbox);
                String message = editText.getText().toString();

                SendMessage(new Message(message, "Gordon"));

                if (message.equals("something")) {
                    ReceiveMessage(new Message("I am here", userData));
                }
            }
        });
    }

    private boolean SendMessage(Message msg) {
        int currentSize = mAdapter.getItemCount();

        mAdapter.AddMessage(msg);
        mAdapter.notifyItemInserted(currentSize);

        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);

        return true;
    }

    private boolean ReceiveMessage(Message msg) {
        int currentSize = mAdapter.getItemCount();

        mAdapter.AddMessage(msg);
        mAdapter.notifyItemInserted(currentSize);

        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);

        return true;
    }
}
