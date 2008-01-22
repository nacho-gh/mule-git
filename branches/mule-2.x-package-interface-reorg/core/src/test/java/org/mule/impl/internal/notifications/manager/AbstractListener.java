/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.impl.internal.notifications.manager;

import org.mule.api.context.ServerNotification;
import org.mule.api.context.ServerNotificationListener;

public abstract class AbstractListener implements ServerNotificationListener
{

    private ServerNotification notification = null;

    public void onNotification(ServerNotification notification)
    {
        this.notification = notification;
    }

    public boolean isNotified()
    {
        return null != notification;
    }

}
