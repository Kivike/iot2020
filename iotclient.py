import asyncio
from bleak import BleakScanner
from bleak import BleakClient

#BLE service UUID
SERVICE_UUID = "00001805-0000-1000-8000-00805f9b34fb"

#BLE characteristic UUIDs
LIGHTS_STATUS_UUID ="00002a2b-0000-1000-8000-00805f9b34fb"

#BLE descriptor UUIDs
SENSOR_ENABLE_UUID = "0000c2fb-0000-1000-8000-00805f9b34fb"


def notification_handler(sender, data):
    """Simple notification handler which prints the data received."""
    print("{0}: {1}".format(sender, data))

#Scan for devices
async def run():
    address = "" #address if you want to use specific device, xx:xx:xx:xx:xx:xx
    use_spef_dev = 0              
    if(use_spef_dev == 0):        #Discovery step, can be skipped with use_spef_dev == 1
        devices = await BleakScanner.discover()
        i = 1
        for d in devices:
            print("({0}):  {1}\n".format(i, d))
            i = i + 1
        selection = int(input("Which device do you want to use? "))
        address = devices[selection - 1]

    async with BleakClient(address) as client:
        connected = await client.is_connected()
							#is connect part of the mobile app done here?
							#tell the mobile app to start sensing
							#read the light status and print it just to be sure
							#after that, timer, alarm part
        print("Connection status: ", connected)
        svcs = await client.get_services()
        print("Services:", svcs)

        await client.read_gatt_char()
        await client.write_gatt_descriptor(SENSOR_ENABLE_UUID, 1)
        await client.start_notify(LIGHTS_STATUS_UUID, notification_handler)
        print("Notify started")
        await asyncio.sleep(5.0)
        await client.stop_notify(LIGHTS_STATUS_UUID)
        print("Notify stopped")
        await client.disconnect()

def main():
    loop = asyncio.get_event_loop()
    loop.run_until_complete(run())	

if __name__ == "__main__":
    main()
