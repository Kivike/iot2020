import datetime
import time
import os
import winsound
from iotclient import BleClient
import asyncio
import sys
from bleak import BleakScanner
from bleak import BleakClient


def main():
    wake_up_time = input("When do you want to wake up? HH:MM:SS ")
    wake_up_time = wake_up_time.split(":")

    while(True):
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
            timer = 0;
            while(True):            #if ble server sends light are on, stop sound
                time.sleep(1)
                
                if(test_callback()):               #This should stop the alarm
                    winsound.PlaySound("soundalarm.wav",  winsound.SND_PURGE)
                    timer +=1
                elif():               #This should restart the alarm and reset the timer
                    winsound.PlaySound("soundalarm.wav",  winsound.SND_ASYNC)
                    timer = 0
                else:
                    print("You shouldn't be here.")
                if(timer == 120):    
                    break
            break
    print("Good morning")

def test_callback(value): 
    if (value):
        print("Lighs turned on")
        return True
        # Wait 60 sec before turning off sensor
    else:
        print("Lights turned off")
        return False
        # Start alarm again
        
if __name__ == "__main__":
    main()
