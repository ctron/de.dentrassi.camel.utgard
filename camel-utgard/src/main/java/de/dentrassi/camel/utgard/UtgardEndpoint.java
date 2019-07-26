/*
 * Copyright (C) 2018 Red Hat Inc
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package de.dentrassi.camel.utgard;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;

/**
 *
 * @author Jens Reimann
 *
 */
public class UtgardEndpoint extends DefaultEndpoint {

    public UtgardEndpoint(final String endpointUri, final UtgardComponent component) throws URISyntaxException {
        super(endpointUri, component);
        this.host = new URI(endpointUri).getHost();
    }

    @Override
    public UtgardComponent getComponent() {
        return (UtgardComponent) super.getComponent();
    }

    @UriPath
    @Metadata(required = "true")
    private String host;

    public void setHost(final String host) {
        this.host = host;
    }

    @UriParam
    @Metadata(required = "true")
    private String classId;

    public void setClassId(final String classId) {
        this.classId = classId;
    }

    @UriParam
    @Metadata(required = "true")
    private String domain;

    public void setDomain(final String domain) {
        this.domain = domain;
    }

    @UriParam
    @Metadata(required = "true")
    private String user;

    public void setUser(final String user) {
        this.user = user;
    }

    @UriParam
    @Metadata(required = "true")
    private String password;

    public void setPassword(final String password) {
        this.password = password;
    }

    @UriParam
    private int connectTimeout = 10_000;

    public void setConnectTimeout(final int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @UriParam
    private int refreshRate = 1_000;

    public void setRefreshRate(final int refreshRate) {
        this.refreshRate = refreshRate;
    }

    @UriParam
    @Metadata(required = "true")
    private String itemId;

    public void setItemId(final String itemId) {
        this.itemId = itemId;
    }

    public String getItemId() {
        return this.itemId;
    }

    private UtgardConnection connection;
    private ConnectionKey connectionKey;

    @Override
    public Producer createProducer() throws Exception {
        throw new RuntimeCamelException("Cannot provider Utgard producer: " + getEndpointUri());
    }

    private ConnectionKey createConnectionKey() {

        final ConnectionKey key = new ConnectionKey();

        key.setClsid(this.classId);
        key.setDomain(this.domain == null ? "" : this.domain);
        key.setHost(this.host);
        key.setPassword(this.password);
        key.setConnectTimeout(this.connectTimeout);
        key.setRefreshRate(this.refreshRate);
        key.setUser(this.user);

        return key;
    }

    @Override
    public Consumer createConsumer(final Processor processor) throws Exception {
        return new UtgardConsumer(this, processor);
    }

    UtgardConnection getConnection() {
        return this.connection;
    }

    @Override
    protected void doStart() throws Exception {
        this.connectionKey = createConnectionKey();
        this.connection = getComponent().acquireConnection(this.connectionKey);
        super.doStart();
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();

        if (this.connection != null) {
            getComponent().releaseConnection(this.connectionKey);
            this.connection = null;
        }

    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
