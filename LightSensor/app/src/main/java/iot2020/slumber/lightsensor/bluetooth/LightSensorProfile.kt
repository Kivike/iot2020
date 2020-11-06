package iot2020.slumber.lightsensor.bluetooth

import java.util.*

object LightSensorProfile {

    val SERVICE_UUID: UUID = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb")

    val LIGHTS_STATUS_UUID: UUID = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb")

    const val BYTE_LIGHTS_OFF: Byte = 0
    const val BYTE_LIGHTS_ON: Byte = 1
}