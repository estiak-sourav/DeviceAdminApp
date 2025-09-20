package com.example.deviceadmintest

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.UserManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class MainActivity : AppCompatActivity() {

    var REQUEST_CODE_ENABLE_ADMIN = 1243

    private var devicePolicyManager: DevicePolicyManager? = null
    private lateinit var adminComponent: ComponentName



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        adminComponent = ComponentName(this, DeviceAdminReceiver::class.java)


        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
        startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)

        devicePolicyManager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager

        val manager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(applicationContext, DeviceAdminReceiver::class.java)


        val buttonBlockFactoryReset = findViewById<Button>(R.id.buttonBlockFactoryReset)
        val buttonUnblockFactoryReset = findViewById<Button>(R.id.buttonUnblockFactoryReset)
        val buttonLock = findViewById<Button>(R.id.buttonLock)
        val buttonUnLock = findViewById<Button>(R.id.buttonUnLock)


        // Lock device
        buttonLock.setOnClickListener {
            lockDevice()
        }

        // Unlock device
        buttonUnLock.setOnClickListener {
            unlockDevice()
        }

        // Block factory reset
        buttonBlockFactoryReset.setOnClickListener {
            blockFactoryReset()
        }

        // Unblock factory reset
        buttonUnblockFactoryReset.setOnClickListener {
            unBlockFactoryReset()
        }

    }

    private fun lockDevice() {
        if (devicePolicyManager!!.isAdminActive(adminComponent)) {
            devicePolicyManager!!.lockNow()
            Toast.makeText(this, "Locked successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Device admin not enabled for lock", Toast.LENGTH_SHORT).show()
        }
    }

    private fun unlockDevice() {
        if (devicePolicyManager!!.isAdminActive(adminComponent)) {
            devicePolicyManager!!.resetPassword("", 0)
            devicePolicyManager!!.lockNow()
            Toast.makeText(this, "Unlocked successfully", Toast.LENGTH_SHORT).show()

        } else {
            Toast.makeText(this, "Device admin not enabled for unlock", Toast.LENGTH_SHORT).show()

        }
    }

    private fun blockFactoryReset() {
        if (devicePolicyManager!!.isAdminActive(adminComponent)) {
            try {
                devicePolicyManager!!.addUserRestriction(
                    adminComponent,
                    UserManager.DISALLOW_FACTORY_RESET
                )
                Toast.makeText(this, "Factory reset blocked successfully", Toast.LENGTH_SHORT).show()
            } catch (e: SecurityException) {
                Toast.makeText(this, "Failed: Need device owner privileges", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Device admin not enabled for blocking", Toast.LENGTH_SHORT).show()
        }
    }

    private fun unBlockFactoryReset() {
        if (devicePolicyManager!!.isAdminActive(adminComponent)) {
            try {
                devicePolicyManager!!.clearUserRestriction(
                    adminComponent,
                    UserManager.DISALLOW_FACTORY_RESET
                )
                Toast.makeText(this, "Factory reset unblocked successfully", Toast.LENGTH_SHORT).show()
            } catch (e: SecurityException) {
                Toast.makeText(this, "Failed: Need device owner privileges", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Device admin not enabled for unblocking", Toast.LENGTH_SHORT).show()
        }
    }
}