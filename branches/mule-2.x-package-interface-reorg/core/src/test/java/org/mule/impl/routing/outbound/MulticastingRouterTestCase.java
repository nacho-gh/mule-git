/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.impl.routing.outbound;

import org.mule.api.MuleMessage;
import org.mule.api.Session;
import org.mule.api.endpoint.Endpoint;
import org.mule.impl.DefaultMuleMessage;
import org.mule.impl.routing.LoggingCatchAllStrategy;
import org.mule.impl.routing.filters.RegExFilter;
import org.mule.impl.routing.outbound.MulticastingRouter;
import org.mule.impl.routing.outbound.DefaultOutboundRouterCollection;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.tck.MuleTestUtils;

import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.Mock;

import java.util.ArrayList;
import java.util.List;

public class MulticastingRouterTestCase extends AbstractMuleTestCase
{
    public void testMulticastingRouter() throws Exception
    {
        Mock session = MuleTestUtils.getMockSession();
        DefaultOutboundRouterCollection messageRouter = new DefaultOutboundRouterCollection();
        messageRouter.setCatchAllStrategy(new LoggingCatchAllStrategy());

        Endpoint endpoint1 = getTestEndpoint("Test1Provider", Endpoint.ENDPOINT_TYPE_SENDER);
        assertNotNull(endpoint1);

        Endpoint endpoint2 = getTestEndpoint("Test2Provider", Endpoint.ENDPOINT_TYPE_SENDER);
        assertNotNull(endpoint2);

        MulticastingRouter router = new MulticastingRouter();
        RegExFilter filter = new RegExFilter("(.*) event");
        router.setFilter(filter);
        List endpoints = new ArrayList();
        endpoints.add(endpoint1);
        endpoints.add(endpoint2);
        router.setEndpoints(endpoints);

        assertEquals(filter, router.getFilter());

        MuleMessage message = new DefaultMuleMessage("test event");

        assertTrue(router.isMatch(message));

        session.expect("dispatchEvent", C.eq(message, endpoint1));
        session.expect("dispatchEvent", C.eq(message, endpoint2));
        router.route(message, (Session)session.proxy(), false);
        session.verify();

        message = new DefaultMuleMessage("test event");

        session.expectAndReturn("sendEvent", C.eq(message, endpoint1), message);
        session.expectAndReturn("sendEvent", C.eq(message, endpoint2), message);
        MuleMessage result = router.route(message, (Session)session.proxy(), true);
        assertNotNull(result);
        assertEquals(message, result);
        session.verify();
    }
}
