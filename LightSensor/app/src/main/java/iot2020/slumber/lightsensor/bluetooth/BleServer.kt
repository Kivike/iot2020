package iot2020.slumber.lightsensor.bluetooth

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.ParcelUuid
import android.util.Log

/**
 * Bluetooth Lower Energy (BLE) server that supports notifying clients on demand
 *
 * No support for characteristic/description read requests
 */
class BleServer(private val context: Context, private val btManager: BluetoothManager) {

    lateinit var btGattServer: BluetoothGattServer

    private val registeredDevices = mutableSetOf<BluetoothDevice>()

    var onBtClientsChanged: ((devices: Array<BluetoothDevice>) -> Unit) = {}

    val LOG_TAG = "LightSensorBleServer"

    fun start() {
        Log.i(LOG_TAG, "BLE server started")

        startAdvertising()
        startServer()

        val btIntentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(btReceiver, btIntentFilter)
    }

    fun stop() {
        stopAdvertising()
        stopServer()

        context.unregisterReceiver(btReceiver)
    }

    fun notifyChange(isLightsOn: Boolean) {
        if (registeredDevices.isEmpty()) {
            return
        }

        val lightCharacteristic = btGattServer
                .getService(LightSensorProfile.SERVICE_UUID)
                .getCharacteristic(LightSensorProfile.LIGHTS_STATUS_UUID)

        val valueBytes = ByteArray(1)
        valueBytes[0] = if (isLightsOn) LightSensorProfile.BYTE_LIGHTS_ON else LightSensorProfile.BYTE_LIGHTS_OFF
        lightCharacteristic.value = valueBytes

        for (device in registeredDevices) {
            btGattServer.notifyCharacteristicChanged(device, lightCharacteristic, false)
        }
    }

    private fun startAdvertising() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        val leAdvertiser = adapter.bluetoothLeAdvertiser

        leAdvertiser.let {
            val settings = AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                    .setConnectable(true)
                    .setTimeout(0)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                    .build()

            val data = AdvertiseData.Builder()
                    .setIncludeDeviceName(true)
                    .setIncludeTxPowerLevel(true)
                    .addServiceUuid(ParcelUuid(LightSensorProfile.SERVICE_UUID))
                    .build()

            it.startAdvertising(settings, data, advertiseCallback)
        }
    }

    private fun stopAdvertising() {
        val leAdvertiser = BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser
        leAdvertiser.stopAdvertising(advertiseCallback)
    }

    private fun startServer() {
        btGattServer = btManager.openGattServer(context, gattServerCallback)
    }

    private fun stopServer() {
        btGattServer.close()
    }

    private val advertiseCallback = object : AdvertiseCallback() {

    }

    /**
     * Handle device registration
     */
    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            Log.i(LOG_TAG, "Connection state change")

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(LOG_TAG, "Client connected")
                registeredDevices.add(device)
                onBtClientsChanged(registeredDevices.toTypedArray())
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(LOG_TAG, "Client disconnected")
                registeredDevices.remove(device)
                onBtClientsChanged(registeredDevices.toTypedArray())
            }
        }
    }

    /**
     * Stop server when bluetooth is disabled
     */
    private val btReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)) {
                BluetoothAdapter.STATE_OFF -> {
                    Log.i(LOG_TAG, "Bluetooth disabled")
                    stop()
                }
                BluetoothAdapter.STATE_ON -> {
                    Log.i(LOG_TAG, "Bluetooth enabled")
                    start()
                }
            }
        }
    }
}