/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.transport.quartz.jobs;

import org.mule.transport.quartz.config.AbstractJobConfig;

import org.quartz.Job;

/**
 * This configuration simply holds a reference to a user defined job to execute.
 */
public class CustomJobConfig extends AbstractJobConfig
{
    private Job job;

    public Job getJob()
    {
        return job;
    }

    public void setJob(Job job)
    {
        this.job = job;
    }

    public Class getJobClass()
    {
        return CustomJob.class;
    }

}