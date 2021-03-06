/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.config.spring.parsers.generic;

import org.mule.config.spring.parsers.assembly.BeanAssembler;
import org.mule.util.StringUtils;

import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Grabs the text from an element and injects it into the parent, for example:
 * 
 * <foo>
 *   <bar-text>A bunch of text.</bar-text>
 * </foo>
 * 
 *   registerBeanDefinitionParser("foo", new OrphanDefinitionParser(Foo.class));
 *   registerBeanDefinitionParser("bar-text", new TextDefinitionParser("barText"));
 * 
 * will result in a call to Foo.setBarText("A bunch of text.")
 */
public class TextDefinitionParser extends ChildDefinitionParser
{
    public TextDefinitionParser(String setterMethod)
    {
        super(setterMethod, String.class);
    }

    //@Override
    protected void postProcess(ParserContext context, BeanAssembler assembler, Element element)
    {        
        Node node = element.getFirstChild();
        if (node != null)
        {
            String value = node.getNodeValue();
            if (!StringUtils.isBlank(value))
            {
                assembler.getTarget().getPropertyValues().addPropertyValue(setterMethod, value);
            }
        }
    }
}
