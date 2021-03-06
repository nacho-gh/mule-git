/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.json.transformers;

import org.mule.tck.testmodels.fruit.Banana;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * TODO
 */

public interface FruitCollectionMixin
{
    @JsonIgnore
    Banana getBanana();

    @JsonIgnore
    void setBanana(Banana banana);
}
