/*
 * $Id: XFireMuleSession.java 6464 2007-05-10 14:04:36Z dirk.olmes $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.cxf;

import org.mule.api.Session;



/**
 * Mules session wrapper for XFire
 */
public class CxfMuleSession implements org.apache.cxf.transport.Session
{
    Session session;

    public CxfMuleSession(Session session)
    {
        if (session == null)
        {
            throw new IllegalArgumentException("Session");
        }
        this.session = session;
    }

    /**
     * Get a variable from the session by the key.
     * 
     * @param key
     * @return Value
     */
    public Object get(Object key)
    {
        return session.getProperty(key);
    }

    /**
     * Put a variable into the session with a key.
     * 
     * @param key
     * @param value
     */
    public void put(Object key, Object value)
    {
        session.setProperty(key, value);
    }
}
