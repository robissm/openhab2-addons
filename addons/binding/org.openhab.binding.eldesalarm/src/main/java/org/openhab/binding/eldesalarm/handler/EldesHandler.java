/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.eldesalarm.handler;

import static org.openhab.binding.eldesalarm.EldesAlarmBindingConstants.*;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.eldesalarm.internal.EldesAlarmProvider;
import org.openhab.binding.eldesalarm.internal.EldesAlarmProviderListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EldesHandler} is base class for Eldes alarm support
 *
 * @author Robertas Mikaitis
 */
public class EldesHandler extends BaseThingHandler implements EldesAlarmProviderListener {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private EldesAlarmProvider eldesAlarmProvider;
    private String connectionType;
    private String alarmType;
    private String login;
    private int pollingInterval;
    private ZoneStateHolder zoneStateHolder;

    public EldesHandler(Thing thing) {
        super(thing);
        logger.debug("EldesHandler(thing-{})", thing);
    }

    @Override
    public void initialize() {
        logger.debug("initialize");
        checkConfiguration();
        eldesAlarmProvider = initializeEldesAlarmProvider();
        zoneStateHolder = new ZoneStateHolder();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command: {} on channelGroup {} on channel {}", command.toFullString(),
                channelUID.getGroupId(), channelUID.getIdWithoutGroup());

        if (!verifyChannel(channelUID)) {
            return;
        }

        logger.debug("This binding support only switch type - nothing to be done in handleCommand");

    }

    private boolean verifyChannel(ChannelUID channelUID) {
        if (!isChannelGroupValid(channelUID) || !isChannelValid(channelUID)) {
            logger.warn("Channel group or channel is invalid. Probably configuration problem");
            return false;
        }
        return true;
    }

    private boolean isChannelGroupValid(ChannelUID channelUID) {
        if (!channelUID.isInGroup()) {
            logger.debug("Defined channel not in group: {}", channelUID.getAsString());
            return false;
        }
        boolean channelGroupValid = SUPPORTED_CHANNEL_GROUPS.contains(channelUID.getGroupId());
        logger.debug("Defined channel in group: {}. Valid: {}", channelUID.getGroupId(), channelGroupValid);

        return channelGroupValid;
    }

    private boolean isChannelValid(ChannelUID channelUID) {
        boolean channelValid = SUPPORTED_CHANNELS.contains(channelUID.getIdWithoutGroup());
        logger.debug("Is channel {} in supported channels: {}", channelUID.getIdWithoutGroup(), channelValid);
        return channelValid;
    }

    protected void checkConfiguration() {
        Configuration configuration = getConfig();
        alarmType = configuration.get(ALARM_TYPE).toString();
        if (!SUPPORTED_ALARM_TYPES.contains(alarmType)) {
            throw new IllegalArgumentException("Unsupported alarm type-" + connectionType);
        }
        login = configuration.get(LOGIN).toString();

        connectionType = configuration.get(CONNECTION_TYPE).toString();
        if (!SUPPORTED_CONNECTION_TYPES.contains(connectionType)) {
            throw new IllegalArgumentException("Unsupported connection type-" + connectionType);
        }

        pollingInterval = (int) Float.parseFloat(configuration.get(POLLING_INTERVAL).toString());
        if (pollingInterval > POLLING_INTERVAL_MAX) {
            pollingInterval = POLLING_INTERVAL_MAX;
        } else if (pollingInterval < POLLING_INTERVAL_MIN) {
            pollingInterval = POLLING_INTERVAL_MIN;
        }

        try {
            updateStatus(ThingStatus.ONLINE);
        } catch (IllegalArgumentException | SecurityException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "An exception occurred while configuring binding. Check binding configuration. Exception: "
                            + e.getMessage());
        }

    }

    private EldesAlarmProvider initializeEldesAlarmProvider() {
        EldesAlarmProvider eldesAlarmProvider = null;
        logger.debug("initializing eldes alarm provider for alarmType {}", alarmType);
        try {
            eldesAlarmProvider = new EldesAlarmProvider(this, alarmType, connectionType, login, pollingInterval);
            eldesAlarmProvider.start();
        } catch (Exception ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Failed to initialize eldes alarm provider: " + ex.getMessage());
        }
        logger.debug("got eldesAlarmProvider {}", eldesAlarmProvider);
        return eldesAlarmProvider;
    }

    @Override
    public void dispose() {
        if (eldesAlarmProvider != null) {
            eldesAlarmProvider.stop();
        }
        super.dispose();
    }

    @Override
    public void onZoneStatusUpdated(String zone, boolean open) {
        OpenClosedType state = OpenClosedType.CLOSED;
        if (open) {
            state = OpenClosedType.OPEN;
        }
        ChannelUID channel = zoneStateHolder.getChannelForZone(zone);
        if (channel != null) {
            if (zoneStateHolder.zoneStateChanged(zone, open)) {
                logger.debug("updating channel {} with state {}", channel, state);
                updateState(channel, state);
            } else {
                logger.debug("skipping update for channel {} with unchanged state {}", channel, state);
            }
        } else {
            logger.debug("no channel set for zone {}. New state {}", zone, state);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("channel linked {}", channelUID.getAsString());
        if (!verifyChannel(channelUID)) {
            return;
        }
        String channelGroup = channelUID.getGroupId();

        if (channelGroup.equals(CHANNEL_GROUP_ZONE)) {
            zoneStateHolder.addZone(channelUID.getIdWithoutGroup(), channelUID);

        }
        super.channelLinked(channelUID);
    }

}
