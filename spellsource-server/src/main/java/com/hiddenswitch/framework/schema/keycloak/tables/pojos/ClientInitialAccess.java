/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.pojos;


import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IClientInitialAccess;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ClientInitialAccess implements VertxPojo, IClientInitialAccess {

    private static final long serialVersionUID = 1L;

    private String id;
    private String realmId;
    private Integer timestamp;
    private Integer expiration;
    private Integer count;
    private Integer remainingCount;

    public ClientInitialAccess() {}

    public ClientInitialAccess(IClientInitialAccess value) {
        this.id = value.getId();
        this.realmId = value.getRealmId();
        this.timestamp = value.getTimestamp();
        this.expiration = value.getExpiration();
        this.count = value.getCount();
        this.remainingCount = value.getRemainingCount();
    }

    public ClientInitialAccess(
        String id,
        String realmId,
        Integer timestamp,
        Integer expiration,
        Integer count,
        Integer remainingCount
    ) {
        this.id = id;
        this.realmId = realmId;
        this.timestamp = timestamp;
        this.expiration = expiration;
        this.count = count;
        this.remainingCount = remainingCount;
    }

        public ClientInitialAccess(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }

    /**
     * Getter for <code>keycloak.client_initial_access.id</code>.
     */
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Setter for <code>keycloak.client_initial_access.id</code>.
     */
    @Override
    public ClientInitialAccess setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Getter for <code>keycloak.client_initial_access.realm_id</code>.
     */
    @Override
    public String getRealmId() {
        return this.realmId;
    }

    /**
     * Setter for <code>keycloak.client_initial_access.realm_id</code>.
     */
    @Override
    public ClientInitialAccess setRealmId(String realmId) {
        this.realmId = realmId;
        return this;
    }

    /**
     * Getter for <code>keycloak.client_initial_access.timestamp</code>.
     */
    @Override
    public Integer getTimestamp() {
        return this.timestamp;
    }

    /**
     * Setter for <code>keycloak.client_initial_access.timestamp</code>.
     */
    @Override
    public ClientInitialAccess setTimestamp(Integer timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Getter for <code>keycloak.client_initial_access.expiration</code>.
     */
    @Override
    public Integer getExpiration() {
        return this.expiration;
    }

    /**
     * Setter for <code>keycloak.client_initial_access.expiration</code>.
     */
    @Override
    public ClientInitialAccess setExpiration(Integer expiration) {
        this.expiration = expiration;
        return this;
    }

    /**
     * Getter for <code>keycloak.client_initial_access.count</code>.
     */
    @Override
    public Integer getCount() {
        return this.count;
    }

    /**
     * Setter for <code>keycloak.client_initial_access.count</code>.
     */
    @Override
    public ClientInitialAccess setCount(Integer count) {
        this.count = count;
        return this;
    }

    /**
     * Getter for <code>keycloak.client_initial_access.remaining_count</code>.
     */
    @Override
    public Integer getRemainingCount() {
        return this.remainingCount;
    }

    /**
     * Setter for <code>keycloak.client_initial_access.remaining_count</code>.
     */
    @Override
    public ClientInitialAccess setRemainingCount(Integer remainingCount) {
        this.remainingCount = remainingCount;
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
        final ClientInitialAccess other = (ClientInitialAccess) obj;
        if (this.id == null) {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.realmId == null) {
            if (other.realmId != null)
                return false;
        }
        else if (!this.realmId.equals(other.realmId))
            return false;
        if (this.timestamp == null) {
            if (other.timestamp != null)
                return false;
        }
        else if (!this.timestamp.equals(other.timestamp))
            return false;
        if (this.expiration == null) {
            if (other.expiration != null)
                return false;
        }
        else if (!this.expiration.equals(other.expiration))
            return false;
        if (this.count == null) {
            if (other.count != null)
                return false;
        }
        else if (!this.count.equals(other.count))
            return false;
        if (this.remainingCount == null) {
            if (other.remainingCount != null)
                return false;
        }
        else if (!this.remainingCount.equals(other.remainingCount))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.realmId == null) ? 0 : this.realmId.hashCode());
        result = prime * result + ((this.timestamp == null) ? 0 : this.timestamp.hashCode());
        result = prime * result + ((this.expiration == null) ? 0 : this.expiration.hashCode());
        result = prime * result + ((this.count == null) ? 0 : this.count.hashCode());
        result = prime * result + ((this.remainingCount == null) ? 0 : this.remainingCount.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ClientInitialAccess (");

        sb.append(id);
        sb.append(", ").append(realmId);
        sb.append(", ").append(timestamp);
        sb.append(", ").append(expiration);
        sb.append(", ").append(count);
        sb.append(", ").append(remainingCount);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IClientInitialAccess from) {
        setId(from.getId());
        setRealmId(from.getRealmId());
        setTimestamp(from.getTimestamp());
        setExpiration(from.getExpiration());
        setCount(from.getCount());
        setRemainingCount(from.getRemainingCount());
    }

    @Override
    public <E extends IClientInitialAccess> E into(E into) {
        into.from(this);
        return into;
    }
}
