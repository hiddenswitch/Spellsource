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
public interface IClientScope extends VertxPojo, Serializable {

    /**
     * Setter for <code>keycloak.client_scope.id</code>.
     */
    public IClientScope setId(String value);

    /**
     * Getter for <code>keycloak.client_scope.id</code>.
     */
    public String getId();

    /**
     * Setter for <code>keycloak.client_scope.name</code>.
     */
    public IClientScope setName(String value);

    /**
     * Getter for <code>keycloak.client_scope.name</code>.
     */
    public String getName();

    /**
     * Setter for <code>keycloak.client_scope.realm_id</code>.
     */
    public IClientScope setRealmId(String value);

    /**
     * Getter for <code>keycloak.client_scope.realm_id</code>.
     */
    public String getRealmId();

    /**
     * Setter for <code>keycloak.client_scope.description</code>.
     */
    public IClientScope setDescription(String value);

    /**
     * Getter for <code>keycloak.client_scope.description</code>.
     */
    public String getDescription();

    /**
     * Setter for <code>keycloak.client_scope.protocol</code>.
     */
    public IClientScope setProtocol(String value);

    /**
     * Getter for <code>keycloak.client_scope.protocol</code>.
     */
    public String getProtocol();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common
     * interface IClientScope
     */
    public void from(IClientScope from);

    /**
     * Copy data into another generated Record/POJO implementing the common
     * interface IClientScope
     */
    public <E extends IClientScope> E into(E into);

        @Override
        public default IClientScope fromJson(io.vertx.core.json.JsonObject json) {
                setOrThrow(this::setId,json::getString,"id","java.lang.String");
                setOrThrow(this::setName,json::getString,"name","java.lang.String");
                setOrThrow(this::setRealmId,json::getString,"realm_id","java.lang.String");
                setOrThrow(this::setDescription,json::getString,"description","java.lang.String");
                setOrThrow(this::setProtocol,json::getString,"protocol","java.lang.String");
                return this;
        }


        @Override
        public default io.vertx.core.json.JsonObject toJson() {
                io.vertx.core.json.JsonObject json = new io.vertx.core.json.JsonObject();
                json.put("id",getId());
                json.put("name",getName());
                json.put("realm_id",getRealmId());
                json.put("description",getDescription());
                json.put("protocol",getProtocol());
                return json;
        }

}