/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.impl.model.direct;

import org.mule.impl.model.AbstractComponentTestCase;
import org.mule.umo.UMOException;
import org.mule.util.object.SingletonObjectFactory;

public class DirectComponentTestCase extends AbstractComponentTestCase
{

    protected void doSetUp() throws Exception
    {
        component = new DirectComponent();
        component.setName("direct");
        component.setServiceFactory(new SingletonObjectFactory(Object.class));
        component.setModel(new DirectModel());
        component.setManagementContext(managementContext);
    }

    protected void doTearDown() throws Exception
    {
        component = null;
    }

    public void testStop() throws UMOException
    {
        // TODO Remove this overridden empty implementation once MULE-2844 is resolved
    }

}
