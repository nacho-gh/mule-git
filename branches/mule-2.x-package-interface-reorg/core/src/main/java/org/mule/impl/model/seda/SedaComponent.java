/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.impl.model.seda;

import org.mule.RegistryContext;
import org.mule.api.AbstractMuleException;
import org.mule.api.ComponentException;
import org.mule.api.Event;
import org.mule.api.MuleMessage;
import org.mule.api.MuleRuntimeException;
import org.mule.api.config.ThreadingProfile;
import org.mule.api.context.WorkManager;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.lifecycle.LifecycleException;
import org.mule.api.lifecycle.Startable;
import org.mule.api.lifecycle.Stoppable;
import org.mule.api.model.MuleProxy;
import org.mule.impl.FailedToQueueEventException;
import org.mule.impl.MuleEvent;
import org.mule.impl.config.MuleConfiguration;
import org.mule.impl.config.QueueProfile;
import org.mule.impl.config.i18n.CoreMessages;
import org.mule.impl.config.i18n.MessageFactory;
import org.mule.impl.management.stats.ComponentStatistics;
import org.mule.impl.model.AbstractComponent;
import org.mule.util.queue.Queue;
import org.mule.util.queue.QueueSession;

import java.util.NoSuchElementException;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkEvent;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;

/**
 * A Seda component runs inside a Seda Model and is responsible for managing a Seda
 * Queue and thread pool for a Mule sevice component. In Seda terms this is
 * equivilent to a stage.
 */
public class SedaComponent extends AbstractComponent implements Work, WorkListener
{
    /**
     * Serial version/
     */
    private static final long serialVersionUID = 7711976708670893015L;

    protected WorkManager workManager;

    /**
     * The time out used for taking from the Seda Queue.
     */
    protected Integer queueTimeout;

    /**
     * The threading profile to use for this component. If this is not set a default
     * will be provided by the server
     */
    protected ThreadingProfile threadingProfile;

    /**
     * The queue profile to use for this component. If this is not set a default
     * will be provided by the server
     */
    protected QueueProfile queueProfile;

    /** For Spring only */
    public SedaComponent()
    {
        super();
    }
    
    /**
     * Initialise the component. The component will first create a Mule UMO from the
     * UMODescriptor and then initialise a pool based on the attributes in the
     * UMODescriptor.
     * 
     * @throws org.mule.api.lifecycle.InitialisationException if the component fails
     *             to initialise
     * @see org.mule.api.UMODescriptor
     */
    public synchronized void doInitialise() throws InitialisationException
    {
        MuleConfiguration config = RegistryContext.getConfiguration();
        if (threadingProfile == null)
        {
            // TODO MULE-2102 This should be configured in the default template.
            threadingProfile = config.getDefaultComponentThreadingProfile();
        }
        // Create thread pool
        workManager = threadingProfile.createWorkManager(getName());

        if (queueProfile == null)
        {
            // TODO MULE-2102 This should be configured in the default template.
            queueProfile = ((SedaModel) model).getQueueProfile();
        }
        
        if (queueTimeout == null)
        {
            // TODO MULE-2102 This should be configured in the default template.
            setQueueTimeout(new Integer(((SedaModel) model).getQueueTimeout()));
        }
        
        try
        {
            if (name == null)
            {
                throw new InitialisationException(MessageFactory.createStaticMessage("Component has no name to identify it"), this);
            }
            // Setup event Queue (used for VM execution).  The queue has the same name as the component.
            queueProfile.configureQueue(name, muleContext.getQueueManager());
        }
        catch (InitialisationException e)
        {
            throw e;
        }
        catch (Throwable e)
        {
            throw new InitialisationException(
                CoreMessages.objectFailedToInitialise("Component Queue"), e, this);
        }
    }

    public void doForceStop() throws AbstractMuleException
    {
        doStop();
    }

    public void doStop() throws AbstractMuleException
    {
        if (muleContext.getQueueManager().getQueueSession().getQueue(name).size() > 0)
        {
            try
            {
                stopping.whenFalse(null);
            }
            catch (InterruptedException e)
            {
                // we can ignore this
                // TODO MULE-863: Why?
            }
        }
        workManager.dispose();
        if (serviceFactory instanceof Stoppable)
        {
            ((Stoppable) serviceFactory).stop();
        }
    }

    public void doStart() throws AbstractMuleException
    {
        try
        {
            //serviceFactory.initialise();
            if (serviceFactory instanceof Startable)
            {
                ((Startable) serviceFactory).start();
            }
            workManager.start();
            workManager.scheduleWork(this, WorkManager.INDEFINITE, null, this);
        }
        catch (Exception e)
        {
            throw new LifecycleException(
                CoreMessages.failedToStart("Component: " + name), e, this);
        }
    }

    protected void doDispose()
    {
        // threadPool.awaitTerminationAfterShutdown();
        if (workManager != null)
        {
            workManager.dispose();
        }

        serviceFactory.dispose();
    }

    protected void doDispatch(Event event) throws AbstractMuleException
    {
        // Dispatching event to the component
        if (stats.isEnabled())
        {
            stats.incReceivedEventASync();
        }
        if (logger.isDebugEnabled())
        {
            logger.debug("Component: " + name + " has received asynchronous event on: "
                            + event.getEndpoint().getEndpointURI());
        }

        // Block until we can queue the next event
        try
        {
            enqueue(event);
            if (stats.isEnabled())
            {
                stats.incQueuedEvent();
            }
        }
        catch (Exception e)
        {
            FailedToQueueEventException e1 = 
                new FailedToQueueEventException(
                    CoreMessages.interruptedQueuingEventFor(this.getName()), 
                    event.getMessage(), this, e);
            handleException(e1);
        }

        if (logger.isTraceEnabled())
        {
            logger.trace("Event added to queue for: " + name);
        }
    }

    public MuleMessage doSend(Event event) throws AbstractMuleException
    {
        MuleMessage result = null;
        Object pojoService = null;
        MuleProxy proxy = null;
        try
        {
            pojoService = getOrCreateService();
            proxy = createComponentProxy(pojoService);
            //proxy.start();

            if (logger.isDebugEnabled())
            {
                logger.debug(this + " : got proxy for " + event.getId() + " = " + proxy);
            }
            result = (MuleMessage) proxy.onCall(event);
        }
        catch (AbstractMuleException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ComponentException(event.getMessage(), this, e);
        }
        finally
        {
            try
            {
                serviceFactory.release(pojoService);
            }
            catch (Exception e)
            {
                // noinspection ThrowFromFinallyBlock
                throw new ComponentException(event.getMessage(), this, e);
            }
        }
        return result;
    }

    public int getQueueSize()
    {
        QueueSession session = muleContext.getQueueManager().getQueueSession();
        Queue queue = session.getQueue(name);
        if (queue == null)
        {
            logger.warn(new InitialisationException(MessageFactory.createStaticMessage("Queue " + name + " not created for component " + name), this));
            return -1;
        }
        return queue.size();
    }

    /**
     * While the component isn't stopped this runs a continuous loop checking for new
     * events in the queue.
     */
    public void run()
    {
        MuleEvent event = null;
        Object pojoService = null;
        MuleProxy proxy = null;
        QueueSession queueSession = muleContext.getQueueManager().getQueueSession();

        while (!stopped.get())
        {
            try
            {
                // Wait if the component is paused
                paused.whenFalse(null);

                // If we're doing a draining stop, read all events from the queue
                // before stopping
                if (stopping.get())
                {
                    if (queueSession == null || getQueueSize() <= 0)
                    {
                        stopping.set(false);
                        break;
                    }
                }

                event = (MuleEvent) dequeue();
                if (event != null)
                {
                    if (stats.isEnabled())
                    {
                        stats.decQueuedEvent();
                    }

                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Component: " + name + " dequeued event on: "
                                        + event.getEndpoint().getEndpointURI());
                    }

                    pojoService = getOrCreateService();
                    proxy = createComponentProxy(pojoService);
                    //proxy.start();
                    proxy.onEvent(queueSession, event);
                    workManager.scheduleWork(proxy, WorkManager.INDEFINITE, null, this);
                }
            }
            catch (Exception e)
            {
                if(isStopped() || isStopping())
                {
                    break;
                }
                try
                {
                    if (serviceFactory != null)
                    {
                        serviceFactory.release(pojoService);
                    }
                }
                catch (Exception e2)
                {
                    // TODO MULE-863: If this is an error, do something about it
                    logger.warn(e2);
                }

                if (e instanceof InterruptedException)
                {
                    stopping.set(false);
                    break;
                }
                else if (e instanceof NoSuchElementException)
                {
                    handleException(new ComponentException(CoreMessages.proxyPoolTimedOut(),
                        (event == null ? null : event.getMessage()), this, e));
                }
                else if (e instanceof AbstractMuleException)
                {
                    handleException(e);
                }
                else if (e instanceof WorkException)
                {
                    handleException(
                        new ComponentException(
                            CoreMessages.eventProcessingFailedFor(name),
                            (event == null ? null : event.getMessage()), this, e));
                }
                else
                {
                    handleException(
                        new ComponentException(
                            CoreMessages.failedToGetPooledObject(),
                            (event == null ? null : event.getMessage()), this, e));
                }
            }
            finally
            {
                stopping.set(false);
            }
        }
    }

    public void release()
    {
        stopping.set(false);
    }

    protected void enqueue(Event event) throws Exception
    {
        QueueSession session = muleContext.getQueueManager().getQueueSession();
        Queue queue = session.getQueue(name);
        if (queue == null)
        {
            throw new InitialisationException(MessageFactory.createStaticMessage("Queue " + name + " not created for component " + name), this);
        }
        if (logger.isDebugEnabled())
        {
            logger.debug("Component " + name + " putting event on queue " + name + ": " + event);
        }
        queue.put(event);
    }

    protected Event dequeue() throws Exception
    {
        QueueSession session = muleContext.getQueueManager().getQueueSession();
        Queue queue = session.getQueue(name);
        if (queue == null)
        {
            throw new InitialisationException(MessageFactory.createStaticMessage("Queue " + name + " not created for component " + name), this);
        }
        if (logger.isDebugEnabled())
        {
            //logger.debug("Component " + name + " polling queue " + name + ", timeout = " + queueTimeout);
        }
        if (getQueueTimeout() == null)
        {
            throw new InitialisationException(CoreMessages.noComponentQueueTimeoutSet(this), this);
        }
        else
        {
            return (Event) queue.poll(getQueueTimeout().intValue());
        }
    }

    public void workAccepted(WorkEvent event)
    {
        handleWorkException(event, "workAccepted");
    }

    public void workRejected(WorkEvent event)
    {
        handleWorkException(event, "workRejected");
    }

    public void workStarted(WorkEvent event)
    {
        handleWorkException(event, "workStarted");
    }

    public void workCompleted(WorkEvent event)
    {
        handleWorkException(event, "workCompleted");
    }

    protected void handleWorkException(WorkEvent event, String type)
    {
        Throwable e;

        if (event != null && event.getException() != null)
        {
            e = event.getException();
        }
        else
        {
            return;
        }

        if (event.getException().getCause() != null)
        {
            e = event.getException().getCause();
        }

        logger.error("Work caused exception on '" + type + "'. Work being executed was: "
                        + event.getWork().toString());

        if (e instanceof Exception)
        {
            handleException((Exception) e);
        }
        else
        {
            throw new MuleRuntimeException(
                CoreMessages.componentCausedErrorIs(this.getName()), e);
        }
    }

    protected ComponentStatistics createStatistics()
    {
        return new ComponentStatistics(getName(), threadingProfile.getMaxThreadsActive());
    }

    public Object getInstance() throws AbstractMuleException
    {
        throw new UnsupportedOperationException("Direct access to underlying service object is not allowed in the SedaModel.  If this is for a unit test, make sure you are using the TestSedaModel ('seda-test')");
    }

    public QueueProfile getQueueProfile()
    {
        return queueProfile;
    }

    public void setQueueProfile(QueueProfile queueProfile)
    {
        this.queueProfile = queueProfile;
    }

    public Integer getQueueTimeout()
    {
        return queueTimeout;
    }

    public void setQueueTimeout(Integer queueTimeout)
    {
        this.queueTimeout = queueTimeout;
    }

    public ThreadingProfile getThreadingProfile()
    {
        return threadingProfile;
    }

    public void setThreadingProfile(ThreadingProfile threadingProfile)
    {
        this.threadingProfile = threadingProfile;
    }

    public WorkManager getWorkManager()
    {
        return workManager;
    }

    public void setWorkManager(WorkManager workManager)
    {
        this.workManager = workManager;
    }
}
