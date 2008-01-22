/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.xmpp;

import org.mule.api.AbstractMuleException;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.transport.MessageRequester;
import org.mule.impl.transport.AbstractMessageRequesterFactory;

/**
 * Creates a dispatcher responsible for writing Xmpp packets to a an Jabber chat
 */

public class XmppMessageRequesterFactory extends AbstractMessageRequesterFactory
{

    public MessageRequester create(ImmutableEndpoint endpoint) throws AbstractMuleException
    {
        return new XmppMessageRequester(endpoint);
    }

}