/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.impl.routing.outbound;

import org.mule.api.AbstractMuleException;
import org.mule.api.MuleMessage;
import org.mule.api.Session;
import org.mule.api.TransactionConfig;
import org.mule.api.config.MuleProperties;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.endpoint.InvalidEndpointTypeException;
import org.mule.api.routing.OutboundRouter;
import org.mule.impl.config.i18n.CoreMessages;
import org.mule.impl.routing.AbstractRouter;
import org.mule.impl.routing.CorrelationPropertiesExtractor;
import org.mule.util.ClassUtils;
import org.mule.util.StringMessageUtils;
import org.mule.util.SystemUtils;
import org.mule.util.properties.PropertyExtractor;

import java.util.Iterator;
import java.util.List;

import edu.emory.mathcs.backport.java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>AbstractOutboundRouter</code> is a base router class that tracks
 * statistics about message processing through the router.
 *
 */
public abstract class AbstractOutboundRouter extends AbstractRouter implements OutboundRouter
{
    public static final int ENABLE_CORRELATION_IF_NOT_SET = 0;
    public static final int ENABLE_CORRELATION_ALWAYS = 1;
    public static final int ENABLE_CORRELATION_NEVER = 2;
    /**
     * logger used by this class
     */
    protected transient Log logger = LogFactory.getLog(getClass());

    protected List endpoints = new CopyOnWriteArrayList();

    protected String replyTo = null;

    protected int enableCorrelation = ENABLE_CORRELATION_IF_NOT_SET;

    protected PropertyExtractor propertyExtractor = new CorrelationPropertiesExtractor();

    protected TransactionConfig transactionConfig;

    public void dispatch(Session session, MuleMessage message, ImmutableEndpoint endpoint) throws AbstractMuleException
    {
        setMessageProperties(session, message, endpoint);

        if (logger.isDebugEnabled())
        {
            try
            {
                logger.debug("Message being sent to: " + endpoint.getEndpointURI() + " Message payload: \n"
                             + StringMessageUtils.truncate(message.getPayloadAsString(), 100, false)
                             + "\n outbound transformer is: " + endpoint.getTransformers());
            }
            catch (Exception e)
            {
                logger.debug("Message being sent to: " + endpoint.getEndpointURI()
                             + " Message payload: \n(unable to retrieve payload: " + e.getMessage()
                             + "\n outbound transformer is: " + endpoint.getTransformers());
            }
        }

        session.dispatchEvent(message, endpoint);
        if (getRouterStatistics() != null)
        {
            if (getRouterStatistics().isEnabled())
            {
                getRouterStatistics().incrementRoutedMessage(endpoint);
            }
        }
    }

    public MuleMessage send(Session session, MuleMessage message, ImmutableEndpoint endpoint) throws AbstractMuleException
    {
        if (replyTo != null)
        {
            logger.debug("event was dispatched synchronously, but there is a ReplyTo endpoint set, so using asynchronous dispatch");
            dispatch(session, message, endpoint);
            return null;
        }

        this.setMessageProperties(session, message, endpoint);

        if (logger.isDebugEnabled())
        {
            logger.debug("Message being sent to: " + endpoint.getEndpointURI());
            logger.debug(message);
        }

        if (logger.isTraceEnabled())
        {
            try
            {
                logger.trace("Message payload: \n" + message.getPayloadAsString());
            }
            catch (Exception e)
            {
                // ignore
            }
        }

        MuleMessage result = session.sendEvent(message, endpoint);

        if (getRouterStatistics() != null)
        {
            if (getRouterStatistics().isEnabled())
            {
                getRouterStatistics().incrementRoutedMessage(endpoint);
            }
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Response message from sending to: " + endpoint.getEndpointURI());
            logger.debug(result);
        }

        if (logger.isTraceEnabled())
        {
            try
            {
                logger.trace("Message payload: \n" + result.getPayloadAsString());
            }
            catch (Exception e)
            {
                // ignore
            }
        }

        return result;
    }

    protected void setMessageProperties(Session session, MuleMessage message, ImmutableEndpoint endpoint)
    {
        if (replyTo != null)
        {
            // if replyTo is set we'll probably want the correlationId set as
            // well
            message.setReplyTo(replyTo);
            message.setProperty(MuleProperties.MULE_REPLY_TO_REQUESTOR_PROPERTY, session.getComponent().getName());
            if (logger.isDebugEnabled())
            {
                logger.debug("Setting replyTo=" + replyTo + " for outbound endpoint: "
                             + endpoint.getEndpointURI());
            }
        }
        if (enableCorrelation != ENABLE_CORRELATION_NEVER)
        {
            boolean correlationSet = message.getCorrelationId() != null;
            if (correlationSet && (enableCorrelation == ENABLE_CORRELATION_IF_NOT_SET))
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("CorrelationId is already set to '" + message.getCorrelationId()
                                 + "' , not setting it again");
                }
                return;
            }
            else if (correlationSet)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("CorrelationId is already set to '" + message.getCorrelationId()
                                 + "', but router is configured to overwrite it");
                }
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("No CorrelationId is set on the message, will set a new Id");
                }
            }

            String correlation;
            Object o = propertyExtractor.getProperty(MuleProperties.MULE_CORRELATION_ID_PROPERTY, message);
            if (logger.isDebugEnabled())
            {
                logger.debug("Extracted correlation Id as: " + o);
            }
            correlation = o.toString();

            if (logger.isDebugEnabled())
            {
                StringBuffer buf = new StringBuffer();
                buf.append("Setting Correlation info on Outbound router for endpoint: ").append(
                    endpoint.getEndpointURI());
                buf.append(SystemUtils.LINE_SEPARATOR).append("Id=").append(correlation);
                // buf.append(", ").append("Seq=").append(seq);
                // buf.append(", ").append("Group Size=").append(group);
                logger.debug(buf.toString());
            }
            message.setCorrelationId(correlation);
            // message.setCorrelationGroupSize(group);
            // message.setCorrelationSequence(seq);
        }
    }

    public List getEndpoints()
    {
        return endpoints;
    }

    public void setEndpoints(List endpoints)
    {
        this.endpoints.clear();
        // this.endpoints = new CopyOnWriteArrayList(endpoints);
        for (Iterator iterator = endpoints.iterator(); iterator.hasNext();)
        {
            ImmutableEndpoint umoEndpoint = (ImmutableEndpoint) iterator.next();
            if (!umoEndpoint.canSend())
            {
                throw new InvalidEndpointTypeException(CoreMessages.outboundRouterMustUseOutboudEndpoints(
                    this, umoEndpoint));
            }
            addEndpoint(umoEndpoint);
        }
    }

    public void addEndpoint(ImmutableEndpoint endpoint)
    {
        if (!endpoint.canSend())
        {
            throw new InvalidEndpointTypeException(CoreMessages.outboundRouterMustUseOutboudEndpoints(
                this, endpoint));
        }
        endpoints.add(endpoint);
    }

    public boolean removeEndpoint(ImmutableEndpoint endpoint)
    {
        return endpoints.remove(endpoint);
    }

    public String getReplyTo()
    {
        return replyTo;
    }

    public void setReplyTo(String replyTo)
    {
        this.replyTo = replyTo;
    }

    public int getEnableCorrelation()
    {
        return enableCorrelation;
    }

    public void setEnableCorrelation(int enableCorrelation)
    {
        this.enableCorrelation = enableCorrelation;
    }

    public void setEnableCorrelationAsString(String enableCorrelation)
    {
        if (enableCorrelation != null)
        {
            if (enableCorrelation.equals("ALWAYS"))
            {
                this.enableCorrelation = ENABLE_CORRELATION_ALWAYS;
            }
            else if (enableCorrelation.equals("NEVER"))
            {
                this.enableCorrelation = ENABLE_CORRELATION_NEVER;
            }
            else if (enableCorrelation.equals("IF_NOT_SET"))
            {
                this.enableCorrelation = ENABLE_CORRELATION_IF_NOT_SET;
            }
            else
            {
                throw new IllegalArgumentException("Value for enableCorrelation not recognised: "
                                                   + enableCorrelation);
            }
        }
    }

    public PropertyExtractor getPropertyExtractor()
    {
        return propertyExtractor;
    }

    public void setPropertyExtractor(PropertyExtractor propertyExtractor)
    {
        this.propertyExtractor = propertyExtractor;
    }

    public void setPropertyExtractorAsString(String className)
    {
        try
        {
            this.propertyExtractor = (PropertyExtractor) ClassUtils.instanciateClass(className, null,
                getClass());
        }
        catch (Exception ex)
        {
            throw (IllegalArgumentException) new IllegalArgumentException(
                "Couldn't instanciate property extractor class " + className).initCause(ex);
        }
    }

    public TransactionConfig getTransactionConfig()
    {
        return transactionConfig;
    }

    public void setTransactionConfig(TransactionConfig transactionConfig)
    {
        this.transactionConfig = transactionConfig;
    }

    public boolean isDynamicEndpoints()
    {
        return false;
    }

    /**
     * @param name the Endpoint identifier
     * @return the Endpoint or null if the endpointUri is not registered
     * @see org.mule.api.routing.InboundRouterCollection
     */
    public ImmutableEndpoint getEndpoint(String name)
    {
        ImmutableEndpoint endpointDescriptor;
        for (Iterator iterator = endpoints.iterator(); iterator.hasNext();)
        {
            endpointDescriptor = (ImmutableEndpoint)iterator.next();
            if (endpointDescriptor.getName().equals(name))
            {
                return endpointDescriptor;
            }
        }
        return null;
    }
}
