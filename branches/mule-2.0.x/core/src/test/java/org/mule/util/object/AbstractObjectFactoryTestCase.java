/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.util.object;

import org.mule.api.lifecycle.InitialisationException;
import org.mule.tck.AbstractMuleTestCase;

public abstract class AbstractObjectFactoryTestCase extends AbstractMuleTestCase
{

    public void _testInitialisationFailure() throws Exception
    {
        AbstractObjectFactory factory = (AbstractObjectFactory) getObjectFactory();

        try
        {
            factory.initialise();
            fail("expected InitialisationException");
        }
        catch (InitialisationException iex)
        {
            // OK
        }

        try
        {
            factory.getInstance();
            fail("expected InitialisationException");
        }
        catch (InitialisationException iex)
        {
            // OK
        }
    }

    public void testInitialiseWithClass() throws Exception
    {
        AbstractObjectFactory factory = (AbstractObjectFactory) getObjectFactory();
        factory.setObjectClass(Object.class);

        try
        {
            factory.initialise();
        }
        catch (InitialisationException iex)
        {
            fail(iex.getDetailedMessage());
        }

        assertNotNull(factory.getInstance());
    }

    public void testInitialiseWithClassName() throws Exception
    {
        AbstractObjectFactory factory = (AbstractObjectFactory) getObjectFactory();
        factory.setObjectClassName(Object.class.getName());

        try
        {
            factory.initialise();
        }
        catch (InitialisationException iex)
        {
            fail(iex.getDetailedMessage());
        }

        assertNotNull(factory.getInstance());
    }

    // @Override
    public void testDispose() throws Exception
    {
        AbstractObjectFactory factory = (AbstractObjectFactory) getObjectFactory();
        factory.setObjectClass(Object.class);

        factory.initialise();
        factory.dispose();

        try
        {
            factory.getInstance();
            fail("expected InitialisationException");
        }
        catch (InitialisationException iex)
        {
            // OK
        }
    }

    public abstract ObjectFactory getObjectFactory();

    public abstract void testGetObjectClass() throws Exception;

    public abstract void testGet() throws Exception;

}
