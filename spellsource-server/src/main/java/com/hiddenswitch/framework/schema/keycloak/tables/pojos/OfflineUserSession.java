/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.pojos;


import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IOfflineUserSession;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OfflineUserSession implements VertxPojo, IOfflineUserSession {

    private static final long serialVersionUID = 1L;

    private String userSessionId;
    private String userId;
    private String realmId;
    private Integer createdOn;
    private String offlineFlag;
    private String data;
    private Integer lastSessionRefresh;

    public OfflineUserSession() {}

    public OfflineUserSession(IOfflineUserSession value) {
        this.userSessionId = value.getUserSessionId();
        this.userId = value.getUserId();
        this.realmId = value.getRealmId();
        this.createdOn = value.getCreatedOn();
        this.offlineFlag = value.getOfflineFlag();
        this.data = value.getData();
        this.lastSessionRefresh = value.getLastSessionRefresh();
    }

    public OfflineUserSession(
        String userSessionId,
        String userId,
        String realmId,
        Integer createdOn,
        String offlineFlag,
        String data,
        Integer lastSessionRefresh
    ) {
        this.userSessionId = userSessionId;
        this.userId = userId;
        this.realmId = realmId;
        this.createdOn = createdOn;
        this.offlineFlag = offlineFlag;
        this.data = data;
        this.lastSessionRefresh = lastSessionRefresh;
    }

        public OfflineUserSession(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }

    /**
     * Getter for <code>keycloak.offline_user_session.user_session_id</code>.
     */
    @Override
    public String getUserSessionId() {
        return this.userSessionId;
    }

    /**
     * Setter for <code>keycloak.offline_user_session.user_session_id</code>.
     */
    @Override
    public OfflineUserSession setUserSessionId(String userSessionId) {
        this.userSessionId = userSessionId;
        return this;
    }

    /**
     * Getter for <code>keycloak.offline_user_session.user_id</code>.
     */
    @Override
    public String getUserId() {
        return this.userId;
    }

    /**
     * Setter for <code>keycloak.offline_user_session.user_id</code>.
     */
    @Override
    public OfflineUserSession setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Getter for <code>keycloak.offline_user_session.realm_id</code>.
     */
    @Override
    public String getRealmId() {
        return this.realmId;
    }

    /**
     * Setter for <code>keycloak.offline_user_session.realm_id</code>.
     */
    @Override
    public OfflineUserSession setRealmId(String realmId) {
        this.realmId = realmId;
        return this;
    }

    /**
     * Getter for <code>keycloak.offline_user_session.created_on</code>.
     */
    @Override
    public Integer getCreatedOn() {
        return this.createdOn;
    }

    /**
     * Setter for <code>keycloak.offline_user_session.created_on</code>.
     */
    @Override
    public OfflineUserSession setCreatedOn(Integer createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    /**
     * Getter for <code>keycloak.offline_user_session.offline_flag</code>.
     */
    @Override
    public String getOfflineFlag() {
        return this.offlineFlag;
    }

    /**
     * Setter for <code>keycloak.offline_user_session.offline_flag</code>.
     */
    @Override
    public OfflineUserSession setOfflineFlag(String offlineFlag) {
        this.offlineFlag = offlineFlag;
        return this;
    }

    /**
     * Getter for <code>keycloak.offline_user_session.data</code>.
     */
    @Override
    public String getData() {
        return this.data;
    }

    /**
     * Setter for <code>keycloak.offline_user_session.data</code>.
     */
    @Override
    public OfflineUserSession setData(String data) {
        this.data = data;
        return this;
    }

    /**
     * Getter for
     * <code>keycloak.offline_user_session.last_session_refresh</code>.
     */
    @Override
    public Integer getLastSessionRefresh() {
        return this.lastSessionRefresh;
    }

    /**
     * Setter for
     * <code>keycloak.offline_user_session.last_session_refresh</code>.
     */
    @Override
    public OfflineUserSession setLastSessionRefresh(Integer lastSessionRefresh) {
        this.lastSessionRefresh = lastSessionRefresh;
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
        final OfflineUserSession other = (OfflineUserSession) obj;
        if (this.userSessionId == null) {
            if (other.userSessionId != null)
                return false;
        }
        else if (!this.userSessionId.equals(other.userSessionId))
            return false;
        if (this.userId == null) {
            if (other.userId != null)
                return false;
        }
        else if (!this.userId.equals(other.userId))
            return false;
        if (this.realmId == null) {
            if (other.realmId != null)
                return false;
        }
        else if (!this.realmId.equals(other.realmId))
            return false;
        if (this.createdOn == null) {
            if (other.createdOn != null)
                return false;
        }
        else if (!this.createdOn.equals(other.createdOn))
            return false;
        if (this.offlineFlag == null) {
            if (other.offlineFlag != null)
                return false;
        }
        else if (!this.offlineFlag.equals(other.offlineFlag))
            return false;
        if (this.data == null) {
            if (other.data != null)
                return false;
        }
        else if (!this.data.equals(other.data))
            return false;
        if (this.lastSessionRefresh == null) {
            if (other.lastSessionRefresh != null)
                return false;
        }
        else if (!this.lastSessionRefresh.equals(other.lastSessionRefresh))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.userSessionId == null) ? 0 : this.userSessionId.hashCode());
        result = prime * result + ((this.userId == null) ? 0 : this.userId.hashCode());
        result = prime * result + ((this.realmId == null) ? 0 : this.realmId.hashCode());
        result = prime * result + ((this.createdOn == null) ? 0 : this.createdOn.hashCode());
        result = prime * result + ((this.offlineFlag == null) ? 0 : this.offlineFlag.hashCode());
        result = prime * result + ((this.data == null) ? 0 : this.data.hashCode());
        result = prime * result + ((this.lastSessionRefresh == null) ? 0 : this.lastSessionRefresh.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("OfflineUserSession (");

        sb.append(userSessionId);
        sb.append(", ").append(userId);
        sb.append(", ").append(realmId);
        sb.append(", ").append(createdOn);
        sb.append(", ").append(offlineFlag);
        sb.append(", ").append(data);
        sb.append(", ").append(lastSessionRefresh);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IOfflineUserSession from) {
        setUserSessionId(from.getUserSessionId());
        setUserId(from.getUserId());
        setRealmId(from.getRealmId());
        setCreatedOn(from.getCreatedOn());
        setOfflineFlag(from.getOfflineFlag());
        setData(from.getData());
        setLastSessionRefresh(from.getLastSessionRefresh());
    }

    @Override
    public <E extends IOfflineUserSession> E into(E into) {
        into.from(this);
        return into;
    }
}
