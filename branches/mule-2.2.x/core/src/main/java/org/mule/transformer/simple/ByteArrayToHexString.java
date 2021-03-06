/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transformer.simple;

import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractTransformer;
import org.mule.util.IOUtils;
import org.mule.util.StringUtils;

import java.io.InputStream;


/**
 * Converts a Byte array to a Hex String.
 */
public class ByteArrayToHexString extends AbstractTransformer
{
    private volatile boolean upperCase = false;

    public ByteArrayToHexString()
    {
        registerSourceType(byte[].class);
        registerSourceType(InputStream.class);
        setReturnClass(String.class);
    }

    public boolean getUpperCase()
    {
        return upperCase;
    }

    public void setUpperCase(boolean value)
    {
        upperCase = value;
    }

    protected Object doTransform(Object src, String encoding) throws TransformerException
    {
        if (src == null)
        {
            return StringUtils.EMPTY;
        }

        try
        {
            byte[] bytes = null;
            if (src instanceof InputStream)
            {
                InputStream input = (InputStream) src;
                try
                {
                    bytes = IOUtils.toByteArray(input);
                }
                finally
                {
                    IOUtils.closeQuietly(input);
                }
            }
            else
            {
                bytes = (byte[]) src;
            }
                
            return StringUtils.toHexString(bytes, upperCase);
        }
        catch (Exception ex)
        {
            throw new TransformerException(this, ex);
        }
    }

}
