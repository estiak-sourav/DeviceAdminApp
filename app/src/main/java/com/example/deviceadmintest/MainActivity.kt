package com.example.deviceadmintest

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.UserManager
import android.util.Log
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


        val buttonClearPermission = findViewById<Button>(R.id.buttonClearPermission)
        val buttonAction = findViewById<Button>(R.id.buttonAction)
        val buttonBlockFactoryReset = findViewById<Button>(R.id.buttonBlockFactoryReset)
        val buttonUnblockFactoryReset = findViewById<Button>(R.id.buttonUnblockFactoryReset)
        val buttonFactoryReset = findViewById<Button>(R.id.buttonFactoryReset)
        val buttonShowPermissions = findViewById<Button>(R.id.buttonShowPermissions)
        val buttonClearAllPermissions = findViewById<Button>(R.id.buttonClearAllPermissions)
        val buttonLock = findViewById<Button>(R.id.buttonLock)
        val buttonUnLock = findViewById<Button>(R.id.buttonUnLock)

        // Clear device owner
        buttonClearPermission.setOnClickListener {
            try {
                manager.clearDeviceOwnerApp(componentName.packageName)
                showToast("Device Owner cleared successfully")
            } catch (e: Exception) {
                showToast("Failed to clear Device Owner: ${e.message}")
            }
        }

        // Reboot device
        buttonAction.setOnClickListener {
            try {
                manager.reboot(componentName)
                showToast("Reboot command sent")
            } catch (e: Exception) {
                showToast("Failed to reboot: ${e.message}")
            }
        }

        // Block factory reset
        buttonBlockFactoryReset.setOnClickListener {
            try {
                manager.addUserRestriction(componentName, UserManager.DISALLOW_FACTORY_RESET)
                showToast("Factory Reset blocked successfully")
            } catch (e: Exception) {
                showToast("Failed to block Factory Reset: ${e.message}")
            }
        }

        // Unblock factory reset
        buttonUnblockFactoryReset.setOnClickListener {
            try {
                manager.clearUserRestriction(componentName, UserManager.DISALLOW_FACTORY_RESET)
                showToast("Factory Reset unblocked successfully")
            } catch (e: Exception) {
                showToast("Failed to unblock Factory Reset: ${e.message}")
            }
        }

        // Perform factory reset
        buttonFactoryReset.setOnClickListener {
            try {
                showToast("Factory Reset will start now...")
                manager.wipeData(0) // CAUTION: this resets device immediately
            } catch (e: Exception) {
                showToast("Failed to perform Factory Reset: ${e.message}")
            }
        }

        buttonShowPermissions.setOnClickListener {
            try {
                val restrictions = listOf(
                    UserManager.DISALLOW_FACTORY_RESET,
                    UserManager.DISALLOW_ADD_USER,
                    UserManager.DISALLOW_REMOVE_USER,
                    UserManager.DISALLOW_CONFIG_WIFI,
                    UserManager.DISALLOW_CONFIG_BLUETOOTH,
                    UserManager.DISALLOW_INSTALL_APPS
                )

                val activeRestrictions = restrictions.filter { manager.getUserRestrictions(componentName).getBoolean(it, false) }

                if (activeRestrictions.isEmpty()) {
                    showToast("No active restrictions")
                } else {
                    val message = "Active Restrictions:\n${activeRestrictions.joinToString("\n")}"
                    showToast(message)
                    Log.d(TAG, message)
                }
            } catch (e: Exception) {
                showToast("Failed to get restrictions: ${e.message}")
            }
        }

        // Clear all permissions
        buttonClearAllPermissions.setOnClickListener {
            try {
                val restrictions = listOf(
                    UserManager.DISALLOW_FACTORY_RESET,
                    UserManager.DISALLOW_ADD_USER,
                    UserManager.DISALLOW_REMOVE_USER,
                    UserManager.DISALLOW_CONFIG_WIFI,
                    UserManager.DISALLOW_CONFIG_BLUETOOTH,
                    UserManager.DISALLOW_INSTALL_APPS
                )

                restrictions.forEach {
                    manager.clearUserRestriction(componentName, it)
                }

                showToast("All restrictions cleared")
            } catch (e: Exception) {
                showToast("Failed to clear restrictions: ${e.message}")
            }
        }

        buttonLock.setOnClickListener {
            blockFactoryReset()
        }


        buttonUnLock.setOnClickListener {
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


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}