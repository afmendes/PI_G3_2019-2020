package com.example.projetopi

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


//import com.google.firebase.database.*


class AddDeviceActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var logoutBtn: Button
    private lateinit var connectDeviceBtn: Button

    //private lateinit var ref: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        //ref = FirebaseDatabase.getInstance().getReference("users/" + auth.currentUser!!.uid)

        if(auth.currentUser == null){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        setContentView(R.layout.activity_add_device)

        logoutBtn = findViewById(R.id.logout_btn)
        connectDeviceBtn = findViewById(R.id.connectDevice)

        logoutBtn.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        connectDeviceBtn.setOnClickListener{
            val intent = Intent(this, DevicesActivity::class.java)
            startActivity(intent)
            finish()

        }
    }
}