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
public interface IClientScopeAttributes extends VertxPojo, Serializable {

    /**
     * Setter for <code>keycloak.client_scope_attributes.scope_id</code>.
     */
    public IClientScopeAttributes setScopeId(String value);

    /**
     * Getter for <code>keycloak.client_scope_attributes.scope_id</code>.
     */
    public String getScopeId();

    /**
     * Setter for <code>keycloak.client_scope_attributes.value</code>.
     */
    public IClientScopeAttributes setValue(String value);

    /**
     * Getter for <code>keycloak.client_scope_attributes.value</code>.
     */
    public String getValue();

    /**
     * Setter for <code>keycloak.client_scope_attributes.name</code>.
     */
    public IClientScopeAttributes setName(String value);

    /**
     * Getter for <code>keycloak.client_scope_attributes.name</code>.
     */
    public String getName();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common
     * interface IClientScopeAttributes
     */
    public void from(IClientScopeAttributes from);

    /**
     * Copy data into another generated Record/POJO implementing the common
     * interface IClientScopeAttributes
     */
    public <E extends IClientScopeAttributes> E into(E into);

        @Override
        public default IClientScopeAttributes fromJson(io.vertx.core.json.JsonObject json) {
                setOrThrow(this::setScopeId,json::getString,"scope_id","java.lang.String");
                setOrThrow(this::setValue,json::getString,"value","java.lang.String");
                setOrThrow(this::setName,json::getString,"name","java.lang.String");
                return this;
        }


        @Override
        public default io.vertx.core.json.JsonObject toJson() {
                io.vertx.core.json.JsonObject json = new io.vertx.core.json.JsonObject();
                json.put("scope_id",getScopeId());
                json.put("value",getValue());
                json.put("name",getName());
                return json;
        }

}
