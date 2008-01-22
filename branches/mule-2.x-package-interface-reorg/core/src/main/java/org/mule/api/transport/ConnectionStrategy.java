/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.api.transport;

import org.mule.impl.transport.FatalConnectException;

/**
 * Allows developers to plug in customised reconnection behaviour
 */

public interface ConnectionStrategy
{
    /**
     * Attempts to connect to a resource according the strategy implemented
     * 
     * @param connectable the object to connect to a resource
     * @throws FatalConnectException is thrown if the strategy finally fails to make
     *             a connection.
     */
    void connect(Connectable connectable) throws FatalConnectException;
}
