/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the BSD style
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.samples.loanbroker;


public class VMLoanBrokerSynchronousFunctionalTestCase extends AxisLoanBrokerSynchronousFunctionalTestCase
{
    protected String getConfigResources()
    {
        return "loan-broker-vm-sync-config.xml";
    }

}


