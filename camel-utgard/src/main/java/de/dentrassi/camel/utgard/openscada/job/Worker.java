/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006-2011 TH4 SYSTEMS GmbH (http://th4-systems.com)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package de.dentrassi.camel.utgard.openscada.job;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The worker take control of a work unit and executes it including guarding it
 * runtime.
 *
 * @author Jens Reimann &lt;jens.reimann@th4-systems.com&gt;
 *
 */
public class Worker implements GuardianHandler {

    private final static Logger logger = LoggerFactory.getLogger(Worker.class);

    private volatile WorkUnit currentWorkUnit;

    private final Guardian guardian;

    private final Thread guardianThread;

    protected static class OPCResultJobHandler<T> implements JobHandler {
        private Throwable error;

        private T result;

        private final JobResult<T> jobResult;

        public OPCResultJobHandler(final JobResult<T> jobResult) {
            this.jobResult = jobResult;
        }

        @Override
        public void handleFailure(final Throwable e) {
            this.error = e;
        }

        @Override
        public void handleInterrupted() {
            this.error = new InterruptedException("Job got interrupted");
            this.error.fillInStackTrace();
        }

        @Override
        public void handleSuccess() {
            // FIXME: isn't there a check for null necessary?
            this.result = this.jobResult.getResult();
        }

        public Throwable getError() {
            return this.error;
        }

        public T getResult() {
            return this.result;
        }

    }

    protected static class OPCRunnableJobHandler implements JobHandler {

        private Throwable error;

        private final Runnable runnable;

        public OPCRunnableJobHandler(final Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void handleFailure(final Throwable e) {
            this.error = e;
        }

        @Override
        public void handleInterrupted() {
            this.error = new InterruptedException("Job got interrupted");
            this.error.fillInStackTrace();
        }

        @Override
        public void handleSuccess() {
            this.runnable.run();
        }

        public Throwable getError() {
            return this.error;
        }

    }

    public Worker() {
        this.guardian = new Guardian();

        synchronized (this.guardian) {
            this.guardianThread = new Thread(this.guardian, "OPCGuardian");
            this.guardianThread.setDaemon(true);
            this.guardianThread.start();

            try {
                logger.info("Waiting for guardian...");
                this.guardian.wait();
                logger.info("Guardian is up...");
            } catch (final InterruptedException e) {
                throw new RuntimeException("Failed to initialize OPC guardian", e);
            }
        }
    }

    public void close() {
        this.guardian.shutdown();
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public <T> T execute(final Job job, final JobResult<T> result) throws InvocationTargetException {
        final OPCResultJobHandler<T> handler = new OPCResultJobHandler<>(result);
        final WorkUnit workUnit = new WorkUnit(job, handler);
        execute(workUnit);
        if (handler.getError() != null) {
            throw new InvocationTargetException(handler.getError(), "Failed to call DCOM method");
        }
        return handler.getResult();
    }

    /**
     * Execute the job and run the runnable if the job was completed without error
     *
     * @param job
     *            the job to run
     * @param runnable
     *            the runnable to run in the case of no error
     * @throws InvocationTargetException
     *             an exception if something went wrong
     */
    public void execute(final Job job, final Runnable runnable) throws InvocationTargetException {
        final OPCRunnableJobHandler handler = new OPCRunnableJobHandler(runnable);
        final WorkUnit workUnit = new WorkUnit(job, handler);
        execute(workUnit);
        if (handler.getError() != null) {
            throw new InvocationTargetException(handler.getError(), "Failed to call DCOM method");
        }
    }

    public void execute(final WorkUnit currentWorkUnit) {
        if (currentWorkUnit == null) {
            throw new RuntimeException("Work unit must not be null");
        }
        if (currentWorkUnit.getJob() == null) {
            throw new RuntimeException("Job must be set");
        }
        if (currentWorkUnit.getJobHandler() == null) {
            throw new RuntimeException("Job handler must be set");
        }

        synchronized (this) {
            if (this.currentWorkUnit != null) {
                throw new RuntimeException("Already running");
            }
            this.currentWorkUnit = currentWorkUnit;
        }

        perform();
    }

    protected Throwable performCancelable() {
        try {
            logger.debug("Start guardian");
            this.guardian.startJob(this.currentWorkUnit.getJob(), this);
            logger.debug("Run job");
            this.currentWorkUnit.getJob().run();
            logger.debug("Run job finished");
        } catch (final Throwable e) {
            logger.warn("Job failed", e);
            return e;
        } finally {
            logger.debug("Notify guardian that job is complete");
            this.guardian.jobCompleted();
            logger.debug("guardian knows now");
        }
        return null;
    }

    protected void perform() {
        try {
            logger.debug("Starting new job");
            performCancelable();
            logger.debug("Job completed");
        } catch (final Throwable e) {
            logger.warn("Failed to process", e);
        }

        // now trigger the result handlers
        if (this.currentWorkUnit.getJob().isCanceled()) {
            // we got canceled
            this.currentWorkUnit.getJobHandler().handleInterrupted();
        } else if (this.currentWorkUnit.getJob().getError() == null) {
            // we succeeded
            this.currentWorkUnit.getJobHandler().handleSuccess();
        } else {
            // we failed
            this.currentWorkUnit.getJobHandler().handleFailure(this.currentWorkUnit.getJob().getError());
        }

        // we are clear again
        this.currentWorkUnit = null;
    }

    @Override
    public void performCancel() {
        final WorkUnit workUnit = this.currentWorkUnit;
        if (workUnit != null) {
            workUnit.getJob().interrupt();
        }
    }

}
