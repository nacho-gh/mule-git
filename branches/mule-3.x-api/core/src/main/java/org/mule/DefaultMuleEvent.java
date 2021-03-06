/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule;

import org.mule.api.DefaultMuleException;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.MuleSession;
import org.mule.api.ThreadSafeAccess;
import org.mule.api.config.MuleProperties;
import org.mule.api.endpoint.EndpointBuilder;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.registry.ServiceType;
import org.mule.api.security.Credentials;
import org.mule.api.service.Service;
import org.mule.api.transformer.DataType;
import org.mule.api.transformer.Transformer;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.config.i18n.CoreMessages;
import org.mule.endpoint.DefaultEndpointFactory;
import org.mule.endpoint.MuleEndpointURI;
import org.mule.security.MuleCredentials;
import org.mule.transformer.types.DataTypeFactory;
import org.mule.transport.service.TransportServiceDescriptor;
import org.mule.util.UUID;
import org.mule.util.store.DeserializationPostInitialisable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>DefaultMuleEvent</code> represents any data event occuring in the Mule
 * environment. All data sent or received within the Mule environment will be passed
 * between components as an MuleEvent. <p/> The MuleEvent holds some data and provides
 * helper methods for obtaining the data in a format that the receiving Mule component
 * understands. The event can also maintain any number of properties that can be set
 * and retrieved by Mule components.
 */

public class DefaultMuleEvent extends EventObject implements MuleEvent, ThreadSafeAccess, DeserializationPostInitialisable
{
    /**
     * Serial version
     */
    private static final long serialVersionUID = 1L;

    /**
     * logger used by this class
     */
    protected transient Log logger = LogFactory.getLog(getClass());

    /**
     * The endpoint associated with the event
     */
    private transient ImmutableEndpoint endpoint = null;

    /**
     * the Universally Unique ID for the event
     */
    private String id = null;

    /**
     * The payload message used to read the payload of the event
     */
    private MuleMessage message = null;

    private MuleSession session;

    private boolean stopFurtherProcessing = false;

    private boolean synchronous = false;

    private int timeout = TIMEOUT_NOT_SET_VALUE;

    private transient ResponseOutputStream outputStream = null;

    private transient Object transformedMessage = null;

    private Credentials credentials = null;

    protected String[] ignoredPropertyOverrides = new String[]{MuleProperties.MULE_METHOD_PROPERTY};

    private transient Map<String, Object> serializedData = null;

    /**
     * Properties cache that only reads properties once from the inbound message and
     * merges them with any properties on the endpoint. The message properties take
     * precedence over the endpoint properties
     */
    public DefaultMuleEvent(MuleMessage message,
                            ImmutableEndpoint endpoint,
                            Service service,
                            MuleEvent previousEvent)
    {
        super(message.getPayload());
        this.message = message;
        this.id = generateEventId();
        this.session = previousEvent.getSession();
        ((DefaultMuleSession) session).setService(service);
        this.endpoint = endpoint;
        this.synchronous = previousEvent.isSynchronous();
        this.timeout = previousEvent.getTimeout();
        this.outputStream = (ResponseOutputStream) previousEvent.getOutputStream();
        fillProperties(previousEvent);
    }

    public DefaultMuleEvent(MuleMessage message,
                            ImmutableEndpoint endpoint,
                            MuleSession session,
                            boolean synchronous)
    {
        this(message, endpoint, session, synchronous, null);
    }

    /**
     * Contructor.
     *
     * @param message  the event payload
     * @param endpoint the endpoint to associate with the event
     * @param session  the previous event if any
     * @see org.mule.api.transport.MessageAdapter
     */
    public DefaultMuleEvent(MuleMessage message,
                            ImmutableEndpoint endpoint,
                            MuleSession session,
                            boolean synchronous,
                            ResponseOutputStream outputStream)
    {
        super(message.getPayload());
        this.message = message;
        this.endpoint = endpoint;
        this.session = session;
        this.id = generateEventId();
        this.synchronous = synchronous;
        this.outputStream = outputStream;
        fillProperties(null);
    }

    /**
     * Contructor.
     *
     * @param message  the event payload
     * @param endpoint the endpoint to associate with the event
     * @param session  the previous event if any
     * @see org.mule.api.transport.MessageAdapter
     */
    public DefaultMuleEvent(MuleMessage message,
                            ImmutableEndpoint endpoint,
                            MuleSession session,
                            String eventId,
                            boolean synchronous)
    {
        super(message.getPayload());
        this.message = message;
        this.endpoint = endpoint;
        this.session = session;
        this.id = eventId;
        this.synchronous = synchronous;
        fillProperties(null);
    }

    /**
     * A helper constructor used to rewrite an event payload
     *
     * @param message
     * @param rewriteEvent
     */
    public DefaultMuleEvent(MuleMessage message, MuleEvent rewriteEvent)
    {
        super(message.getPayload());
        this.message = message;
        this.id = rewriteEvent.getId();
        this.session = rewriteEvent.getSession();
        ((DefaultMuleSession) session).setService(rewriteEvent.getService());
        this.endpoint = rewriteEvent.getEndpoint();
        this.synchronous = rewriteEvent.isSynchronous();
        this.timeout = rewriteEvent.getTimeout();
        this.outputStream = (ResponseOutputStream) rewriteEvent.getOutputStream();
        if (rewriteEvent instanceof DefaultMuleEvent)
        {
            this.transformedMessage = ((DefaultMuleEvent) rewriteEvent).getCachedMessage();
        }
        fillProperties(rewriteEvent);
    }

    protected void fillProperties(MuleEvent previousEvent)
    {
//        if (previousEvent != null)
//        {
//            MuleMessage msg = previousEvent.getMessage();
//            synchronized (msg)
//            {
//                for (Iterator iterator = msg.getPropertyNames().iterator(); iterator.hasNext();)
//                {
//                    String prop = (String) iterator.next();
//                    Object value = msg.getProperty(prop);
//                    // don't overwrite property on the message
//                    if (!ignoreProperty(prop))
//                    {
//                        message.setProperty(prop, value);
//                    }
//                }
//            }
//        }

        if (endpoint != null && endpoint.getProperties() != null)
        {
//            PropertyScope scope = endpoint instanceof OutboundEndpoint ? PropertyScope.OUTBOUND : PropertyScope.INVOCATION;

            for (Iterator iterator = endpoint.getProperties().keySet().iterator(); iterator.hasNext();)
            {
                String prop = (String) iterator.next();
                Object value = endpoint.getProperties().get(prop);
                // don't overwrite property on the message
                if (!ignoreProperty(prop))
                {
                    //inbound endpoint properties are in the invocation scope
                    message.setProperty(prop, value, PropertyScope.INVOCATION);
                }
            }
        }
        //TODO MULE-3999. Should session properties be copied to the message?
//        if (session != null)
//        {
//            for (Iterator iterator = session.getPropertyNames(); iterator.hasNext();)
//            {
//                String prop = (String) iterator.next();
//                message.setProperty(prop, session.getProperty(prop), PropertyScope.SESSION);
//            }
//        }

        setCredentials();
    }

    /**
     * This method is used to determine if a property on the previous event should be
     * ignorred for the next event. This method is here because we don't have proper
     * scoped handlng of meta data yet The rules are
     * <ol>
     * <li>If a property is already set on the currect event don't overwrite with the previous event value
     * <li>If the propery name appears in the ignorredPropertyOverrides list, then we always set it on the new event
     * </ol>
     *
     * @param key
     */
    protected boolean ignoreProperty(String key)
    {
        if (key == null)
        {
            return true;
        }

        for (int i = 0; i < ignoredPropertyOverrides.length; i++)
        {
            if (key.equals(ignoredPropertyOverrides[i]))
            {
                return false;
            }
        }

        return null != message.getProperty(key);
    }

    protected void setCredentials()
    {
        if (null != endpoint && null != endpoint.getEndpointURI() && null != endpoint.getEndpointURI().getUserInfo())
        {
            final String userName = endpoint.getEndpointURI().getUser();
            final String password = endpoint.getEndpointURI().getPassword();
            if (password != null && userName != null)
            {
                credentials = new MuleCredentials(userName, password.toCharArray());
            }
        }
    }

    public Credentials getCredentials()
    {
        return (credentials != null ? credentials : (MuleCredentials) message.getProperty(MuleProperties.MULE_CREDENTIALS_PROPERTY));
    }

    Object getCachedMessage()
    {
        return transformedMessage;
    }

    public MuleMessage getMessage()
    {
        return message;
    }

    public byte[] getMessageAsBytes() throws DefaultMuleException
    {
        try
        {
            return message.getPayloadAsBytes();
        }
        catch (Exception e)
        {
            throw new DefaultMuleException(
                    CoreMessages.cannotReadPayloadAsBytes(message.getPayload().getClass().getName()), e);
        }
    }

    public Object transformMessage() throws TransformerException
    {
        message.applyTransformers(endpoint.getTransformers());
        return message.getPayload();
    }

    public <T> T transformMessage(Class<T> outputType) throws TransformerException
    {
        return (T)transformMessage(new DataTypeFactory().create(outputType));
    }

    public <T> T transformMessage(DataType<T> outputType) throws TransformerException
    {
        if (outputType == null)
        {
            throw new TransformerException(CoreMessages.objectIsNull("outputType"));
        }
        message.applyTransformers(endpoint.getTransformers());
        return message.getPayload(outputType);
    }

    /**
     * This method will attempt to convert the transformed message into an array of
     * bytes It will first check if the result of the transformation is a byte array
     * and return that. Otherwise if the the result is a string it will serialized
     * the CONTENTS of the string not the String object. finally it will check if the
     * result is a Serializable object and convert that to an array of bytes.
     *
     * @return a byte[] representation of the message
     * @throws TransformerException if an unsupported encoding is being used or if
     *                              the result message is not a String byte[] or Seializable object
     */
    public byte[] transformMessageToBytes() throws TransformerException
    {
        return (byte[]) transformMessage(byte[].class);
    }

    /**
     * Returns the message transformed into it's recognised or expected format and
     * then into a String. The transformer used is the one configured on the endpoint
     * through which this event was received.
     *
     * @return the message transformed into it's recognised or expected format as a
     *         Strings.
     * @throws org.mule.api.transformer.TransformerException
     *          if a failure occurs in
     *          the transformer
     * @see org.mule.api.transformer.Transformer
     */
    public String transformMessageToString() throws TransformerException
    {
        try
        {
            return new String(transformMessageToBytes(), getEncoding());
        }
        catch (UnsupportedEncodingException e)
        {
            throw new TransformerException(endpoint.getTransformers(), e);
        }

        /* TODO MULE-2691 Note that the above code actually transforms the message 
         * to byte[] instead of String.  The following code would transform the 
         * message to a String but breaks some tests in transports/http:
         * 
        transformMessageToBytes();
        
        ByteArrayToObject t = new ByteArrayToObject();
        t.setEncoding(getEncoding());
        List list = new ArrayList();
        list.add(t);
        message.applyTransformers(list);

        return (String) message.getPayload();
        */
    }

    public String getMessageAsString() throws MuleException
    {
        return getMessageAsString(getEncoding());
    }

    /**
     * Returns the message contents as a string
     *
     * @param encoding the encoding to use when converting the message to string
     * @return the message contents as a string
     * @throws org.mule.api.MuleException if the message cannot be converted into a
     *                                    string
     */
    public String getMessageAsString(String encoding) throws MuleException
    {
        try
        {
            return message.getPayloadAsString(encoding);
        }
        catch (Exception e)
        {
            throw new DefaultMuleException(
                    CoreMessages.cannotReadPayloadAsString(message.getClass().getName()), e);
        }
    }

    public String getId()
    {
        return id;
    }

    /**
     * @see org.mule.api.MuleEvent#getProperty(java.lang.String)
     */
    public Object getProperty(String name)
    {
        return getProperty(name, /* defaultValue */null);
    }

    public Object getProperty(String name, Object defaultValue)
    {
        Object property = message.getProperty(name);

        if (property == null)
        {
            property = session.getProperty(name);
        }

        return (property == null ? defaultValue : property);
    }

    public ImmutableEndpoint getEndpoint()
    {
        return endpoint;
    }

    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer(64);
        buf.append("MuleEvent: ").append(getId());
        buf.append(", sync=").append(isSynchronous());
        buf.append(", stop processing=").append(isStopFurtherProcessing());
        buf.append(", ").append(endpoint);

        return buf.toString();
    }

    protected String generateEventId()
    {
        return UUID.getUUID();
    }

    public MuleSession getSession()
    {
        return session;
    }

    void setSession(MuleSession session)
    {
        this.session = session;
    }

    /**
     * Gets the recipient service of this event
     */
    public Service getService()
    {
        return session.getService();
    }

    /**
     * Determines whether the default processing for this event will be executed
     *
     * @return Returns the stopFurtherProcessing.
     */
    public boolean isStopFurtherProcessing()
    {
        return stopFurtherProcessing;
    }

    /**
     * Setting this parameter will stop the Mule framework from processing this event
     * in the standard way. This allow for client code to override default behaviour.
     * The common reasons for doing this are - 1. The service has more than one send
     * endpoint configured; the service must dispatch to other prviders
     * programatically by using the service on the current event 2. The service doesn't
     * send the current event out through a endpoint. i.e. the processing of the
     * event stops in the uMO.
     *
     * @param stopFurtherProcessing The stopFurtherProcessing to set.
     */
    public void setStopFurtherProcessing(boolean stopFurtherProcessing)
    {
        this.stopFurtherProcessing = stopFurtherProcessing;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof DefaultMuleEvent))
        {
            return false;
        }

        final DefaultMuleEvent event = (DefaultMuleEvent) o;

        if (message != null ? !message.equals(event.message) : event.message != null)
        {
            return false;
        }
        return id.equals(event.id);
    }

    @Override
    public int hashCode()
    {
        return 29 * id.hashCode() + (message != null ? message.hashCode() : 0);
    }

    public boolean isSynchronous()
    {
        return synchronous;
    }

    public void setSynchronous(boolean value)
    {
        synchronous = value;
    }

    public int getTimeout()
    {
        if (timeout == TIMEOUT_NOT_SET_VALUE)
        {
            // If this is not set it will use the default timeout value
            timeout = endpoint.getResponseTimeout();
        }
        return timeout;
    }

    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }

    /**
     * An outputstream can optionally be used to write response data to an incoming
     * message.
     *
     * @return an output strem if one has been made available by the message receiver
     *         that received the message
     */
    public OutputStream getOutputStream()
    {
        return outputStream;
    }

    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();
        out.writeInt(endpoint.hashCode());
        out.writeBoolean(endpoint instanceof InboundEndpoint);
        out.writeObject(endpoint.getEndpointBuilderName());
        
        // make sure to write out the connector's name along with the endpoint URI. Omitting the
        // connector will fail rebuilding the endpoint when this event is read back in and there
        // is more than one connector for the protocol.
        String uri = endpoint.getEndpointURI().getUri().toString();
        String connectorName = endpoint.getConnector().getName();
        out.writeObject(uri + "?connector=" + connectorName);

        // write number of Transformers
        out.writeInt(endpoint.getTransformers().size());

        // write transformer names if necessary
        if (endpoint.getTransformers().size() > 0)
        {
            for (Transformer transformer : endpoint.getTransformers())
            {
                out.writeObject(transformer.getName());
            }
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException, MuleException
    {
        logger = LogFactory.getLog(getClass());

        in.defaultReadObject();
        serializedData = new HashMap<String, Object>();
        serializedData.put("endpointHashcode", in.readInt());
        serializedData.put("isInboundEndpoint", in.readBoolean());
        serializedData.put("endpointBuilderName", in.readObject());
        serializedData.put("endpointUri", in.readObject());
        int count = in.readInt();

        List<String> transformerNames = new LinkedList<String>();
        if (count > 0)
        {
            while (--count > 0)
            {
                transformerNames.add((String) in.readObject());
            }
        }
        serializedData.put("transformers", transformerNames);
    }

    /**
     * Invoked after deserialization. This is called when the marker interface
     * {@link org.mule.util.store.DeserializationPostInitialisable} is used. This will get invoked
     * after the object has been deserialized passing in the current MuleContext when using either
     * {@link org.mule.transformer.wire.SerializationWireFormat},
     * {@link org.mule.transformer.wire.SerializedMuleMessageWireFormat} or the
     * {@link org.mule.transformer.simple.ByteArrayToSerializable} transformer.
     *
     * @param muleContext the current muleContext instance
     * @throws MuleException if there is an error initializing
     */
    @SuppressWarnings({"unused", "unchecked"})
    private void initAfterDeserialisation(MuleContext muleContext) throws MuleException
    {
        if (session instanceof DefaultMuleSession)
        {
            ((DefaultMuleSession) session).initAfterDeserialisation(muleContext);
        }
        if (message instanceof DefaultMuleMessage)
        {
            ((DefaultMuleMessage) message).initAfterDeserialisation(muleContext);
        }
        int endpointHashcode = (Integer) serializedData.get("endpointHashcode");
        boolean isInboundEndpoint = (Boolean) serializedData.get("isInboundEndpoint");
        String endpointBuilderName = (String) serializedData.get("endpointBuilderName");
        String endpointUri = (String) serializedData.get("endpointUri");
        List<String> transformerNames = (List<String>) serializedData.get("transformers");

        // 1) First attempt to get same endpoint instance from registry using
        // hashcode, this will work if registry hasn't been disposed.
        endpoint = (ImmutableEndpoint) muleContext.getRegistry().lookupObject(
                DefaultEndpointFactory.ENDPOINT_REGISTRY_PREFIX + endpointHashcode);

        // Registry has been disposed so we need to recreate endpoint
        if (endpoint == null)
        {
            // 2) If endpoint references it's builder and this is available then use
            // the builder to recreate the endpoint
            if ((endpointBuilderName != null)
                    && muleContext.getRegistry().lookupEndpointBuilder(endpointBuilderName) != null)
            {
                if (isInboundEndpoint)
                {
                    endpoint = muleContext.getRegistry().lookupEndpointFactory().getInboundEndpoint(
                            endpointBuilderName);
                }
                else
                {
                    endpoint = muleContext.getRegistry().lookupEndpointFactory().getOutboundEndpoint(
                            endpointBuilderName);
                }
            }
            // 3) Otherwise recreate using endpoint uri string and transformers. (As in 1.4)
            else
            {
                List<Transformer> transformers = new LinkedList<Transformer>();
                for (String name : transformerNames)
                {
                    Transformer next = muleContext.getRegistry().lookupTransformer(name);
                    if (next == null)
                    {
                        throw new IllegalStateException(CoreMessages.objectNotFound(name).toString());
                    }
                    else
                    {
                        transformers.add(next);
                    }
                }
                EndpointURI uri = new MuleEndpointURI(endpointUri, muleContext);

                TransportServiceDescriptor tsd = (TransportServiceDescriptor) muleContext.getRegistry().lookupServiceDescriptor(ServiceType.TRANSPORT, uri.getFullScheme(), null);
                EndpointBuilder endpointBuilder = tsd.createEndpointBuilder(endpointUri);
                endpointBuilder.setTransformers(transformers);

                if (isInboundEndpoint)
                {
                    endpoint = muleContext.getRegistry().lookupEndpointFactory().getInboundEndpoint(
                            endpointBuilder);
                }
                else
                {
                    endpoint = muleContext.getRegistry().lookupEndpointFactory().getOutboundEndpoint(
                            endpointBuilder);
                }
            }
        }

        serializedData = null;
    }

    /**
     * Gets the encoding for this message. First it looks to see if encoding has been
     * set on the endpoint, if not it will check the message itself and finally it
     * will fall back to the Mule global configuration for encoding which cannot be
     * null.
     *
     * @return the encoding for the event
     */
    public String getEncoding()
    {
        String encoding = message.getEncoding();
        if (encoding == null)
        {
            encoding = endpoint.getEncoding();
        }

        return encoding;
    }

    public MuleContext getMuleContext()
    {
        return endpoint.getMuleContext();
    }

    public ThreadSafeAccess newThreadCopy()
    {
        if (message instanceof ThreadSafeAccess)
        {
            DefaultMuleEvent copy = new DefaultMuleEvent((MuleMessage) ((ThreadSafeAccess) message).newThreadCopy(), this);
            copy.resetAccessControl();
            return copy;
        }
        else
        {
            return this;
        }
    }

    public void resetAccessControl()
    {
        if (message instanceof ThreadSafeAccess)
        {
            ((ThreadSafeAccess) message).resetAccessControl();
        }
    }

    public void assertAccess(boolean write)
    {
        if (message instanceof ThreadSafeAccess)
        {
            ((ThreadSafeAccess) message).assertAccess(write);
        }
    }

}
