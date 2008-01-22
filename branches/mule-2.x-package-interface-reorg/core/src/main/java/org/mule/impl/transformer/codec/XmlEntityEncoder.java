/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.impl.transformer.codec;

import org.mule.api.transformer.TransformerException;
import org.mule.impl.config.i18n.CoreMessages;
import org.mule.impl.transformer.AbstractTransformer;
import org.mule.util.XMLEntityCodec;

/**
 * Encodes a string with XML entities
 */
public class XmlEntityEncoder extends AbstractTransformer
{

    public XmlEntityEncoder()
    {
        registerSourceType(String.class);
        registerSourceType(byte[].class);
        setReturnClass(String.class);
    }

    public Object doTransform(Object src, String encoding) throws TransformerException
    {
        try
        {
            String data;

            if (src instanceof byte[])
            {
                data = new String((byte[]) src, encoding);
            }
            else
            {
                data = (String) src;
            }

            return XMLEntityCodec.encodeString(data);
        }
        catch (Exception ex)
        {
            throw new TransformerException(
                CoreMessages.transformFailed(src.getClass().getName(), "XML"), 
                this, ex);
        }
    }

}
