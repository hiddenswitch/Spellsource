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
public interface IRoleAttribute extends VertxPojo, Serializable {

    /**
     * Setter for <code>keycloak.role_attribute.id</code>.
     */
    public IRoleAttribute setId(String value);

    /**
     * Getter for <code>keycloak.role_attribute.id</code>.
     */
    public String getId();

    /**
     * Setter for <code>keycloak.role_attribute.role_id</code>.
     */
    public IRoleAttribute setRoleId(String value);

    /**
     * Getter for <code>keycloak.role_attribute.role_id</code>.
     */
    public String getRoleId();

    /**
     * Setter for <code>keycloak.role_attribute.name</code>.
     */
    public IRoleAttribute setName(String value);

    /**
     * Getter for <code>keycloak.role_attribute.name</code>.
     */
    public String getName();

    /**
     * Setter for <code>keycloak.role_attribute.value</code>.
     */
    public IRoleAttribute setValue(String value);

    /**
     * Getter for <code>keycloak.role_attribute.value</code>.
     */
    public String getValue();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common
     * interface IRoleAttribute
     */
    public void from(IRoleAttribute from);

    /**
     * Copy data into another generated Record/POJO implementing the common
     * interface IRoleAttribute
     */
    public <E extends IRoleAttribute> E into(E into);

        @Override
        public default IRoleAttribute fromJson(io.vertx.core.json.JsonObject json) {
                setOrThrow(this::setId,json::getString,"id","java.lang.String");
                setOrThrow(this::setRoleId,json::getString,"role_id","java.lang.String");
                setOrThrow(this::setName,json::getString,"name","java.lang.String");
                setOrThrow(this::setValue,json::getString,"value","java.lang.String");
                return this;
        }


        @Override
        public default io.vertx.core.json.JsonObject toJson() {
                io.vertx.core.json.JsonObject json = new io.vertx.core.json.JsonObject();
                json.put("id",getId());
                json.put("role_id",getRoleId());
                json.put("name",getName());
                json.put("value",getValue());
                return json;
        }

}