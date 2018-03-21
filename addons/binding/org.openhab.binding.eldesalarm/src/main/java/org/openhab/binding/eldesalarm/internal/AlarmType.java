package org.openhab.binding.eldesalarm.internal;

import static org.openhab.binding.eldesalarm.EldesAlarmBindingConstants.ALARM_TYPE_ESIM364;

public enum AlarmType {
    ESIM364(ALARM_TYPE_ESIM364, (short) 0xc201, (short) 0x1318);

    private String alarmType;
    private short vendorId;
    private short productId;

    private AlarmType(String alarmType, short vendorId, short productId) {
        this.alarmType = alarmType;
        this.vendorId = vendorId;
        this.productId = productId;
    }

    public String getAlarmType() {
        return alarmType;
    }

    public short getVendorId() {
        return vendorId;
    }

    public short getProductId() {
        return productId;
    }

    public static AlarmType getAlarmType(String alarmType) {
        for (AlarmType type : AlarmType.values()) {
            if (type.getAlarmType().equals(alarmType)) {
                return type;
            }
        }
        return null;
    }
}
