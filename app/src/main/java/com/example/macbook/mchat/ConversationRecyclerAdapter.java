package com.example.macbook.mchat;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
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

        Contact tmpContact = getContact(msg.getContactId());
        if (tmpContact != null) {
            holder.contactName.setText(tmpContact.getName());
        }
        else {
            tmpContact = new Contact(msg.getContactId(), msg.getContactId(), "https://i.redd.it/tpsnoz5bzo501.jpg");
            holder.contactName.setText(msg.getContactId());
        }

        final Contact contact = tmpContact;

        holder.conversationMessage.setText(msg.getBody());
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

    private Contact getContact(String address) {
        Log.d(TAG, "Retrieving contacts");
        Cursor phone = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.NUMBER + " = " + address, null, null);
        if (phone != null) {
            if (phone.moveToFirst()) {
                String name = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String number = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                number = Contact.formatPhoneNumber(number);
                Contact contact = new Contact(name, number, "https://i.redd.it/tpsnoz5bzo501.jpg");
                return contact;
            }
        }

        return null;
    }
}
