package com.example.macbook.mchat;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import android.view.LayoutInflater;

public class ChatAdapter extends RecyclerView.Adapter {
    public static final int VIEW_TYPE_MESSAGE_SENT = 1;
    public static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private ArrayList<Message> mMessageList;
    private String mUserName;

    public ChatAdapter(ArrayList<Message> messageList, String user) {
        mMessageList = messageList;
        mUserName = user;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = mMessageList.get(position);

        if (message.getUser() == mUserName) {
            return VIEW_TYPE_MESSAGE_SENT;
        }
        else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_received_listitem, parent, false);
//        ReceivedMessageViewHolder holder = new ReceivedMessageViewHolder(view);
//        return holder;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_sent_listitem, parent, false);
            SentMessageViewHolder holder = new SentMessageViewHolder(view);
            return holder;
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_received_listitem, parent, false);
            ReceivedMessageViewHolder holder = new ReceivedMessageViewHolder(view);
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        Message currentMessage = mMessageList.get(position);
//       ((ReceivedMessageViewHolder) holder).bind(currentMessage);

        if (holder.getItemViewType() == VIEW_TYPE_MESSAGE_SENT) {
            ((SentMessageViewHolder) holder).bind(currentMessage);
        }
        else {
            ((ReceivedMessageViewHolder) holder).bind(currentMessage);
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class SentMessageViewHolder extends RecyclerView.ViewHolder {
        public TextView mMessageBody;

        public SentMessageViewHolder(View itemView) {
            super(itemView);
            mMessageBody = itemView.findViewById(R.id.text_message_body);
        }

        void bind(Message message) {
            mMessageBody.setText(message.getMessageBody());
        }
    }

    public class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        public TextView mMessageBody;

        public ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            mMessageBody = itemView.findViewById(R.id.text_message_body);
        }

        void bind(Message message) {
            mMessageBody.setText(message.getMessageBody());
        }
    }
}
