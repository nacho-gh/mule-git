/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.config;

import org.mule.config.MuleConfiguration;
import org.mule.config.PoolingProfile;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.util.ObjectPool;

public class MuleConfigurationTestCase extends AbstractMuleTestCase
{

    public void testConfigurationDefaults()
    {
        MuleConfiguration mc = new MuleConfiguration();
        assertEquals(MuleConfiguration.DEFAULT_MAX_OUTSTANDING_MESSAGES, mc.getQueueProfile()
            .getMaxOutstandingMessages());
        assertEquals(PoolingProfile.DEFAULT_MAX_POOL_WAIT, mc.getPoolingProfile().getMaxWait());
        assertEquals(PoolingProfile.DEFAULT_MAX_POOL_ACTIVE, mc.getPoolingProfile().getMaxActive());
        assertEquals(PoolingProfile.DEFAULT_MAX_POOL_IDLE, mc.getPoolingProfile().getMaxIdle());
        assertEquals(PoolingProfile.DEFAULT_POOL_EXHAUSTED_ACTION, mc.getPoolingProfile()
            .getExhaustedAction());
        assertEquals(PoolingProfile.DEFAULT_POOL_INITIALISATION_POLICY, mc.getPoolingProfile()
            .getInitialisationPolicy());
        assertEquals(MuleConfiguration.DEFAULT_SYNCHRONOUS, mc.isSynchronous());
        assertNull(mc.getModel());
    }

    public void testConfiguration()
    {
        MuleConfiguration mc = new MuleConfiguration();

        mc.getQueueProfile().setMaxOutstandingMessages(1);
        mc.getPoolingProfile().setMaxWait(0);
        mc.getPoolingProfile().setMaxActive(1);
        mc.getPoolingProfile().setMaxIdle(1);
        mc.getPoolingProfile().setExhaustedAction((byte) 1);
        mc.getPoolingProfile().setInitialisationPolicy((byte) 0);
        mc.setSynchronous(false);
        mc.setModel("Test");

        PoolingProfile pp = mc.getPoolingProfile();

        pp.setExhaustedActionString("GROW");
        assertEquals(ObjectPool.WHEN_EXHAUSTED_GROW, pp.getExhaustedAction());
        pp.setExhaustedActionString("WHEN_EXHAUSTED_GROW");
        assertEquals(ObjectPool.WHEN_EXHAUSTED_GROW, pp.getExhaustedAction());

        pp.setExhaustedActionString("WAIT");
        assertEquals(ObjectPool.WHEN_EXHAUSTED_WAIT, pp.getExhaustedAction());
        pp.setExhaustedActionString("WHEN_EXHAUSTED_WAIT");
        assertEquals(ObjectPool.WHEN_EXHAUSTED_WAIT, pp.getExhaustedAction());

        pp.setExhaustedActionString("FAIL");
        assertEquals(ObjectPool.WHEN_EXHAUSTED_FAIL, pp.getExhaustedAction());
        pp.setExhaustedActionString("WHEN_EXHAUSTED_FAIL");
        assertEquals(ObjectPool.WHEN_EXHAUSTED_FAIL, pp.getExhaustedAction());

        pp.setExhaustedActionString("BLAH");
        assertEquals(ObjectPool.DEFAULT_EXHAUSTED_ACTION, pp.getExhaustedAction());

        pp.setExhaustedActionString(null);
        assertEquals(ObjectPool.DEFAULT_EXHAUSTED_ACTION, pp.getExhaustedAction());

        pp.setInitialisationPolicyString("INITIALISE_NONE");
        assertEquals(PoolingProfile.INITIALISE_NONE, pp.getInitialisationPolicy());

        pp.setInitialisationPolicyString("INITIALISE_ONE");
        assertEquals(PoolingProfile.INITIALISE_ONE, pp.getInitialisationPolicy());
        pp.setInitialisationPolicyString("POOL_INITIALISE_ONE_COMPONENT");
        assertEquals(PoolingProfile.INITIALISE_ONE, pp.getInitialisationPolicy());

        pp.setInitialisationPolicyString("INITIALISE_ALL");
        assertEquals(PoolingProfile.INITIALISE_ALL, pp.getInitialisationPolicy());

        pp.setInitialisationPolicyString("FOO");
        assertEquals(PoolingProfile.DEFAULT_POOL_INITIALISATION_POLICY, pp.getInitialisationPolicy());

        pp.setInitialisationPolicyString(null);
        assertEquals(PoolingProfile.DEFAULT_POOL_INITIALISATION_POLICY, pp.getInitialisationPolicy());
    }

}
