/*
 * $Header$
 * $Revision$
 * $Date$
 * -----------------------------------------------------------------------------------------------------
 *
 * Copyright (c) Cubis Limited. All rights reserved.
 * http://www.cubis.co.uk
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 */


package org.mule.providers.jms;


import org.mule.tck.providers.AbstractMessageAdapterTestCase;
import org.mule.umo.provider.UMOMessageAdapter;

import javax.jms.Message;


/**
 * @author <a href="mailto:ross.mason@cubis.co.uk">Ross Mason</a>
 * @version $Revision$
 */

public class JmsMessageAdapterTestCase extends AbstractMessageAdapterTestCase
{
    /* (non-Javadoc)
     * @see org.mule.tck.providers.AbstractMessageAdapterTestCase#createAdapter()
     */
    public UMOMessageAdapter createAdapter(Object payload) throws Exception
    {
        return new JmsMessageAdapter((Message)payload);
    }

    public Object getValidMessage() throws Exception
    {
        return JmsConnectorTestCase.getMessage();
    }
}
