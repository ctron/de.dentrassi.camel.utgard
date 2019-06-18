/*
 * Copyright (C) 2018 Red Hat Inc
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package de.dentrassi.camel.utgard;

public class ConnectionKey {
    private String clsid;
    private String domain;
    private String host;
    private String password;
    private int reconnectDelay = 5_000;
    private int refreshRate = 1_000;
    private int pushRate = 1_000;
    private int connectTimeout = 10_000;
    private String user;

    public void setClsid(final String clsid) {
        this.clsid = clsid;
    }

    public void setDomain(final String domain) {
        this.domain = domain;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setReconnectDelay(final int reconnectDelay) {
        this.reconnectDelay = reconnectDelay;
    }

    public void setRefreshRate(final int refreshRate) {
        this.refreshRate = refreshRate;
    }

    public void setPushRate(final int pushRate) {
        this.pushRate = pushRate;
    }

    public void setConnectTimeout(final int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public String getClsid() {
        return this.clsid;
    }

    public String getDomain() {
        return this.domain;
    }

    public String getHost() {
        return this.host;
    }

    public String getPassword() {
        return this.password;
    }

    public int getReconnectDelay() {
        return this.reconnectDelay;
    }

    public int getRefreshRate() {
        return this.refreshRate;
    }

    public int getPushRate() {
        return this.pushRate;
    }

    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    public String getUser() {
        return this.user;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.clsid == null ? 0 : this.clsid.hashCode());
        result = prime * result + (this.domain == null ? 0 : this.domain.hashCode());
        result = prime * result + (this.host == null ? 0 : this.host.hashCode());
        result = prime * result + (this.password == null ? 0 : this.password.hashCode());
        result = prime * result + this.reconnectDelay;
        result = prime * result + this.refreshRate;
        result = prime * result + this.pushRate;
        result = prime * result + this.connectTimeout;
        result = prime * result + (this.user == null ? 0 : this.user.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ConnectionKey other = (ConnectionKey) obj;
        if (this.clsid == null) {
            if (other.clsid != null) {
                return false;
            }
        } else if (!this.clsid.equals(other.clsid)) {
            return false;
        }
        if (this.domain == null) {
            if (other.domain != null) {
                return false;
            }
        } else if (!this.domain.equals(other.domain)) {
            return false;
        }
        if (this.host == null) {
            if (other.host != null) {
                return false;
            }
        } else if (!this.host.equals(other.host)) {
            return false;
        }
        if (this.password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!this.password.equals(other.password)) {
            return false;
        }
        if (this.reconnectDelay != other.reconnectDelay) {
            return false;
        }
        if (this.refreshRate != other.refreshRate) {
            return false;
        }
        if (this.pushRate != other.pushRate) {
            return false;
        }
        if (this.connectTimeout != other.connectTimeout) {
            return false;
        }
        if (this.user == null) {
            if (other.user != null) {
                return false;
            }
        } else if (!this.user.equals(other.user)) {
            return false;
        }
        return true;
    }

}
