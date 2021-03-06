/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.registry;

import org.mule.api.transformer.DiscoverableTransformer;
import org.mule.api.transformer.Transformer;
import org.mule.api.transformer.TransformerException;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.transformer.AbstractTransformer;
import org.mule.transformer.simple.ObjectToByteArray;

import java.io.FilterInputStream;
import java.io.InputStream;

public class TransformerCachingTestCase extends AbstractMuleTestCase
{
    public void testCacheUpdate() throws Exception
    {
        Transformer trans = muleContext.getRegistry().lookupTransformer(FilterInputStream.class, byte[].class);
        assertNotNull(trans);
        assertTrue(trans instanceof ObjectToByteArray);

        Transformer trans2 = new FilterInputStreamToByteArray();
        muleContext.getRegistry().registerTransformer(trans2);

        trans = muleContext.getRegistry().lookupTransformer(FilterInputStream.class, byte[].class);
        assertNotNull(trans);
        assertTrue(trans instanceof FilterInputStreamToByteArray);

        trans = muleContext.getRegistry().lookupTransformer(InputStream.class, byte[].class);
        assertNotNull(trans);
        assertTrue(trans instanceof ObjectToByteArray);

        muleContext.getRegistry().unregisterTransformer(trans2.getName());

        trans = muleContext.getRegistry().lookupTransformer(FilterInputStream.class, byte[].class);
        assertNotNull(trans);
        assertTrue(trans instanceof ObjectToByteArray);

    }

    public static class FilterInputStreamToByteArray extends AbstractTransformer implements DiscoverableTransformer
    {
        public FilterInputStreamToByteArray()
        {
            registerSourceType(FilterInputStream.class);
            setReturnClass(byte[].class);
        }

        protected Object doTransform(Object src, String encoding) throws TransformerException
        {
            throw new UnsupportedOperationException("This is a transformer only to be used for testing");
        }

        public int getPriorityWeighting()
        {
            return 0;
        }

        public void setPriorityWeighting(int weighting)
        {
            //no-op
        }
    }
}
