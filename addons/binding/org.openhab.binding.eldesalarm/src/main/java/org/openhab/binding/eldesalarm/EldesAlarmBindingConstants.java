/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.eldesalarm;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.Lists;

/**
 * The {@link EldesAlarmBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Robertas Mikaitis
 */
public class EldesAlarmBindingConstants {

    public static final String BINDING_ID = "eldesAlarm";
    public static final String THING_ID = "eldesAlarm";

    public static final String ALARM_TYPE = "alarmType";
    public static final String LOGIN = "login";
    public static final String CONNECTION_TYPE = "connectionType";

    public static final String POLLING_INTERVAL = "pollingInterval";

    public static final int POLLING_INTERVAL_MIN = 100;
    public static final int POLLING_INTERVAL_MAX = 60000;

    public static final String ALARM_TYPE_ESIM364 = "esim364";
    public static final List<String> SUPPORTED_ALARM_TYPES = Lists.newArrayList(ALARM_TYPE_ESIM364);

    public static final String CONNECTION_TYPE_USB = "usb";
    public static final List<String> SUPPORTED_CONNECTION_TYPES = Lists.newArrayList(CONNECTION_TYPE_USB);

    public static final String CHANNEL_GROUP_ZONE = "zone";
    public static final List<String> SUPPORTED_CHANNEL_GROUPS = Lists.newArrayList(CHANNEL_GROUP_ZONE);

    public static final List<String> SUPPORTED_CHANNELS = getSupportedChannels();

    public static List<String> getSupportedChannels() {
        List<String> channels = new ArrayList<>();
        for (int i = 0; i < 77; i++) {
            channels.add("Z" + i);
        }
        return channels;
    }

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ELDES_ALARM = new ThingTypeUID(BINDING_ID, THING_ID);
}
