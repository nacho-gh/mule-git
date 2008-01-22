/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.impl.routing.inbound;

import org.mule.api.Event;
import org.mule.api.MuleMessage;
import org.mule.impl.DefaultMuleMessage;
import org.mule.impl.routing.AggregationException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.SerializationUtils;

public class MessageChunkingAggregator extends CorrelationAggregator
{
    public static final int DEFAULT_BUFFER_SIZE = 4096;
    
    protected final Comparator eventComparator = new CorrelationSequenceComparator();

    public MessageChunkingAggregator()
    {
        super();
    }

    /**
     * This method is invoked if the shouldAggregate method is called and returns
     * true. Once this method returns an aggregated message the event group is
     * removed from the router
     * 
     * @param events the event group for this request
     * @return an aggregated message
     * @throws org.mule.impl.routing.AggregationException if the aggregation fails. in
     *             this scenario the whole event group is removed and passed to the
     *             exception handler for this componenet
     */
    protected MuleMessage aggregateEvents(EventGroup events) throws AggregationException
    {
        Event[] collectedEvents = events.toArray();
        Event firstEvent = collectedEvents[0];
        Arrays.sort(collectedEvents, eventComparator);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);

        try
        {
            for (Iterator iterator = IteratorUtils.arrayIterator(collectedEvents); iterator.hasNext();)
            {
                Event event = (Event) iterator.next();
                baos.write(event.getMessageAsBytes());
            }

            MuleMessage message;

            // try to deserialize message, since ChunkingRouter might have serialized
            // the object...
            try
            {
                message = new DefaultMuleMessage(SerializationUtils.deserialize(baos.toByteArray()),
                    firstEvent.getMessage());

            }
            catch (SerializationException e)
            {
                message = new DefaultMuleMessage(baos.toByteArray(), firstEvent.getMessage());
            }

            message.setCorrelationGroupSize(-1);
            message.setCorrelationSequence(-1);

            return message;
        }
        catch (Exception e)
        {
            throw new AggregationException(events, firstEvent.getEndpoint(), e);
        }
        finally
        {
            IOUtils.closeQuietly(baos);
        }
    }

}
