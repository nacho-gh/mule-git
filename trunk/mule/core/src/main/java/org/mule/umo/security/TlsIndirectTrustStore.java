/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.umo.security;

import org.mule.umo.security.tls.TlsConfiguration;

import java.io.IOException;

/**
 * Configure indirect trust stores.
 * TLS/SSL connections are made to trusted systems - the public certificates of trusted systems are store in 
 * a keystore (called a trust store) and used to verify that the connection made to a remote system "really
 * is" the expected identity.
 * 
 * <p>The information specified in this interface may be used to configure a trust store directly, as
 * part of {@link TlsDirectKeyStore}, or it may be stored as property values and used later, or both.  
 * It may therefore be specific to a single
 * connector, or global to all connectors made by that protocol, or even (in the case of the SSL transport)
 * become a global default value.  For more information see the documentation for the connector or protocol in
 * question.  The comments in {@link TlsConfiguration} may also be useful.</p>
 */
public interface TlsIndirectTrustStore
{

    /**
     * @return The location (resolved relative to the current classpath and file system, if possible)
     * of the keystore that contains public certificates of trusted servers.
     */
    public String getTrustStore();

    /**
     * @param name The location of the keystore that contains public certificates of trusted servers.
     * @throws IOException If the location cannot be resolved via the file system or classpath
     */
    public void setTrustStore(String name) throws IOException;

    /**
     * @return The password used to protected the trust store defined in {@link #getTrustStore()}
     */
    public String getTrustStorePassword();

    /**
     * @param trustStorePassword The password used to protected the trust store defined in 
     * {@link #setTrustStore(String)}
     */
    public void setTrustStorePassword(String trustStorePassword);

}


