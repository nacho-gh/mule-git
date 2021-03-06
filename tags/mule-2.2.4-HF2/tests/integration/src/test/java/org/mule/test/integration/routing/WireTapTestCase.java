/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.integration.routing;


import org.mule.api.context.notification.ServerNotification;
import org.mule.module.client.MuleClient;
import org.mule.tck.FunctionalTestCase;
import org.mule.tck.functional.FunctionalTestNotificationListener;
import org.mule.util.concurrent.Latch;

import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

public class WireTapTestCase extends FunctionalTestCase
{

    protected String getConfigResources()
    {
        return "org/mule/test/integration/routing/wire-tap.xml";
    }

    public void testWireTap() throws Exception
    {
        final Latch receiverLatch = new Latch();
        final Latch tappedReceiver1Latch = new Latch();
        final Latch tappedReceiver2Latch = new Latch();
        muleContext.registerListener(new FunctionalTestNotificationListener()
        {
            public void onNotification(ServerNotification notification)
            {
                if (notification.getResourceIdentifier().equals("Receiver"))
                {
                    receiverLatch.countDown();
                }
                else if (notification.getResourceIdentifier().equals("TappedReceiver1"))
                {
                    tappedReceiver1Latch.countDown();
                }
                else if (notification.getResourceIdentifier().equals("TappedReceiver2"))
                {
                    tappedReceiver2Latch.countDown();
                }
            }
        });
        MuleClient client = new MuleClient();
        client.send("vm://inbound.channel", "test", null);
        assertTrue(receiverLatch.await(3L, TimeUnit.SECONDS));
        assertTrue(tappedReceiver1Latch.await(1L, TimeUnit.SECONDS));
        assertTrue(tappedReceiver2Latch.await(1L, TimeUnit.SECONDS));
    }
}
