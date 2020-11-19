package iot2020.slumber.lightsensor.bluetooth

import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log

/**
 * Bluetooth Lower Energy (BLE) server that can notify client about illuminance change
 *
 * @see LightSensorProfile
 */
class BleServer(private val context: Context, private val btManager: BluetoothManager) : Service() {

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

    /**
     * Notify clients about light status change
     */
    fun notifyChange(isLightsOn: Boolean) {
        if (registeredDevices.isEmpty()) {
            return
        }

        val lightCharacteristic = btGattServer
                .getService(LightSensorProfile.SERVICE_UUID)
                .getCharacteristic(LightSensorProfile.LIGHTS_STATUS_UUID)

        lightCharacteristic.value =
                if (isLightsOn) LightSensorProfile.BYTES_LIGHTS_ON
                else LightSensorProfile.BYTES_LIGHTS_OFF

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
        override fun onStartFailure(errorCode: Int) {
            Log.e(LOG_TAG, "BLE advertise failed")
        }
    }

    /**
     * Handle device registration
     *
     * Forces single client mode
     */
    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            Log.i(LOG_TAG, "Connection state change")

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (registeredDevices.isEmpty()) {
                    // Only single client allowed
                    Log.i(LOG_TAG, "Client connected")

                    registeredDevices.add(device)
                    broadcastUpdate(LightSensorProfile.ACTION_CLIENT_CONNECTED)
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(LOG_TAG, "Client disconnected")
                registeredDevices.remove(device)

                if (registeredDevices.isEmpty()) {
                    broadcastUpdate(LightSensorProfile.ACTION_CLIENT_DISCONNECTED)
                }
            }
        }

        /**
         * Handle enabling/disabling of the light sensor
         */
        override fun onDescriptorWriteRequest(
                device: BluetoothDevice,
                requestId: Int,
                descriptor: BluetoothGattDescriptor,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray?
        ) {
            if (LightSensorProfile.SENSOR_ENABLE == descriptor.uuid) {
                when (value) {
                    LightSensorProfile.BYTES_SENSOR_ENABLE -> {
                        broadcastUpdate(LightSensorProfile.ACTION_SENSOR_ENABLE)
                    }
                    LightSensorProfile.BYTES_SENSOR_DISABLE -> {
                        broadcastUpdate(LightSensorProfile.ACTION_SENSOR_DISABLE)
                    }
                }
                if (responseNeeded) {
                    btGattServer.sendResponse(
                            device,
                            requestId,
                            BluetoothGatt.GATT_SUCCESS,
                            0,
                            null
                    )
                }
            } else {
                btGattServer.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0,
                        null
                )
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

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        //Mandatory implementation
        return null
    }
}