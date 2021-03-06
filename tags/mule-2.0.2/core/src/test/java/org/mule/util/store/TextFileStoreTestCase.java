/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.util.store;

import org.mule.tck.AbstractMuleTestCase;
import org.mule.tck.testmodels.fruit.Apple;
import org.mule.tck.testmodels.fruit.Banana;
import org.mule.tck.testmodels.fruit.Kiwi;
import org.mule.util.FileUtils;

import java.io.File;

public class TextFileStoreTestCase extends AbstractMuleTestCase
{
    public static final String DIR = ".mule/temp";
    TextFileObjectStore store;

    //@Override
    protected void doSetUp() throws Exception
    {
        super.doSetUp();
        FileUtils.deleteTree(new File(DIR));

    }

    //@Override
    protected void doTearDown() throws Exception
    {
        if (store != null)
        {
            store.dispose();
        }
        FileUtils.deleteTree(new File(DIR));
        super.doTearDown();

    }

    public void testTimedExpiry() throws Exception
    {
        // entryTTL=3 and expiryInterval=1 will cause background expiry
        store = new TextFileObjectStore();
        store.setDirectory(DIR);
        store.setMuleContext(muleContext);
        store.setName("timed");
        store.setMaxEntries(3);
        store.setEntryTTL(3000);
        store.setExpirationInterval(1000);
        store.initialise();

        // store entries in quick succession
        assertTrue(store.storeObject("1", "1"));
        assertTrue(store.storeObject("2", "2"));
        assertTrue(store.storeObject("3", "3"));

        // they should still be alive at this point
        assertTrue(store.containsObject("1"));
        assertTrue(store.containsObject("2"));
        assertTrue(store.containsObject("3"));

        // wait until the entry TTL has been exceeded
        Thread.sleep(4000);

        // make sure all values are gone
        assertFalse(store.containsObject("1"));
        assertFalse(store.containsObject("2"));
        assertFalse(store.containsObject("3"));

    }

    public void testTimedExpiryWithRestart() throws Exception
    {
        // entryTTL=3 and expiryInterval=1 will cause background expiry
        store = new TextFileObjectStore();
        store.setDirectory(DIR);
        store.setMuleContext(muleContext);
        store.setName("timed");
        store.setMaxEntries(3);
        store.setEntryTTL(3000);
        store.setExpirationInterval(1000);
        store.initialise();

        // store entries in quick succession
        assertTrue(store.storeObject("1", "1"));
        assertTrue(store.storeObject("2", "2"));
        assertTrue(store.storeObject("3", "3"));

        // they should still be alive at this point
        assertTrue(store.containsObject("1"));
        assertTrue(store.containsObject("2"));
        assertTrue(store.containsObject("3"));

        store.dispose();

        store = new TextFileObjectStore();
        store.setDirectory(DIR);
        store.setMuleContext(muleContext);
        store.setName("timed");
        store.setMaxEntries(3);
        store.setEntryTTL(3000);
        store.setExpirationInterval(1000);
        store.initialise();

        assertTrue(store.containsObject("1"));
        assertTrue(store.containsObject("2"));
        assertTrue(store.containsObject("3"));

        // wait until the entry TTL has been exceeded
        Thread.sleep(4000);

        // make sure all values are gone
        assertFalse(store.containsObject("1"));
        assertFalse(store.containsObject("2"));
        assertFalse(store.containsObject("3"));

        store.dispose();

        store = new TextFileObjectStore();
        store.setDirectory(DIR);
        store.setMuleContext(muleContext);
        store.setName("timed");
        store.setMaxEntries(3);
        store.setEntryTTL(3000);
        store.setExpirationInterval(1000);
        store.initialise();

// make sure all values are gone
        assertFalse(store.containsObject("1"));
        assertFalse(store.containsObject("2"));
        assertFalse(store.containsObject("3"));


    }

    public void testTimedExpiryWithObjects() throws Exception
    {
        // entryTTL=3 and expiryInterval=1 will cause background expiry
        store = new TextFileObjectStore();
        store.setDirectory(DIR);
        store.setMuleContext(muleContext);
        store.setName("timed");
        store.setMaxEntries(3);
        store.setEntryTTL(3000);
        store.setExpirationInterval(1000);
        store.initialise();

        // store entries in quick succession
        try
        {
            assertTrue(store.storeObject("1", new Apple()));
            fail("should not be abe to store objects");
        }
        catch (Exception e)
        {
            //expected
        }
    }

    public void testMaxSize() throws Exception
    {
        // entryTTL=-1 means we will have to expire manually
        store = new TextFileObjectStore();
        store.setDirectory(DIR);
        store.setMuleContext(muleContext);
        store.setName("bounded");
        store.setMaxEntries(3);
        store.setEntryTTL(-1);
        store.setExpirationInterval(1000);
        store.initialise();


        assertTrue(store.storeObject("1", "1"));
        assertTrue(store.storeObject("2", "2"));
        assertTrue(store.storeObject("3", "3"));

        assertTrue(store.containsObject("1"));
        assertTrue(store.containsObject("2"));
        assertTrue(store.containsObject("3"));

        // sleep a bit to make sure that entries are not expired, even though the expiry
        // thread is running every second
        Thread.sleep(3000);
        assertTrue(store.containsObject("1"));
        assertTrue(store.containsObject("2"));
        assertTrue(store.containsObject("3"));

        // exceed threshold
        assertTrue(store.storeObject("4", "4"));

        // the oldest entry should still be there
        assertTrue(store.containsObject("1"));

        // expire manually
        store.expire();
        assertFalse(store.containsObject("1"));
        assertTrue(store.containsObject("2"));
        assertTrue(store.containsObject("3"));
        assertTrue(store.containsObject("4"));

        // exceed some more
        assertTrue(store.storeObject("5", "5"));
        store.expire();
        assertFalse(store.containsObject("2"));
        assertTrue(store.containsObject("3"));
        assertTrue(store.containsObject("4"));
        assertTrue(store.containsObject("5"));

        // and multiple times
        assertTrue(store.storeObject("6", "6"));
        assertTrue(store.storeObject("7", "7"));
        assertTrue(store.storeObject("8", "8"));
        assertTrue(store.storeObject("9", "9"));

        store.expire();
        assertTrue(store.containsObject("7"));
        assertTrue(store.containsObject("8"));
        assertTrue(store.containsObject("9"));
        assertFalse(store.containsObject("3"));
        assertFalse(store.containsObject("4"));
        assertFalse(store.containsObject("5"));
        assertFalse(store.containsObject("6"));
    }

}