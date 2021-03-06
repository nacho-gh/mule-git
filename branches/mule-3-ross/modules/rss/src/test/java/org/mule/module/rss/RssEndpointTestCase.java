/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.rss;

import org.mule.api.endpoint.EndpointFactory;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.registry.ServiceException;
import org.mule.module.rss.endpoint.RssInboundEndpoint;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.transport.file.FileConnector;
import org.mule.transport.http.HttpPollingConnector;

public class RssEndpointTestCase extends AbstractMuleTestCase
{
    public void testHttpInboundEndpointCreation() throws Exception
    {
        String uri = "rss:http://blog.com/rss";
        EndpointFactory factory = muleContext.getRegistry().lookupEndpointFactory();
        InboundEndpoint in = factory.getEndpointBuilder(uri).buildInboundEndpoint();
        assertNotNull(in);
        assertEquals("rss:http", in.getEndpointURI().getFullScheme());
        assertEquals("http", in.getProtocol());
        assertTrue(in.getConnector() instanceof HttpPollingConnector);
        assertTrue(in instanceof RssInboundEndpoint);
    }

    public void testHttpOutboundEndpointCreation() throws Exception
    {
        String uri = "rss:http://blog.com/rss";
        EndpointFactory factory = muleContext.getRegistry().lookupEndpointFactory();
        try
        {
            factory.getEndpointBuilder(uri).buildOutboundEndpoint();
            fail("RSS outbound endpoints are not supported");
        }
        catch (UnsupportedOperationException e)
        {
            //exprected
        }
    }

    public void testFileInboundEndpointCreation() throws Exception
    {
        String uri = "rss:file://./src/foo";
        EndpointFactory factory = muleContext.getRegistry().lookupEndpointFactory();
        InboundEndpoint in = factory.getEndpointBuilder(uri).buildInboundEndpoint();
        assertNotNull(in);
        assertEquals("rss:file", in.getEndpointURI().getFullScheme());
        assertEquals("file", in.getProtocol());
        assertTrue(in.getConnector() instanceof FileConnector);
        assertTrue(in instanceof RssInboundEndpoint);
    }

    public void testFileOutboundEndpointCreation() throws Exception
    {
        String uri = "rss:file://./src/foo";
        EndpointFactory factory = muleContext.getRegistry().lookupEndpointFactory();
        try
        {
            factory.getEndpointBuilder(uri).buildOutboundEndpoint();
            fail("RSS outbound endpoints are not supported");
        }
        catch (UnsupportedOperationException e)
        {
            //exprected
        }
    }

    public void testXXInboundEndpointCreation() throws Exception
    {
        String uri = "rss:xxx://./src/foo";
        EndpointFactory factory = muleContext.getRegistry().lookupEndpointFactory();
        try
        {
            factory.getEndpointBuilder(uri).buildInboundEndpoint();
            fail("xxx is not a valid transport");
        }
        catch (ServiceException e)
        {
            //expected
        }
    }
}