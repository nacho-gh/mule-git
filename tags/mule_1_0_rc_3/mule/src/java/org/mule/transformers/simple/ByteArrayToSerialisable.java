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
package org.mule.transformers.simple;

import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * <code>ByteArrayToSerialisable</code> converts a Serialised object to its
 * object representation
 *
 * @author <a href="mailto:ross.mason@cubis.co.uk">Ross Mason</a>
 * @version $Revision$
 */
public class ByteArrayToSerialisable extends AbstractTransformer
{
    public ByteArrayToSerialisable()
    {
        registerSourceType(byte[].class);
    }

    public Object doTransform(Object src) throws TransformerException
    {
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        try
        {
            bais = new ByteArrayInputStream((byte[])src);
            ois = new ObjectInputStream(bais);
            Object result = ois.readObject();
            bais.close();


            return result;
        } catch (Exception e)
        {
            throw new TransformerException("Faield to convert byte[] to Object: " + e.getMessage(), e);
        } finally {
            try
            {
                if(ois!=null) ois.close();
            } catch (IOException e)
            {
                //ignore
            }
        }
    }
}
