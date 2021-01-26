/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.records;


import com.hiddenswitch.framework.schema.keycloak.tables.ResourceServerResource;
import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IResourceServerResource;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record8;
import org.jooq.Row8;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ResourceServerResourceRecord extends UpdatableRecordImpl<ResourceServerResourceRecord> implements VertxPojo, Record8<String, String, String, String, String, String, Boolean, String>, IResourceServerResource {

    private static final long serialVersionUID = -349038286;

    /**
     * Setter for <code>keycloak.resource_server_resource.id</code>.
     */
    @Override
    public ResourceServerResourceRecord setId(String value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.resource_server_resource.id</code>.
     */
    @Override
    public String getId() {
        return (String) get(0);
    }

    /**
     * Setter for <code>keycloak.resource_server_resource.name</code>.
     */
    @Override
    public ResourceServerResourceRecord setName(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.resource_server_resource.name</code>.
     */
    @Override
    public String getName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>keycloak.resource_server_resource.type</code>.
     */
    @Override
    public ResourceServerResourceRecord setType(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.resource_server_resource.type</code>.
     */
    @Override
    public String getType() {
        return (String) get(2);
    }

    /**
     * Setter for <code>keycloak.resource_server_resource.icon_uri</code>.
     */
    @Override
    public ResourceServerResourceRecord setIconUri(String value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.resource_server_resource.icon_uri</code>.
     */
    @Override
    public String getIconUri() {
        return (String) get(3);
    }

    /**
     * Setter for <code>keycloak.resource_server_resource.owner</code>.
     */
    @Override
    public ResourceServerResourceRecord setOwner(String value) {
        set(4, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.resource_server_resource.owner</code>.
     */
    @Override
    public String getOwner() {
        return (String) get(4);
    }

    /**
     * Setter for <code>keycloak.resource_server_resource.resource_server_id</code>.
     */
    @Override
    public ResourceServerResourceRecord setResourceServerId(String value) {
        set(5, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.resource_server_resource.resource_server_id</code>.
     */
    @Override
    public String getResourceServerId() {
        return (String) get(5);
    }

    /**
     * Setter for <code>keycloak.resource_server_resource.owner_managed_access</code>.
     */
    @Override
    public ResourceServerResourceRecord setOwnerManagedAccess(Boolean value) {
        set(6, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.resource_server_resource.owner_managed_access</code>.
     */
    @Override
    public Boolean getOwnerManagedAccess() {
        return (Boolean) get(6);
    }

    /**
     * Setter for <code>keycloak.resource_server_resource.display_name</code>.
     */
    @Override
    public ResourceServerResourceRecord setDisplayName(String value) {
        set(7, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.resource_server_resource.display_name</code>.
     */
    @Override
    public String getDisplayName() {
        return (String) get(7);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<String> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record8 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row8<String, String, String, String, String, String, Boolean, String> fieldsRow() {
        return (Row8) super.fieldsRow();
    }

    @Override
    public Row8<String, String, String, String, String, String, Boolean, String> valuesRow() {
        return (Row8) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return ResourceServerResource.RESOURCE_SERVER_RESOURCE.ID;
    }

    @Override
    public Field<String> field2() {
        return ResourceServerResource.RESOURCE_SERVER_RESOURCE.NAME;
    }

    @Override
    public Field<String> field3() {
        return ResourceServerResource.RESOURCE_SERVER_RESOURCE.TYPE;
    }

    @Override
    public Field<String> field4() {
        return ResourceServerResource.RESOURCE_SERVER_RESOURCE.ICON_URI;
    }

    @Override
    public Field<String> field5() {
        return ResourceServerResource.RESOURCE_SERVER_RESOURCE.OWNER;
    }

    @Override
    public Field<String> field6() {
        return ResourceServerResource.RESOURCE_SERVER_RESOURCE.RESOURCE_SERVER_ID;
    }

    @Override
    public Field<Boolean> field7() {
        return ResourceServerResource.RESOURCE_SERVER_RESOURCE.OWNER_MANAGED_ACCESS;
    }

    @Override
    public Field<String> field8() {
        return ResourceServerResource.RESOURCE_SERVER_RESOURCE.DISPLAY_NAME;
    }

    @Override
    public String component1() {
        return getId();
    }

    @Override
    public String component2() {
        return getName();
    }

    @Override
    public String component3() {
        return getType();
    }

    @Override
    public String component4() {
        return getIconUri();
    }

    @Override
    public String component5() {
        return getOwner();
    }

    @Override
    public String component6() {
        return getResourceServerId();
    }

    @Override
    public Boolean component7() {
        return getOwnerManagedAccess();
    }

    @Override
    public String component8() {
        return getDisplayName();
    }

    @Override
    public String value1() {
        return getId();
    }

    @Override
    public String value2() {
        return getName();
    }

    @Override
    public String value3() {
        return getType();
    }

    @Override
    public String value4() {
        return getIconUri();
    }

    @Override
    public String value5() {
        return getOwner();
    }

    @Override
    public String value6() {
        return getResourceServerId();
    }

    @Override
    public Boolean value7() {
        return getOwnerManagedAccess();
    }

    @Override
    public String value8() {
        return getDisplayName();
    }

    @Override
    public ResourceServerResourceRecord value1(String value) {
        setId(value);
        return this;
    }

    @Override
    public ResourceServerResourceRecord value2(String value) {
        setName(value);
        return this;
    }

    @Override
    public ResourceServerResourceRecord value3(String value) {
        setType(value);
        return this;
    }

    @Override
    public ResourceServerResourceRecord value4(String value) {
        setIconUri(value);
        return this;
    }

    @Override
    public ResourceServerResourceRecord value5(String value) {
        setOwner(value);
        return this;
    }

    @Override
    public ResourceServerResourceRecord value6(String value) {
        setResourceServerId(value);
        return this;
    }

    @Override
    public ResourceServerResourceRecord value7(Boolean value) {
        setOwnerManagedAccess(value);
        return this;
    }

    @Override
    public ResourceServerResourceRecord value8(String value) {
        setDisplayName(value);
        return this;
    }

    @Override
    public ResourceServerResourceRecord values(String value1, String value2, String value3, String value4, String value5, String value6, Boolean value7, String value8) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        return this;
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IResourceServerResource from) {
        setId(from.getId());
        setName(from.getName());
        setType(from.getType());
        setIconUri(from.getIconUri());
        setOwner(from.getOwner());
        setResourceServerId(from.getResourceServerId());
        setOwnerManagedAccess(from.getOwnerManagedAccess());
        setDisplayName(from.getDisplayName());
    }

    @Override
    public <E extends IResourceServerResource> E into(E into) {
        into.from(this);
        return into;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ResourceServerResourceRecord
     */
    public ResourceServerResourceRecord() {
        super(ResourceServerResource.RESOURCE_SERVER_RESOURCE);
    }

    /**
     * Create a detached, initialised ResourceServerResourceRecord
     */
    public ResourceServerResourceRecord(String id, String name, String type, String iconUri, String owner, String resourceServerId, Boolean ownerManagedAccess, String displayName) {
        super(ResourceServerResource.RESOURCE_SERVER_RESOURCE);

        set(0, id);
        set(1, name);
        set(2, type);
        set(3, iconUri);
        set(4, owner);
        set(5, resourceServerId);
        set(6, ownerManagedAccess);
        set(7, displayName);
    }

    public ResourceServerResourceRecord(io.vertx.core.json.JsonObject json) {
        this();
        fromJson(json);
    }
}