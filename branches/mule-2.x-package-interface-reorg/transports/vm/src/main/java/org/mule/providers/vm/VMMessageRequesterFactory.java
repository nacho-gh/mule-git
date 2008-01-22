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

import org.mule.api.AbstractMuleException;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.transport.MessageRequester;
import org.mule.impl.transport.AbstractMessageRequesterFactory;

/**
 * <code>VMMessageDispatcherFactory</code> creates an in-memory event dispatcher
 */
public class VMMessageRequesterFactory extends AbstractMessageRequesterFactory
{
    /*
     * (non-Javadoc)
     *
     * @see org.mule.api.transport.MessageDispatcherFactory#create(org.mule.api.transport.Connector)
     */
    public MessageRequester create(ImmutableEndpoint endpoint) throws AbstractMuleException
    {
        return new VMMessageRequester(endpoint);
    }
}