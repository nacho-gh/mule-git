/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.impl.component.builder;

import org.mule.api.MuleMessage;

/**
 * A Strategy Class for Building one message from the invocation results of a chain
 * if endpoints. This is used for invoking different endpoints to obain parts of a
 * larger message.
 */
public interface MessageBuilder
{
    Object buildMessage(MuleMessage request, MuleMessage response) throws MessageBuilderException;
}
