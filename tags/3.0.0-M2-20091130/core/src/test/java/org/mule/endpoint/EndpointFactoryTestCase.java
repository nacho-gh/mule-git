/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.endpoint;

import org.mule.api.MuleException;
import org.mule.api.endpoint.EndpointBuilder;
import org.mule.api.endpoint.EndpointFactory;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.registry.Registry;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.tck.testmodels.mule.TestConnector;

public class EndpointFactoryTestCase extends AbstractMuleTestCase
{

    public void testCreateInboundEndpoint() throws Exception
    {
        String uri = "test://address";
        EndpointFactory endpointFactory = new DefaultEndpointFactory();
        endpointFactory.setMuleContext(muleContext);
        ImmutableEndpoint ep = endpointFactory.getInboundEndpoint(uri);
        assertEquals(DefaultInboundEndpoint.class, ep.getClass());
        assertEquals(ep.getEndpointURI().getUri().toString(), "test://address");
        assertTrue(ep instanceof InboundEndpoint);
    }

    public void testCreateInboundEndpointFromGlobalEndpoint() throws Exception
    {
        muleContext.getRegistry().registerEndpointBuilder("myGlobalEndpoint",
                new EndpointURIEndpointBuilder("test://address", muleContext));
        String uri = "myGlobalEndpoint";
        EndpointFactory endpointFactory = new DefaultEndpointFactory();
        endpointFactory.setMuleContext(muleContext);
        try
        {
            ImmutableEndpoint ep = endpointFactory.getInboundEndpoint(uri);
            assertEquals(DefaultInboundEndpoint.class, ep.getClass());
            assertEquals(ep.getEndpointURI().getUri().toString(), "test://address");
            assertTrue(ep instanceof InboundEndpoint);
        }
        catch (Exception e)
        {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testCreateInboundEndpointFromNamedConcreteEndpoint() throws Exception
    {
        muleContext.getRegistry().registerEndpointBuilder("&myNamedConcreateEndpoint",
                new EndpointURIEndpointBuilder("test://address", muleContext));
        String uri = "&myNamedConcreateEndpoint";
        EndpointFactory endpointFactory = new DefaultEndpointFactory();
        endpointFactory.setMuleContext(muleContext);
        ImmutableEndpoint ep = endpointFactory.getInboundEndpoint(uri);
        assertEquals(DefaultInboundEndpoint.class, ep.getClass());
        assertEquals(ep.getEndpointURI().getUri().toString(), "test://address");
        assertTrue(ep instanceof InboundEndpoint);
    }

    public void testCreateOutboundEndpoint() throws Exception
    {
        String uri = "test://address";
        EndpointFactory endpointFactory = new DefaultEndpointFactory();
        endpointFactory.setMuleContext(muleContext);
        ImmutableEndpoint ep = endpointFactory.getOutboundEndpoint(uri);
        assertEquals(DefaultOutboundEndpoint.class, ep.getClass());
        assertEquals(ep.getEndpointURI().getUri().toString(), "test://address");
        assertTrue(ep instanceof OutboundEndpoint);
    }

    public void testCreateoutboundEndpointFromGlobalEndpoint() throws Exception
    {
        muleContext.getRegistry().registerEndpointBuilder("myGlobalEndpoint",
                new EndpointURIEndpointBuilder("test://address", muleContext));
        String uri = "myGlobalEndpoint";
        EndpointFactory endpointFactory = new DefaultEndpointFactory();
        endpointFactory.setMuleContext(muleContext);
        ImmutableEndpoint ep = endpointFactory.getOutboundEndpoint(uri);
        assertEquals(DefaultOutboundEndpoint.class, ep.getClass());
        assertEquals(ep.getEndpointURI().getUri().toString(), "test://address");
        assertTrue(ep instanceof OutboundEndpoint);
    }

    public void testCreateoutboundEndpointFromNamedConcreteEndpoint() throws Exception
    {
        muleContext.getRegistry().registerEndpointBuilder("&myNamedConcreateEndpoint",
                new EndpointURIEndpointBuilder("test://address", muleContext));
        String uri = "&myNamedConcreateEndpoint";
        EndpointFactory endpointFactory = new DefaultEndpointFactory();
        endpointFactory.setMuleContext(muleContext);
        ImmutableEndpoint ep = endpointFactory.getOutboundEndpoint(uri);
        assertEquals(DefaultOutboundEndpoint.class, ep.getClass());
        assertEquals(ep.getEndpointURI().getUri().toString(), "test://address");
        assertTrue(ep instanceof OutboundEndpoint);
    }

    public void testCreateInboundEndpointWithBuilder() throws Exception
    {
        EndpointBuilder builder = new EndpointURIEndpointBuilder("test://address", muleContext);
        EndpointFactory endpointFactory = new DefaultEndpointFactory();
        endpointFactory.setMuleContext(muleContext);
        ImmutableEndpoint ep = endpointFactory.getInboundEndpoint(builder);
        assertEquals(DefaultInboundEndpoint.class, ep.getClass());
        assertEquals(ep.getEndpointURI().getUri().toString(), "test://address");
        assertTrue(ep instanceof InboundEndpoint);
    }

    public void testCreateOutboundEndpointWithBuilder() throws Exception
    {
        EndpointBuilder builder = new EndpointURIEndpointBuilder("test://address", muleContext);
        EndpointFactory endpointFactory = new DefaultEndpointFactory();
        endpointFactory.setMuleContext(muleContext);
        ImmutableEndpoint ep = endpointFactory.getOutboundEndpoint(builder);
        assertEquals(DefaultOutboundEndpoint.class, ep.getClass());
        assertEquals(ep.getEndpointURI().getUri().toString(), "test://address");
        assertTrue(ep instanceof OutboundEndpoint);
    }

    public void testCreateEndpoint() throws MuleException
    {
        String uri = "test://address";
        EndpointFactory endpointFactory = new DefaultEndpointFactory();
        endpointFactory.setMuleContext(muleContext);
        ImmutableEndpoint ep = endpointFactory.getInboundEndpoint(uri);
        assertEquals(ep.getEndpointURI().getUri().toString(), "test://address");
    }

    public void testCreateEndpointFromGlobalEndpoint() throws MuleException
    {
        Registry r = muleContext.getRegistry();
        r.registerObject("myGlobalEndpoint", new EndpointURIEndpointBuilder("test://address", muleContext), muleContext);
        String uri = "myGlobalEndpoint";
        EndpointFactory endpointFactory = new DefaultEndpointFactory();
        endpointFactory.setMuleContext(muleContext);
        try
        {
            ImmutableEndpoint ep = endpointFactory.getInboundEndpoint(uri);
            assertEquals(ep.getEndpointURI().getUri().toString(), "test://address");
        }
        catch (Exception e)
        {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testCreateEndpointFromNamedConcreteEndpoint() throws MuleException
    {
        Registry r = muleContext.getRegistry();
        r.registerObject("&myNamedConcreteEndpoint", new EndpointURIEndpointBuilder("test://address", muleContext));
        String uri = "&myNamedConcreteEndpoint";
        EndpointFactory endpointFactory = new DefaultEndpointFactory();
        endpointFactory.setMuleContext(muleContext);
        ImmutableEndpoint ep = endpointFactory.getInboundEndpoint(uri);
        assertEquals(ep.getEndpointURI().getUri().toString(), "test://address");
    }

    public void testCreateEndpointByCustomizingEndpointBuilder() throws MuleException
    {
        // Create and register two connectors
        TestConnector testConnector1 = new TestConnector();
        testConnector1.setName("testConnector1");
        TestConnector testConnector2 = new TestConnector();
        testConnector2.setName("testConnector2");
        muleContext.getRegistry().registerConnector(testConnector1);
        muleContext.getRegistry().registerConnector(testConnector2);

        String globalEndpointName = "concreteEndpoint";

        // Create and register a endpoint builder (global endpoint) with connector1
        EndpointBuilder endpointBuilder = new EndpointURIEndpointBuilder("test://address", muleContext);
        endpointBuilder.setConnector(testConnector1);
        muleContext.getRegistry().registerObject(globalEndpointName, endpointBuilder);
        EndpointFactory endpointFactory = new DefaultEndpointFactory();
        endpointFactory.setMuleContext(muleContext);
        // Test that DefaultEndpointFactory.getEndpointBuilder() returns a new
        // EndpointBuilder instance equal to
        // the one we registered earlier
        EndpointBuilder endpointBuilder1 = endpointFactory.getEndpointBuilder(globalEndpointName);
        assertNotSame(endpointBuilder1, endpointBuilder);
        assertTrue(endpointBuilder1.equals(endpointBuilder));

        // Test that DefaultEndpointFactory.getEndpointBuilder() returns a new
        // EndpointBuilder instance equal to
        // the one we registered earlier
        EndpointBuilder endpointBuilder2 = endpointFactory.getEndpointBuilder(globalEndpointName);
        assertNotSame(endpointBuilder2, endpointBuilder);
        assertTrue(endpointBuilder2.equals(endpointBuilder));

        // Check that all EndpointBuilder's returned are unique but equal
        assertNotSame(endpointBuilder1, endpointBuilder2);
        assertTrue(endpointBuilder1.equals(endpointBuilder2));
        assertEquals(endpointBuilder1.hashCode(), endpointBuilder2.hashCode());

        // Test creating an endpoint from endpointBuilder1
        endpointBuilder1.setSynchronous(true);
        endpointBuilder1.setResponseTimeout(99);
        ImmutableEndpoint ep = endpointFactory.getInboundEndpoint(endpointBuilder1);
        assertEquals(ep.getEndpointURI().getUri().toString(), "test://address");
        assertTrue(ep.isSynchronous());
        assertEquals(99, ep.getResponseTimeout());
        assertNotNull(ep.getConnector());
        assertEquals(testConnector1, ep.getConnector());

        // Test creating an endpoint from endpointBuilder2
        endpointBuilder2.setSynchronous(false);
        endpointBuilder2.setResponseTimeout(0);
        endpointBuilder2.setConnector(testConnector2);
        ImmutableEndpoint ep2 = endpointFactory.getInboundEndpoint(endpointBuilder2);
        assertEquals(ep2.getEndpointURI().getUri().toString(), "test://address");
        assertFalse(ep2.isSynchronous());
        assertEquals(0, ep2.getResponseTimeout());
        assertNotNull(ep.getConnector());
        assertEquals(testConnector2, ep2.getConnector());

        // Test creating a new endpoint from endpointBuilder1
        ImmutableEndpoint ep3 = endpointFactory.getInboundEndpoint(endpointBuilder1);
        assertEquals(ep3.getEndpointURI().getUri().toString(), "test://address");
        assertTrue(ep3.getResponseTimeout() != 0);
        assertTrue(ep3.isSynchronous());
        assertNotNull(ep.getConnector());
        assertEquals(testConnector1, ep3.getConnector());
    }

}
