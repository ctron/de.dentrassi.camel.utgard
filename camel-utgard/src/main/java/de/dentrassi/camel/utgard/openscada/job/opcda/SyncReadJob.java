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

import org.openscada.opc.dcom.common.KeyedResultSet;
import org.openscada.opc.dcom.da.OPCDATASOURCE;
import org.openscada.opc.dcom.da.OPCITEMSTATE;
import org.openscada.opc.dcom.da.impl.OPCSyncIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.camel.utgard.openscada.job.JobResult;
import de.dentrassi.camel.utgard.openscada.job.ThreadJob;

/**
 * This job performs a sync read operation
 *
 * @author Jens Reimann &lt;jens.reimann@th4-systems.com&gt;
 *
 */
public class SyncReadJob extends ThreadJob implements JobResult<KeyedResultSet<Integer, OPCITEMSTATE>> {
    public static final long DEFAULT_TIMEOUT = 5000L;

    private final static Logger logger = LoggerFactory.getLogger(SyncReadJob.class);

    private final OPCSyncIO syncIo;

    private final Integer[] serverHandles;

    private final OPCDATASOURCE dataSource;

    private KeyedResultSet<Integer, OPCITEMSTATE> result;

    public SyncReadJob(final long timeout, final OPCSyncIO syncIo, final OPCDATASOURCE dataSource,
            final Integer[] serverHandles) {
        super(timeout);
        this.syncIo = syncIo;
        this.serverHandles = serverHandles;
        this.dataSource = dataSource;
    }

    @Override
    protected void perform() throws Exception {
        logger.debug("Sync read job");
        this.result = this.syncIo.read(this.dataSource, this.serverHandles);
    }

    @Override
    public KeyedResultSet<Integer, OPCITEMSTATE> getResult() {
        return this.result;
    }
}
