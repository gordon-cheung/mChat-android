package com.example.macbook.mchat

import android.Manifest
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val newMessageButton = findViewById<Button>(R.id.newMessageButton)

        newMessageButton.setOnClickListener{
            val intent = Intent(this, SelectContactActivity::class.java)
            startActivity(intent)
        }

        getPermissions()
    }

    private fun getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 1)
        }
    }
}
