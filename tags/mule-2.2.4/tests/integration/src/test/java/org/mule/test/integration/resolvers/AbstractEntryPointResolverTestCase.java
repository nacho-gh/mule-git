/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.integration.resolvers;

import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.tck.FunctionalTestCase;

import java.util.Map;

public abstract class AbstractEntryPointResolverTestCase extends FunctionalTestCase
{

    protected void doTest(String path, Object payload, String result) throws Exception
    {
        doTest(path, payload, result, null);
    }

    protected void doTest(String path, Object payload, String result, Map properties) throws Exception
    {
        MuleClient client = new MuleClient();
        MuleMessage response = client.send("vm://" + path, payload, properties);
        assertEquals(result, response.getPayloadAsString());
    }

}