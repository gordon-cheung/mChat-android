package com.example.macbook.mchat;

import android.Manifest;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.content.Intent;

import java.util.ArrayList;

public class SelectContactActivity extends MChatActivity {

    private static final String TAG = SelectContactActivity.class.getSimpleName();
    private ArrayList<Contact> mContacts = new ArrayList<>();
    private ArrayList<String> mPhoneNumbers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contact);
        setSupportActionBar((Toolbar) findViewById(R.id.app_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getContactList();
        initRecyclerView();
    }

    @Override
    protected void onAppNotificationReceived(Intent intent) {

    }

    private void getContactList() {
        Log.d(TAG, "Retrieving contacts");
        if (isPermissionGranted(Manifest.permission.READ_CONTACTS)) {
            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            while (phones.moveToNext()) {
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                number = Contact.formatPhoneNumber(number);
                if (!mPhoneNumbers.contains(number)) {
                    mPhoneNumbers.add(number);
                    mContacts.add(new Contact(name, number, "https://i.redd.it/tpsnoz5bzo501.jpg"));
                }
            }
        }
        else {
            Log.e(TAG, "Access to contacts is required for this application. MChat will now close.");
            showErrorDialog("Access to contacts is required for this application. MChat will now close.", true);
        }
    }

    private void initRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.contactsRecyclerView);
        ContactsRecyclerAdapter adapter = new ContactsRecyclerAdapter(this, mContacts);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
