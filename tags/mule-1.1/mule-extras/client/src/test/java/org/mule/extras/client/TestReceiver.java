/*
 * $Header$
 * $Revision$
 * $Date$
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.extras.client;

/**
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public class TestReceiver
{
    private int count = 0;

    public String receive(String message) throws Exception
    {
        System.out.println("Received: " + message + " Number: " + inc() + " in thread: "
                + Thread.currentThread().getName());
        return "Received: " + message;
    }

    protected int inc()
    {
        count++;
        return count;
    }
}
