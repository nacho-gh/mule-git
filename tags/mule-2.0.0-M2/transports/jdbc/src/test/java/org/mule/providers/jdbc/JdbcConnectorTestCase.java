/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.jdbc;

import org.mule.tck.providers.AbstractConnectorTestCase;
import org.mule.umo.provider.UMOConnector;

import java.util.HashMap;
import java.util.Map;

import org.apache.derby.jdbc.EmbeddedDataSource;

public class JdbcConnectorTestCase extends AbstractConnectorTestCase
{
    // @Override
    public UMOConnector createConnector() throws Exception
    {
        JdbcConnector c = new JdbcConnector();
        EmbeddedDataSource embeddedDS = new EmbeddedDataSource();
        embeddedDS.setDatabaseName("embeddedDB");
        c.setName("JdbcConnector");
        c.setDataSource(embeddedDS);
        c.setPollingFrequency(1000);
        return c;
    }

    public Object getValidMessage() throws Exception
    {
        Map map = new HashMap();
        return map;
    }

    public String getTestEndpointURI()
    {
        return "jdbc://test?sql=SELECT * FROM TABLE";
    }
}
