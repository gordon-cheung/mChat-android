package com.example.macbook.mchat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        String userData = getIntent().getExtras().getString("USER_DATA");
        getSupportActionBar().setTitle(userData);

        ArrayList<Message> messageList =  new ArrayList<Message>();
        messageList.add(new Message("Hello", userData));
        messageList.add(new Message("hello", "Gordon"));
        messageList.add(new Message("hello again", userData));
        messageList.add(new Message("Hello", userData));
        messageList.add(new Message("hello", "Gordon"));
        messageList.add(new Message("hello again", userData));
        messageList.add(new Message("Hello", userData));
        messageList.add(new Message("hello", "Gordon"));
        messageList.add(new Message("hello again", userData));
        messageList.add(new Message("Hello", userData));
        messageList.add(new Message("hello", "Gordon"));
        messageList.add(new Message("hello again", userData));
        messageList.add(new Message("Hello", userData));
        messageList.add(new Message("hello", "Gordon"));
        messageList.add(new Message("hello again", userData));
        messageList.add(new Message("Hello", userData));
        messageList.add(new Message("hello", "Gordon"));
        messageList.add(new Message("hello again", userData));

        mRecyclerView = findViewById(R.id.reyclerview_message_list);
        mAdapter = new ChatAdapter(messageList, userData);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
