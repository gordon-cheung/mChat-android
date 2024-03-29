package com.example.macbook.mchat;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
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

    public void updateConversations(Message message) {
        int position = 0;
        boolean isFound = false;
        for (position = 0; position < mConversations.size(); position ++) {
            if (mConversations.get(position).getContactId().equals(message.getContactId())) {
                mConversations.remove(position);
                isFound = true;
            }
        }

        if (isFound) {
            notifyItemRemoved(position);
        }

        mConversations.add(0, message);
        notifyItemInserted(0);
    }

    public void clearConversations() {
        mConversations.clear();
        notifyDataSetChanged();
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

        final Contact contact = getContact(msg.getContactId());

        holder.contactName.setText(contact.getName());
        holder.conversationTimestamp.setText(DateUtilities.getDateString(msg.getTimestamp()));

        if (msg.getDataType() == Message.PICTURE) {
            String displayMessage = msg.getType() == Message.IS_RECEIVE ? "A picture message was received" : "A picture message was sent";
            holder.conversationMessage.setText(displayMessage);
        }
        else {
            holder.conversationMessage.setText(msg.getBody());
        }

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message msg = mConversations.get(position);
                String contactName = msg.getContactId();
                Log.d(TAG, "onClick: clicked on: " + contactName);
                Toast.makeText(mContext, contactName, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra(AppNotification.CONTACT_DATA, contact);
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
        TextView conversationTimestamp;
        TextView conversationMessage;
        ConstraintLayout parentLayout;

        public ViewHolder(View itemView){
            super(itemView);
            image = itemView.findViewById(R.id.image);
            contactName = itemView.findViewById(R.id.contact_name);
            conversationTimestamp = itemView.findViewById(R.id.conversation_timestamp);
            conversationMessage = itemView.findViewById(R.id.conversation_message);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }

    private Contact getContact(String address) {
        Log.d(TAG, "Retrieving contacts");
        Cursor phones = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while(phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String number = Contact.formatPhoneNumber(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
            if (number.equals(address)) {
                return new Contact(name, number, "https://i.redd.it/tpsnoz5bzo501.jpg" );
            }
        }

        return new Contact(address, address, "https://i.redd.it/tpsnoz5bzo501.jpg");
    }
}
