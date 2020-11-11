import asyncio
from bleak import BleakScanner
from bleak import BleakClient


#Scan for devices
async def run():
    devices = await BleakScanner.discover()
    i = 1
    for d in devices:
        print("(", i, ")", d, "\n")
        i = i + 1
    selection = input("Which device do you want to use?")
    client = BleakClient(devices[selection-1])
    try:
        await client.connect()
    except Exception as e:
        print(e)
    finally:
        print("Connection status: ", client.is_connected())
        print("Reading in action: ", client.read_gatt_char())
        await client.disconnect()

def main():
    loop = asyncio.get_event_loop()
    loop.run_until_complete(run())	

if __name__ == "__main__":
    main()
