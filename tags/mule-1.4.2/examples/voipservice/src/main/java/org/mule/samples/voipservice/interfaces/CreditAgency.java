/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.samples.voipservice.interfaces;

import org.mule.samples.voipservice.to.CreditProfileTO;

import java.io.IOException;

/**
 * @author Binildas Christudas
 */
public interface CreditAgency
{

    CreditProfileTO getCreditProfile(CreditProfileTO creditProfileTO) throws IOException;
}
