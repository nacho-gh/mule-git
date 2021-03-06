/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.extras.pgp;

import org.mule.api.security.Authentication;
import org.mule.api.security.SecurityContext;
import org.mule.api.security.SecurityContextFactory;

public class PGPSecurityContextFactory implements SecurityContextFactory
{

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.api.security.SecurityContextFactory#create(org.mule.api.security.Authentication)
     */
    public SecurityContext create(Authentication authentication)
    {
        return new PGPSecurityContext((PGPAuthentication)authentication);
    }

}
