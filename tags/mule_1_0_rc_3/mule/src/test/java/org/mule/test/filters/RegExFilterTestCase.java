/*
 * $Header$
 * $Revision$
 * $Date$
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) Cubis Limited. All rights reserved.
 * http://www.cubis.co.uk
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.test.filters;

import org.mule.routing.filters.RegExFilter;
import org.mule.tck.NamedTestCase;

/**
 * @author <a href="mailto:ross.mason@cubis.co.uk">Ross Mason</a>
 * @version $Revision$
 */

public class RegExFilterTestCase extends NamedTestCase
{
    public void testWildcardFilter() {


        RegExFilter filter = new RegExFilter("The quick (.*)");
        assertNotNull(filter.getPattern());

        assertTrue(filter.accept("The quick brown fox"));
        assertTrue(filter.accept("The quick "));

        assertTrue(!filter.accept("The quickbrown fox"));
        assertTrue(!filter.accept("he quick brown fox"));

        filter.setPattern("(.*) brown fox");
        assertTrue(filter.accept("The quick brown fox"));
        assertTrue(filter.accept(" brown fox"));

        assertTrue(!filter.accept("The quickbrown fox"));
        assertTrue(!filter.accept("The quick brown fo"));

        filter.setPattern("(.*) brown (.*)");
        assertTrue(filter.accept("The quick brown fox"));
        assertTrue(filter.accept("(.*) brown fox"));

        assertTrue(!filter.accept("The quickbrown fox"));
        assertTrue(filter.accept("The quick brown fo"));

        filter.setPattern("(.*)");
        assertTrue(filter.accept("The quick brown fox"));
    }
}
