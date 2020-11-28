import datetime
import time
import os

if (os.name == "nt"):
    is_windows = True
    import winsound

from iotclient import BleClient
import asyncio
import sys
from bleak import BleakScanner
from bleak import BleakClient
                                

class Timer():
    def __init__(self):

        self.client = BleClient(self.sensor_callback)
        self.loop = asyncio.get_event_loop()
        self.active = False
            
    def stop_task(self):
        self.task1.cancel()
     
    async def start_timer(self):
        await asyncio.sleep(30)
        print("Stopping client")
        await self.client.stop()
        self.active = False
        
    def play_sound(self):
        #Alarm sound, this should play until callback return True
        if (is_windows):
            winsound.PlaySound("soundalarm.wav",  winsound.SND_ASYNC)
        else:
            print("PLAY SOUND")

    async def start_loop(self):
        try:
            #self.task1 = self.loop.create_task(self.start_timer())           #Does this need while loop
            await self.client.run(self.loop)
            self.play_sound()
            self.active = True

            while(self.active):
                await asyncio.sleep(1)
        finally:
            await self.client.stop()
            
    def sensor_callback(self, value):
        if(value):
            print("Lights are on")  #Start task here or start in the init
            self.task1 = self.loop.create_task(self.start_timer())
            
            # Wait 60 sec before turning off sensor
        if(not value):     
            print("Lights are off") #Cancel task here
            self.task1.cancel()
            
            if (is_windows):
                winsound.PlaySound("soundalarm.wav",  winsound.SND_ASYNC)
            # Start alarm again 

def main():
    wake_up_time = input("When do you want to wake up? HH:MM:SS ")
    wake_up_time = wake_up_time.split(":")
    i = 0
    while(i == 0):
        time.sleep(3)
        current_time = datetime.datetime.now()
        now = current_time.strftime("%H:%M:%S")
        now = now.split(":")
        if(int(wake_up_time[0]) == int(now[0]) and int(wake_up_time[1]) == int(now[1])):    #Alarm loop
            print("Wake up")
            timer = Timer()
            loop = asyncio.get_event_loop()
            loop.run_until_complete(timer.start_loop())
            i = 1
            
    print("Good morning")

if __name__ == "__main__":
    main()
