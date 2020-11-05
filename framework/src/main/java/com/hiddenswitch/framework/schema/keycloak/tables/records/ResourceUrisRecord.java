/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.records;


import com.hiddenswitch.framework.schema.keycloak.tables.ResourceUris;
import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IResourceUris;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ResourceUrisRecord extends UpdatableRecordImpl<ResourceUrisRecord> implements VertxPojo, Record2<String, String>, IResourceUris {

    private static final long serialVersionUID = -760597197;

    /**
     * Setter for <code>keycloak.resource_uris.resource_id</code>.
     */
    @Override
    public ResourceUrisRecord setResourceId(String value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.resource_uris.resource_id</code>.
     */
    @Override
    public String getResourceId() {
        return (String) get(0);
    }

    /**
     * Setter for <code>keycloak.resource_uris.value</code>.
     */
    @Override
    public ResourceUrisRecord setValue(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.resource_uris.value</code>.
     */
    @Override
    public String getValue() {
        return (String) get(1);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record2<String, String> key() {
        return (Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row2<String, String> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    @Override
    public Row2<String, String> valuesRow() {
        return (Row2) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return ResourceUris.RESOURCE_URIS.RESOURCE_ID;
    }

    @Override
    public Field<String> field2() {
        return ResourceUris.RESOURCE_URIS.VALUE;
    }

    @Override
    public String component1() {
        return getResourceId();
    }

    @Override
    public String component2() {
        return getValue();
    }

    @Override
    public String value1() {
        return getResourceId();
    }

    @Override
    public String value2() {
        return getValue();
    }

    @Override
    public ResourceUrisRecord value1(String value) {
        setResourceId(value);
        return this;
    }

    @Override
    public ResourceUrisRecord value2(String value) {
        setValue(value);
        return this;
    }

    @Override
    public ResourceUrisRecord values(String value1, String value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IResourceUris from) {
        setResourceId(from.getResourceId());
        setValue(from.getValue());
    }

    @Override
    public <E extends IResourceUris> E into(E into) {
        into.from(this);
        return into;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ResourceUrisRecord
     */
    public ResourceUrisRecord() {
        super(ResourceUris.RESOURCE_URIS);
    }

    /**
     * Create a detached, initialised ResourceUrisRecord
     */
    public ResourceUrisRecord(String resourceId, String value) {
        super(ResourceUris.RESOURCE_URIS);

        set(0, resourceId);
        set(1, value);
    }

    public ResourceUrisRecord(io.vertx.core.json.JsonObject json) {
        this();
        fromJson(json);
    }
}