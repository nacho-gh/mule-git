/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.api.routing;

import org.mule.api.lifecycle.Disposable;
import org.mule.api.lifecycle.Initialisable;
import org.mule.management.stats.RouterStatistics;

import java.util.List;

/**
 * <code>RouterCollection</code> defines the interface for a MessageRouter that
 * manages more than one router. A {@link RouterCatchAllStrategy} can be set on
 * this router to route unwanted or unfiltered events. If a catch strategy is not
 * set, the router just returns null.
 */

public interface RouterCollection extends Initialisable, Disposable
{
    void setRouters(List routers);

    List getRouters();

    void addRouter(Router router);

    Router removeRouter(Router router);

    RouterCatchAllStrategy getCatchAllStrategy();

    void setCatchAllStrategy(RouterCatchAllStrategy catchAllStrategy);

    boolean isMatchAll();

    RouterStatistics getStatistics();

    void setStatistics(RouterStatistics stat);

    void setMatchAll(boolean matchAll);
}
