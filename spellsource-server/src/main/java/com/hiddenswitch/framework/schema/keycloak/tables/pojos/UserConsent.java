/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.pojos;


import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IUserConsent;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class UserConsent implements VertxPojo, IUserConsent {

    private static final long serialVersionUID = 1L;

    private String id;
    private String clientId;
    private String userId;
    private Long createdDate;
    private Long lastUpdatedDate;
    private String clientStorageProvider;
    private String externalClientId;

    public UserConsent() {}

    public UserConsent(IUserConsent value) {
        this.id = value.getId();
        this.clientId = value.getClientId();
        this.userId = value.getUserId();
        this.createdDate = value.getCreatedDate();
        this.lastUpdatedDate = value.getLastUpdatedDate();
        this.clientStorageProvider = value.getClientStorageProvider();
        this.externalClientId = value.getExternalClientId();
    }

    public UserConsent(
        String id,
        String clientId,
        String userId,
        Long createdDate,
        Long lastUpdatedDate,
        String clientStorageProvider,
        String externalClientId
    ) {
        this.id = id;
        this.clientId = clientId;
        this.userId = userId;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
        this.clientStorageProvider = clientStorageProvider;
        this.externalClientId = externalClientId;
    }

        public UserConsent(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }

    /**
     * Getter for <code>keycloak.user_consent.id</code>.
     */
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Setter for <code>keycloak.user_consent.id</code>.
     */
    @Override
    public UserConsent setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Getter for <code>keycloak.user_consent.client_id</code>.
     */
    @Override
    public String getClientId() {
        return this.clientId;
    }

    /**
     * Setter for <code>keycloak.user_consent.client_id</code>.
     */
    @Override
    public UserConsent setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Getter for <code>keycloak.user_consent.user_id</code>.
     */
    @Override
    public String getUserId() {
        return this.userId;
    }

    /**
     * Setter for <code>keycloak.user_consent.user_id</code>.
     */
    @Override
    public UserConsent setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Getter for <code>keycloak.user_consent.created_date</code>.
     */
    @Override
    public Long getCreatedDate() {
        return this.createdDate;
    }

    /**
     * Setter for <code>keycloak.user_consent.created_date</code>.
     */
    @Override
    public UserConsent setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    /**
     * Getter for <code>keycloak.user_consent.last_updated_date</code>.
     */
    @Override
    public Long getLastUpdatedDate() {
        return this.lastUpdatedDate;
    }

    /**
     * Setter for <code>keycloak.user_consent.last_updated_date</code>.
     */
    @Override
    public UserConsent setLastUpdatedDate(Long lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
        return this;
    }

    /**
     * Getter for <code>keycloak.user_consent.client_storage_provider</code>.
     */
    @Override
    public String getClientStorageProvider() {
        return this.clientStorageProvider;
    }

    /**
     * Setter for <code>keycloak.user_consent.client_storage_provider</code>.
     */
    @Override
    public UserConsent setClientStorageProvider(String clientStorageProvider) {
        this.clientStorageProvider = clientStorageProvider;
        return this;
    }

    /**
     * Getter for <code>keycloak.user_consent.external_client_id</code>.
     */
    @Override
    public String getExternalClientId() {
        return this.externalClientId;
    }

    /**
     * Setter for <code>keycloak.user_consent.external_client_id</code>.
     */
    @Override
    public UserConsent setExternalClientId(String externalClientId) {
        this.externalClientId = externalClientId;
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
        final UserConsent other = (UserConsent) obj;
        if (this.id == null) {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.clientId == null) {
            if (other.clientId != null)
                return false;
        }
        else if (!this.clientId.equals(other.clientId))
            return false;
        if (this.userId == null) {
            if (other.userId != null)
                return false;
        }
        else if (!this.userId.equals(other.userId))
            return false;
        if (this.createdDate == null) {
            if (other.createdDate != null)
                return false;
        }
        else if (!this.createdDate.equals(other.createdDate))
            return false;
        if (this.lastUpdatedDate == null) {
            if (other.lastUpdatedDate != null)
                return false;
        }
        else if (!this.lastUpdatedDate.equals(other.lastUpdatedDate))
            return false;
        if (this.clientStorageProvider == null) {
            if (other.clientStorageProvider != null)
                return false;
        }
        else if (!this.clientStorageProvider.equals(other.clientStorageProvider))
            return false;
        if (this.externalClientId == null) {
            if (other.externalClientId != null)
                return false;
        }
        else if (!this.externalClientId.equals(other.externalClientId))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.clientId == null) ? 0 : this.clientId.hashCode());
        result = prime * result + ((this.userId == null) ? 0 : this.userId.hashCode());
        result = prime * result + ((this.createdDate == null) ? 0 : this.createdDate.hashCode());
        result = prime * result + ((this.lastUpdatedDate == null) ? 0 : this.lastUpdatedDate.hashCode());
        result = prime * result + ((this.clientStorageProvider == null) ? 0 : this.clientStorageProvider.hashCode());
        result = prime * result + ((this.externalClientId == null) ? 0 : this.externalClientId.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("UserConsent (");

        sb.append(id);
        sb.append(", ").append(clientId);
        sb.append(", ").append(userId);
        sb.append(", ").append(createdDate);
        sb.append(", ").append(lastUpdatedDate);
        sb.append(", ").append(clientStorageProvider);
        sb.append(", ").append(externalClientId);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IUserConsent from) {
        setId(from.getId());
        setClientId(from.getClientId());
        setUserId(from.getUserId());
        setCreatedDate(from.getCreatedDate());
        setLastUpdatedDate(from.getLastUpdatedDate());
        setClientStorageProvider(from.getClientStorageProvider());
        setExternalClientId(from.getExternalClientId());
    }

    @Override
    public <E extends IUserConsent> E into(E into) {
        into.from(this);
        return into;
    }
}
