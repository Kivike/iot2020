import asyncio
from bleak import BleakScanner
from bleak import BleakClient


CHARACTERISTIC_UUID = "f000aa65-0451-4000-b000-000000000000"  # <--- Change to the characteristic you want to enable notifications from.

def notification_handler(sender, data):
    """Simple notification handler which prints the data received."""
    print("{0}: {1}".format(sender, data))

#Scan for devices
async def run():
    address = "66:80:4D:1A:2F:03" #address if you want to use specific device, oneplus = 66:80:4D:1A:2F:03
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
        print("Connection status: ", connected)

        await client.start_notify(CHARACTERISTIC_UUID, notification_handler)
        print("Notify started")
        #await write_gatt_descriptor(,)	#handle, data. Dataan start sensing
        await asyncio.sleep(5.0)
        await client.stop_notify(CHARACTERISTIC_UUID)
        print("Notify stopped")
        await client.disconnect()

def main():
    loop = asyncio.get_event_loop()
    loop.run_until_complete(run())	

if __name__ == "__main__":
    main()
