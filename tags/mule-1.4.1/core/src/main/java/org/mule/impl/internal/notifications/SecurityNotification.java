/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.impl.internal.notifications;

import org.mule.umo.manager.UMOServerNotification;

/**
 * <code>SecurityNotification</code> is fired when a request for authorisation
 * occurs. The event may denote successful access or denied access depending on the
 * type of event. Subscribing to these notifications developers can maintain an
 * access log, block clients, etc.
 * 
 * @see org.mule.MuleManager
 * @see org.mule.umo.manager.UMOManager
 */
public class SecurityNotification extends UMOServerNotification
{
    /**
     * Serial version
     */
    private static final long serialVersionUID = 5951835321289699941L;

    public static final int SECURITY_AUTHENTICATION_FAILED = SECURITY_EVENT_ACTION_START_RANGE + 1;

    private static final transient String[] ACTIONS = new String[]{"authentication failed"};

    public SecurityNotification(org.mule.umo.security.SecurityException message, int action)
    {
        super(message, action);
        resourceIdentifier = message.toString();
    }

    protected String getPayloadToString()
    {
        return source.toString();
    }

    protected String getActionName(int action)
    {
        int i = action - SECURITY_EVENT_ACTION_START_RANGE;
        if (i - 1 > ACTIONS.length)
        {
            return String.valueOf(action);
        }
        return ACTIONS[i - 1];
    }

    public String getType()
    {
        return TYPE_WARNING;
    }
}
