/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006-2011 TH4 SYSTEMS GmbH (http://th4-systems.com)
 * Copyright (C) 2018 Red Hat Inc
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package de.dentrassi.camel.utgard.openscada.job.opcda;

import java.util.Set;

import org.openscada.opc.dcom.common.ResultSet;
import org.openscada.opc.dcom.da.impl.OPCItemMgt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.camel.utgard.openscada.job.JobResult;
import de.dentrassi.camel.utgard.openscada.job.ThreadJob;

/**
 * This job removes items from an opc group
 *
 * @author Jens Reimann &lt;jens.reimann@th4-systems.com&gt;
 *
 */
public class UnrealizeItemsJob extends ThreadJob implements JobResult<ResultSet<Integer>> {
    public static final long DEFAULT_TIMEOUT = 5000L;

    private final static Logger logger = LoggerFactory.getLogger(UnrealizeItemsJob.class);

    private final OPCItemMgt itemMgt;

    private final Set<Integer> serverHandles;

    private ResultSet<Integer> result;

    public UnrealizeItemsJob(final long timeout, final OPCItemMgt itemMgt, final Set<Integer> serverHandles) {
        super(timeout);
        this.itemMgt = itemMgt;
        this.serverHandles = serverHandles;
    }

    @Override
    protected void perform() throws Exception {
        logger.info("UnRealizing items: {}", new Object[] { this.serverHandles });
        this.result = this.itemMgt.remove(this.serverHandles.toArray(new Integer[this.serverHandles.size()]));
    }

    @Override
    public ResultSet<Integer> getResult() {
        return this.result;
    }
}
