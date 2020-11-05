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
public interface IResourceServer extends VertxPojo, Serializable {

    /**
     * Setter for <code>keycloak.resource_server.id</code>.
     */
    public IResourceServer setId(String value);

    /**
     * Getter for <code>keycloak.resource_server.id</code>.
     */
    public String getId();

    /**
     * Setter for <code>keycloak.resource_server.allow_rs_remote_mgmt</code>.
     */
    public IResourceServer setAllowRsRemoteMgmt(Boolean value);

    /**
     * Getter for <code>keycloak.resource_server.allow_rs_remote_mgmt</code>.
     */
    public Boolean getAllowRsRemoteMgmt();

    /**
     * Setter for <code>keycloak.resource_server.policy_enforce_mode</code>.
     */
    public IResourceServer setPolicyEnforceMode(String value);

    /**
     * Getter for <code>keycloak.resource_server.policy_enforce_mode</code>.
     */
    public String getPolicyEnforceMode();

    /**
     * Setter for <code>keycloak.resource_server.decision_strategy</code>.
     */
    public IResourceServer setDecisionStrategy(Short value);

    /**
     * Getter for <code>keycloak.resource_server.decision_strategy</code>.
     */
    public Short getDecisionStrategy();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common interface IResourceServer
     */
    public void from(IResourceServer from);

    /**
     * Copy data into another generated Record/POJO implementing the common interface IResourceServer
     */
    public <E extends IResourceServer> E into(E into);

    @Override
    public default IResourceServer fromJson(io.vertx.core.json.JsonObject json) {
        try {
            setId(json.getString("id"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("id","java.lang.String",e);
        }
        try {
            setAllowRsRemoteMgmt(json.getBoolean("allow_rs_remote_mgmt"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("allow_rs_remote_mgmt","java.lang.Boolean",e);
        }
        try {
            setPolicyEnforceMode(json.getString("policy_enforce_mode"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("policy_enforce_mode","java.lang.String",e);
        }
        try {
            setDecisionStrategy(json.getInteger("decision_strategy")==null?null:json.getInteger("decision_strategy").shortValue());
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("decision_strategy","java.lang.Short",e);
        }
        return this;
    }


    @Override
    public default io.vertx.core.json.JsonObject toJson() {
        io.vertx.core.json.JsonObject json = new io.vertx.core.json.JsonObject();
        json.put("id",getId());
        json.put("allow_rs_remote_mgmt",getAllowRsRemoteMgmt());
        json.put("policy_enforce_mode",getPolicyEnforceMode());
        json.put("decision_strategy",getDecisionStrategy());
        return json;
    }

}