/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.impl.config.support;

import org.mule.api.AbstractMuleException;
import org.mule.api.Event;
import org.mule.api.MuleMessage;
import org.mule.impl.model.AbstractComponent;
import org.mule.impl.model.AbstractModel;

/**
 * TODO
 */
public class InheritedModel extends AbstractModel
{
    public String getType()
    {
        return "inherited";
    }


    /*
    * (non-Javadoc)
    *
    * @see org.mule.api.UMOModel#getName()
    */
    //@Override
    public String getName()
    {
        return super.getName() + "#" + hashCode();
    }

    public String getParentName()
    {
        return super.getName();
    }

    private class InheritedComponent extends AbstractComponent
    {
        public InheritedComponent()
        {
            super();
        }

        protected MuleMessage doSend(Event event) throws AbstractMuleException
        {
            throw new UnsupportedOperationException("doSend()");
        }

        protected void doDispatch(Event event) throws AbstractMuleException
        {
            throw new UnsupportedOperationException("doDispatch()");
        }
    }

}
