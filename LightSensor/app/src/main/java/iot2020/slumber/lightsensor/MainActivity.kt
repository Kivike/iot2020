package iot2020.slumber.lightsensor

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Activity checks that bluetooth is enabled and asks for necessary permissions
 */
class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE_ENABLE_BT = 8
    private val PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        enableBluetooth()
    }

    private fun enableBluetooth() {
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_CODE_ENABLE_BT)
        } else {
            onBluetoothEnabled()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                onBluetoothEnabled()
            } else {
                val msg = "Failed to enable bluetooth"
                Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun onBluetoothEnabled() {
        requestPermissions()
    }

    /**
     * Request permissions needed for Bluetooth
     */
    private fun requestPermissions() {
        val permissions = mutableListOf<String>()
        permissions.add(Manifest.permission.BLUETOOTH)
        permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)

        var hasAllPermissions = true

        for (perm in permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                hasAllPermissions = false
                break
            }
        }
        if (!hasAllPermissions) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_CODE)
        } else {
            nextActivity()
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.contains(PackageManager.PERMISSION_DENIED)) {
                Toast.makeText(
                        applicationContext,
                        "Need permissions!",
                        Toast.LENGTH_SHORT
                ).show()
                finish()
            }
            nextActivity()
        }
    }

    private fun nextActivity() {
        val intent = Intent(applicationContext, ConnectionActivity::class.java)
        startActivity(intent)
    }
}
