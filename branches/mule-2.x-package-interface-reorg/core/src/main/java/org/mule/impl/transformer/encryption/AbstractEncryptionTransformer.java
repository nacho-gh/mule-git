/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.impl.transformer.encryption;

import org.mule.api.EncryptionStrategy;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.security.CryptoFailureException;
import org.mule.api.transformer.TransformerException;
import org.mule.impl.config.i18n.CoreMessages;
import org.mule.impl.transformer.AbstractTransformer;

import java.io.UnsupportedEncodingException;

/**
 * <code>EncryptionTransformer</code> will transform an array of bytes or string
 * into an encrypted array of bytes
 *
 */
public abstract class AbstractEncryptionTransformer extends AbstractTransformer
{
    private EncryptionStrategy strategy = null;
    private String strategyName = null;

    public AbstractEncryptionTransformer()
    {
        registerSourceType(byte[].class);
        registerSourceType(String.class);
        setReturnClass(byte[].class);
    }

    // @Override
    public Object clone() throws CloneNotSupportedException
    {
        AbstractEncryptionTransformer clone = (AbstractEncryptionTransformer) super.clone();
        /*
         * The actual strategy is *shared* - not sure if this is right? both shallow
         * and deep copy make sense - think about security, passwords, required
         * external authentication dependencies etc. :(
         */
        clone.setStrategy(strategy);
        clone.setStrategyName(strategyName);
        return clone;
    }

    public Object doTransform(Object src, String encoding) throws TransformerException
    {
        byte[] buf;
        if (src instanceof String)
        {
            buf = src.toString().getBytes();
        }
        else
        {
            buf = (byte[]) src;
        }
        try
        {
            byte[] result = getTransformedBytes(buf);
            if (getReturnClass().equals(String.class))
            {
                if (encoding != null)
                {
                    try
                    {
                        return new String(result, encoding);
                    }
                    catch (UnsupportedEncodingException ex)
                    {
                        return new String(result);
                    }
                }
                else
                {
                    return new String(result);
                }
            }
            else
            {
                return result;
            }
        }
        catch (CryptoFailureException e)
        {
            throw new TransformerException(this, e);
        }
    }

    protected abstract byte[] getTransformedBytes(byte[] buffer) throws CryptoFailureException;

    /**
     * Template method were deriving classes can do any initialisation after the
     * properties have been set on this transformer
     * 
     * @throws org.mule.api.lifecycle.InitialisationException
     */
    public void initialise() throws InitialisationException
    {
        if (strategyName != null)
        {
            if (endpoint.getMuleContext().getSecurityManager() == null)
            {
                if (strategy == null)
                {
                    throw new InitialisationException(CoreMessages.authSecurityManagerNotSet(), this);
                }
            }
            else
            {
                strategy = endpoint.getMuleContext().getSecurityManager().getEncryptionStrategy(strategyName);
            }
        }
        if (strategy == null)
        {
            throw new InitialisationException(CoreMessages.encryptionStrategyNotSet(), this);
        }
    }

    public EncryptionStrategy getStrategy()
    {
        return strategy;
    }

    public void setStrategy(EncryptionStrategy strategy)
    {
        this.strategy = strategy;
    }

    public String getStrategyName()
    {
        return strategyName;
    }

    public void setStrategyName(String strategyName)
    {
        this.strategyName = strategyName;
    }

}
