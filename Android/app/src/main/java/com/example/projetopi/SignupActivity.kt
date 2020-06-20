package com.example.projetopi

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

/**
 * #SignupActivity
 *
 * This activity allows the user to create an account using the Firebase Authentication System.
 * It allows the user to sign up using any kind of email account, it doesn't have a verification
 * system to check if the account is valid. Maybe to be implemented in the future.
 *
 * Also on account creation, a handful of data is sent to the Firebase Database to setup the initial
 * configurations for the Bluetooth module to operate on.
 *
 */
class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText

    private lateinit var signUpBtn: Button
    private lateinit var loginBtn: Button

    private lateinit var ref: DatabaseReference
    private lateinit var user: DatabaseReference

    /**
     * This function is executed every time the [SignupActivity] activity is executed.
     *
     * On an user account creation, a handful of data is sent to the Firebase Database as the following:
     *
     * Under users/<UserID>
     *
     * /exists = 0;
     *
     * /flags/airHumidity = 0;
     *
     * /flags/batteryCharge = 0;
     *
     * /flags/luminosity = 0;
     *
     * /flags/soilHumidity = 0;
     *
     * /flags/temperature = 0;
     *
     * /BOMBA = 0;
     *
     * /offset = 3000;
     *
     * If the user creation is failed, a Toast is shown indicating it and nothing else happens.
     *
     * @param savedInstanceState Internal variable used to save the instance activity state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        emailEt = findViewById(R.id.email_edt_text)
        passwordEt = findViewById(R.id.pass_edt_text)

        loginBtn = findViewById(R.id.login_btn)
        signUpBtn = findViewById(R.id.signup_btn)

        signUpBtn.setOnClickListener{
            val email: String = emailEt.text.toString()
            val password: String = passwordEt.text.toString()

            if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_LONG).show()
            } else{
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this
                ) { task ->
                    if(task.isSuccessful){
                        Toast.makeText(this, "Successfully Registered", Toast.LENGTH_LONG).show()

                        auth = FirebaseAuth.getInstance()
                        ref = FirebaseDatabase.getInstance().reference
                        user = ref.child("users").child(auth.currentUser!!.uid)
                        user.child("exists").setValue(0)
                        user.child("flags").child("airHumidity").setValue(0)
                        user.child("flags").child("batteryCharge").setValue(0)
                        user.child("flags").child("luminosity").setValue(0)
                        user.child("flags").child("soilHumidity").setValue(0)
                        user.child("flags").child("temperature").setValue(0)
                        user.child("BOMBA").setValue(0)
                        user.child("offset").setValue(3000)

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }else {
                        Toast.makeText(this, "Registration Failed", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        loginBtn.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}