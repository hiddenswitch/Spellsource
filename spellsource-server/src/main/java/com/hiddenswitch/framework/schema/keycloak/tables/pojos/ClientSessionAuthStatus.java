/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.pojos;


import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IClientSessionAuthStatus;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ClientSessionAuthStatus implements VertxPojo, IClientSessionAuthStatus {

    private static final long serialVersionUID = 1L;

    private String authenticator;
    private Integer status;
    private String clientSession;

    public ClientSessionAuthStatus() {}

    public ClientSessionAuthStatus(IClientSessionAuthStatus value) {
        this.authenticator = value.getAuthenticator();
        this.status = value.getStatus();
        this.clientSession = value.getClientSession();
    }

    public ClientSessionAuthStatus(
        String authenticator,
        Integer status,
        String clientSession
    ) {
        this.authenticator = authenticator;
        this.status = status;
        this.clientSession = clientSession;
    }

        public ClientSessionAuthStatus(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }

    /**
     * Getter for
     * <code>keycloak.client_session_auth_status.authenticator</code>.
     */
    @Override
    public String getAuthenticator() {
        return this.authenticator;
    }

    /**
     * Setter for
     * <code>keycloak.client_session_auth_status.authenticator</code>.
     */
    @Override
    public ClientSessionAuthStatus setAuthenticator(String authenticator) {
        this.authenticator = authenticator;
        return this;
    }

    /**
     * Getter for <code>keycloak.client_session_auth_status.status</code>.
     */
    @Override
    public Integer getStatus() {
        return this.status;
    }

    /**
     * Setter for <code>keycloak.client_session_auth_status.status</code>.
     */
    @Override
    public ClientSessionAuthStatus setStatus(Integer status) {
        this.status = status;
        return this;
    }

    /**
     * Getter for
     * <code>keycloak.client_session_auth_status.client_session</code>.
     */
    @Override
    public String getClientSession() {
        return this.clientSession;
    }

    /**
     * Setter for
     * <code>keycloak.client_session_auth_status.client_session</code>.
     */
    @Override
    public ClientSessionAuthStatus setClientSession(String clientSession) {
        this.clientSession = clientSession;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ClientSessionAuthStatus other = (ClientSessionAuthStatus) obj;
        if (this.authenticator == null) {
            if (other.authenticator != null)
                return false;
        }
        else if (!this.authenticator.equals(other.authenticator))
            return false;
        if (this.status == null) {
            if (other.status != null)
                return false;
        }
        else if (!this.status.equals(other.status))
            return false;
        if (this.clientSession == null) {
            if (other.clientSession != null)
                return false;
        }
        else if (!this.clientSession.equals(other.clientSession))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.authenticator == null) ? 0 : this.authenticator.hashCode());
        result = prime * result + ((this.status == null) ? 0 : this.status.hashCode());
        result = prime * result + ((this.clientSession == null) ? 0 : this.clientSession.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ClientSessionAuthStatus (");

        sb.append(authenticator);
        sb.append(", ").append(status);
        sb.append(", ").append(clientSession);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IClientSessionAuthStatus from) {
        setAuthenticator(from.getAuthenticator());
        setStatus(from.getStatus());
        setClientSession(from.getClientSession());
    }

    @Override
    public <E extends IClientSessionAuthStatus> E into(E into) {
        into.from(this);
        return into;
    }
}
