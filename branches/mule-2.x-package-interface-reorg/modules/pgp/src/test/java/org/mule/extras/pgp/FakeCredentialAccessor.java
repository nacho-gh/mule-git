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

import org.mule.api.Event;
import org.mule.api.security.CredentialsAccessor;

public class FakeCredentialAccessor implements CredentialsAccessor
{

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.api.security.CredentialsAccessor#getCredentials(org.mule.api.Event)
     */
    public Object getCredentials(Event event)
    {
        return "Mule client <mule_client@mule.com>";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.api.security.CredentialsAccessor#setCredentials(org.mule.api.Event,
     *      java.lang.Object)
     */
    public void setCredentials(Event event, Object credentials)
    {
        // dummy
    }

}
