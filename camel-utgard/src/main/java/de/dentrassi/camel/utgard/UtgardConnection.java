/*
 * Copyright (C) 2018 Red Hat Inc
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package de.dentrassi.camel.utgard;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.openscada.opc.dcom.common.KeyedResult;
import org.openscada.opc.dcom.common.KeyedResultSet;
import org.openscada.opc.dcom.da.OPCDATASOURCE;
import org.openscada.opc.dcom.da.OPCITEMDEF;
import org.openscada.opc.dcom.da.OPCITEMRESULT;
import org.openscada.opc.dcom.da.OPCITEMSTATE;
import org.openscada.opc.lib.common.ConnectionInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.camel.utgard.openscada.job.Worker;
import de.dentrassi.camel.utgard.openscada.job.opcda.ConnectJob;
import de.dentrassi.camel.utgard.openscada.job.opcda.RealizeItemsJob;
import de.dentrassi.camel.utgard.openscada.job.opcda.SyncReadJob;
import de.dentrassi.camel.utgard.openscada.job.opcda.UnrealizeItemsJob;
import io.glutamate.lang.ThrowingConsumer;

public class UtgardConnection {

    private static final Logger logger = LoggerFactory.getLogger(UtgardConnection.class);

    private static final AtomicLong COUNTER = new AtomicLong();

    private final Worker worker;
    private final ConnectionKey connectionKey;

    private final ScheduledExecutorService executor;

    private boolean connected;

    private final ConnectionInformation connectionInformation;

    private ConnectJob connection;

    private final Set<String> adding = new HashSet<>();
    private final Set<String> removing = new HashSet<>();

    private final Map<String, Integer> serverHandles = new HashMap<>();
    private final Map<Integer, String> serverHandlesMap = new HashMap<>();
    private Integer[] serverHandleArray = null;
    private final Map<String, ThrowingConsumer<OPCITEMSTATE>> consumers = new HashMap<>();

    private int lastClientId;

    public UtgardConnection(final ConnectionKey connectionKey) {
        this.connectionKey = connectionKey;
        this.worker = new Worker();

        this.connectionInformation = new ConnectionInformation();
        this.connectionInformation.setDomain(connectionKey.getDomain());
        this.connectionInformation.setUser(connectionKey.getUser());
        this.connectionInformation.setHost(connectionKey.getHost());
        this.connectionInformation.setPassword(connectionKey.getPassword());
        this.connectionInformation.setClsid(connectionKey.getClsid());

        this.executor = Executors
                .newSingleThreadScheduledExecutor(r -> new Thread(r, "OPCExecutor/" + COUNTER.getAndIncrement()));

        this.executor.scheduleAtFixedRate(this::runOnce, 0, connectionKey.getPushRate(), TimeUnit.MILLISECONDS);
    }

    public void close() {
        this.executor.shutdown();
        this.worker.close();
    }

    protected void runOnce() {

        try {
            if (!this.connected) {
                performConnect();
            }

            if (!this.connected) {
                return;
            }

            final Set<String> toAdd = new HashSet<>();
            final Set<String> toRemove = new HashSet<>();

            synchronized (this) {
                toAdd.addAll(this.adding);
                toRemove.addAll(this.removing);
            }

            performItems(toAdd, toRemove);

            if (!this.consumers.isEmpty()) {
                performRead();
            }

        } catch (final Exception e) {
            logger.warn("Failed to runOnce", e);
            handleDisconnected();
        }

    }

    private void performItems(final Set<String> toAdd, final Set<String> toRemove)
            throws InvocationTargetException {

        if (!toRemove.isEmpty()) {

            logger.debug("Removing items: {}", toRemove);

            // unsubscribe old

            final Set<Integer> removeHandles = new HashSet<>();
            for (final String itemId : toRemove) {
                final Integer id = this.serverHandles.remove(itemId);
                if (id != null) {
                    removeHandles.add(id);
                }
            }

            final UnrealizeItemsJob job = new UnrealizeItemsJob(
                    getTimeout(),
                    this.connection.getItemMgt(),
                    removeHandles);

            this.worker.execute(job, () -> {

                toRemove.forEach(this.serverHandles::remove);
                removeHandles.forEach(this.serverHandlesMap::remove);

            });
        }

        if (!toAdd.isEmpty()) {

            logger.debug("Adding items: {}", toAdd);

            // subscribe new

            final OPCITEMDEF[] itemDefs = new OPCITEMDEF[toAdd.size()];
            int i = 0;

            for (final String itemId : toAdd) {
                final OPCITEMDEF def = new OPCITEMDEF();

                def.setActive(true);
                def.setClientHandle(this.lastClientId++);
                def.setItemID(itemId);

                itemDefs[i] = def;
                i++;
            }

            final RealizeItemsJob job = new RealizeItemsJob(getTimeout(), this.connection.getItemMgt(), itemDefs);
            this.worker.execute(job, () -> {
            });

            final KeyedResultSet<OPCITEMDEF, OPCITEMRESULT> result = job.getResult();
            for (final KeyedResult<OPCITEMDEF, OPCITEMRESULT> entry : result) {
                final String itemId = entry.getKey().getItemID();
                if (entry.getErrorCode() != 0) {
                    // error
                    handleSubscriptionError(itemId);
                } else {
                    this.serverHandles.put(itemId, entry.getValue().getServerHandle());
                    this.serverHandlesMap.put(entry.getValue().getServerHandle(), itemId);
                }
            }
        }

        if (!toAdd.isEmpty() || !toRemove.isEmpty()) {
            logger.info("updating server array");
            this.serverHandleArray = createServerHandleArray();
        }

        this.adding.clear();
        this.removing.clear();
    }

    private Integer[] createServerHandleArray() {
        final int size = this.serverHandles.size();
        if (size <= 0) {
            return null;
        }
        return this.serverHandles.values().toArray(new Integer[this.serverHandles.size()]);
    }

    private void handleSubscriptionError(final String itemId) {
        // TODO Auto-generated method stub

    }

    private void performRead() throws Exception {

        if (this.serverHandleArray == null) {
            return;
        }

        final SyncReadJob job = new SyncReadJob(getReadTimeout(),
                this.connection.getSyncIo(),
                OPCDATASOURCE.OPC_DS_CACHE,
                this.serverHandleArray);

        this.worker.execute(job, () -> {
        });

        for (final KeyedResult<Integer, OPCITEMSTATE> entry : job.getResult()) {
            final int error = entry.getErrorCode();

            if (logger.isDebugEnabled()) {
                logger.debug("    {} - {} / {}", entry.getKey(), String.format("0x%08X", entry.getErrorCode()),
                        entry.getValue());
            }

            if (error != 0) {
                // FIXME: report error
            } else {
                final String itemId = this.serverHandlesMap.get(entry.getKey());

                if (itemId == null) {
                    continue;
                }

                handleItemChange(itemId, entry.getValue());
            }
        }

    }

    private void handleItemChange(final String itemId, final OPCITEMSTATE value) {
        final ThrowingConsumer<OPCITEMSTATE> consumer = this.consumers.get(itemId);
        if (consumer != null) {
            try {
                consumer.consume(value);
            } catch (final Exception e) {
                logger.warn("Consumer failed: " + itemId, e);
            }
        }
    }

    private synchronized void handleDisconnected() {
        this.lastClientId = 0;

        this.serverHandleArray = null;
        this.serverHandles.clear();

        this.connection = null;
        this.removing.clear();

        // add our existing subscriptions the next time

        this.adding.clear();
        this.adding.addAll(this.consumers.keySet());
    }

    private void performConnect() throws Exception {
        final ConnectJob job = new ConnectJob(getConnectTimeout(), this.connectionInformation, getConnectTimeout(),
                this.connectionKey.getRefreshRate());

        this.worker.execute(job, () -> {
            setConnection(job);
        });
    }

    private void setConnection(final ConnectJob job) {
        this.connection = job;
        this.connected = true;
    }

    private int getConnectTimeout() {
        return 10_000;
    }

    private int getTimeout() {
        return 5_000;
    }

    private int getReadTimeout() {
        return 1_000;
    }

    public synchronized void addItem(final String itemId, final ThrowingConsumer<OPCITEMSTATE> consumer) {
        this.adding.add(itemId);
        this.consumers.put(itemId, consumer);
    }

    public synchronized void removeItem(final String itemId) {
        this.adding.remove(itemId);
        this.removing.add(itemId);
        this.consumers.remove(itemId);
    }

}
