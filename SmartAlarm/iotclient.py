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
        device = await self.select_server_device()

        print("Using device %s" % device.address)

        self.client = BleakClient(device, loop=loop, address_type="random")
        print("Client created, connecting")

        connected = await self.client.connect()

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
        if (self.client is not None and await self.client.is_connected()):
            await self.client.stop_notify(self.LIGHTS_STATUS_UUID)
            print("Notify stopped")

            await self.client.disconnect()
            print("BLE disconnected")

        self.running = False

    async def select_server_device(self):
        '''
        Select MAC address for BLE server
        '''
        devices = await BleakScanner.discover()
        i = 1
        for d in devices:
            print("({0}):  {1}\n".format(i, d))
            i = i + 1

        for d in devices:
            uuids = d.metadata['uuids']

            if self.SERVICE_UUID in uuids:
                return d

        selection = int(input("Which device do you want to use? "))
        return devices[selection - 1]

def main():
    # Move to alarm code
    client = BleClient(test_callback) 
    loop = asyncio.get_event_loop()

    try:
        loop.run_until_complete(client.run(loop))
    finally:
        loop.run_until_complete(client.stop())

def test_callback(value): # Implement in alarm code
    if (value):
        print("Lights turned on")
        # Wait 60 sec before turning off sensor
    else:
        print("Lights turned off")
        # Start alarm again
              
if __name__ == "__main__":
    main()
