package iot2020.slumber.lightsensor

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import iot2020.slumber.lightsensor.bluetooth.LightSensorProfile

/**
 * Activity is just initial screen with button to start sensor
 * Pressing the button will check that bluetooth is enabled and asks for necessary permissions
 */
class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE_ENABLE_BT = 8
    private val PERMISSION_CODE = 100

    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.text_status)

        initConnectButton()
        enableBluetooth()
    }

    private fun initConnectButton() {
        val startSensingBtn = findViewById<Button>(R.id.btn_start_sensing)

        startSensingBtn.setOnClickListener() {
            startSensing()
        }
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
            } else {
                startSensing()
            }
        }
    }

    private fun startSensing() {
        val sensingIntent = Intent(applicationContext, SensingActivity::class.java)
        startActivity(sensingIntent)
    }

    private val btReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                LightSensorProfile.ACTION_CLIENT_CONNECTED -> {
                    statusText.text = resources.getString(R.string.status_wait_wakeup)
                }
                LightSensorProfile.ACTION_CLIENT_DISCONNECTED -> {
                    statusText.text = resources.getString(R.string.status_wait_client)
                }
                LightSensorProfile.ACTION_SENSOR_ENABLE -> {
                    startSensing()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val intentFilter = IntentFilter()
        intentFilter.addAction(LightSensorProfile.ACTION_CLIENT_CONNECTED)
        intentFilter.addAction(LightSensorProfile.ACTION_CLIENT_DISCONNECTED)
        intentFilter.addAction(LightSensorProfile.ACTION_SENSOR_ENABLE)
        registerReceiver(btReceiver, intentFilter)

    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(btReceiver)
    }
}
