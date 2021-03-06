/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers;

import edu.emory.mathcs.backport.java.util.concurrent.CountDownLatch;

import javax.resource.spi.work.Work;

import java.util.Iterator;
import java.util.List;

import org.mule.config.ThreadingProfile;
import org.mule.transaction.TransactionCallback;
import org.mule.transaction.TransactionTemplate;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;

/**
 * The TransactedPollingMessageReceiver is an abstract receiver that handles
 * polling and transaction management. Derived implementations of these class
 * must be thread safe as several threads can be started at once for an
 * improveded throuput.
 * 
 * @author <a href=mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public abstract class TransactedPollingMessageReceiver extends PollingMessageReceiver
{
    /** determines whether messages will be received in a transaction template */
    protected boolean receiveMessagesInTransaction = true;

    /** determines whether Multiple receivers are created to improve throughput */
    protected boolean useMultipleReceivers = true;

    public TransactedPollingMessageReceiver(UMOConnector connector,
                                            UMOComponent component,
                                            final UMOEndpoint endpoint,
                                            Long frequency) throws InitialisationException
    {
        super(connector, component, endpoint, frequency);

        if (endpoint.getTransactionConfig().getFactory() != null) {
            receiveMessagesInTransaction = true;
        } else {
            receiveMessagesInTransaction = false;
        }
    }

    public void doStart() throws UMOException
    {
        //Connector property overrides any implied value
        useMultipleReceivers = connector.isCreateMultipleTransactedReceivers();
        ThreadingProfile tp = connector.getReceiverThreadingProfile();
        if (useMultipleReceivers && receiveMessagesInTransaction && tp.isDoThreading()) {
            for (int i = 0; i < tp.getMaxThreadsActive(); i++) {
                super.doStart();
            }
        } else {
            super.doStart();
        }
    }

    public void poll() throws Exception
    {
        TransactionTemplate tt = new TransactionTemplate(endpoint.getTransactionConfig(),
                                                         connector.getExceptionListener());
        if (receiveMessagesInTransaction) {
            // Receive messages and process them in a single transaction
            // Do not enable threading here, but serveral workers
            // may have been started
            TransactionCallback cb = new TransactionCallback() {
                public Object doInTransaction() throws Exception
                {
                    List messages = getMessages();
                    if (messages != null && messages.size() > 0) {
                        for (Iterator it = messages.iterator(); it.hasNext();) {
                            Object message = it.next();
                            if (logger.isTraceEnabled()) {
                                logger.trace("Received Message: " + message);
                            }
                            processMessage(message);
                        }
                    }
                    return null;
                }
            };
            tt.execute(cb);
        } else {
            // Receive messages and launch a worker thread
            // for each message
            List messages = getMessages();
            if (messages != null && messages.size() > 0) {
                final CountDownLatch countdown = new CountDownLatch(messages.size());
                for (Iterator it = messages.iterator(); it.hasNext();) {
                    final Object message = it.next();
                    if (logger.isTraceEnabled()) {
                        logger.trace("Received Message: " + message);
                    }
                    try {
                        getWorkManager().scheduleWork(new MessageProcessorWorker(tt, countdown, message));
                    } catch (Exception e) {
                        countdown.countDown();
                        throw e;
                    }
                }
                countdown.await();
            }
        }
    }

    protected class MessageProcessorWorker implements Work, TransactionCallback
    {
        private TransactionTemplate tt;
        private Object message;
        private CountDownLatch latch;

        public MessageProcessorWorker(TransactionTemplate tt, CountDownLatch latch, Object message)
        {
            this.tt = tt;
            this.message = message;
            this.latch = latch;
        }

        public void release()
        {
            // nothing to do
        }

        public void run()
        {
            try {
                tt.execute(this);
            } catch (Exception e) {
                handleException(e);
            } finally {
                latch.countDown();
            }
        }

        public Object doInTransaction() throws Exception
        {
            processMessage(message);
            return null;
        }

    }

    protected abstract List getMessages() throws Exception;

    protected abstract void processMessage(Object message) throws Exception;

}
