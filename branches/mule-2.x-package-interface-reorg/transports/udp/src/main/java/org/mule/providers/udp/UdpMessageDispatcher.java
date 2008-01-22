/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.udp;

import org.mule.api.Event;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.impl.DefaultMuleMessage;
import org.mule.impl.transport.AbstractMessageDispatcher;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;

/**
 * <code>UdpMessageDispatcher</code> is responsible for dispatching MuleEvents as
 * UDP packets on the network
 */

public class UdpMessageDispatcher extends AbstractMessageDispatcher
{
    protected final UdpConnector connector;

    public UdpMessageDispatcher(ImmutableEndpoint endpoint)
    {
        super(endpoint);
        this.connector = (UdpConnector)endpoint.getConnector();
    }

    protected void doConnect() throws Exception
    {
        // Test the connection
        DatagramSocket socket = connector.getSocket(endpoint);
        connector.releaseSocket(socket, endpoint);
    }

    protected void doDisconnect() throws Exception
    {
        // nothing to do
    }


    protected synchronized void doDispatch(Event event) throws Exception
    {
        ImmutableEndpoint ep = event.getEndpoint();

        DatagramSocket socket = connector.getSocket(ep);
        try
        {
            byte[] payload = event.transformMessageToBytes();

            int port = ep.getEndpointURI().getPort();
            InetAddress inetAddress = null;
            //TODO, check how expensive this operation is
            if("null".equalsIgnoreCase(ep.getEndpointURI().getHost()))
            {
                inetAddress = InetAddress.getLocalHost();
            }
            else
            {
                inetAddress = InetAddress.getByName(ep.getEndpointURI().getHost());
            }

            write(socket, payload, port, inetAddress);
        }
        finally
        {
            connector.releaseSocket(socket, ep);
        }
    }

    protected void write(DatagramSocket socket, byte[] data, int port, InetAddress inetAddress) throws IOException
    {
        DatagramPacket packet = new DatagramPacket(data, data.length);
        if (port >= 0)
        {
            packet.setPort(port);
        }
        packet.setAddress(inetAddress);
        socket.send(packet);
    }

    protected synchronized MuleMessage doSend(Event event) throws Exception
    {
        doDispatch(event);
        // If we're doing sync receive try and read return info from socket
        if (event.getEndpoint().isRemoteSync())
        {
            DatagramSocket socket = connector.getSocket(event.getEndpoint());
            DatagramPacket result = receive(socket, event.getTimeout());
            if (result == null)
            {
                return null;
            }
            return new DefaultMuleMessage(connector.getMessageAdapter(result), event.getMessage());
        }
        else
        {
            return event.getMessage();
        }
    }

    private DatagramPacket receive(DatagramSocket socket, int timeout) throws IOException
    {
        int origTimeout = socket.getSoTimeout();
        try
        {
            DatagramPacket packet = new DatagramPacket(new byte[connector.getReceiveBufferSize()],
                connector.getReceiveBufferSize());

            if(timeout > 0 && timeout != socket.getSoTimeout())
            {
                socket.setSoTimeout(timeout);
            }
            socket.receive(packet);
            return packet;
        }
        finally
        {
            if(socket.getSoTimeout()!= origTimeout)
            {
                socket.setSoTimeout(origTimeout);
            }
        }
    }

    protected void doDispose()
    {
        // template method
    }
}
