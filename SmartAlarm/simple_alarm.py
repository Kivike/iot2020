import datetime
import time
import os
import asyncio
import sys
if (os.name == "nt"):
    is_windows = True
    import winsound
from iotclient import BleClient

# How long to listen for lights off -signal after receiving lights on -signal
FLICK_WAIT_TIME = 30

class Timer():
    def __init__(self):
        self.client = BleClient(self.sensor_callback)
        self.loop = asyncio.get_event_loop()
        self.active = False
            
    def stop_task(self):
        self.task1.cancel()
     
    async def start_timer(self):
        await asyncio.sleep(FLICK_WAIT_TIME)
        print("Stopping client")
        await self.client.stop()
        self.active = False
        
    def play_sound(self):
        #Alarm sound, this should play until callback return True
        if (is_windows):
            winsound.PlaySound("soundalarm.wav",  winsound.SND_ASYNC | winsound.SND_LOOP)
        else:
            print("PLAY SOUND")

    def stop_sound(self):
        if (is_windows):
            winsound.PlaySound(None,  winsound.SND_PURGE)
        else:
            print("STOP SOUND")

    async def connect_sensor(self):
        await self.client.connect(self.loop)

    async def start_loop(self):
        try:
            await self.client.run(self.loop)
            self.play_sound()
            self.active = True

            while (self.active):
                await asyncio.sleep(1)
        finally:
            await self.client.stop()
            
    def sensor_callback(self, value):
        if (value):
            # Stop alarm
            print("Lights are on, waiting for %i seconds" % FLICK_WAIT_TIME)
            self.task1 = self.loop.create_task(self.start_timer())
            self.stop_sound()
        if (not value):    
            # Start alarm again 
            print("Lights are off") #Cancel task here
            self.task1.cancel()
            
            if (is_windows):
                self.play_sound()

def main():
    wake_up_time = input("When do you want to wake up? HH:MM ")
    wake_up_time = wake_up_time.split(":")
    wake_up_hour = int(wake_up_time[0])
    wake_up_minute = int(wake_up_time[1])

    current_time = datetime.datetime.now()

    wake_up_dt = current_time.replace(
        hour=wake_up_hour,
        minute=wake_up_minute,
        second=0,
        microsecond=0
    )
    if (wake_up_dt < current_time):
        wake_up_dt = wake_up_dt + datetime.timedelta(days=1)

    print("Wake up time: " + str(wake_up_dt))

    timer = Timer()
    loop = asyncio.get_event_loop()
    bt_connected = False

    while (True): #Alarm loop
        current_time = datetime.datetime.now()
        sec_to_wake_up = (wake_up_dt - current_time).total_seconds()

        if (not bt_connected and sec_to_wake_up < 15):
            print("Connecting to sensor..")
            loop.run_until_complete(timer.connect_sensor())
            print("Connected")
            bt_connected = True

        if (sec_to_wake_up <= 0):
            print("Wake up")
            loop.run_until_complete(timer.start_loop())
            break

        time.sleep(1)
            
    print("Good morning")

if __name__ == "__main__":
    main()
