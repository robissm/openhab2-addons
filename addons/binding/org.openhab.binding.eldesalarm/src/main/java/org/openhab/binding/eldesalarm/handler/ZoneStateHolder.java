/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.eldesalarm.handler;


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.smarthome.core.thing.ChannelUID;

/**
 * The {@link ZoneStateHolder} is a class where Eldes alarm zone state is held
 *
 * @author Robertas Mikaitis
 */
public class ZoneStateHolder {
    private Map<ChannelUID, String> zones = new HashMap<>();
    private Map<String, Boolean> states = new HashMap<>();

    public String getZone(ChannelUID channel) {
        return zones.get(channel);
    }

    public ChannelUID getChannelForZone(String zone) {
        Optional<Entry<ChannelUID, String>> result = zones.entrySet().stream()
                .filter(entry -> entry.getValue().equals(zone)).findFirst();
        if (result.isPresent()) {
            return result.get().getKey();
        }
        return null;
    }
    
    public boolean zoneStateChanged(String zone, Boolean newState) {
        Boolean state = states.get(zone);
        states.put(zone, newState);
        if (state == null || state != newState) {
            return true;
        }
        return false;
    }

    public void addZone(String zone, ChannelUID channel) {
        zones.put(channel, zone);
    }
}
