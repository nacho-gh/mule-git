/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.tcp;

import org.mule.config.i18n.CoreMessages;
import org.mule.impl.model.streaming.CallbackOutputStream;
import org.mule.providers.AbstractConnector;
import org.mule.providers.tcp.i18n.TcpMessages;
import org.mule.providers.tcp.protocols.SafeProtocol;
import org.mule.umo.MessagingException;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;
import org.mule.util.ClassUtils;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;

/**
 * <code>TcpConnector</code> can bind or sent to a given TCP port on a given host.
 * Other socket-based transports can be built on top of this class by providing the
 * appropriate socket factories and application level protocols as required (see
 * the constructor and the SSL transport for examples).
 */
public class TcpConnector extends AbstractConnector
{
    /** Property can be set on the endpoint to configure how the socket is managed */
    public static final String KEEP_SEND_SOCKET_OPEN_PROPERTY = "keepSendSocketOpen";
    public static final int DEFAULT_SOCKET_TIMEOUT = INT_VALUE_NOT_SET;
    public static final int DEFAULT_BUFFER_SIZE = INT_VALUE_NOT_SET;
    public static final int DEFAULT_BACKLOG = INT_VALUE_NOT_SET;

    // to clarify arg to configureSocket 
    public static final boolean SERVER = false;
    public static final boolean CLIENT = true;

    private int clientSoTimeout = DEFAULT_SOCKET_TIMEOUT;
    private int serverSoTimeout = DEFAULT_SOCKET_TIMEOUT;
    private int sendBufferSize = DEFAULT_BUFFER_SIZE;
    private int receiveBufferSize = DEFAULT_BUFFER_SIZE;
    private int receiveBacklog = DEFAULT_BACKLOG;
    private boolean sendTcpNoDelay;
    private boolean validateConnections = true;
    private int socketSoLinger = INT_VALUE_NOT_SET;
    private String tcpProtocolClassName;
    private TcpProtocol tcpProtocol;
    private boolean keepSendSocketOpen = false;
    private boolean keepAlive = false;
    private PooledSocketFactory socketFactory;
    private SimpleServerSocketFactory serverSocketFactory;
    private GenericKeyedObjectPool dispatcherSocketsPool = new GenericKeyedObjectPool();

    public TcpConnector()
    {
        setSocketFactory(new TcpSocketFactory());
        setServerSocketFactory(new TcpServerSocketFactory());
        setTcpProtocolClassName(SafeProtocol.class.getName());
    }

    public void configureSocket(boolean client, Socket socket) throws SocketException
    {
        // There is some overhead in setting socket timeout and buffer size, so we're
        // careful here only to set if needed

        if (newValue(getReceiveBufferSize(), socket.getReceiveBufferSize()))
        {
            socket.setReceiveBufferSize(getReceiveBufferSize());
        }
        if (newValue(getSendBufferSize(), socket.getSendBufferSize()))
        {
            socket.setSendBufferSize(getSendBufferSize());
        }
        if (client)
        {
            if (newValue(getClientSoTimeout(), socket.getSoTimeout()))
            {
                socket.setSoTimeout(getClientSoTimeout());
            }
        }
        else
        {
            if (newValue(getServerSoTimeout(), socket.getSoTimeout()))
            {
                socket.setSoTimeout(getServerSoTimeout());
            }
        }
        if (newValue(getSocketSoLinger(), socket.getSoLinger()))
        {
            socket.setSoLinger(true, getSocketSoLinger());
        }
        socket.setTcpNoDelay(isSendTcpNoDelay());
        socket.setKeepAlive(isKeepAlive());
    }

    private boolean newValue(int parameter, int socketValue)
    {
        return parameter != UMOConnector.INT_VALUE_NOT_SET && parameter != socketValue;
    }

    protected void doInitialise() throws InitialisationException
    {
        if (tcpProtocol == null)
        {
            try
            {
                tcpProtocol = (TcpProtocol) ClassUtils.instanciateClass(tcpProtocolClassName, null);
            }
            catch (Exception e)
            {
                throw new InitialisationException(TcpMessages.failedToInitMessageReader(), e, this);
            }
        }

        dispatcherSocketsPool.setFactory(getSocketFactory());
        dispatcherSocketsPool.setTestOnBorrow(true);
        dispatcherSocketsPool.setTestOnReturn(true);
        //There should only be one pooled instance per socket (key)        
        dispatcherSocketsPool.setMaxActive(1);
    }

    protected void doDispose()
    {
        logger.debug("Closing TCP connector");
        try
        {
            dispatcherSocketsPool.close();
        }
        catch (Exception e)
        {
            logger.warn("Failed to close dispatcher socket pool: " + e.getMessage());
        }
    }

    /**
     * Lookup a socket in the list of dispatcher sockets but don't create a new
     * socket
     */
    protected Socket getSocket(UMOImmutableEndpoint endpoint) throws Exception
    {
        Socket socket = (Socket) dispatcherSocketsPool.borrowObject(endpoint);
        if (logger.isDebugEnabled())
        {
            logger.debug("borrowing socket; debt " + dispatcherSocketsPool.getNumActive());
        }
        return socket;
    }

    void releaseSocket(Socket socket, UMOImmutableEndpoint endpoint) throws Exception
    {
        dispatcherSocketsPool.returnObject(endpoint, socket);
        if (logger.isDebugEnabled())
        {
            logger.debug("returned socket; debt " + dispatcherSocketsPool.getNumActive());
        }
    }

    public OutputStream getOutputStream(final UMOImmutableEndpoint endpoint, UMOMessage message)
            throws UMOException
    {
        final Socket socket;
        try
        {
            socket = getSocket(endpoint);
        }
        catch (Exception e)
        {
            throw new MessagingException(CoreMessages.failedToGetOutputStream(), message, e);
        }
        if (socket == null)
        {
            // This shouldn't happen
            throw new IllegalStateException("could not get socket for endpoint: "
                    + endpoint.getEndpointURI().getAddress());
        }
        try
        {
            return new CallbackOutputStream(
                    new DataOutputStream(new BufferedOutputStream(socket.getOutputStream())),
                    new CallbackOutputStream.Callback()
                    {
                        public void onClose() throws Exception
                        {
                            releaseSocket(socket, endpoint);
                        }
                    });
        }
        catch (IOException e)
        {
            throw new MessagingException(CoreMessages.failedToGetOutputStream(), message, e);
        }
    }

    protected void doConnect() throws Exception
    {
        // template method
    }

    protected void doDisconnect() throws Exception
    {
        dispatcherSocketsPool.clear();
    }

    protected void doStart() throws UMOException
    {
        // template method
    }

    protected void doStop() throws UMOException
    {
        // template method
    }

    public String getProtocol()
    {
        return "tcp";
    }

    // getters and setters ---------------------------------------------------------

    public boolean isKeepSendSocketOpen()
    {
        return keepSendSocketOpen;
    }

    public void setKeepSendSocketOpen(boolean keepSendSocketOpen)
    {
        this.keepSendSocketOpen = keepSendSocketOpen;
    }

    /**
     * A shorthand property setting timeout for both SEND and RECEIVE sockets.
     *
     * @deprecated The time out should be set explicitly for each
     */
    public void setTimeout(int timeout)
    {
        setClientSoTimeout(timeout);
        setServerSoTimeout(timeout);
    }

    public int getClientSoTimeout()
    {
        return this.clientSoTimeout;
    }

    public void setClientSoTimeout(int timeout)
    {
        this.clientSoTimeout = valueOrDefault(timeout, 0, DEFAULT_SOCKET_TIMEOUT);
    }

    public int getServerSoTimeout()
    {
        return serverSoTimeout;
    }

    public void setServerSoTimeout(int timeout)
    {
        this.serverSoTimeout = valueOrDefault(timeout, 0, DEFAULT_SOCKET_TIMEOUT);
    }

    /** @deprecated Should use {@link #getSendBufferSize()} or {@link #getReceiveBufferSize()} */
    public int getBufferSize()
    {
        return sendBufferSize;
    }

    /** @deprecated Should use {@link #setSendBufferSize(int)} or {@link #setReceiveBufferSize(int)} */
    public void setBufferSize(int bufferSize)
    {
        sendBufferSize = valueOrDefault(bufferSize, 1, DEFAULT_BUFFER_SIZE);
    }

    public int getSendBufferSize()
    {
        return sendBufferSize;
    }

    public void setSendBufferSize(int bufferSize)
    {
        sendBufferSize = valueOrDefault(bufferSize, 1, DEFAULT_BUFFER_SIZE);
    }

    public int getReceiveBufferSize()
    {
        return receiveBufferSize;
    }

    public void setReceiveBufferSize(int bufferSize)
    {
        receiveBufferSize = valueOrDefault(bufferSize, 1, DEFAULT_BUFFER_SIZE);
    }

    public int getReceiveBacklog()
    {
        return receiveBacklog;
    }

    public void setReceiveBacklog(int receiveBacklog)
    {
        this.receiveBacklog = valueOrDefault(receiveBacklog, 0, DEFAULT_BACKLOG);
    }

    public int getSocketSoLinger()
    {
        return socketSoLinger;
    }

    public void setSocketSoLinger(int soLinger)
    {
        this.socketSoLinger = valueOrDefault(soLinger, 0, INT_VALUE_NOT_SET);
    }

    /**
     * @return
     * @deprecated should use {@link #getReceiveBacklog()}
     */
    public int getBacklog()
    {
        return receiveBacklog;
    }

    /**
     * @param backlog
     * @deprecated should use {@link #setReceiveBacklog(int)}
     */
    public void setBacklog(int backlog)
    {
        this.receiveBacklog = backlog;
    }

    public TcpProtocol getTcpProtocol()
    {
        return tcpProtocol;
    }

    public void setTcpProtocol(TcpProtocol tcpProtocol)
    {
        this.tcpProtocol = tcpProtocol;
    }

    public String getTcpProtocolClassName()
    {
        return tcpProtocolClassName;
    }

    public void setTcpProtocolClassName(String protocolClassName)
    {
        this.tcpProtocolClassName = protocolClassName;
    }

    public boolean isRemoteSyncEnabled()
    {
        return true;
    }

    public boolean isKeepAlive()
    {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive)
    {
        this.keepAlive = keepAlive;
    }

    public boolean isSendTcpNoDelay()
    {
        return sendTcpNoDelay;
    }

    public void setSendTcpNoDelay(boolean sendTcpNoDelay)
    {
        this.sendTcpNoDelay = sendTcpNoDelay;
    }

    protected void setSocketFactory(PooledSocketFactory socketFactory)
    {
        this.socketFactory = socketFactory;
    }

    protected PooledSocketFactory getSocketFactory()
    {
        return socketFactory;
    }

    public SimpleServerSocketFactory getServerSocketFactory()
    {
        return serverSocketFactory;
    }

    public void setServerSocketFactory(SimpleServerSocketFactory serverSocketFactory)
    {
        this.serverSocketFactory = serverSocketFactory;
    }

    protected ServerSocket getServerSocket(URI uri) throws IOException
    {
        return getServerSocketFactory().createServerSocket(uri, getReceiveBacklog());
    }

    private static int valueOrDefault(int value, int threshhold, int deflt)
    {
        if (value < threshhold)
        {
            return deflt;
        }
        else
        {
            return value;
        }
    }

    /**
     * Should the connection be checked before sending data?
     *
     * @return If true, the message adapter opens and closes the socket on intialisation.
     */
    public boolean isValidateConnections()
    {
        return validateConnections;
    }

    /**
     * @param validateConnections If true, the message adapter opens and closes the socket on intialisation.
     * @see #isValidateConnections()
     */
    public void setValidateConnections(boolean validateConnections)
    {
        this.validateConnections = validateConnections;
    }
}
