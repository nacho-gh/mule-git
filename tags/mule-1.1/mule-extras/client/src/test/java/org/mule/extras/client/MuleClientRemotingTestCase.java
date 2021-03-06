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

import org.mule.MuleManager;
import org.mule.tck.FunctionalTestCase;
import org.mule.umo.UMOMessage;

/**
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public class MuleClientRemotingTestCase extends FunctionalTestCase
{

    protected String getConfigResources() {
        return "test-client-mule-config-remoting-tcp.xml";
    }

    public String getServerUrl()
    {
        return "tcp://localhost:60504";
    }

    public void testClientSendToRemoteComponent() throws Exception
    {
        // Will connect to the server using tcp://localhost:60504
        MuleClient client = new MuleClient();
        MuleManager.getConfiguration().setSynchronous(true);

        RemoteDispatcher dispatcher = client.getRemoteDispatcher(getServerUrl());
        UMOMessage message = dispatcher.sendToRemoteComponent("TestReceiverUMO", "Test Client Send message", null);
        assertNotNull(message);
        assertEquals("Received: Test Client Send message", message.getPayload());
    }

    public void testClientSendAndReceiveRemote() throws Exception
    {
        String remoteEndpoint = "vm://vmRemoteProvider/remote.queue";
        // Will connect to the server using tcp://localhost:60504
        MuleClient client = new MuleClient();
        MuleManager.getConfiguration().setSynchronous(true);

        RemoteDispatcher dispatcher = client.getRemoteDispatcher(getServerUrl());
        UMOMessage message = dispatcher.receiveRemote(remoteEndpoint, 1000);
        assertNull(message);

        // Dispatch a message
        dispatcher.dispatchRemote(remoteEndpoint, "Test Remote Message 2", null);

        // Receive the message
        message = dispatcher.receiveRemote(remoteEndpoint, 2000);
        assertNotNull(message);
        assertEquals("Test Remote Message 2", message.getPayload());

    }
}
