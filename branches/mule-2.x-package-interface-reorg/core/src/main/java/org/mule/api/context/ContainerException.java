/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.api.context;

import org.mule.impl.config.i18n.Message;

/**
 * <code>ContainerException</code> is an Exception thrown by the component resolver
 * when it fials to find a component
 */
public class ContainerException extends ManagerException
{
    /**
     * Serial version
     */
    private static final long serialVersionUID = 5024452108618986265L;

    /**
     * @param message the exception message
     */
    public ContainerException(Message message)
    {
        super(message);
    }

    /**
     * @param message the exception message
     * @param cause the exception that cause this exception to be thrown
     */
    public ContainerException(Message message, Throwable cause)
    {
        super(message, cause);
    }

}
