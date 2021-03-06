/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.i18n;

import org.mule.config.i18n.Message;
import org.mule.config.i18n.MessageFactory;

public class XmlMessages extends MessageFactory
{
    private static final XmlMessages factory = new XmlMessages();
    
    private static final String BUNDLE_PATH = getBundlePath("xml");

    public static Message failedToProcessXPath(String expression)
    {
        return factory.createMessage(BUNDLE_PATH, 1, expression);
    }

    public static Message domTypeNotSupported(Class type)
    {
        return factory.createMessage(BUNDLE_PATH, 2, type);
    }

    public static Message invalidReturnTypeForTransformer(Class resultCls)
    {
        return factory.createMessage(BUNDLE_PATH, 3, resultCls.getName());
    }

    public static Message failedToRegisterNamespace(String prefix, String uri)
    {
        return factory.createMessage(BUNDLE_PATH, 4, prefix, uri);
    }

    public static Message failedToCreateDocumentBuilder()
    {
        return factory.createMessage(BUNDLE_PATH, 5);
    }
}
