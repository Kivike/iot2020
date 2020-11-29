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
            print("Lights are on")
            self.task1 = self.loop.create_task(self.start_timer())
            self.stop_sound()
        if (not value):    
            # Start alarm again 
            print("Lights are off") #Cancel task here
            self.task1.cancel()
            
            if (is_windows):
                self.play_sound()

def main():
    wake_up_time = input("When do you want to wake up? HH:MM:SS ")
    wake_up_time = wake_up_time.split(":")

    while (True): #Alarm loop
        time.sleep(3)
        current_time = datetime.datetime.now()
        now = current_time.strftime("%H:%M:%S")
        now = now.split(":")
        if(int(wake_up_time[0]) == int(now[0]) and int(wake_up_time[1]) == int(now[1])):
            print("Wake up")
            timer = Timer()
            loop = asyncio.get_event_loop()
            loop.run_until_complete(timer.start_loop())
            break
            
    print("Good morning")

if __name__ == "__main__":
    main()
