/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.jms.transformers;

import org.mule.config.MuleProperties;
import org.mule.config.i18n.Messages;
import org.mule.impl.RequestContext;
import org.mule.impl.internal.notifications.ConnectionNotification;
import org.mule.impl.internal.notifications.ConnectionNotificationListener;
import org.mule.providers.jms.JmsMessageUtils;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.UMOEventContext;
import org.mule.umo.UMOMessage;
import org.mule.umo.manager.UMOServerNotification;
import org.mule.umo.provider.UMOMessageDispatcher;
import org.mule.umo.transformer.TransformerException;
import org.mule.util.compression.CompressionHelper;
import org.mule.util.compression.CompressionStrategy;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.Iterator;

/**
 * <code>AbstractJmsTransformer</code> is an abstract class that should be used
 * for all transformers where a JMS message will be the transformed or
 * transformee object. It provides services for compressing and uncompressing
 * messages.
 *
 * @author Ross Mason
 * @version 1.2
 */

public abstract class AbstractJmsTransformer extends AbstractTransformer implements ConnectionNotificationListener
{
    public static final char REPLACEMENT_CHAR = '_';

    protected boolean requireNewSession = true;

    private Session session = null;

    public AbstractJmsTransformer()
    {
        super();
    }

    /**
     * Transforms the object.
     *
     * @param src The source object to transform.
     * @param session
     * @return The transformed object as an XMLMessage
     */
    // TODO This method never seems to get called by anyone...
    public Object transform(Object src, Session session) throws TransformerException
    {
        if (session == null && this.session == null) {
            throw new TransformerException(new org.mule.config.i18n.Message("jms", 1), this);
        }
        if (session != null) {
            this.session = session;
            requireNewSession=false;
        }
        if (src == null) {
            throw new TransformerException(new org.mule.config.i18n.Message(Messages.TRANSFORM_FAILED_FROM_X_TO_X,
                                                                            "null",
                                                                            "Object"), this);
        }
        Object ret = transform(src);
        if (logger.isDebugEnabled()) {
            logger.debug("Transformed message from type: " + src.getClass().getName() + " to type: "
                    + ret.getClass().getName());
        }
        return ret;
    }

    /**
     * @param src The source data to compress
     * @return
     * @throws TransformerException
     */
    protected Message transformToMessage(Object src) throws TransformerException
    {
        try {
            // The session can be closed by the dispatcher closing so its more
            // reliable to get it from the dispatcher each time
            if (requireNewSession || getEndpoint() != null) {
                UMOMessageDispatcher dispatcher = getEndpoint().getConnector().getDispatcher(getEndpoint());
                session = (Session) dispatcher.getDelegateSession();
                requireNewSession = session==null;
            }

            Message msg = null;
            if (src instanceof Message) {
                msg = (Message) src;
                msg.clearProperties();
            } else {
                msg = JmsMessageUtils.getMessageForObject(src, session);
            }
            // set the event properties on the Message
            UMOEventContext ctx = RequestContext.getEventContext();
            if (ctx == null) {
                logger.warn("There is no current event context");
                return msg;
            }

            setJmsProperties(ctx.getMessage(), msg);

            return msg;
            // }
        } catch (Exception e) {
            throw new TransformerException(this, e);
        }
    }

    protected void setJmsProperties(UMOMessage umoMessage, Message msg) throws JMSException {
        Object value;
        String key;
        for (Iterator iterator = umoMessage.getPropertyNames().iterator(); iterator.hasNext();) {
            key = iterator.next().toString();
            if(!key.startsWith("JMS")) {
                value = umoMessage.getProperty(key);
                if (MuleProperties.MULE_CORRELATION_ID_PROPERTY.equals(key)) {
                    msg.setJMSCorrelationID(umoMessage.getCorrelationId());
                }
                //We dont want to set the ReplyTo property again as it will be set using JMSReplyTo
                if(!(MuleProperties.MULE_REPLY_TO_PROPERTY.equals(key) && value instanceof Destination)) {
                    try {
                        msg.setObjectProperty(encodeHeader(key), value);
                    } catch (JMSException e) {
                        //Various Jms servers have slightly different rules to what can be set as an object property on the message
                        //As such we have to take a hit n' hope approach
                        if(logger.isDebugEnabled()) {
                            logger.debug("Unable to set property '" + encodeHeader(key) + "' of type " +  value.getClass().getName() + "': " + e.getMessage());
                        }
                    }
                }
            }
        }
    }
    /**
     * Encode a string so that is is a valid java identifier
     *
     * @param name
     * @return A valid Jms header name
     */
    public static String encodeHeader(String name)
    {
        StringBuffer sb = new StringBuffer(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (i == 0) {
                if (!Character.isJavaIdentifierStart(c)) {
                    c = REPLACEMENT_CHAR;
                }
            } else {
                if (!Character.isJavaIdentifierPart(c)) {
                    c = REPLACEMENT_CHAR;
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    protected Object transformFromMessage(Message source) throws TransformerException
    {
        Object result = null;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Message type received is: " + source.getClass().getName());
            }
            if (source instanceof BytesMessage) {
                // If this bytes Message is not compressed it will throw a
                // NotGZipFormatException
                // It would be nice if we could check the custom JMS compression
                // property here. However
                // When jms bridging other, non-JMS-compliant message servers
                // occur, there is no guarantee that
                // Custom properties will be propagated
                byte[] bytes = JmsMessageUtils.getBytesFromMessage(source);
                CompressionStrategy strategy = CompressionHelper.getDefaultCompressionStrategy();
                if (strategy.isCompressed(bytes)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Message received is compressed");
                    }
                    result = strategy.uncompressByteArray(bytes);
                } else {
                    // If the message is not compressed, handle it the standard
                    // way
                    result = JmsMessageUtils.getObjectForMessage(source);
                }
            } else {
                result = JmsMessageUtils.getObjectForMessage(source);
            }
        } catch (Exception e) {
            throw new TransformerException(this, e);
        }
        return result;
    }

    public Session getSession()
    {
        return session;
    }

    public void setSession(Session session)
    {
        this.session = session;
    }

    public void onNotification(UMOServerNotification notification) {
        if(notification.getAction() == ConnectionNotification.CONNECTION_DISCONNECTED) {
            session = null;
            requireNewSession = true;
        }
    }
}
