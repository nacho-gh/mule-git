/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.jms;

import org.mule.impl.endpoint.MuleEndpointURI;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.umo.endpoint.UMOEndpointURI;

public class JmsEndpointTestCase extends AbstractMuleTestCase
{

    public void testWithoutFullUrl() throws Exception
    {
        UMOEndpointURI url = new MuleEndpointURI("jms:/my.queue");
        assertEquals("jms", url.getScheme());
        assertEquals("my.queue", url.getAddress());
        assertNull(url.getEndpointName());
        assertEquals("jms:/my.queue", url.toString());
    }

    public void testFullUrlWithSlashes() throws Exception
    {
        UMOEndpointURI url = new MuleEndpointURI("jms:////my/queue");
        assertEquals("jms", url.getScheme());
        assertEquals("my/queue", url.getAddress());
        assertNull(url.getEndpointName());
        assertEquals("jms:////my/queue", url.toString());
    }

    public void testWithoutFullUrlAndProvider() throws Exception
    {
        UMOEndpointURI url = new MuleEndpointURI("jms:/jmsProvider/my.queue");
        assertEquals("jms", url.getScheme());
        assertEquals("my.queue", url.getAddress());
        assertNotNull(url.getEndpointName());
        assertEquals("jmsProvider", url.getEndpointName());
        assertEquals("jms:/jmsProvider/my.queue", url.toString());
    }

    public void testJmsUrl() throws Exception
    {
        UMOEndpointURI url = new MuleEndpointURI("jms://localhost/jmsProvider/queue1");
        assertEquals("jms", url.getScheme());
        assertEquals("queue1", url.getAddress());
        assertEquals("jmsProvider", url.getEndpointName());
        assertEquals("jms://localhost/jmsProvider/queue1", url.toString());
    }

    public void testJmsUrlWithHostOverride() throws Exception
    {
        UMOEndpointURI url = new MuleEndpointURI("jms://jmsProvider/queue1");
        assertEquals("jms", url.getScheme());
        assertEquals("queue1", url.getAddress());
        assertEquals("jmsProvider", url.getEndpointName());
        assertEquals("jms://jmsProvider/queue1", url.toString());
    }

    public void testJmsTopic() throws Exception
    {
        UMOEndpointURI url = new MuleEndpointURI("jms://topic:topic1");
        assertEquals("jms", url.getScheme());
        assertEquals("topic1", url.getAddress());
        assertEquals("topic", url.getResourceInfo());
        assertEquals(null, url.getEndpointName());
        assertEquals("jms://topic:topic1", url.toString());
    }

    public void testJmsTopicWithProvider() throws Exception
    {
        UMOEndpointURI url = new MuleEndpointURI("jms://jmsProvider/topic:topic1");
        assertEquals("jms", url.getScheme());
        assertEquals("topic1", url.getAddress());
        assertEquals("jmsProvider", url.getEndpointName());
        assertEquals("topic", url.getResourceInfo());
        assertEquals("jms://jmsProvider/topic:topic1", url.toString());
    }

    public void testJmsTopicWithUserInfo() throws Exception
    {
        UMOEndpointURI url = new MuleEndpointURI("jms://user:password@topic:topic1");
        assertEquals("jms", url.getScheme());
        assertEquals("topic1", url.getAddress());
        assertEquals("topic", url.getResourceInfo());
        assertEquals("user:password", url.getUserInfo());
        assertEquals("user", url.getUsername());
        assertEquals("password", url.getPassword());
        assertEquals("jms://user:password@topic:topic1", url.toString());
    }

    public void testJmsTopicWithUserInfoAndProvider() throws Exception
    {
        UMOEndpointURI url = new MuleEndpointURI("jms://user:password@jmsProvider/topic:topic1");
        assertEquals("jms", url.getScheme());
        assertEquals("topic1", url.getAddress());
        assertEquals("jmsProvider", url.getEndpointName());
        assertEquals("topic", url.getResourceInfo());
        assertEquals("user:password", url.getUserInfo());
        assertEquals("user", url.getUsername());
        assertEquals("password", url.getPassword());
        assertEquals("jms://user:password@jmsProvider/topic:topic1", url.toString());
    }

}
