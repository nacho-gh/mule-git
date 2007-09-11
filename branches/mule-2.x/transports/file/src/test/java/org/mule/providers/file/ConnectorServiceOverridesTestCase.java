/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.file;

import org.mule.impl.endpoint.MuleEndpoint;
import org.mule.providers.AbstractConnector;
import org.mule.tck.FunctionalTestCase;
import org.mule.transformers.simple.ByteArrayToSerializable;
import org.mule.transformers.simple.SerializableToByteArray;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;

public class ConnectorServiceOverridesTestCase extends FunctionalTestCase
{

    protected String getConfigResources()
    {
        return "test-connector-config.xml";
    }

    public void testServiceOverrides() throws InterruptedException
    {
        FileConnector c = (FileConnector)managementContext.getRegistry().lookupConnector("fileConnector2");
        assertNotNull(c);
        assertNotNull(c.getServiceOverrides());
        assertEquals("org.mule.transformers.simple.ByteArrayToSerializable",
                c.getServiceOverrides().get("inbound.transformer"));
        assertNotNull(c.getDefaultInboundTransformer());
        assertNotNull(c.getDefaultOutboundTransformer());
        assertTrue(c.getDefaultInboundTransformer() instanceof ByteArrayToSerializable);
        assertTrue(c.getDefaultOutboundTransformer() instanceof SerializableToByteArray);
    }

    public void testServiceOverrides2() throws InterruptedException
    {
        FileConnector c = (FileConnector)managementContext.getRegistry().lookupConnector("fileConnector1");
        assertNotNull(c);
        assertNull(c.getServiceOverrides());

        c = (FileConnector)managementContext.getRegistry().lookupConnector("fileConnector2");
        assertNotNull(c);
        assertNotNull(c.getServiceOverrides());

        c = (FileConnector)managementContext.getRegistry().lookupConnector("fileConnector3");
        assertNotNull(c);
        assertNull(c.getServiceOverrides());
    }

    public void testServiceOverrides3() throws InterruptedException, UMOException
    {
        //UMOEndpointURI uri = new MuleEndpointURI("file:///temp?connector=fileConnector1");
        UMOEndpoint endpoint = (UMOEndpoint) managementContext.getRegistry().lookupInboundEndpoint("file:///temp?connector=fileConnector1", managementContext);

        assertNotNull(endpoint);
        assertNotNull(endpoint.getConnector());
        assertNull(((AbstractConnector)endpoint.getConnector()).getServiceOverrides());

        endpoint = new MuleEndpoint("file:///temp?connector=fileConnector1", true);

        FileConnector c = (FileConnector)managementContext.getRegistry().lookupConnector("fileConnector2");
        assertNotNull(c);
        assertNotNull(c.getServiceOverrides());
        endpoint.setConnector(c);

        endpoint.initialise();
        assertNotNull(((AbstractConnector)endpoint.getConnector()).getServiceOverrides());

        endpoint = new MuleEndpoint("file:///temp?connector=fileConnector3", true);
        endpoint.initialise();
        assertNull(((AbstractConnector)endpoint.getConnector()).getServiceOverrides());

        endpoint = new MuleEndpoint("file:///temp?connector=fileConnector2", true);
        endpoint.initialise();
        assertNotNull(((AbstractConnector)endpoint.getConnector()).getServiceOverrides());

    }
}
