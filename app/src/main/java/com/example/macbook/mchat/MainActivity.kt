package com.example.macbook.mchat

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val connectButton = findViewById<Button>(R.id.refreshButton)
        val connectStatusTextView = findViewById<TextView>(R.id.bluetoothStatus)

        connectButton.setOnClickListener{
            val rand = Random().nextInt(10)
            if (rand < 5){
                connectStatusTextView.text = "Disconnected"
            }
            else {
                connectStatusTextView.text = "Connected"
            }
        }

    }
}
