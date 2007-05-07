/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.soap;

import org.mule.config.i18n.Message;
import org.mule.config.i18n.MessageFactory;

public class SoapMessages extends MessageFactory
{
    private static final String BUNDLE_PATH = getBundlePath("soap");

    public static Message couldNotFindSoapProvider(String name)
    {
        return createMessage(BUNDLE_PATH, 8, name);
    }
}  


