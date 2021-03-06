/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport;

import org.mule.OptimizedRequestContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.config.MuleProperties;
import org.mule.api.context.WorkManager;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.routing.ResponseRouterCollection;
import org.mule.api.transaction.Transaction;
import org.mule.api.transaction.TransactionException;
import org.mule.api.transport.DispatchException;
import org.mule.api.transport.MessageDispatcher;
import org.mule.context.notification.EndpointMessageNotification;
import org.mule.context.notification.SecurityNotification;
import org.mule.transaction.TransactionCoordination;
import org.mule.work.AbstractMuleEventWork;

import java.util.List;

/**
 * Provide a default dispatch (client) support for handling threads lifecycle and validation.
 */
public abstract class AbstractMessageDispatcher extends AbstractConnectable implements MessageDispatcher
{
    public AbstractMessageDispatcher(OutboundEndpoint endpoint)
    {
        super(endpoint);
    }

    @Override
    public final void initialise() throws InitialisationException
    {
        super.initialise();
        
        doInitialise();
    }

    @Override
    public final synchronized void dispose()
    {
        super.dispose();
        try
        {
            doDispose();
        }
        finally
        {
            disposed.set(true);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.api.transport.MessageDispatcher#dispatch(org.mule.api.MuleEvent)
     */
    public final void dispatch(MuleEvent event) throws DispatchException
    {
        event.setSynchronous(false);
        event.getMessage().setProperty(MuleProperties.MULE_ENDPOINT_PROPERTY,
            event.getEndpoint().getEndpointURI().toString());
        
        // Apply Security filter if one is set
        ImmutableEndpoint endpoint = event.getEndpoint();
        if (endpoint.getSecurityFilter() != null)
        {
            try
            {
                endpoint.getSecurityFilter().authenticate(event);
            }
            catch (org.mule.api.security.SecurityException e)
            {
                // TODO MULE-863: Do we need this warning?
                logger.warn("Outbound Request was made but was not authenticated: " + e.getMessage(), e);
                connector.fireNotification(new SecurityNotification(e,
                    SecurityNotification.SECURITY_AUTHENTICATION_FAILED));
                handleException(e);
                return;
            }
            catch (Exception e)
            {
                handleException(e);
                return;
            }
        }

        try
        {
            Transaction tx = TransactionCoordination.getInstance().getTransaction();
            Worker worker = new Worker(event);
            if (isDoThreading() && !event.isSynchronous() && tx == null)
            {
                connector.getDispatcherWorkManager().scheduleWork(worker, WorkManager.INDEFINITE, null, connector);
            }
            else
            {
                // Execute within this thread
                worker.run();
            }
        }
        catch (Exception e)
        {
            handleException(e);
        }
    }

    public final MuleMessage send(MuleEvent event) throws DispatchException
    {
        // No point continuing if the service has rolledback the transaction
        if (isTransactionRollback())
        {
            return event.getMessage();
        }

        event.setSynchronous(true);
        event.getMessage().setProperty(MuleProperties.MULE_ENDPOINT_PROPERTY,
            event.getEndpoint().getEndpointURI().getUri().toString());
        event = OptimizedRequestContext.unsafeSetEvent(event);

        // Apply Security filter if one is set
        ImmutableEndpoint endpoint = event.getEndpoint();
        if (endpoint.getSecurityFilter() != null)
        {
            try
            {
                endpoint.getSecurityFilter().authenticate(event);
            }
            catch (org.mule.api.security.SecurityException e)
            {
                logger.warn("Outbound Request was made but was not authenticated: " + e.getMessage(), e);
                connector.fireNotification(new SecurityNotification(e,
                    SecurityNotification.SECURITY_AUTHENTICATION_FAILED));
                handleException(e);
                return event.getMessage();
            }
            catch (Exception e)
            {
                handleException(e);
                throw new DispatchException(event.getMessage(), event.getEndpoint(), e);
            }
        }

        try
        {
            // Make sure we are connected
            connect();

            MuleMessage result = doSend(event);

            if (result != null)
            {
                // Properties which should be carried over from the request message to the response message
                List<String> responseProperties = ((OutboundEndpoint) endpoint).getResponseProperties();
                for (String propertyName : responseProperties)
                {
                    Object propertyValue = event.getMessage().getProperty(propertyName);
                    if (propertyValue != null)
                    {
                        result.setProperty(propertyName, propertyValue);
                    }
                }
            }

            if (connector.isEnableMessageEvents())
            {
                String component = null;
                if (event.getService() != null)
                {
                    component = event.getService().getName();
                }
                connector.fireNotification(new EndpointMessageNotification(event.getMessage(), event.getEndpoint(),
                    component, EndpointMessageNotification.MESSAGE_SENT));
            }
            return result;
        }
        catch (DispatchException e)
        {
            handleException(e);
            throw e;
        }
        catch (Exception e)
        {
            handleException(e);
            throw new DispatchException(event.getMessage(), event.getEndpoint(), e);
        }
    }

    /**
     * RemoteSync causes the message dispatch to wait for a response to an event on a
     * response channel after it sends the event. The following rules apply to
     * RemoteSync:
     * <ol><li>The connector has to support remoteSync. Some transports do not
     * have the notion of a response channel.
     * <li>Check if the endpoint has been configured for remoteSync.
     * <li>Check if the REMOTE_SYNC message header has been set.
     * <li>Finally, if the current service has a response router configured,
     * that the router will handle the response channel event and we should not try
     * and receive a response in the Message dispatcher If remotesync should not be
     * used we must remove the REMOTE_SYNC header Note the MuleClient will
     * automatically set the REMOTE_SYNC header when client.send(..) is called so
     * that results are returned from remote invocations too.
     * </ol>
     * @param event the current event
     * @return true if a response channel should be used to get a resposne from the
     *         event dispatch.
     */
    protected boolean returnResponse(MuleEvent event)
    {
        boolean remoteSync = false;
        if (event.getEndpoint().getConnector().isResponseEnabled())
        {
            remoteSync = event.getEndpoint().isSynchronous()
                            || event.getMessage().getBooleanProperty(
                                MuleProperties.MULE_REMOTE_SYNC_PROPERTY, false);
            if (remoteSync)
            {
                // service will be null for client calls
                if (event.getService() != null)
                {
                    ResponseRouterCollection responseRouters = event.getService().getResponseRouter();
                    if (responseRouters != null && responseRouters.hasEndpoints())
                    {
                        remoteSync = false;
                    }
                    else
                    {
                        remoteSync = true;
                    }
                }
            }
        }
        if (!remoteSync)
        {
            event.getMessage().removeProperty(MuleProperties.MULE_REMOTE_SYNC_PROPERTY);
        }
        return remoteSync;
    }

    private class Worker extends AbstractMuleEventWork
    {
        public Worker(MuleEvent event)
        {
            super(event);
        }

        protected void doRun()
        {
            try
            {
                // Make sure we are connected
                connect();
                doDispatch(event);

                if (connector.isEnableMessageEvents())
                {
                    String component = null;
                    if (event.getService() != null)
                    {
                        component = event.getService().getName();
                    }

                    connector.fireNotification(new EndpointMessageNotification(event.getMessage(), event
                        .getEndpoint(), component, EndpointMessageNotification.MESSAGE_DISPATCHED));
                }
            }
            catch (Exception e)
            {
                handleException(e);
            }
        }
    }

    /**
     * Checks to see if the current transaction has been rolled back
     */
    protected boolean isTransactionRollback()
    {
        try
        {
            Transaction tx = TransactionCoordination.getInstance().getTransaction();
            if (tx != null && tx.isRollbackOnly())
            {
                return true;
            }
        }
        catch (TransactionException e)
        {
            // TODO MULE-863: What should we really do?
            logger.warn(e.getMessage());
        }
        return false;
    }

    @Override
    protected WorkManager getWorkManager()
    {
        try
        {
            return connector.getDispatcherWorkManager();
        }
        catch (MuleException e)
        {
            logger.error(e);
            return null;
        }
    }
    
    public OutboundEndpoint getEndpoint()
    {
        return (OutboundEndpoint) super.getEndpoint();
    }
    
    protected abstract void doDispatch(MuleEvent event) throws Exception;

    protected abstract MuleMessage doSend(MuleEvent event) throws Exception;                                             
}
