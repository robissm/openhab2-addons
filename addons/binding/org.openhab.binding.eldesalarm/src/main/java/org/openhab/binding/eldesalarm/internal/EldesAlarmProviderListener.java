package org.openhab.binding.eldesalarm.internal;

public interface EldesAlarmProviderListener {
    public void onZoneStatusUpdated(String zone, boolean closed);
}
