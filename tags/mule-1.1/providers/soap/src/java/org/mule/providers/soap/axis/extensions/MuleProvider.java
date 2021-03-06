/*
 * $Header$
 * $Revision$
 * $Date$
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.providers.soap.axis.extensions;

import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.providers.java.RPCProvider;
import org.mule.MuleManager;
import org.mule.impl.RequestContext;
import org.mule.providers.soap.ServiceProxy;
import org.mule.providers.soap.axis.AxisConnector;
import org.mule.providers.soap.axis.AxisMessageReceiver;
import org.mule.umo.UMOSession;

import java.lang.reflect.Proxy;

/**
 * <code>MuleProvider</code> Is an Axis service endpoint that builds services
 * from Mule managed components
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public class MuleProvider extends RPCProvider
{
    private AxisConnector connector;

    public MuleProvider(AxisConnector connector)
    {
        this.connector = connector;
    }

    protected Object makeNewServiceObject(MessageContext messageContext, String s) throws Exception
    {
        AxisMessageReceiver receiver = (AxisMessageReceiver)connector.getReceiver(messageContext.getTargetService());
        if (receiver == null) {
            throw new AxisFault("Could not find Mule registered service: " + s);
        }
        Class[] classes = ServiceProxy.getInterfacesForComponent(receiver.getComponent());
        return ServiceProxy.createAxisProxy(receiver, true, classes);
    }

    protected Class getServiceClass(String s, SOAPService soapService, MessageContext messageContext) throws AxisFault
    {
        UMOSession session = MuleManager.getInstance().getModel().getComponentSession(soapService.getName());
        try {
            Class[] classes = ServiceProxy.getInterfacesForComponent(session.getComponent());
            return Proxy.getProxyClass(Thread.currentThread().getContextClassLoader(), classes);
        } catch (Exception e) {
            throw new AxisFault("Failed to implementation class for component: " + e.getMessage(), e);
        }
    }

    public void invoke(MessageContext msgContext) throws AxisFault
    {
        super.invoke(msgContext);
        if (RequestContext.getExceptionPayload() != null) {
            Throwable t = RequestContext.getExceptionPayload().getException();
            if (t instanceof Exception) {
                AxisFault fault = AxisFault.makeFault((Exception) t);
                if (t instanceof RuntimeException)
                    fault.addFaultDetail(Constants.QNAME_FAULTDETAIL_RUNTIMEEXCEPTION, "true");
                throw fault;
            } else {
                throw (Error) t;
            }
        }
    }

}
