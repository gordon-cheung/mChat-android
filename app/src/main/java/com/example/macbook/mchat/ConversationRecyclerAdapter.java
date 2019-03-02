package com.example.macbook.mchat;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import de.hdodenhof.circleimageview.CircleImageView;

import java.util.ArrayList;

public class ConversationRecyclerAdapter extends RecyclerView.Adapter<ConversationRecyclerAdapter.ViewHolder> {
    private String TAG = ChatActivity.class.getSimpleName();
    private ArrayList<Message> mConversations;
    private Context mContext;

    public ConversationRecyclerAdapter(Context context, ArrayList<Message> conversations) {
        mContext = context;
        mConversations = conversations;
    }

    public void addConversation(Message message) {
        mConversations.add(message);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversation_listitem, parent, false);
        ConversationRecyclerAdapter.ViewHolder holder = new ConversationRecyclerAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        //Glide.with(mContext).asBitmap().load(mContacts.get(position).getImage()).into(holder.image);
        Message msg = mConversations.get(position);
        // TODO use appdata user id
        String contactName = msg.getContactId();
        holder.contactName.setText(contactName);
        holder.conversationMessage.setText(msg.getMessageBody());
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message msg = mConversations.get(position);
                String contactName = msg.getContactId();
                Log.d(TAG, "onClick: clicked on: " + contactName);
                Toast.makeText(mContext, contactName, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("USER_DATA", contactName);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mConversations.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        CircleImageView image;
        TextView contactName;
        TextView conversationMessage;
        RelativeLayout parentLayout;

        public ViewHolder(View itemView){
            super(itemView);
            image = itemView.findViewById(R.id.image);
            contactName = itemView.findViewById(R.id.contact_name);
            conversationMessage = itemView.findViewById(R.id.conversation_message);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }

    }
}