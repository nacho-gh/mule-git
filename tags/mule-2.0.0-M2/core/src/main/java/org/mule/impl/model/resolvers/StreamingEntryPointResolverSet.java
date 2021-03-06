/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.impl.model.resolvers;

/**
 * This resolver can be removed once we move to the API refactoring
 *
 * @deprecated
 */
public class StreamingEntryPointResolverSet extends DefaultEntryPointResolverSet
{
    public StreamingEntryPointResolverSet()
    {
        addEntryPointResolver(new StreamingEntryPointResolver());
    }
}