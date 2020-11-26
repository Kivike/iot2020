import datetime
import time
import os
#import winsound
from iotclient import BleClient
import asyncio
import sys
from bleak import BleakScanner
from bleak import BleakClient

async def wake_up_timer(client):
    await asyncio.sleep(60)
    client.stop()
                                

class Timer():
    def __init__(self):
        self.start_time_of_timer = time.time()
        
    def reset_time(self):
        self.start_time_of_timer = time.time()
    
    def get_time(self):
        return_this_variable = 0.0
        return_this_variable = time.time() - self.start_time_of_timer
        return return_this_variable
        

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
            client = BleClient(test_callback) 
            loop = asyncio.get_event_loop()
            
            task1 = loop.create_task(wake_up_timer(client))
 #           winsound.PlaySound("soundalarm.wav",  winsound.SND_ASYNC)  #Alarm sound, this should play until if(test_callback()) return True
            try:
                loop.run_until_complete(client.run(loop))
                i = 1
            except KeyboardInterrupt:
                # Handle Ctrl+C
                loop.run_until_complete(client.stop())
    print("Good morning")

def test_callback(value):
    if(value):
        print("Lights are on")
        # Wait 60 sec before turning off sensor
    if(not value):     
        print("Lights are off")
        task1.cancel()
    #    winsound.PlaySound("soundalarm.wav",  winsound.SND_ASYNC)
        # Start alarm again
        
if __name__ == "__main__":
    main()
