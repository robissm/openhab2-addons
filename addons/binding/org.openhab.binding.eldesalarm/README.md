# Eldes Alarm Binding

This binding allows you to interface Eldes alarm panels. 
This is in a very early stated and at the moment implementation support only Eldes ESIM364 alarm panel connected through USB for reading zones status.


## Supported Things

This binding supports one thing type:

eldesAlarm - which is a Eldes alarm panel

## USB access on linux
You need to configure USB permissions for hid device to user who runs OpenHab. Onraspbian linux you will need to configure udev. For example(not the most restrictive one) add line below to /etc/udev/rules.d/99-com.rules file:
KERNEL=="hidraw*", SUBSYSTEM=="hidraw", GROUP="input", MODE="0666"

## Thing Configuration

* Required configuration for eldesAlarm thing:

| Parameter       | Description                                   | Default value |
|-----------------|-----------------------------------------------|---------------|
| alarmType       | Eldes alarm type: esim364                     | "esim354"     |     
| connectionType  | Panel connection type: usb                    | "usb"         |
| pollingInterval | PanelpPolling interval in ms                  | 50            |
| login           | Login code                                    | 12            |


## Channels

Eldes alarm support zone type channels 

 | Group |                       Channels                                  |                 
 |  ---  |                          ---                                    |                
 | zone  |                       Z1 to Z76                                 | 

 Channel determines Eldes alarm panel zone we want to use.


## Configuration Example

Let's imagine a setup with Eldes alarm panel ESIM364 connected through USB, with living room PIR attached to zone 1.

*   Things:

```
eldesAlarm:eldesAlarm:esim364  "Eldes Alarm ESIM364 usb connected panel" [alarmType=esim364,connectionType=usb,pollingInterval=50,login=0000]
```

*   Items:

```
Contact living_room_PIR_contact "Living room PIR "  {channel="eldesAlarm:eldesAlarm:esim364:zone#Z1"}
```

