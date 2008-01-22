/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.impl.security;

import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.security.MuleAuthentication;
import org.mule.api.security.SecurityContext;
import org.mule.api.security.SecurityProvider;
import org.mule.api.security.UnknownAuthenticationTypeException;

/**
 * Empty mock for test
 */
public class MockSecurityProvider extends Named implements SecurityProvider
{

    public MuleAuthentication authenticate(MuleAuthentication authentication) throws org.mule.api.security.SecurityException
    {
        return null;
    }

    public boolean supports(Class aClass)
    {
        return false;
    }

    public SecurityContext createSecurityContext(MuleAuthentication auth) throws UnknownAuthenticationTypeException
    {
        return null;
    }

    public void initialise() throws InitialisationException
    {
        // mock
    }

}
