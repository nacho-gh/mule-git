/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.impl;

import org.mule.umo.lifecycle.InitialisationException;

/**
 * <code>InitialisationCallback</code> is used to provide customised
 * initialiation for more complex components. For example, soap services have a
 * custom initialisation that passes the service object to the mule component.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public interface InitialisationCallback
{
    void initialise(Object component) throws InitialisationException;
}
