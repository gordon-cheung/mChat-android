package com.example.macbook.mchat

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: ConversationRecyclerAdapter? = null
    private var mAppDatabase: AppDatabase? = null

    // TODO: on navigated to pull from database (not just onCreate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAppDatabase = AppDatabase.getInstance(this)

        val messageList = ArrayList<Message>()
//        messageList.add(Message("Hello how are you", "Bryson Ding", "Gordon"))

        mAdapter = ConversationRecyclerAdapter(this, messageList)

        recyclerview_conversation_list.layoutManager = LinearLayoutManager(this)
        recyclerview_conversation_list.adapter = mAdapter;

        val newMessageButton = findViewById<Button>(R.id.newMessageButton)

        newMessageButton.setOnClickListener{
            val intent = Intent(this, SelectContactActivity::class.java)
            startActivity(intent)
        }

        GetConversationTask().execute()

        val connectBluetoothButton = findViewById<Button>(R.id.bluetoothButton)

        connectBluetoothButton.setOnClickListener{
            val intent = Intent (this, SelectDeviceActivity::class.java)
            startActivity(intent)
        }

        // TODO: Remove
        val testButton = findViewById<Button>(R.id.testButton)
        testButton.setOnClickListener(View.OnClickListener {
            Log.d(TAG, "TEST BUTTON CLICKED")
            val broadcastTestIntent = Intent(AppNotification.MESSAGE_RECEIVED_NOTIFICATION)
            val message = Message("TESTING 123 in MAIN", "Aaron Gile", Message.MESSAGE_RECEIVED, Message.TEXT, System.currentTimeMillis())
            broadcastTestIntent.putExtra(AppNotification.MESSAGE_RECEIVED_NOTIFICATION, message)
            sendBroadcast(broadcastTestIntent)

            val broadcastTestIntent2 = Intent(AppNotification.MESSAGE_RECEIVED_NOTIFICATION)
            val message2 = Message("TESTING 123 Bryson in MAIN", "Bryson Ding", Message.MESSAGE_RECEIVED, Message.TEXT, System.currentTimeMillis())
            broadcastTestIntent2.putExtra(AppNotification.MESSAGE_RECEIVED_NOTIFICATION, message2)
            sendBroadcast(broadcastTestIntent2)
        })

        getPermissions()
    }

    // TODO: TEST THIS
    private val messageUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "Broadcast received")
            val action = intent.action
            if (action === AppNotification.MESSAGE_RECEIVED_NOTIFICATION) {
                Log.d(TAG, "Message received")
                val msg = intent.getSerializableExtra(AppNotification.MESSAGE_RECEIVED_NOTIFICATION) as Message
                mAdapter?.updateConversations(msg)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(messageUpdateReceiver, mainActivityIntentFilter())
        GetConversationTask().execute();
        //        if (mBluetoothLeService != null) {
        //            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
        //            Log.d(TAG, "Connect request result=" + result);
        //        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(messageUpdateReceiver)
    }

    private fun mainActivityIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(AppNotification.MESSAGE_RECEIVED_NOTIFICATION)
        return intentFilter;
    }

    private inner class GetConversationTask: AsyncTask<Void, Void, List<Message>>()
    {
        override fun doInBackground(vararg params: Void?): List<Message> {
            return mAppDatabase!!.messageDao()!!.latestUniqueMessages
        }

        override fun onPostExecute(messages: List<Message>) {
            mAdapter!!.clearConversations();
            for (msg in messages) {
                Log.d(TAG, String.format("ContactId %s, MessageBody %s", msg.contactId, msg.messageBody))
                val currentSize = mAdapter!!.getItemCount()

                mAdapter!!.addConversation(msg)
                mAdapter!!.notifyItemInserted(currentSize)

                mRecyclerView?.scrollToPosition(mAdapter!!.getItemCount() - 1)
            }
        }

    }

    private fun getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 1)
        }
    }
}
