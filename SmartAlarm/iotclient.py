import asyncio
import sys
from bleak import BleakScanner
from bleak import BleakClient

class BleClient():
    #BLE service UUID
    SERVICE_UUID = "e8acc040-2b01-11eb-adc1-0242ac120000"

    #BLE characteristic UUIDs
    LIGHTS_STATUS_UUID ="e8acc040-2b01-11eb-adc1-0242ac120001"

    #BLE descriptor UUIDs
    SENSOR_ENABLE_UUID = "e8acc040-2b01-11eb-adc1-0242ac120002"

    #Light status values
    BYTES_LIGHTS_OFF = bytearray(b'\x00\x00')
    BYTES_LIGHTS_ON = bytearray(b'\x00\x01')

    def __init__(self, callback):
        self.light_status_handle = None
        self.callback = callback
        self.client = None
        self.running = False

    def notification_handler(self, handle, data):
        """Simple notification handler which prints the data received."""
        print("{0}: {1}".format(handle, data))

        if (handle == self.light_status_handle):
            if (data == self.BYTES_LIGHTS_OFF):
                self.callback(False)
            elif (data == self.BYTES_LIGHTS_ON):
                self.callback(True)
            else:
                raise("Invalid notification value")

    async def run(self, loop):
        '''
        Connect to BLE server and start receiving notifications
        '''
        self.running = True
        address = await self.select_server_mac()

        print("Using address %s" % address)
        
        self.client = BleakClient(address, loop=loop)
        print("Client created, connecting")

        await self.client.connect(timeout=15.0)

        connected = await self.client.is_connected()
        print("Connection status: ", connected)

        self.light_status_handle = await self.get_light_status_handle()

        await self.client.start_notify(self.LIGHTS_STATUS_UUID, self.notification_handler)
        print("Notify started")

        while self.running and await self.client.is_connected():
            # Keep connected until stop-function is called
            await asyncio.sleep(1)

    async def get_light_status_handle(self):
        '''
        Get light status handle which is the only ID available in notification_handler
        '''
        svcs = await self.client.get_services()
        print("Services:", svcs)

        for service in svcs:
            for char in service.characteristics:
                if (char.uuid == self.LIGHTS_STATUS_UUID):
                    return char.handle

    async def stop(self):
        if (self.client is None):
            raise("Client not started")

        await self.client.stop_notify(self.LIGHTS_STATUS_UUID)
        print("Notify stopped")

        await self.client.disconnect()
        print("BLE disconnected")
        self.running = False

    async def select_server_mac(self):
        '''
        Select MAC address for BLE server
        '''
        if (len(sys.argv) > 1):
            # Note that BLE server MAC address changes even when connecting to same device
            address = sys.argv[1]
        else:
            devices = await BleakScanner.discover()
            i = 1
            for d in devices:
                print("({0}):  {1}\n".format(i, d))
                i = i + 1
            selection = int(input("Which device do you want to use? "))
            address = devices[selection - 1].address
        return address

def main():
    # Move to alarm code
    client = BleClient(test_callback) 
    loop = asyncio.get_event_loop()

    try:
        loop.run_until_complete(client.run(loop))
    except KeyboardInterrupt:
        # Handle Ctrl+C
        loop.run_until_complete(client.stop())

def test_callback(value): # Implement in alarm code
    if (value):
        print("Lighs turned on")
        # Wait 60 sec before turning off sensor
    else:
        print("Lights turned off")
        # Start alarm again
              
if __name__ == "__main__":
    main()
