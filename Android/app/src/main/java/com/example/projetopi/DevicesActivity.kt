package com.example.projetopi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager

/**
 * #DevicesActivity
 * This activity has a fragment [DevicesFragment] which utilizes the android bluetooth connection
 * to perform a connection to a bluetooth module to operate in a double sided communication
 *
 *
 *
 */
class DevicesActivity : AppCompatActivity(),
    FragmentManager.OnBackStackChangedListener {
    /**
     * ##onCreate
     *
     * This function is executed every time the [DevicesFragment] activity is executed.
     *
     * @param savedInstanceState Internal variable used to save the instance activity state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_devices)

        supportFragmentManager.addOnBackStackChangedListener(this)
        if (savedInstanceState == null) supportFragmentManager.beginTransaction()
            .add(R.id.fragment, DevicesFragment(), "devices").commit() else onBackStackChanged()
    }

    override fun onBackStackChanged() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(supportFragmentManager.backStackEntryCount > 0)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}