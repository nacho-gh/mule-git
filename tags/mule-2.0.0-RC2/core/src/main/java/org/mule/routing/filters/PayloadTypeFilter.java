/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.routing.filters;

import org.mule.api.MuleMessage;
import org.mule.api.routing.filter.Filter;

/**
 * <code>PayloadTypeFilter</code> filters based on the type of the object received.
 */

public class PayloadTypeFilter implements Filter
{
    private Class expectedType;

    public PayloadTypeFilter()
    {
        super();
    }

    public PayloadTypeFilter(Class expectedType)
    {
        this.expectedType = expectedType;
    }

    public boolean accept(MuleMessage message)
    {
        return (expectedType != null ? expectedType.isAssignableFrom(message.getPayload().getClass()) : false);
    }

    public Class getExpectedType()
    {
        return expectedType;
    }

    public void setExpectedType(Class expectedType)
    {
        this.expectedType = expectedType;
    }

}
