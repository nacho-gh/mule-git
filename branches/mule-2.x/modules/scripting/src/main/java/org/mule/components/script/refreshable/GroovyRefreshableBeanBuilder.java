/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.components.script.refreshable;

import org.mule.MuleException;
import org.mule.config.i18n.CoreMessages;
import org.mule.umo.UMOEventContext;
import org.mule.umo.lifecycle.Callable;
import org.mule.util.StringUtils;

import groovy.lang.GroovyObject;

public class GroovyRefreshableBeanBuilder implements Callable
{
    private volatile Object refreshableBean;
    private String methodName;
    
    public GroovyRefreshableBeanBuilder()
    {
        super();
    }

    public Object onCall(UMOEventContext eventContext) throws Exception
    {
//        Refreshable rBean = (Refreshable)refreshableBean;
//        rBean.refresh();
        
        if (refreshableBean instanceof Callable)
        {
            return ((Callable)refreshableBean).onCall(eventContext);
        }
        if (refreshableBean instanceof GroovyObject)
        {
            if (StringUtils.isEmpty(methodName))
            {
                throw new MuleException(CoreMessages.propertiesNotSet("methodName"));
            }
            GroovyObject script = (GroovyObject)refreshableBean;
            return script.invokeMethod(methodName, eventContext.getTransformedMessage());
        }
        
        throw new Exception(new MuleException("script engine not supported"));
    }

    public Object getRefreshableBean()
    {
        return refreshableBean;
    }

    public void setRefreshableBean(Object refreshableBean)
    {
        this.refreshableBean = refreshableBean;
    }
    
    public String getMethodName()
    {
        return methodName;
    }

    public void setMethodName(String methodName)
    {
        this.methodName = methodName;
    }
}


