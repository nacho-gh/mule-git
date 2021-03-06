/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.sxc;

import static org.mule.util.ClassUtils.equal;
import static org.mule.util.ClassUtils.hash;

import org.mule.api.MuleMessage;
import org.mule.api.routing.filter.Filter;

import com.envoisolutions.sxc.xpath.XPathBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SxcFilter implements Filter
{
    protected transient Log logger = LogFactory.getLog(getClass());

    private String pattern;
    
    public SxcFilter()
    {
        super();
    }

    public SxcFilter(String pattern)
    {
        this.pattern = pattern;
    }

    public boolean accept(MuleMessage obj)
    {
         Object accept = obj.getProperty(toString());
         
         if (accept == null && SxcFilteringOutboundRouter.getCurrentMessage() == null) 
         {
             return false;
         }
         else if (accept == null)
         {
             throw new UndefinedMatchException();
         }
         
         return ((Boolean) accept).booleanValue();
    }

    /** @return XPath expression */
    public String getPattern()
    {
        return pattern;
    }

    /** @param pattern The XPath expression */
    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    public void addEventHandler(SxcFilteringOutboundRouter router, XPathBuilder builder)
    {    
        builder.listen(pattern, new FilterEventHandler(router, this));
    }
    
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        final SxcFilter other = (SxcFilter) obj;
        return equal(pattern, other.pattern);
    }

    public int hashCode()
    {
        return hash(new Object[]{this.getClass(), pattern});
    }
}


