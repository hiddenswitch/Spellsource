/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.interfaces;


import io.github.jklingsporn.vertx.jooq.shared.UnexpectedJsonValueType;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import java.io.Serializable;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public interface IWebOrigins extends VertxPojo, Serializable {

    /**
     * Setter for <code>keycloak.web_origins.client_id</code>.
     */
    public IWebOrigins setClientId(String value);

    /**
     * Getter for <code>keycloak.web_origins.client_id</code>.
     */
    public String getClientId();

    /**
     * Setter for <code>keycloak.web_origins.value</code>.
     */
    public IWebOrigins setValue(String value);

    /**
     * Getter for <code>keycloak.web_origins.value</code>.
     */
    public String getValue();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common interface IWebOrigins
     */
    public void from(IWebOrigins from);

    /**
     * Copy data into another generated Record/POJO implementing the common interface IWebOrigins
     */
    public <E extends IWebOrigins> E into(E into);

    @Override
    public default IWebOrigins fromJson(io.vertx.core.json.JsonObject json) {
        try {
            setClientId(json.getString("client_id"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("client_id","java.lang.String",e);
        }
        try {
            setValue(json.getString("value"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("value","java.lang.String",e);
        }
        return this;
    }


    @Override
    public default io.vertx.core.json.JsonObject toJson() {
        io.vertx.core.json.JsonObject json = new io.vertx.core.json.JsonObject();
        json.put("client_id",getClientId());
        json.put("value",getValue());
        return json;
    }

}
