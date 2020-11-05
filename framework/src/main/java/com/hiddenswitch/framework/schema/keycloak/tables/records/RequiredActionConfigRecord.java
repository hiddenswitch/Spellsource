/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.records;


import com.hiddenswitch.framework.schema.keycloak.tables.RequiredActionConfig;
import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IRequiredActionConfig;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class RequiredActionConfigRecord extends UpdatableRecordImpl<RequiredActionConfigRecord> implements VertxPojo, Record3<String, String, String>, IRequiredActionConfig {

    private static final long serialVersionUID = -1102922316;

    /**
     * Setter for <code>keycloak.required_action_config.required_action_id</code>.
     */
    @Override
    public RequiredActionConfigRecord setRequiredActionId(String value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.required_action_config.required_action_id</code>.
     */
    @Override
    public String getRequiredActionId() {
        return (String) get(0);
    }

    /**
     * Setter for <code>keycloak.required_action_config.value</code>.
     */
    @Override
    public RequiredActionConfigRecord setValue(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.required_action_config.value</code>.
     */
    @Override
    public String getValue() {
        return (String) get(1);
    }

    /**
     * Setter for <code>keycloak.required_action_config.name</code>.
     */
    @Override
    public RequiredActionConfigRecord setName(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.required_action_config.name</code>.
     */
    @Override
    public String getName() {
        return (String) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record2<String, String> key() {
        return (Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<String, String, String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<String, String, String> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return RequiredActionConfig.REQUIRED_ACTION_CONFIG.REQUIRED_ACTION_ID;
    }

    @Override
    public Field<String> field2() {
        return RequiredActionConfig.REQUIRED_ACTION_CONFIG.VALUE;
    }

    @Override
    public Field<String> field3() {
        return RequiredActionConfig.REQUIRED_ACTION_CONFIG.NAME;
    }

    @Override
    public String component1() {
        return getRequiredActionId();
    }

    @Override
    public String component2() {
        return getValue();
    }

    @Override
    public String component3() {
        return getName();
    }

    @Override
    public String value1() {
        return getRequiredActionId();
    }

    @Override
    public String value2() {
        return getValue();
    }

    @Override
    public String value3() {
        return getName();
    }

    @Override
    public RequiredActionConfigRecord value1(String value) {
        setRequiredActionId(value);
        return this;
    }

    @Override
    public RequiredActionConfigRecord value2(String value) {
        setValue(value);
        return this;
    }

    @Override
    public RequiredActionConfigRecord value3(String value) {
        setName(value);
        return this;
    }

    @Override
    public RequiredActionConfigRecord values(String value1, String value2, String value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IRequiredActionConfig from) {
        setRequiredActionId(from.getRequiredActionId());
        setValue(from.getValue());
        setName(from.getName());
    }

    @Override
    public <E extends IRequiredActionConfig> E into(E into) {
        into.from(this);
        return into;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached RequiredActionConfigRecord
     */
    public RequiredActionConfigRecord() {
        super(RequiredActionConfig.REQUIRED_ACTION_CONFIG);
    }

    /**
     * Create a detached, initialised RequiredActionConfigRecord
     */
    public RequiredActionConfigRecord(String requiredActionId, String value, String name) {
        super(RequiredActionConfig.REQUIRED_ACTION_CONFIG);

        set(0, requiredActionId);
        set(1, value);
        set(2, name);
    }

    public RequiredActionConfigRecord(io.vertx.core.json.JsonObject json) {
        this();
        fromJson(json);
    }
}