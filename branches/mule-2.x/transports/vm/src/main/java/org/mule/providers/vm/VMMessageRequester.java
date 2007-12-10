/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.vm;

import org.mule.providers.AbstractMessageRequester;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.util.queue.Queue;
import org.mule.util.queue.QueueSession;

/**
 * <code>VMMessageDispatcher</code> is used for providing in memory interaction
 * between components.
 */
public class VMMessageRequester extends AbstractMessageRequester
{

    private final VMConnector connector;

    public VMMessageRequester(UMOImmutableEndpoint endpoint)
    {
        super(endpoint);
        this.connector = (VMConnector) endpoint.getConnector();
    }

    /**
     * Make a specific request to the underlying transport
     *
     * @param timeout the maximum time the operation should block before returning.
     *                The call should return immediately if there is data available. If
     *                no data becomes available before the timeout elapses, null will be
     *                returned
     * @return the result of the request wrapped in a UMOMessage object. Null will be
     *         returned if no data was available
     * @throws Exception if the call to the underlying protocol causes an exception
     */
    protected UMOMessage doRequest(long timeout) throws Exception
    {
        if (!connector.isQueueEvents())
        {
            throw new UnsupportedOperationException("Receive requested on VM Connector, but queueEvents is false");
        }

        try
        {
            QueueSession queueSession = connector.getQueueSession();
            Queue queue = queueSession.getQueue(endpoint.getEndpointURI().getAddress());

            if (queue == null)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("No queue with name " + endpoint.getEndpointURI().getAddress());
                }
                return null;
            }
            else
            {
                UMOMessage message = null;
                if (logger.isDebugEnabled())
                {
                    logger.debug("Waiting for a message on " + endpoint.getEndpointURI().getAddress());
                }
                try
                {
                    message = (UMOMessage) queue.poll(timeout);
                }
                catch (InterruptedException e)
                {
                    logger.error("Failed to receive message from queue: " + endpoint.getEndpointURI());
                }
                if (message != null)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Message received: " + message);
                    }
                    return message;
                }
                else
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("No event received after " + timeout + " ms");
                    }
                    return null;
                }
            }
        }
        catch (Exception e)
        {
            throw e;
        }
    }

    protected void doDispose()
    {
        // template method
    }

    protected void doConnect() throws Exception
    {
        if (connector.isQueueEvents())
        {
            // use the default queue profile to configure this queue.
            connector.getQueueProfile().configureQueue(
                    endpoint.getEndpointURI().getAddress(), connector.getQueueManager());
        }
    }

    protected void doDisconnect() throws Exception
    {
        // template method
    }

}