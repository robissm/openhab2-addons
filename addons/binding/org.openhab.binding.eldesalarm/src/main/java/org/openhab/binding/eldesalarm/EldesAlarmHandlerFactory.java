/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.eldesalarm;

import static org.openhab.binding.eldesalarm.EldesAlarmBindingConstants.THING_TYPE_ELDES_ALARM;

import java.util.List;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.eldesalarm.handler.EldesHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * The {@link EldesAlarmHandlerFactory} is responsible for creating thing
 * handlers.
 *
 * @author Robertas Mikaitis
 */
public class EldesAlarmHandlerFactory extends BaseThingHandlerFactory {

    private static final List<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Lists.newArrayList(THING_TYPE_ELDES_ALARM);
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        logger.debug("supportsThingType(thingTypeUID-{})", thingTypeUID.getAsString());
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        logger.debug("Trying to create handler for {}", thingTypeUID.getAsString());
        if (thingTypeUID.equals(THING_TYPE_ELDES_ALARM)) {
            logger.debug("Handler match for {}", thingTypeUID.getAsString());
            return new EldesHandler(thing);
        }
        logger.debug("No handler match for {}", thingTypeUID.getAsString());
        return null;
    }

}
