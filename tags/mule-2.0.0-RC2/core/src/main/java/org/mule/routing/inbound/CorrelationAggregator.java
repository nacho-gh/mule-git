/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.routing.inbound;

import org.mule.api.MuleEvent;

/**
 * <code>CorrelationAggregator</code> uses the CorrelationID and
 * CorrelationGroupSize properties of the {@link org.mule.api.MuleMessage} to manage
 * message groups.
 */
public abstract class CorrelationAggregator extends AbstractEventAggregator
{

    /**
     * Creates a new EventGroup that will expect the number of events as returned by
     * {@link org.mule.api.transport.MessageAdapter#getCorrelationGroupSize()}.
     */
    // //@Override
    protected EventGroup createEventGroup(MuleEvent event, Object groupId)
    {
        return new EventGroup(groupId, event.getMessage().getCorrelationGroupSize());
    }

    /**
     * @see AbstractEventAggregator#shouldAggregateEvents(EventGroup)
     * @return <code>true</code> if the correlation size is not set or exactly the
     *         expected size of the event group.
     */
    protected boolean shouldAggregateEvents(EventGroup events)
    {
        int size = events.expectedSize();

        if (size == -1)
        {
            logger.warn("Correlation Group Size not set, but CorrelationAggregator is being used."
                            + " Message is being forwarded");
            return true;
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Correlation size is " + size + ". Current event group size is " + events.size()
                            + " for correlation " + events.getGroupId());
        }

        return size == events.size();
    }

}
