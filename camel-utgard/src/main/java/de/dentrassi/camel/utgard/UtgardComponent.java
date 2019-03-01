/*
 * Copyright (C) 2018, 2019 Red Hat Inc
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package de.dentrassi.camel.utgard;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

/**
 *
 * @author Jens Reimann
 *
 */
public class UtgardComponent extends DefaultComponent {

    private final Map<ConnectionKey, AtomicLong> connectionRefCounter = new HashMap<>();
    private final Map<ConnectionKey, UtgardConnection> connections = new HashMap<>();

    public UtgardComponent() {
        super();
    }

    public UtgardComponent(final CamelContext context) {
        super(context);
    }

    @Override
    protected Endpoint createEndpoint(final String uri, final String remaining, final Map<String, Object> parameters)
            throws Exception {

        final UtgardEndpoint endpoint = new UtgardEndpoint(uri, this);

        setProperties(endpoint, parameters);

        return endpoint;
    }

    public synchronized UtgardConnection acquireConnection(final ConnectionKey connectionKey) {
        UtgardConnection connection = this.connections.get(connectionKey);

        if (connection == null) {
            connection = new UtgardConnection(connectionKey);
            this.connections.put(connectionKey, connection);
        }

        AtomicLong ref = this.connectionRefCounter.get(connectionKey);
        if (ref == null) {
            ref = new AtomicLong();
            this.connectionRefCounter.put(connectionKey, ref);
        }
        ref.incrementAndGet();

        return connection;
    }

    public synchronized void releaseConnection(final ConnectionKey connectionKey) {

        // get ref counter

        final AtomicLong ref = this.connectionRefCounter.get(connectionKey);
        if (ref == null) {
            return;
        }

        // check number of users

        if (ref.decrementAndGet() > 0) {
            return;
        }

        // last connection ... close

        this.connectionRefCounter.remove(connectionKey);
        final UtgardConnection connection = this.connections.remove(connectionKey);
        if (connection != null) {
            connection.close();
        }
    }

}
