/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.integration.providers.jms.activemq;

import org.mule.test.integration.providers.jms.AbstractJmsQueueFunctionalTestCase;

public class ActiveMQJmsQueueFunctionalTestCase extends AbstractJmsQueueFunctionalTestCase
{
    protected String getConfigResources()
    {
        return "activemq-config.xml," + super.getConfigResources();
    }
}
