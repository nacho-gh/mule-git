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
import org.mule.api.MuleMessage;
import org.mule.api.transport.MessageTypeNotSupportedException;
import org.mule.api.transport.MessageAdapter;
import org.mule.impl.DefaultMuleMessage;
import org.mule.impl.transport.DefaultMessageAdapter;
import org.mule.tck.providers.AbstractMessageAdapterTestCase;

/**
 * <code>VMMessageAdapterTestCase</code> TODO (document class)
 */
public class VMMessageAdapterTestCase extends AbstractMessageAdapterTestCase
{

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.tck.providers.AbstractMessageAdapterTestCase#createAdapter()
     */
    public MessageAdapter createAdapter(Object payload) throws MessageTypeNotSupportedException
    {
        if (payload instanceof MuleMessage)
        {
            return new DefaultMessageAdapter((MuleMessage)payload);
        }
        else
        {
            throw new MessageTypeNotSupportedException(payload, DefaultMessageAdapter.class);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.tck.providers.AbstractMessageAdapterTestCase#getValidMessage()
     */
    public Object getValidMessage() throws AbstractMuleException
    {
        return new DefaultMuleMessage("Valid Message");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.tck.providers.AbstractMessageAdapterTestCase#getInvalidMessage()
     */
    public Object getInvalidMessage()
    {
        return "Invalid message";
    }

}
