/*
 * $Header$
 * $Revision$
 * $Date$
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) Cubis Limited. All rights reserved.
 * http://www.cubis.co.uk
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.providers.soap.glue;

import electric.glue.context.ApplicationContext;
import electric.glue.context.ServiceContext;
import electric.registry.Registry;
import electric.registry.RegistryException;
import electric.server.http.HTTP;
import electric.service.virtual.VirtualService;
import electric.util.Context;
import electric.util.interceptor.ReceiveThreadContext;
import electric.util.interceptor.SendThreadContext;
import org.mule.umo.lifecycle.DisposeException;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.config.MuleProperties;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleDescriptor;
import org.mule.impl.ImmutableMuleEndpoint;
import org.mule.providers.AbstractMessageReceiver;
import org.mule.providers.soap.ServiceProxy;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.provider.UMOConnector;

import java.util.Iterator;
import java.util.Map;

/**
 * <code>GlueMessageReceiver</code> is used to recieve Glue bounded services
 * for Mule compoennts.
 *
 * services are bound in the Glue Registry using the Virtualservice implementation
 *
 * @author <a href="mailto:ross.mason@cubis.co.uk">Ross Mason</a>
 * @version $Revision$
 */

public class GlueMessageReceiver extends AbstractMessageReceiver
{
    public GlueMessageReceiver(UMOConnector connector, UMOComponent component,
                               UMOEndpoint endpoint, boolean createServer) throws InitialisationException
    {
        super.create(connector, component, endpoint);
        try
        {
            Class[] interfaces = ServiceProxy.getInterfacesForComponent(component);
            if(interfaces.length==0) {
                throw new InitialisationException(new Message("soap", 2, component.getDescriptor().getName()), this);
            }
            VirtualService.enable();
            //this is always initialisaed as synchronous as ws invocations should
            //always execute in a single thread unless the endpont has explicitly
            //been set to run asynchronously
            if(endpoint instanceof ImmutableMuleEndpoint  &&
                    !((ImmutableMuleEndpoint)endpoint).isSynchronousExplicitlySet()) {
                if(!endpoint.isSynchronous()) {
                    logger.debug("overriding endpoint synchronicity and setting it to true. Web service requests are executed in a single thread");
                    endpoint.setSynchronous(true);
                }
            }
            VirtualService vService = new VirtualService(interfaces, ServiceProxy.createGlueServiceHandler(this,  endpoint.isSynchronous()));

            if(createServer) {
                registerContextHeaders();
                HTTP.startup(getEndpointURI().getScheme() + "://" + getEndpointURI().getHost() + ":" + getEndpointURI().getPort());
            }
            //Add initialisation callback for the Glue service
            //The callback will actually register the service
            MuleDescriptor desc =(MuleDescriptor)component.getDescriptor();
            String serviceName = getEndpointURI().getPath();
            if(!serviceName.endsWith("/")) {
                serviceName += "/";
            }
            serviceName += component.getDescriptor().getName();
            desc.addInitialisationCallback(new GlueInitialisationCallback(vService, serviceName, new ServiceContext()));

        } catch (ClassNotFoundException e)
        {
            throw new InitialisationException(new Message(Messages.CLASS_X_NOT_FOUND, e.getMessage()), e, this);
        }  catch (UMOException e)
        {
            throw new InitialisationException(new Message("soap", 3, component.getDescriptor().getName()), e, this);
        }catch (Exception e)
        {
            throw new InitialisationException(new Message(Messages.FAILED_TO_START_X, "Soap Server"), e, this);
        }
    }

    protected void registerContextHeaders()
    {
        ApplicationContext.addOutboundSoapRequestInterceptor(new SendThreadContext(MuleProperties.MULE_CORRELATION_ID_PROPERTY));
        ApplicationContext.addOutboundSoapRequestInterceptor(new SendThreadContext(MuleProperties.MULE_CORRELATION_SEQUENCE_PROPERTY));
        ApplicationContext.addOutboundSoapRequestInterceptor(new SendThreadContext(MuleProperties.MULE_CORRELATION_GROUP_SIZE_PROPERTY));
        ApplicationContext.addOutboundSoapRequestInterceptor(new SendThreadContext(MuleProperties.MULE_REPLY_TO_PROPERTY, true));

        ApplicationContext.addInboundSoapRequestInterceptor( new ReceiveThreadContext( MuleProperties.MULE_CORRELATION_ID_PROPERTY ) );
        ApplicationContext.addInboundSoapRequestInterceptor( new ReceiveThreadContext( MuleProperties.MULE_CORRELATION_SEQUENCE_PROPERTY ) );
        ApplicationContext.addInboundSoapRequestInterceptor( new ReceiveThreadContext( MuleProperties.MULE_CORRELATION_GROUP_SIZE_PROPERTY ) );
        ApplicationContext.addInboundSoapRequestInterceptor( new ReceiveThreadContext( MuleProperties.MULE_REPLY_TO_PROPERTY, true ) );
    }


    /**
     * Template method to dispose any resources associated with this receiver.  There
     * is not need to dispose the connector as this is already done by the framework
     *
     * @throws org.mule.umo.UMOException
     */
    protected void doDispose() throws UMOException
    {
        try
        {
            Registry.unpublish(component.getDescriptor().getName());
        } catch (RegistryException e)
        {
            throw new DisposeException(new Message(Messages.FAILED_TO_UNREGISTER_X_ON_ENDPOINT_X, component.getDescriptor().getName(), endpoint.getEndpointURI()), e);
        }
    }

    protected Context getContext() {
        Context c = null;
        if(endpoint.getProperties() != null) {
            c = (Context)endpoint.getProperties().get("glueContext");
            if(c==null && endpoint.getProperties().size() > 0) {
                c = new Context();
                for (Iterator iterator = endpoint.getProperties().entrySet().iterator(); iterator.hasNext();)
                {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    c.addProperty(entry.getKey().toString(), entry.getValue());
                }
            }
        }
        return c;
    }
}
