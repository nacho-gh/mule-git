/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.cxf;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.tck.FunctionalTestCase;

public class MultipleConnectorsTestCase extends FunctionalTestCase
{

    protected String getConfigResources()
    {
        return "multiple-connectors.xml";
    }

    public void testCxfConnector1() throws Exception
    {
        MuleClient client = new MuleClient();
        MuleMessage reply = client.send("cxf1", new DefaultMuleMessage("mule"));
        assertEquals("Received: mule", reply.getPayloadAsString());
    }

    public void testCxfConnector2() throws Exception
    {
        MuleClient client = new MuleClient();
        MuleMessage reply = client.send("cxf2", new DefaultMuleMessage("mule"));
        assertEquals("Received: mule", reply.getPayloadAsString());
    }

}
