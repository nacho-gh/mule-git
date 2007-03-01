/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.jbpm.actions.mule;

import org.mule.providers.bpm.ProcessConnector;

import org.jbpm.actions.LoggingActionHandler;
import org.jbpm.graph.exe.ExecutionContext;

public abstract class IntegrationActionHandler extends LoggingActionHandler
{

    private Object incoming;

    public void execute(ExecutionContext executionContext) throws Exception
    {
        super.execute(executionContext);
        incoming = executionContext.getVariable(ProcessConnector.PROCESS_VARIABLE_INCOMING);
    }

    protected Object getIncomingMessage()
    {
        return incoming;
    }

}
