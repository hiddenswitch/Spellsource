/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.records;


import com.hiddenswitch.framework.schema.keycloak.tables.IdentityProviderMapper;
import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IIdentityProviderMapper;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record5;
import org.jooq.Row5;
import org.jooq.impl.UpdatableRecordImpl;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class IdentityProviderMapperRecord extends UpdatableRecordImpl<IdentityProviderMapperRecord> implements VertxPojo, Record5<String, String, String, String, String>, IIdentityProviderMapper {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>keycloak.identity_provider_mapper.id</code>.
     */
    @Override
    public IdentityProviderMapperRecord setId(String value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.identity_provider_mapper.id</code>.
     */
    @Override
    public String getId() {
        return (String) get(0);
    }

    /**
     * Setter for <code>keycloak.identity_provider_mapper.name</code>.
     */
    @Override
    public IdentityProviderMapperRecord setName(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.identity_provider_mapper.name</code>.
     */
    @Override
    public String getName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>keycloak.identity_provider_mapper.idp_alias</code>.
     */
    @Override
    public IdentityProviderMapperRecord setIdpAlias(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.identity_provider_mapper.idp_alias</code>.
     */
    @Override
    public String getIdpAlias() {
        return (String) get(2);
    }

    /**
     * Setter for
     * <code>keycloak.identity_provider_mapper.idp_mapper_name</code>.
     */
    @Override
    public IdentityProviderMapperRecord setIdpMapperName(String value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for
     * <code>keycloak.identity_provider_mapper.idp_mapper_name</code>.
     */
    @Override
    public String getIdpMapperName() {
        return (String) get(3);
    }

    /**
     * Setter for <code>keycloak.identity_provider_mapper.realm_id</code>.
     */
    @Override
    public IdentityProviderMapperRecord setRealmId(String value) {
        set(4, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.identity_provider_mapper.realm_id</code>.
     */
    @Override
    public String getRealmId() {
        return (String) get(4);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<String> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record5 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row5<String, String, String, String, String> fieldsRow() {
        return (Row5) super.fieldsRow();
    }

    @Override
    public Row5<String, String, String, String, String> valuesRow() {
        return (Row5) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return IdentityProviderMapper.IDENTITY_PROVIDER_MAPPER.ID;
    }

    @Override
    public Field<String> field2() {
        return IdentityProviderMapper.IDENTITY_PROVIDER_MAPPER.NAME;
    }

    @Override
    public Field<String> field3() {
        return IdentityProviderMapper.IDENTITY_PROVIDER_MAPPER.IDP_ALIAS;
    }

    @Override
    public Field<String> field4() {
        return IdentityProviderMapper.IDENTITY_PROVIDER_MAPPER.IDP_MAPPER_NAME;
    }

    @Override
    public Field<String> field5() {
        return IdentityProviderMapper.IDENTITY_PROVIDER_MAPPER.REALM_ID;
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
        return getIdpAlias();
    }

    @Override
    public String component4() {
        return getIdpMapperName();
    }

    @Override
    public String component5() {
        return getRealmId();
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
        return getIdpAlias();
    }

    @Override
    public String value4() {
        return getIdpMapperName();
    }

    @Override
    public String value5() {
        return getRealmId();
    }

    @Override
    public IdentityProviderMapperRecord value1(String value) {
        setId(value);
        return this;
    }

    @Override
    public IdentityProviderMapperRecord value2(String value) {
        setName(value);
        return this;
    }

    @Override
    public IdentityProviderMapperRecord value3(String value) {
        setIdpAlias(value);
        return this;
    }

    @Override
    public IdentityProviderMapperRecord value4(String value) {
        setIdpMapperName(value);
        return this;
    }

    @Override
    public IdentityProviderMapperRecord value5(String value) {
        setRealmId(value);
        return this;
    }

    @Override
    public IdentityProviderMapperRecord values(String value1, String value2, String value3, String value4, String value5) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        return this;
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IIdentityProviderMapper from) {
        setId(from.getId());
        setName(from.getName());
        setIdpAlias(from.getIdpAlias());
        setIdpMapperName(from.getIdpMapperName());
        setRealmId(from.getRealmId());
    }

    @Override
    public <E extends IIdentityProviderMapper> E into(E into) {
        into.from(this);
        return into;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached IdentityProviderMapperRecord
     */
    public IdentityProviderMapperRecord() {
        super(IdentityProviderMapper.IDENTITY_PROVIDER_MAPPER);
    }

    /**
     * Create a detached, initialised IdentityProviderMapperRecord
     */
    public IdentityProviderMapperRecord(String id, String name, String idpAlias, String idpMapperName, String realmId) {
        super(IdentityProviderMapper.IDENTITY_PROVIDER_MAPPER);

        setId(id);
        setName(name);
        setIdpAlias(idpAlias);
        setIdpMapperName(idpMapperName);
        setRealmId(realmId);
    }

    /**
     * Create a detached, initialised IdentityProviderMapperRecord
     */
    public IdentityProviderMapperRecord(com.hiddenswitch.framework.schema.keycloak.tables.pojos.IdentityProviderMapper value) {
        super(IdentityProviderMapper.IDENTITY_PROVIDER_MAPPER);

        if (value != null) {
            setId(value.getId());
            setName(value.getName());
            setIdpAlias(value.getIdpAlias());
            setIdpMapperName(value.getIdpMapperName());
            setRealmId(value.getRealmId());
        }
    }

        public IdentityProviderMapperRecord(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }
}
