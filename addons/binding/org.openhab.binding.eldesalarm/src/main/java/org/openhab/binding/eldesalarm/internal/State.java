package org.openhab.binding.eldesalarm.internal;

public enum State {
    NO_HID_DEVICE,
    OPENING_HID_DEVICE,
    HID_DEVICE_OPEN,
    LOGIN_REQUESTED,
    LOGED_IN,
    ZONE_STATUS_REQUESTED,
    ZONE_STATUS_PART_1_RECEIVED,
    ZONE_STATUS_PART_2_RECEIVED
}
