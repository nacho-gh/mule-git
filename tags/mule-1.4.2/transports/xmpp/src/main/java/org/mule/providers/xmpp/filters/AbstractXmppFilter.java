/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.xmpp.filters;

import org.mule.umo.UMOFilter;
import org.mule.umo.UMOMessage;

import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

/**
 * <code>AbstractXmppFilter</code> is a filter adapter so that Smack Filters can be
 * configured as Mule filters.
 */
public abstract class AbstractXmppFilter implements UMOFilter, PacketFilter
{
    protected volatile PacketFilter delegate;

    public boolean accept(Packet packet)
    {
        if (delegate == null)
        {
            delegate = createFilter();
        }

        return delegate.accept(packet);
    }

    public boolean accept(UMOMessage message)
    {
        // If we have received a UMOMessage the filter has already been applied
        return true;
    }

    protected abstract PacketFilter createFilter();

}
