/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.providers.service;

import org.mule.config.i18n.Message;

/**
 * <code>ConnectorServiceNotFoundException</code> is thorown if no matching
 * service endpoint descriptor is found for the connector protocol.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */

public class ConnectorServiceNotFoundException extends ConnectorFactoryException
{
    /**
     * Serial version
     */
    private static final long serialVersionUID = -8321406750213654479L;

    /**
     * @param location the path of the service
     */
    public ConnectorServiceNotFoundException(String location)
    {
        super(Message.createStaticMessage(location));
    }

    /**
     * @param location the path of the service
     * @param cause the exception that cause this exception to be thrown
     */
    public ConnectorServiceNotFoundException(String location, Throwable cause)
    {
        super(Message.createStaticMessage(location), cause);
    }
}
