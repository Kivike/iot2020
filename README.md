# Light-controlled smart alarm
## Internet of Things course project (Autumn 2020)

### Python (SmartAlarm)
- Python version 3.7
- Tested with Windows and Linux

#### Installation
```
pip install -r SmartAlarm/requirements.py
```

### Android (LightSensor)
- Min SDK version 21 (Android 5.0 Lollipop)

### Usage
1. Start the Android app (LightSensor)
    1. Allow requested permissions and enable bluetooth when asked
    1. Wait for SmartAlarm to connect (continue to step 2)
        * Optionally just press the displayed button to start light sensor manually
1. Start the Python app (SmartAlarm)
    1. Run ```python SmartAlarm/simple_alarm.py```
    1. Input wanted wake-up time
1. When alarm starts ringing, turn on the lights and keep them on for at least 30 seconds
