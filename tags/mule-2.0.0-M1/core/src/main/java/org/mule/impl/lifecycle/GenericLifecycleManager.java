/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.impl.lifecycle;

import org.mule.impl.lifecycle.phases.NotInLifecyclePhase;
import org.mule.umo.UMOException;
import org.mule.umo.UMOManagementContext;
import org.mule.umo.lifecycle.UMOLifecycleManager;
import org.mule.umo.lifecycle.UMOLifecyclePhase;
import org.mule.util.StringMessageUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO
 */
public class GenericLifecycleManager implements UMOLifecycleManager
{
    /**
     * logger used by this class
     */
    protected transient final Log logger = LogFactory.getLog(GenericLifecycleManager.class);
    protected static NotInLifecyclePhase notInLifecyclePhase = new NotInLifecyclePhase();
    protected String currentPhase = notInLifecyclePhase.getName();
    protected String executingPhase = null;
    protected ListOrderedSet lifecycles = new ListOrderedSet();
    protected Map index = new HashMap(6);
    protected Set completedPhases = new HashSet(6);

    public Set getLifecycles()
    {
        return lifecycles;
    }

    public void setLifecycles(Set lifecycles)
    {
        for (Iterator iterator = lifecycles.iterator(); iterator.hasNext();)
        {
            UMOLifecyclePhase phase = (UMOLifecyclePhase) iterator.next();
            registerLifecycle(phase);
        }

    }

    public void registerLifecycle(UMOLifecyclePhase lci)
    {
        index.put(lci.getName(), new Integer(lifecycles.size()));
        lifecycles.add(lci);
    }

    public void firePhase(UMOManagementContext managementContext, String phase) throws UMOException
    {
        if (currentPhase.equalsIgnoreCase(phase))
        {
            logger.debug("Already in lifecycle phase: " + phase);
            return;
        }
        Integer phaseIndex = (Integer) index.get(phase);
        if (phaseIndex == null)
        {
            throw new IllegalArgumentException("No lifeccycle phase registered with name: " + phase);
        }
        try
        {
            setExecutingPhase(phase);
            UMOLifecyclePhase li = (UMOLifecyclePhase) lifecycles.get(phaseIndex.intValue());
            li.fireLifecycle(managementContext, currentPhase);
            setCurrentPhase(li);
        }
        finally
        {
            setExecutingPhase(null);
        }
    }

    public String getCurrentPhase()
    {
        return currentPhase;
    }

    /**
     * Returns the name of the currently executing phase or null if there is not current phase
     *
     * @return
     */
    public String getExecutingPhase()
    {
        return executingPhase;
    }

    protected synchronized void setCurrentPhase(UMOLifecyclePhase phase)
    {
        completedPhases.add(phase.getName());
        completedPhases.remove(phase.getOppositeLifecyclePhase());
        this.currentPhase = phase.getName();
    }

    protected synchronized void setExecutingPhase(String phase)
    {
        this.executingPhase = phase;
    }

    public void reset()
    {
        setExecutingPhase(null);
        completedPhases.clear();
        setCurrentPhase(notInLifecyclePhase);
    }

    public boolean isPhaseComplete(String phaseName)
    {
        return completedPhases.contains(phaseName);
    }

    public void applyLifecycle(UMOManagementContext managementContext, Object object) throws UMOException
    {
        //String startingPhase = UMOLifecyclePhase.PHASE_NAME;
        UMOLifecyclePhase lcp;
        String phase;
        Integer phaseIndex;
        for (Iterator iterator = completedPhases.iterator(); iterator.hasNext();)
        {
            phase = (String) iterator.next();
            phaseIndex = (Integer) index.get(phase);
            lcp = (UMOLifecyclePhase) lifecycles.get(phaseIndex.intValue());
            lcp.applyLifecycle(object);
            //startingPhase = phase;
        }
        //If we're currently in a phase, fire that too
        if (getExecutingPhase() != null)
        {
            phaseIndex = (Integer) index.get(getExecutingPhase());
            lcp = (UMOLifecyclePhase) lifecycles.get(phaseIndex.intValue());
            lcp.applyLifecycle(object);
        }
    }

    public void checkPhase(String name) throws IllegalStateException
    {
        if (completedPhases.contains(name))
        {
            throw new IllegalStateException("Phase '" + name + "' has already been executed");
        }

        if (name.equalsIgnoreCase(executingPhase))
        {
            throw new IllegalStateException("Phase '" + name + "' is already currently being executed");
        }

        if (executingPhase != null)
        {
            throw new IllegalStateException("Currently executing lifecycle phase: " + executingPhase);
        }

        Integer phaseIndex = (Integer) index.get(name);
        if (phaseIndex == null)
        {
            throw new IllegalStateException("Phase does not exist: " + name);
        }
        if (NotInLifecyclePhase.PHASE_NAME.equals(currentPhase))
        {
            if (phaseIndex.intValue() > 0)
            {
                throw new IllegalStateException("The first lifecycle phase has to be calle before the '" + name + "' phase");
            }
        }
        else
        {
            UMOLifecyclePhase phase = (UMOLifecyclePhase) lifecycles.get(phaseIndex.intValue());
            if (!phase.isPhaseSupported(currentPhase))
            {
                throw new IllegalStateException("Lifecycle phase: " + currentPhase + " does not support current phase: "
                        + name + ". Phases supported are: " + StringMessageUtils.toString(phase.getSupportedPhases()));
            }
        }
    }
}
