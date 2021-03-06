/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.soap.xfire.transport;

import org.mule.providers.soap.xfire.MuleInvoker;
import org.mule.umo.manager.UMOWorkManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.service.Binding;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.soap.Soap11;
import org.codehaus.xfire.soap.Soap12;
import org.codehaus.xfire.soap.SoapTransport;
import org.codehaus.xfire.soap.SoapTransportHelper;
import org.codehaus.xfire.soap.SoapVersion;
import org.codehaus.xfire.transport.AbstractTransport;
import org.codehaus.xfire.transport.Channel;
import org.codehaus.xfire.transport.DefaultEndpoint;
import org.codehaus.xfire.transport.MapSession;
import org.codehaus.xfire.transport.Session;
import org.codehaus.xfire.wsdl11.WSDL11Transport;

/**
 * TODO document
 */
public class MuleLocalTransport extends AbstractTransport implements SoapTransport, WSDL11Transport
{
	
    public static final String SOAP11_HTTP_BINDING = "http://schemas.xmlsoap.org/soap/http";
    public static final String SOAP12_HTTP_BINDING = "http://www.w3.org/2003/05/soap/bindings/HTTP/";
    public static final String BINDING_ID = "urn:xfire:transport:local";
    public static final String URI_PREFIX = "xfire.local://";

    /**
     * logger used by this class
     */
    protected transient Log logger = LogFactory.getLog(getClass());

    private Session session;
    private boolean maintainSession;
    protected final UMOWorkManager workManager;

    public MuleLocalTransport(UMOWorkManager workManager)
    {
        super();
        SoapTransportHelper.createSoapTransport(this);
        this.workManager = workManager;
    }

    public String getServiceURL(Service service)
    {
        String ep = ((MuleInvoker) service.getInvoker()).getEndpoint().getEndpointURI().getAddress();
        return ep + "/" + service.getSimpleName();
    }

    protected Channel createNewChannel(String uri)
    {
        logger.debug("Creating new channel for uri: " + uri);

        MuleLocalChannel c = new MuleLocalChannel(uri, this, session);
        c.setWorkManager(workManager);
        c.setEndpoint(new DefaultEndpoint());

        return c;
    }

    public void setMaintainSession(boolean maintainSession)
    {
        this.maintainSession = maintainSession;
        resetSession();
    }

    public void resetSession()
    {
        if (maintainSession)
        {
            session = new MapSession();
        }
        else
        {
            session = null;
        }
    }

    protected String getUriPrefix()
    {
        return URI_PREFIX;
    }

    public String[] getSupportedBindings()
    {
        return new String[]{BINDING_ID};
    }

    public String[] getKnownUriSchemes()
    {
        return new String[]{URI_PREFIX};
    }

    public String getName()
    {
        return "Local";
    }

    public String[] getSoapTransportIds()
    {
        return new String[]{BINDING_ID};
    }
    
    public Binding findBinding(MessageContext context, Service service)
    {
        SoapVersion version = context.getCurrentMessage().getSoapVersion();

        if (version instanceof Soap11)
        {
            return service.getBinding(SOAP11_HTTP_BINDING);
        }
        else if (version instanceof Soap12)
        {
            return service.getBinding(SOAP12_HTTP_BINDING);
        }

        return super.findBinding(context, service);
    }
}
