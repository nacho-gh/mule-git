/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.mule.commonspool;

import org.mule.config.pool.CommonsPoolFactory;
import org.mule.config.pool.CommonsPoolProxyPool;
import org.mule.impl.MuleDescriptor;
import org.mule.tck.model.AbstractPoolTestCase;
import org.mule.tck.testmodels.mule.TestSedaModel;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.model.UMOPoolFactory;
import org.mule.util.object.ObjectPool;

import org.apache.commons.pool.impl.GenericObjectPool;

public class CommonsPoolTestCase extends AbstractPoolTestCase
{
    public ObjectPool createPool(MuleDescriptor descriptor, byte action) throws InitialisationException
    {
        GenericObjectPool.Config config = new GenericObjectPool.Config();
        config.maxActive = DEFAULT_POOL_SIZE;
        config.maxWait = DEFAULT_WAIT;

        if (action == FAIL_WHEN_EXHAUSTED)
        {
            config.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_FAIL;
        }
        else if (action == GROW_WHEN_EXHAUSTED)
        {
            config.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_GROW;
        }
        else if (action == BLOCK_WHEN_EXHAUSTED)
        {
            config.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
        }
        else
        {
            fail("Action type for pool not recognised. Type is: " + action);
        }
        return new CommonsPoolProxyPool(descriptor, new TestSedaModel(), config);
    }

    public UMOPoolFactory getPoolFactory()
    {
        return new CommonsPoolFactory();
    }
}
