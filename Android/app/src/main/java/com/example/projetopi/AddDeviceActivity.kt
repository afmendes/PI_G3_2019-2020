package com.example.projetopi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

/**
 * #AddDeviceActivity
 * This activity directs the user to use the bluetooth to set a connection with the module
 * and sends the data to the Firebase Database to confirm the successful connection
 * and redirects the user to the [MainActivity] (needs to be correctly implemented - needs fixes)
 *
 */
class AddDeviceActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var logoutBtn: Button
    private lateinit var connectDeviceBtn: Button

    //private lateinit var ref: DatabaseReference

    /**
     * On creation, it initiates the [DevicesActivity]
     *
     * If the user presses the [logoutBtn] the user is redirected to the [LoginActivity]
     *
     * @param savedInstanceState
     */
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