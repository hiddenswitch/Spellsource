/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.interfaces;


import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import java.io.Serializable;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public interface IAdminEventEntity extends VertxPojo, Serializable {

    /**
     * Setter for <code>keycloak.admin_event_entity.id</code>.
     */
    public IAdminEventEntity setId(String value);

    /**
     * Getter for <code>keycloak.admin_event_entity.id</code>.
     */
    public String getId();

    /**
     * Setter for <code>keycloak.admin_event_entity.admin_event_time</code>.
     */
    public IAdminEventEntity setAdminEventTime(Long value);

    /**
     * Getter for <code>keycloak.admin_event_entity.admin_event_time</code>.
     */
    public Long getAdminEventTime();

    /**
     * Setter for <code>keycloak.admin_event_entity.realm_id</code>.
     */
    public IAdminEventEntity setRealmId(String value);

    /**
     * Getter for <code>keycloak.admin_event_entity.realm_id</code>.
     */
    public String getRealmId();

    /**
     * Setter for <code>keycloak.admin_event_entity.operation_type</code>.
     */
    public IAdminEventEntity setOperationType(String value);

    /**
     * Getter for <code>keycloak.admin_event_entity.operation_type</code>.
     */
    public String getOperationType();

    /**
     * Setter for <code>keycloak.admin_event_entity.auth_realm_id</code>.
     */
    public IAdminEventEntity setAuthRealmId(String value);

    /**
     * Getter for <code>keycloak.admin_event_entity.auth_realm_id</code>.
     */
    public String getAuthRealmId();

    /**
     * Setter for <code>keycloak.admin_event_entity.auth_client_id</code>.
     */
    public IAdminEventEntity setAuthClientId(String value);

    /**
     * Getter for <code>keycloak.admin_event_entity.auth_client_id</code>.
     */
    public String getAuthClientId();

    /**
     * Setter for <code>keycloak.admin_event_entity.auth_user_id</code>.
     */
    public IAdminEventEntity setAuthUserId(String value);

    /**
     * Getter for <code>keycloak.admin_event_entity.auth_user_id</code>.
     */
    public String getAuthUserId();

    /**
     * Setter for <code>keycloak.admin_event_entity.ip_address</code>.
     */
    public IAdminEventEntity setIpAddress(String value);

    /**
     * Getter for <code>keycloak.admin_event_entity.ip_address</code>.
     */
    public String getIpAddress();

    /**
     * Setter for <code>keycloak.admin_event_entity.resource_path</code>.
     */
    public IAdminEventEntity setResourcePath(String value);

    /**
     * Getter for <code>keycloak.admin_event_entity.resource_path</code>.
     */
    public String getResourcePath();

    /**
     * Setter for <code>keycloak.admin_event_entity.representation</code>.
     */
    public IAdminEventEntity setRepresentation(String value);

    /**
     * Getter for <code>keycloak.admin_event_entity.representation</code>.
     */
    public String getRepresentation();

    /**
     * Setter for <code>keycloak.admin_event_entity.error</code>.
     */
    public IAdminEventEntity setError(String value);

    /**
     * Getter for <code>keycloak.admin_event_entity.error</code>.
     */
    public String getError();

    /**
     * Setter for <code>keycloak.admin_event_entity.resource_type</code>.
     */
    public IAdminEventEntity setResourceType(String value);

    /**
     * Getter for <code>keycloak.admin_event_entity.resource_type</code>.
     */
    public String getResourceType();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common
     * interface IAdminEventEntity
     */
    public void from(IAdminEventEntity from);

    /**
     * Copy data into another generated Record/POJO implementing the common
     * interface IAdminEventEntity
     */
    public <E extends IAdminEventEntity> E into(E into);

        @Override
        public default IAdminEventEntity fromJson(io.vertx.core.json.JsonObject json) {
                setOrThrow(this::setId,json::getString,"id","java.lang.String");
                setOrThrow(this::setAdminEventTime,json::getLong,"admin_event_time","java.lang.Long");
                setOrThrow(this::setRealmId,json::getString,"realm_id","java.lang.String");
                setOrThrow(this::setOperationType,json::getString,"operation_type","java.lang.String");
                setOrThrow(this::setAuthRealmId,json::getString,"auth_realm_id","java.lang.String");
                setOrThrow(this::setAuthClientId,json::getString,"auth_client_id","java.lang.String");
                setOrThrow(this::setAuthUserId,json::getString,"auth_user_id","java.lang.String");
                setOrThrow(this::setIpAddress,json::getString,"ip_address","java.lang.String");
                setOrThrow(this::setResourcePath,json::getString,"resource_path","java.lang.String");
                setOrThrow(this::setRepresentation,json::getString,"representation","java.lang.String");
                setOrThrow(this::setError,json::getString,"error","java.lang.String");
                setOrThrow(this::setResourceType,json::getString,"resource_type","java.lang.String");
                return this;
        }


        @Override
        public default io.vertx.core.json.JsonObject toJson() {
                io.vertx.core.json.JsonObject json = new io.vertx.core.json.JsonObject();
                json.put("id",getId());
                json.put("admin_event_time",getAdminEventTime());
                json.put("realm_id",getRealmId());
                json.put("operation_type",getOperationType());
                json.put("auth_realm_id",getAuthRealmId());
                json.put("auth_client_id",getAuthClientId());
                json.put("auth_user_id",getAuthUserId());
                json.put("ip_address",getIpAddress());
                json.put("resource_path",getResourcePath());
                json.put("representation",getRepresentation());
                json.put("error",getError());
                json.put("resource_type",getResourceType());
                return json;
        }

}
