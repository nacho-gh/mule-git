/*
 * $Id: XFireWsdlTestCase.java 6489 2007-05-11 14:00:13Z Lajos $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.cxf;

import org.mule.api.MuleMessage;
import org.mule.api.Session;
import org.mule.api.endpoint.Endpoint;
import org.mule.api.registry.Registry;
import org.mule.extras.client.MuleClient;
import org.mule.impl.MuleEvent;
import org.mule.impl.DefaultMuleMessage;
import org.mule.impl.MuleSession;
import org.mule.impl.transport.AbstractConnector;
import org.mule.tck.AbstractMuleTestCase;

import org.custommonkey.xmlunit.XMLAssert;
import org.w3c.dom.Document;

public class CxfWsdlTestCase extends AbstractMuleTestCase
{
    public static final String TEST_URL = "wsdl-cxf:http://localhost:8080/mule-tests-external-cxf/services/TestService?WSDL&method=getTest";
    public static final String TEST_URL_NOWSDL = "wsdl-cxf:http://localhost:8080/mule-tests-external-cxf/services/TestService?method=getTest";
    public static final String TEST_URL_WSDL = "http://localhost:8080/mule-tests-external-cxf/services/TestService?wsdl";

    public void testCxfWsdlService() throws Exception
    {
        MuleClient client = new MuleClient();

        MuleMessage message = new DefaultMuleMessage("test1");
        MuleMessage reply = client.send(TEST_URL, message);
        assertNotNull(reply);

        Document response = (Document) reply.getPayload();
        assertNotNull(response);

        XMLAssert.assertXpathEvaluatesTo("test1",
            "//*[namespace-uri()='http://applications.external.tck.mule.org' and local-name()='key']",
            response);
    }

    /**
     * This tests the endpoint propery of wsdlUrl which specifies an alternative WSDL
     * location (see MULE-1368)
     */
    public void testCxfWsdlServiceWithEndpointParam() throws Exception
    {

        Registry registry = muleContext.getRegistry();

        Endpoint endpoint = (Endpoint) registry.lookupEndpointFactory().getOutboundEndpoint(TEST_URL_NOWSDL);
        endpoint.setProperty("wsdlUrl", TEST_URL_WSDL);

        MuleMessage message = new DefaultMuleMessage("test1");
        Session session = new MuleSession(message,
            ((AbstractConnector) endpoint.getConnector()).getSessionHandler());
        MuleEvent event = new MuleEvent(message, endpoint, session, true);
        MuleMessage reply = session.sendEvent(event);

        assertNotNull(reply);

        Document response = (Document) reply.getPayload();
        assertNotNull(response);

        XMLAssert.assertXpathEvaluatesTo("test1",
            "//*[namespace-uri()='http://applications.external.tck.mule.org' and local-name()='key']",
            response);
    }
}
