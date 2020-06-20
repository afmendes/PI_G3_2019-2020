package com.example.projetopi

import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

/**
 * #UpdatePasswordActivity
 *
 * This activity allows the user to change its account password, assuming the user is already
 * logged on. The user has to input the current password to be able to change the password on the
 * Firebase Authentication system.
 *
 * A toast is displayed indicating if the update was successful or not.
 *
 *
 */
class UpdatePasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var passwordEt: EditText

    private lateinit var changePasswordBtn: Button
    private lateinit var back: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_password)

        auth = FirebaseAuth.getInstance()

        passwordEt = findViewById(R.id.password_edt_text)

        changePasswordBtn = findViewById(R.id.reset_pass_btn)
        back = findViewById(R.id.back_btn)

        back.setOnClickListener{
            finish()
        }

        changePasswordBtn.setOnClickListener{
            val password: String = passwordEt.text.toString()
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter password", Toast.LENGTH_LONG).show()
            } else {
                auth.currentUser?.updatePassword(password)
                    ?.addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Password changes successfully", Toast.LENGTH_LONG)
                                .show()
                            finish()
                        } else {
                            Toast.makeText(this, "password not changed", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
            }
        }
    }
}