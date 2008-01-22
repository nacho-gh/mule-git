/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.jms;

import org.mule.api.AbstractMuleException;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.transport.MessageDispatcher;
import org.mule.impl.transport.AbstractMessageDispatcherFactory;

/**
 * <code>JmsMessageDispatcherFactory</code> creates a message adapter that will
 * send JMS messages
 */
public class JmsMessageDispatcherFactory extends AbstractMessageDispatcherFactory
{

    public MessageDispatcher create(ImmutableEndpoint endpoint) throws AbstractMuleException
    {
        return new JmsMessageDispatcher(endpoint);
    }

}
