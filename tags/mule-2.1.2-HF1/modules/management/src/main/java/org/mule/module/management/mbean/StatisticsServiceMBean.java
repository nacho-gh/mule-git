/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.management.mbean;

import org.mule.api.management.stats.Statistics;

/**
 * <code>StatisticsServiceMBean</code> is a JMX interfaces for querying Mule event
 * processing statistics.
 */
public interface StatisticsServiceMBean extends Statistics
{

    String printCSVSummary ();

    String printHtmlSummary ();

    String printXmlSummary ();
}
