/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006-2010 TH4 SYSTEMS GmbH (http://th4-systems.com)
 * Copyright (C) 2018 Red Hat Inc
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package de.dentrassi.camel.utgard.openscada.job.opcda;

import org.openscada.opc.dcom.common.impl.OPCCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.camel.utgard.openscada.job.JobResult;
import de.dentrassi.camel.utgard.openscada.job.ThreadJob;

/**
 * This job resolves an error string
 *
 * @author Jens Reimann &lt;jens.reimann@th4-systems.com&gt;
 *
 */
public class ErrorMessageJob extends ThreadJob implements JobResult<String> {
    public static final long DEFAULT_TIMEOUT = 1000L;

    private final static Logger logger = LoggerFactory.getLogger(ErrorMessageJob.class);

    private final OPCCommon common;

    private String result;

    private final int errorCode;

    public ErrorMessageJob(final long timeout, final OPCCommon common, final int errorCode) {
        super(timeout);
        this.common = common;
        this.errorCode = errorCode;
    }

    @Override
    protected void perform() throws Exception {
        logger.debug("Request error message: {}", this.errorCode);
        this.result = this.common.getErrorString(this.errorCode, 0);
    }

    @Override
    public String getResult() {
        return this.result;
    }
}
