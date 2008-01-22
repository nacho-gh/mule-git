/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.impl.routing;

import org.mule.api.Event;
import org.mule.api.MuleMessage;
import org.mule.api.Session;
import org.mule.api.endpoint.Endpoint;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.MessageDispatcher;
import org.mule.impl.MuleEvent;
import org.mule.impl.DefaultMuleMessage;
import org.mule.impl.endpoint.MuleEndpointURI;
import org.mule.impl.routing.ForwardingCatchAllStrategy;
import org.mule.impl.routing.LoggingCatchAllStrategy;
import org.mule.impl.routing.filters.PayloadTypeFilter;
import org.mule.impl.routing.outbound.FilteringOutboundRouter;
import org.mule.impl.routing.outbound.DefaultOutboundRouterCollection;
import org.mule.impl.transformer.AbstractTransformer;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.tck.MuleTestUtils;
import org.mule.util.CollectionUtils;

import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.Mock;

import java.util.HashMap;

public class CatchAllStrategiesTestCase extends AbstractMuleTestCase
{

    public void testLoggingOnlyStrategy() throws Exception
    {
        Event event = getTestEvent("UncaughtEvent");
        LoggingCatchAllStrategy strategy = new LoggingCatchAllStrategy();
        try
        {
            strategy.setEndpoint(getTestEndpoint("testProvider", Endpoint.ENDPOINT_TYPE_SENDER));
            fail("Illegal operation exception should have been thrown");
        }
        catch (Exception e)
        {
            // expected
        }

        assertNull(strategy.getEndpoint());
        strategy.catchMessage(event.getMessage(), null, false);
    }

    public void testForwardingStrategy() throws Exception
    {
        ForwardingCatchAllStrategy strategy = new ForwardingCatchAllStrategy();
        Mock endpoint = MuleTestUtils.getMockEndpoint();
        Mock dispatcher = new Mock(MessageDispatcher.class);
        Mock connector = MuleTestUtils.getMockConnector();
        Event event = getTestEvent("UncaughtEvent");
        strategy.setEndpoint((Endpoint)endpoint.proxy());

        endpoint.expectAndReturn("getProperties", new HashMap());
        endpoint.expectAndReturn("getProperties", new HashMap());
        endpoint.expectAndReturn("getEndpointURI", new MuleEndpointURI("test://dummy"));
        endpoint.expectAndReturn("getEndpointURI", new MuleEndpointURI("test://dummy"));
        endpoint.expect("dispatch", C.isA(MuleEvent.class));

        strategy.catchMessage(event.getMessage(), null, false);

        endpoint.verify();
        dispatcher.verify();
        connector.verify();

        assertNotNull(strategy.getEndpoint());
    }

    private class TestEventTransformer extends AbstractTransformer
    {
        /*
         * (non-Javadoc)
         * 
         * @see org.mule.impl.transformer.AbstractTransformer#doTransform(java.lang.Object)
         */
        public Object doTransform(Object src, String encoding) throws TransformerException
        {
            return "Transformed Test Data";
        }
    }

    public void testForwardingStrategyWithTransform() throws Exception
    {
        ForwardingCatchAllStrategy strategy = new ForwardingCatchAllStrategy();
        strategy.setSendTransformed(true);
        Mock endpoint = MuleTestUtils.getMockEndpoint();
        Mock dispatcher = new Mock(MessageDispatcher.class);
        Mock connector = MuleTestUtils.getMockConnector();
        Event event = getTestEvent("UncaughtEvent");
        strategy.setEndpoint((Endpoint)endpoint.proxy());

        endpoint.expectAndReturn("getTransformers", CollectionUtils.singletonList(new TestEventTransformer()));
        endpoint.expectAndReturn("getTransformers", CollectionUtils.singletonList(new TestEventTransformer()));
        endpoint.expectAndReturn("getProperties", new HashMap());
        endpoint.expectAndReturn("getProperties", new HashMap());
        endpoint.expectAndReturn("getEndpointURI", new MuleEndpointURI("test://dummy"));
        endpoint.expectAndReturn("getEndpointURI", new MuleEndpointURI("test://dummy"));
        endpoint.expect("send", new Constraint()
        {
            public boolean eval(Object arg0)
            {
                if (arg0 instanceof Event)
                {
                    return "Transformed Test Data".equals(((Event)arg0).getMessage().getPayload());
                }
                return false;
            }
        });

        strategy.catchMessage(event.getMessage(), null, true);

        endpoint.verify();
        dispatcher.verify();
        connector.verify();

        assertNotNull(strategy.getEndpoint());
    }

    public void testFullRouter() throws Exception
    {
        final int[] count1 = new int[]{0};
        final int[] count2 = new int[]{0};
        final int[] catchAllCount = new int[]{0};

        DefaultOutboundRouterCollection messageRouter = new DefaultOutboundRouterCollection();

        FilteringOutboundRouter filterRouter1 = new FilteringOutboundRouter()
        {
            public MuleMessage route(MuleMessage message, Session session, boolean synchronous)
            {
                count1[0]++;
                return message;
            }
        };

        FilteringOutboundRouter filterRouter2 = new FilteringOutboundRouter()
        {
            public MuleMessage route(MuleMessage message, Session session, boolean synchronous)
            {
                count2[0]++;
                return message;
            }
        };

        filterRouter1.setFilter(new PayloadTypeFilter(Exception.class));
        filterRouter2.setFilter(new PayloadTypeFilter(StringBuffer.class));
        messageRouter.addRouter(filterRouter1);
        messageRouter.addRouter(filterRouter2);

        LoggingCatchAllStrategy strategy = new LoggingCatchAllStrategy()
        {
            public MuleMessage catchMessage(MuleMessage message, Session session, boolean synchronous)
            {
                catchAllCount[0]++;
                return null;
            }
        };
        messageRouter.setCatchAllStrategy(strategy);

        Session session = getTestSession(getTestComponent());

        messageRouter.route(new DefaultMuleMessage("hello"), session, true);
        assertEquals(1, catchAllCount[0]);
        assertEquals(0, count1[0]);
        assertEquals(0, count2[0]);

        messageRouter.route(new DefaultMuleMessage(new StringBuffer()), session, true);
        assertEquals(1, catchAllCount[0]);
        assertEquals(0, count1[0]);
        assertEquals(1, count2[0]);

        messageRouter.route(new DefaultMuleMessage(new Exception()), session, true);
        assertEquals(1, catchAllCount[0]);
        assertEquals(1, count1[0]);
        assertEquals(1, count2[0]);
    }

}
