import datetime
import time
import os
#import winsound
from iotclient import BleClient
import asyncio
import sys
from bleak import BleakScanner
from bleak import BleakClient
                                

class Timer():
    def __init__(self):

        client = BleClient(self.test_callback)
        loop = asyncio.get_event_loop()
            
 #      winsound.PlaySound("soundalarm.wav",  winsound.SND_ASYNC)  #Alarm sound, this should play until if(test_callback()) return True
        try:
            self.task1 = loop.create_task(self.start_timer())
            loop.run_until_complete(client.run(loop))
        except finally:
            loop.run_until_complete(client.stop())
            
    def stop_task(self):
        self.task1.cancel()
     
    async def start_timer(self):
        await asyncio.sleep(60)
        client.stop()
        
    def test_callback(self, value):
        if(value):
            print("Lights are on")
            start_timer()
            
            # Wait 60 sec before turning off sensor
        if(not value):     
            print("Lights are off")
            task1.cancel()
        #    winsound.PlaySound("soundalarm.wav",  winsound.SND_ASYNC)
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
            timeri = Timer()
            i = 1
            
            
    print("Good morning")


        
if __name__ == "__main__":
    main()
