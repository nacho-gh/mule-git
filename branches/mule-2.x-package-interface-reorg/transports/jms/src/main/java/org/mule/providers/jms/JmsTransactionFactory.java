/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.jms;

import org.mule.api.MuleContext;
import org.mule.api.TransactionException;
import org.mule.api.Transaction;
import org.mule.api.TransactionFactory;

/**
 * <p>
 * <code>JmsTransactionFactory</code> creates a JMS local transaction
 */
public class JmsTransactionFactory implements TransactionFactory
{
    private String name;
    
    /*
     * (non-Javadoc)
     * 
     * @see org.mule.api.TransactionFactory#beginTransaction(org.mule.api.transport.MessageDispatcher)
     */
    public Transaction beginTransaction(MuleContext muleContext) throws TransactionException
    {
        JmsTransaction tx = new JmsTransaction();
        tx.begin();
        return tx;
    }

    public boolean isTransacted()
    {
        return true;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
