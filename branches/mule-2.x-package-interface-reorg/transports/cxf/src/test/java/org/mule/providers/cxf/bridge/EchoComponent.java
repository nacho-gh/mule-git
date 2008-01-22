
package org.mule.providers.cxf.bridge;

import org.mule.api.EventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;

import javax.xml.transform.Source;

public class EchoComponent implements Callable
{

    public Object onCall(EventContext eventContext) throws Exception
    {
        MuleMessage message = eventContext.getMessage();
        Source s = (Source) message.getPayload();
        return s;
    }

}
