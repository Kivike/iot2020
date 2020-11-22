package iot2020.slumber.lightsensor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import iot2020.slumber.lightsensor.bluetooth.BleServer
import iot2020.slumber.lightsensor.bluetooth.LightSensorProfile

class ConnectionActivity : AppCompatActivity() {

    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection)

        statusText = findViewById(R.id.text_status)

        startBleServer()
        initSensingButton()
    }

    private fun startBleServer() {
        val intent = Intent(this, BleServer::class.java)
        startService(intent)
    }

    private fun initSensingButton() {
        val startSensingBtn = findViewById<Button>(R.id.btn_start_sensing)

        startSensingBtn.setOnClickListener() {
            startSensing()
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