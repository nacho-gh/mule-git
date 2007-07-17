/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.impl;

import org.mule.impl.endpoint.MuleEndpoint;
import org.mule.impl.model.direct.DirectComponent;
import org.mule.providers.DefaultMessageAdapter;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.MalformedEndpointException;
import org.mule.config.MuleProperties;

import java.util.Map;

import junit.framework.TestCase;

public class SafeThreadAccessTestCase extends TestCase
{

    public void testMessage() throws InterruptedException
    {
        basicPattern(new MuleMessage(new Object(), (Map)null));
        newCopy(new MuleMessage(new Object(), (Map)null));
        resetAccessControl(new MuleMessage(new Object(), (Map)null));
    }

    public void testAdapter() throws InterruptedException
    {
        basicPattern(new DefaultMessageAdapter(new Object()));
        newCopy(new DefaultMessageAdapter(new Object()));
        resetAccessControl(new DefaultMessageAdapter(new Object()));
    }

    public void testEvent() throws InterruptedException, MalformedEndpointException
    {
        basicPattern(dummyEvent());
        newCopy(dummyEvent());
        resetAccessControl(dummyEvent());
    }

    public void testDisable() throws InterruptedException
    {
        try
        {
            System.setProperty(MuleProperties.MULE_THREAD_UNSAFE_MESSAGES_PROPERTY, "true");
            SafeThreadAccess target = new DefaultMessageAdapter(new Object());
            newThread(target, false, new boolean[]{true, true, false, true});
            newThread(target, false, new boolean[]{false});
            newThread(target, false, new boolean[]{true});
        }
        finally
        {
            System.getProperties().remove(MuleProperties.MULE_THREAD_UNSAFE_MESSAGES_PROPERTY);
        }
    }

    protected SafeThreadAccess dummyEvent()
    {
        UMOMessage message = new MuleMessage(new Object(), (Map)null);
        return new MuleEvent(message, new MuleEndpoint(),
                new MuleSession(new DirectComponent(new MuleDescriptor(""), null)), false);
    }

    protected void resetAccessControl(SafeThreadAccess target) throws InterruptedException
    {
        target.assertAccess(true);
        newThread(target, true, new boolean[]{true});
        target.resetAccessControl();
        newThread(target, false, new boolean[]{true});
    }

    protected void basicPattern(SafeThreadAccess target) throws InterruptedException
    {
        newThread(target, false, new boolean[]{true, true, false, true});
        newThread(target, false, new boolean[]{false});
        newThread(target, true, new boolean[]{true});
    }

    protected void newCopy(SafeThreadAccess target) throws InterruptedException
    {
        basicPattern(target);
        basicPattern(target.newCopy());
    }

    protected void newThread(SafeThreadAccess target, boolean error, boolean[] pattern) throws InterruptedException
    {
        Caller caller = new Caller(target, pattern);
        Thread thread =  new Thread(caller);
        thread.start();
        thread.join();
        assertEquals(error, caller.isError());
    }

    protected static class Caller implements Runnable
    {

        private boolean isError = false;
        private SafeThreadAccess target;
        private boolean[] write;

        public Caller(SafeThreadAccess target, boolean[] write)
        {
            this.target = target;
            this.write = write;
        }

        public void run()
        {
            try
            {
                for (int i = 0; i < write.length; i++)
                {
                    target.assertAccess(write[i]);
                }
            }
            catch (IllegalStateException e)
            {
                isError = true;
            }
        }

        public boolean isError()
        {
            return isError;
        }

    }

}
