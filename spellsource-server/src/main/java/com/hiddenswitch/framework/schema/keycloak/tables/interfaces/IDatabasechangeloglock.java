/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.interfaces;


import io.github.jklingsporn.vertx.jooq.shared.UnexpectedJsonValueType;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public interface IDatabasechangeloglock extends VertxPojo, Serializable {

    /**
     * Setter for <code>keycloak.databasechangeloglock.id</code>.
     */
    public IDatabasechangeloglock setId(Integer value);

    /**
     * Getter for <code>keycloak.databasechangeloglock.id</code>.
     */
    public Integer getId();

    /**
     * Setter for <code>keycloak.databasechangeloglock.locked</code>.
     */
    public IDatabasechangeloglock setLocked(Boolean value);

    /**
     * Getter for <code>keycloak.databasechangeloglock.locked</code>.
     */
    public Boolean getLocked();

    /**
     * Setter for <code>keycloak.databasechangeloglock.lockgranted</code>.
     */
    public IDatabasechangeloglock setLockgranted(LocalDateTime value);

    /**
     * Getter for <code>keycloak.databasechangeloglock.lockgranted</code>.
     */
    public LocalDateTime getLockgranted();

    /**
     * Setter for <code>keycloak.databasechangeloglock.lockedby</code>.
     */
    public IDatabasechangeloglock setLockedby(String value);

    /**
     * Getter for <code>keycloak.databasechangeloglock.lockedby</code>.
     */
    public String getLockedby();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common interface IDatabasechangeloglock
     */
    public void from(IDatabasechangeloglock from);

    /**
     * Copy data into another generated Record/POJO implementing the common interface IDatabasechangeloglock
     */
    public <E extends IDatabasechangeloglock> E into(E into);

    @Override
    public default IDatabasechangeloglock fromJson(io.vertx.core.json.JsonObject json) {
        try {
            setId(json.getInteger("id"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("id","java.lang.Integer",e);
        }
        try {
            setLocked(json.getBoolean("locked"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("locked","java.lang.Boolean",e);
        }
        try {
            String lockgrantedString = json.getString("lockgranted");
            setLockgranted(lockgrantedString == null?null:java.time.LocalDateTime.parse(lockgrantedString));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("lockgranted","java.time.LocalDateTime",e);
        }
        try {
            setLockedby(json.getString("lockedby"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("lockedby","java.lang.String",e);
        }
        return this;
    }


    @Override
    public default io.vertx.core.json.JsonObject toJson() {
        io.vertx.core.json.JsonObject json = new io.vertx.core.json.JsonObject();
        json.put("id",getId());
        json.put("locked",getLocked());
        json.put("lockgranted",getLockgranted()==null?null:getLockgranted().toString());
        json.put("lockedby",getLockedby());
        return json;
    }

}
