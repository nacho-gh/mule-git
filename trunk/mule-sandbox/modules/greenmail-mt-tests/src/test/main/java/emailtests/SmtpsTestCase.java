/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package emailtests;

/**
 * Send email using Smtps
 */
public class SmtpsTestCase extends SmtpTestCase
{

    protected void doPreFunctionalSetUp() throws Exception
    {
        super.doPreFunctionalSetUp();
        servers.setUser("email@address.com", "login", "password");
    }

    // @Override
    protected String getConfigResources()
    {
        return "mule-smtps-config.xml";
    }

}
