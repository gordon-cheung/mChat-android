package com.example.macbook.mchat

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList

class MainActivity : MChatActivity() {

    private val TAG = MainActivity::class.java.simpleName
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: ConversationRecyclerAdapter? = null
    private var mAppDatabase: AppDatabase? = null

    // TODO: on navigated to pull from database (not just onCreate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.app_toolbar))

        mAppDatabase = AppDatabase.getInstance(this)

        val messageList = ArrayList<Message>()

        mAdapter = ConversationRecyclerAdapter(this, messageList)

        recyclerview_conversation_list.layoutManager = LinearLayoutManager(this)
        recyclerview_conversation_list.adapter = mAdapter;

        val newMessageButton = findViewById<Button>(R.id.newMessageButton)

        newMessageButton.setOnClickListener{
            val intent = Intent(this, SelectContactActivity::class.java)
            startActivity(intent)
        }

        GetConversationTask().execute()
        getPermissions()
    }

    override fun onResume() {
        super.onResume()
        GetConversationTask().execute();
        //        if (mBluetoothLeService != null) {
        //            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
        //            Log.d(TAG, "Connect request result=" + result);
        //        }
    }

    override fun onAppNotificationReceived(intent: Intent) {
        val action = intent.getAction()
        if (action === AppNotification.MESSAGE_RECEIVED_NOTIFICATION) {
            Log.d(TAG, "Message received")
            val msg = intent.getSerializableExtra(AppNotification.MESSAGE_RECEIVED_NOTIFICATION) as Message
            mAdapter?.updateConversations(msg)
        }
    }

    private inner class GetConversationTask: AsyncTask<Void, Void, List<Message>>()
    {
        override fun doInBackground(vararg params: Void?): List<Message> {
            return mAppDatabase!!.messageDao()!!.latestUniqueMessages
        }

        override fun onPostExecute(messages: List<Message>) {
            mAdapter!!.clearConversations();
            for (msg in messages) {
                Log.d(TAG, String.format("ContactId %s, Body %s", msg.contactId, msg.body))
                val currentSize = mAdapter!!.getItemCount()

                mAdapter!!.addConversation(msg)
                mAdapter!!.notifyItemInserted(currentSize)

                mRecyclerView?.scrollToPosition(mAdapter!!.getItemCount() - 1)
            }
        }

    }
}
