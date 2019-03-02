package com.example.macbook.mchat;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.content.Intent;

import java.util.ArrayList;

public class SelectContactActivity extends AppCompatActivity {

    private static final String TAG = "SelectContactActivity";
    private ArrayList<Contact> mContacts = new ArrayList<>();

    // TODO implement a better way to do this such as LINQ?
    private ArrayList<String> mPhoneNumbers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contact);
        Log.d(TAG, "onCreate: started");
        getContactList();
        initRecyclerView();
    }

    private void getContactList() {
        Log.d(TAG, "contactList: retrieving contacts...");
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        while(phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            number = Contact.formatPhoneNumber(number);
            Log.d(TAG, "name: " + name + " number: " + number);
            if (!mPhoneNumbers.contains(number)) {
                mPhoneNumbers.add(number);
                mContacts.add(new Contact(name, number, "https://i.redd.it/tpsnoz5bzo501.jpg"));
            }
        }
    }

    private void initRecyclerView(){
        Log.d(TAG, "initRecyclerView: init recyclerview");
        RecyclerView recyclerView = findViewById(R.id.contactsRecyclerView);
        ContactsRecyclerAdapter adapter = new ContactsRecyclerAdapter(this, mContacts);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
