package iot2020.slumber.lightsensor.bluetooth

import java.util.*

object LightSensorProfile {

    /**
     * BLE service UUID
     */
    val SERVICE_UUID: UUID = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb")

    /**
     * BLE characteristic UUIDs
     */
    val LIGHTS_STATUS_UUID: UUID = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb")

    /**
     * BLE descriptor UUIDs
     */
    val SENSOR_ENABLE: UUID = UUID.fromString("0000c2fb-0000-1000-8000-00805f9b34fb")

    /**
     * BLE characteristic values
     */
    val BYTES_LIGHTS_OFF: ByteArray = byteArrayOf(0x00, 0x00)
    val BYTES_LIGHTS_ON: ByteArray = byteArrayOf(0x00, 0x01)

    /**
     * BLE descriptor values
     */
    val BYTES_SENSOR_DISABLE: ByteArray = byteArrayOf(0x02, 0x00)
    val BYTES_SENSOR_ENABLE: ByteArray = byteArrayOf(0x02, 0x01)

    /**
     * Broadcast actions
     */
    const val ACTION_CLIENT_CONNECTED = "iot2020.slumber.lightsensor.ACTION_CLIENT_CONNECTED"
    const val ACTION_CLIENT_DISCONNECTED = "iot2020.slumber.lightsensor.ACTION_CLIENT_DISCONNECTED"
    const val ACTION_SENSOR_ENABLE = "iot2020.slumber.lightsensor.ACTION_SENSOR_ENABLE"
    const val ACTION_SENSOR_DISABLE = "iot2020.slumber.lightsensor.ACTION_SENSOR_DISABLE"
}