/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.records;


import com.hiddenswitch.framework.schema.keycloak.tables.UserFederationConfig;
import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IUserFederationConfig;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class UserFederationConfigRecord extends UpdatableRecordImpl<UserFederationConfigRecord> implements VertxPojo, Record3<String, String, String>, IUserFederationConfig {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for
     * <code>keycloak.user_federation_config.user_federation_provider_id</code>.
     */
    @Override
    public UserFederationConfigRecord setUserFederationProviderId(String value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for
     * <code>keycloak.user_federation_config.user_federation_provider_id</code>.
     */
    @Override
    public String getUserFederationProviderId() {
        return (String) get(0);
    }

    /**
     * Setter for <code>keycloak.user_federation_config.value</code>.
     */
    @Override
    public UserFederationConfigRecord setValue(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.user_federation_config.value</code>.
     */
    @Override
    public String getValue() {
        return (String) get(1);
    }

    /**
     * Setter for <code>keycloak.user_federation_config.name</code>.
     */
    @Override
    public UserFederationConfigRecord setName(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.user_federation_config.name</code>.
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
        return UserFederationConfig.USER_FEDERATION_CONFIG.USER_FEDERATION_PROVIDER_ID;
    }

    @Override
    public Field<String> field2() {
        return UserFederationConfig.USER_FEDERATION_CONFIG.VALUE;
    }

    @Override
    public Field<String> field3() {
        return UserFederationConfig.USER_FEDERATION_CONFIG.NAME;
    }

    @Override
    public String component1() {
        return getUserFederationProviderId();
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
        return getUserFederationProviderId();
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
    public UserFederationConfigRecord value1(String value) {
        setUserFederationProviderId(value);
        return this;
    }

    @Override
    public UserFederationConfigRecord value2(String value) {
        setValue(value);
        return this;
    }

    @Override
    public UserFederationConfigRecord value3(String value) {
        setName(value);
        return this;
    }

    @Override
    public UserFederationConfigRecord values(String value1, String value2, String value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IUserFederationConfig from) {
        setUserFederationProviderId(from.getUserFederationProviderId());
        setValue(from.getValue());
        setName(from.getName());
    }

    @Override
    public <E extends IUserFederationConfig> E into(E into) {
        into.from(this);
        return into;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached UserFederationConfigRecord
     */
    public UserFederationConfigRecord() {
        super(UserFederationConfig.USER_FEDERATION_CONFIG);
    }

    /**
     * Create a detached, initialised UserFederationConfigRecord
     */
    public UserFederationConfigRecord(String userFederationProviderId, String value, String name) {
        super(UserFederationConfig.USER_FEDERATION_CONFIG);

        setUserFederationProviderId(userFederationProviderId);
        setValue(value);
        setName(name);
    }

    /**
     * Create a detached, initialised UserFederationConfigRecord
     */
    public UserFederationConfigRecord(com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserFederationConfig value) {
        super(UserFederationConfig.USER_FEDERATION_CONFIG);

        if (value != null) {
            setUserFederationProviderId(value.getUserFederationProviderId());
            setValue(value.getValue());
            setName(value.getName());
        }
    }

        public UserFederationConfigRecord(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }
}
