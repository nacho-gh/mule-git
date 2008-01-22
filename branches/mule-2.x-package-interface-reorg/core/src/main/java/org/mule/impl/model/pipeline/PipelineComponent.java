/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.impl.model.pipeline;

import org.mule.api.AbstractMuleException;
import org.mule.api.Event;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.lifecycle.Initialisable;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.transport.DispatchException;
import org.mule.impl.DefaultMuleMessage;
import org.mule.impl.RequestContext;
import org.mule.impl.config.i18n.CoreMessages;
import org.mule.impl.model.direct.DirectComponent;

public class PipelineComponent extends DirectComponent
{
    /**
     * Serial version
     */
    private static final long serialVersionUID = -2788210157354765190L;

    private Callable callable;

    public PipelineComponent()
    {
        super();
    }

    protected void doInitialise() throws InitialisationException
    {

        super.doInitialise();
        Object component = null;
        try
        {
            component = getOrCreateService();
        }
        catch (AbstractMuleException e)
        {
            throw new InitialisationException(e, this);
        }
        if (component instanceof Callable)
        {
            callable = (Callable) component;
        }
        else
        {
            throw new InitialisationException(
                CoreMessages.objectNotOfCorrectType(component.getClass(), Callable.class), this);
        }

        if (component instanceof Initialisable)
        {
            ((Initialisable) component).initialise();
        }

    }

    protected MuleMessage doSend(Event event) throws AbstractMuleException
    {
        try
        {
            Object result = callable.onCall(RequestContext.getEventContext());
            MuleMessage returnMessage = null;
            if (result instanceof MuleMessage)
            {
                returnMessage = (MuleMessage) result;
            }
            else
            {
                returnMessage = new DefaultMuleMessage(result, event.getMessage());
            }
            if (!event.isStopFurtherProcessing())
            {
                // // TODO what about this code?
                // Map context = RequestContext.clearProperties();
                // if (context != null) {
                // returnMessage.addProperties(context);
                // }
                if (outboundRouter.hasEndpoints())
                {
                    MuleMessage outboundReturnMessage = outboundRouter.route(returnMessage,
                        event.getSession(), event.isSynchronous());
                    if (outboundReturnMessage != null)
                    {
                        returnMessage = outboundReturnMessage;
                    }
                }
                else
                {
                    logger.debug("Outbound router on component '" + name
                                 + "' doesn't have any endpoints configured.");
                }
            }

            return returnMessage;
        }
        catch (Exception e)
        {
            throw new DispatchException(event.getMessage(), event.getEndpoint(), e);
        }
    }

    protected void doDispatch(Event event) throws AbstractMuleException
    {
        sendEvent(event);
    }

    protected void doDispose()
    {
        try
        {
            serviceFactory.release(callable);
        }
        catch (Exception e)
        {
            logger.warn(e);
        }
    }
}
