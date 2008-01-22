/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.components.script.jsr223;

import org.mule.api.EventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.util.MuleLogger;

import javax.script.Bindings;

/**
 * A JSR 223 Script component. Allows any JSR 223 compliant script engines such as
 * JavaScript, Groovy or Rhino to be embedded as Mule components.
 */
public class ScriptComponent extends Scriptable implements Callable
{
    private Bindings bindings;

    public void initialise() throws InitialisationException
    {
        super.initialise();
        bindings = getScriptEngine().createBindings();
    }

    public Object onCall(EventContext eventContext) throws Exception
    {
        populateBindings(bindings, eventContext);
        Object result = runScript(bindings);
        if (result == null)
        {
            result = bindings.get("result");
        }
        return result;
    }

    protected void populateBindings(Bindings namespace, EventContext context)
    {
        namespace.put("eventContext", context);
        namespace.put("muleContext", context.getMuleContext());
        namespace.put("message", context.getMessage());
        namespace.put("descriptor", context.getComponent());
        namespace.put("componentNamespace", this.bindings);
        namespace.put("log", new MuleLogger(logger));
        namespace.put("result", new Object());
    }

    public Bindings getBindings()
    {
        return bindings;
    }

}
