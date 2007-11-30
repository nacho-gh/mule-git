/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.soap.axis;

import org.mule.providers.AbstractMessageRequesterFactory;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.umo.provider.UMOMessageRequester;

/**
 * <code>AxisMessageRequesterFactory</code> creates an AxisMessageRequester, used
 * for making SOAP calls using the Axis stack.
 */
public class AxisMessageRequesterFactory extends AbstractMessageRequesterFactory
{
    public UMOMessageRequester create(UMOImmutableEndpoint endpoint) throws UMOException
    {
        return new AxisMessageRequester(endpoint);
    }
}