import datetime
import time
import os
import winsound
from iotclient import BleClient
import asyncio
import sys
from bleak import BleakScanner
from bleak import BleakClient

class Timer():
    def __init__(self):
        self.current_time = time.time()
        
    def reset_time(self):
        self.current_time = time.time()
    
    def get_time(self):
        time = time.time() - current_time
        return time
        

def main():
    wake_up_time = input("When do you want to wake up? HH:MM:SS ")
    wake_up_time = wake_up_time.split(":")

    while(True):
        time.sleep(3)
        current_time = datetime.datetime.now()
        now = current_time.strftime("%H:%M:%S")
        now = now.split(":")
        if(int(wake_up_time[0]) == int(now[0]) and int(wake_up_time[1]) == int(now[1])):    #Alarm loop
            print("Wake up")
            client = BleClient(test_callback) 
            loop = asyncio.get_event_loop()
            winsound.PlaySound("soundalarm.wav",  winsound.SND_ASYNC)  #Alarm sound, this should play until if(test_callback()) return True
            try:
                loop.run_until_complete(client.run(loop))
            except KeyboardInterrupt:
                # Handle Ctrl+C
                loop.run_until_complete(client.stop())
    print("Good morning")

def test_callback(value): 
    if (value):
        print("Lighs turned on")
        winsound.PlaySound("soundalarm.wav",  winsound.SND_PURGE)
        timer = Timer()
        while(True):
            if(not value):
                timer = 0
                winsound.PlaySound("soundalarm.wav",  winsound.SND_ASYNC)
            if(timer.get_time() >= 60):
                break
            timer += 1
            time.sleep(1)
        # Wait 60 sec before turning off sensor
        
    else:
        print("Lights turned off")
        winsound.PlaySound("soundalarm.wav",  winsound.SND_ASYNC)
        # Start alarm again
        
if __name__ == "__main__":
    main()
