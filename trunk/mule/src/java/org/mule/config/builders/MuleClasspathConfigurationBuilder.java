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
package org.mule.config.builders;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.config.ConfigurationException;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.umo.UMOManager;

import java.net.URL;
import java.util.Enumeration;

/**
 * <code>MuleClasspathConfigurationBuilder</code> can be used to configure a MuleManager
 * based on the configuration files on the classpath.  the default config resource name
 * is <b>mule-config.xml</b>  but this can be overrided by passing the config resourse
 * name to the configure method.
 *
 * @author <a href="mailto:ross.mason@cubis.co.uk">Ross Mason</a>
 * @version $Revision$
 */
public class MuleClasspathConfigurationBuilder extends MuleXmlConfigurationBuilder
{
    /**
     * logger used by this class
     */
    protected static transient Log logger = LogFactory.getLog(MuleClasspathConfigurationBuilder.class);

    public static final String MULE_CONFIGURATION_RESOURCE = "mule-config.xml";

    public MuleClasspathConfigurationBuilder() throws ConfigurationException {
        super();
    }

    /**
     * Will configure a UMOManager based on the configuration file(s) provided.
     *
     * @param configResources can be null or a single resource name that will be used
     * to seach the classpath.  The default is mule-config.xml
     * @return A configured UMOManager
     * @throws org.mule.config.ConfigurationException if the configResources param is invalid or the configurations fail to
     * load
     *
     */
    public UMOManager configure(String configResources) throws ConfigurationException
    {
        if(configResources!=null && !"".equals(configResources)) {
            if(configResources.indexOf(",") > -1) {
                throw new ConfigurationException(new Message(Messages.ONLY_SINGLE_RESOURCE_CAN_BE_SPECIFIED));
            }
        } else {
            configResources = MULE_CONFIGURATION_RESOURCE;
        }

        URL url = null;
        try
        {
            Enumeration e = Thread.currentThread().getContextClassLoader().getResources(configResources);
            while(e.hasMoreElements()) {
                url = (URL)e.nextElement();
                logger.info("Loading resource: " + url.toExternalForm());
                digester.parse(url.openStream());
            }
        } catch (Exception e)
        {
            throw new ConfigurationException(new Message(Messages.FAILED_LOAD_X, "Config: " + url.toString()), e);
        }

        return manager;
    }
}
