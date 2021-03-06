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
public interface IUserFederationProvider extends VertxPojo, Serializable {

    /**
     * Setter for <code>keycloak.user_federation_provider.id</code>.
     */
    public IUserFederationProvider setId(String value);

    /**
     * Getter for <code>keycloak.user_federation_provider.id</code>.
     */
    public String getId();

    /**
     * Setter for <code>keycloak.user_federation_provider.changed_sync_period</code>.
     */
    public IUserFederationProvider setChangedSyncPeriod(Integer value);

    /**
     * Getter for <code>keycloak.user_federation_provider.changed_sync_period</code>.
     */
    public Integer getChangedSyncPeriod();

    /**
     * Setter for <code>keycloak.user_federation_provider.display_name</code>.
     */
    public IUserFederationProvider setDisplayName(String value);

    /**
     * Getter for <code>keycloak.user_federation_provider.display_name</code>.
     */
    public String getDisplayName();

    /**
     * Setter for <code>keycloak.user_federation_provider.full_sync_period</code>.
     */
    public IUserFederationProvider setFullSyncPeriod(Integer value);

    /**
     * Getter for <code>keycloak.user_federation_provider.full_sync_period</code>.
     */
    public Integer getFullSyncPeriod();

    /**
     * Setter for <code>keycloak.user_federation_provider.last_sync</code>.
     */
    public IUserFederationProvider setLastSync(Integer value);

    /**
     * Getter for <code>keycloak.user_federation_provider.last_sync</code>.
     */
    public Integer getLastSync();

    /**
     * Setter for <code>keycloak.user_federation_provider.priority</code>.
     */
    public IUserFederationProvider setPriority(Integer value);

    /**
     * Getter for <code>keycloak.user_federation_provider.priority</code>.
     */
    public Integer getPriority();

    /**
     * Setter for <code>keycloak.user_federation_provider.provider_name</code>.
     */
    public IUserFederationProvider setProviderName(String value);

    /**
     * Getter for <code>keycloak.user_federation_provider.provider_name</code>.
     */
    public String getProviderName();

    /**
     * Setter for <code>keycloak.user_federation_provider.realm_id</code>.
     */
    public IUserFederationProvider setRealmId(String value);

    /**
     * Getter for <code>keycloak.user_federation_provider.realm_id</code>.
     */
    public String getRealmId();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common interface IUserFederationProvider
     */
    public void from(IUserFederationProvider from);

    /**
     * Copy data into another generated Record/POJO implementing the common interface IUserFederationProvider
     */
    public <E extends IUserFederationProvider> E into(E into);

    @Override
    public default IUserFederationProvider fromJson(io.vertx.core.json.JsonObject json) {
        try {
            setId(json.getString("id"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("id","java.lang.String",e);
        }
        try {
            setChangedSyncPeriod(json.getInteger("changed_sync_period"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("changed_sync_period","java.lang.Integer",e);
        }
        try {
            setDisplayName(json.getString("display_name"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("display_name","java.lang.String",e);
        }
        try {
            setFullSyncPeriod(json.getInteger("full_sync_period"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("full_sync_period","java.lang.Integer",e);
        }
        try {
            setLastSync(json.getInteger("last_sync"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("last_sync","java.lang.Integer",e);
        }
        try {
            setPriority(json.getInteger("priority"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("priority","java.lang.Integer",e);
        }
        try {
            setProviderName(json.getString("provider_name"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("provider_name","java.lang.String",e);
        }
        try {
            setRealmId(json.getString("realm_id"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("realm_id","java.lang.String",e);
        }
        return this;
    }


    @Override
    public default io.vertx.core.json.JsonObject toJson() {
        io.vertx.core.json.JsonObject json = new io.vertx.core.json.JsonObject();
        json.put("id",getId());
        json.put("changed_sync_period",getChangedSyncPeriod());
        json.put("display_name",getDisplayName());
        json.put("full_sync_period",getFullSyncPeriod());
        json.put("last_sync",getLastSync());
        json.put("priority",getPriority());
        json.put("provider_name",getProviderName());
        json.put("realm_id",getRealmId());
        return json;
    }

}
