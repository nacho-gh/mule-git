/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jms;

import org.mule.api.MuleContext;
import org.mule.api.transaction.Transaction;
import org.mule.api.transaction.TransactionException;
import org.mule.api.transaction.TransactionFactory;

/**
 * <code>JmsClientAcknowledgeTransactionFactory</code> creates a JMS Client
 * Acknowledge Transaction using a JMS Message.
 */

public class JmsClientAcknowledgeTransactionFactory implements TransactionFactory
{
    public Transaction beginTransaction(MuleContext muleContext) throws TransactionException
    {
        JmsClientAcknowledgeTransaction tx = new JmsClientAcknowledgeTransaction(muleContext);
        tx.begin();
        return tx;
    }

    public boolean isTransacted()
    {
        return false;
    }
}
