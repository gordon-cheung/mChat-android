package com.example.macbook.mchat

import android.Manifest
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: ConversationRecyclerAdapter? = null
    private var mAppDatabase: AppDatabase? = null

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

//        val connectBluetoothButton = findViewById<Button>(R.id.refreshButton)
//
//        connectBluetoothButton.setOnClickListener{
//            val intent = Intent (this, SelectDeviceActivity::class.java)
//            startActivity(intent)
//        }

        getPermissions()
    }

    override fun onStart() {
        super.onStart()
    }

    private inner class GetConversationTask: AsyncTask<Void, Void, List<Message>>()
    {
        override fun doInBackground(vararg params: Void?): List<Message> {
            return mAppDatabase!!.messageDao()!!.latestUniqueMessages
        }

        override fun onPostExecute(messages: List<Message>) {
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
