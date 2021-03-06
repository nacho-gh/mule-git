/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.management.mbeans;

import java.util.Date;

import org.mule.umo.UMOException;

/**
 * <code>MuleServiceMBean</code> is a JMX service interface for the UMOManager
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public interface MuleServiceMBean
{
    boolean isInstanciated();

    boolean isInitialised();

    boolean isStopped();

    Date getStartTime();

    String getVersion();

    String getVendor();

    void start() throws UMOException;

    void stop() throws UMOException;

    void dispose() throws UMOException;

    long getFreeMemory();

    long getMaxMemory();

    long getTotalMemory();

    String getServerId();

    String getHostname();

    String getHostIp();

    String getOsVersion();

    String getJdkVersion();

    String getCopyright();

    String getLicense();

    String getBuildDate();

    String getInstanceId();
}
