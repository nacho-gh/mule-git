/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.integration.transport.file;


import org.mule.api.MuleEventContext;
import org.mule.api.context.notification.ServerNotification;
import org.mule.tck.FunctionalTestCase;
import org.mule.tck.functional.FunctionalTestComponent;
import org.mule.tck.functional.FunctionalTestNotification;
import org.mule.tck.functional.FunctionalTestNotificationListener;
import org.mule.util.FileUtils;
import org.mule.util.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class FileFunctionalTestCase extends FunctionalTestCase implements FunctionalTestNotificationListener
{
    private Object receivedData = null;

    //@Override
    protected void doSetUp() throws Exception
    {
        super.doSetUp();
        muleContext.registerListener(this);
    }

    // @Override
    protected void doTearDown() throws Exception
    {
        super.doTearDown();
        muleContext.unregisterListener(this);
    }

    // @Override
    protected String getConfigResources()
    {
        return "org/mule/test/integration/providers/file/file-config.xml";
    }

    public void testRelative() throws IOException, InterruptedException
    {
        // create binary file data to be written
        byte[] data = new byte[100000];
        for (int i = 0; i < data.length; i++)
        {
            data[i] = (byte)(Math.random() * 128);
        }

        File f = FileUtils.newFile("./test/testfile.temp");
        f.createNewFile();
        FileOutputStream fos = new FileOutputStream(f);
        IOUtils.write(data, fos);
        IOUtils.closeQuietly(fos);

        // atomically rename the file to make it available for polling
        f.renameTo(FileUtils.newFile(f.getPath().replaceAll(".temp", ".data")));

        // give polling a chance
        Thread.sleep(5000);

        synchronized (this)
        {
            assertNotNull(receivedData);
            assertTrue(receivedData instanceof byte[]);
            byte[] receivedBytes = (byte[])receivedData;
            assertEquals(data.length, receivedBytes.length);
            assertTrue(Arrays.equals(data, receivedBytes));
        }
    }

    public void onNotification(ServerNotification notification)
    {
        synchronized (this)
        {
            logger.debug("received notification: " + notification);
            // save the received message data for verification
            this.receivedData = ((FunctionalTestNotification)notification).getReplyMessage();
        }
    }

    public static class FileTestComponent extends FunctionalTestComponent
    {
        public Object onCall(MuleEventContext context) throws Exception
        {
            // there should not be any transformers configured by default, so the
            // return message should be a byte[]
            super.setReturnMessage(context.transformMessage());
            return super.onCall(context);
        }
    }

}
