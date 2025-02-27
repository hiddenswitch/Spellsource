/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.records;


import com.hiddenswitch.framework.schema.keycloak.tables.BrokerLink;
import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IBrokerLink;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Record7;
import org.jooq.Row7;
import org.jooq.impl.UpdatableRecordImpl;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class BrokerLinkRecord extends UpdatableRecordImpl<BrokerLinkRecord> implements VertxPojo, Record7<String, String, String, String, String, String, String>, IBrokerLink {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>keycloak.broker_link.identity_provider</code>.
     */
    @Override
    public BrokerLinkRecord setIdentityProvider(String value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.broker_link.identity_provider</code>.
     */
    @Override
    public String getIdentityProvider() {
        return (String) get(0);
    }

    /**
     * Setter for <code>keycloak.broker_link.storage_provider_id</code>.
     */
    @Override
    public BrokerLinkRecord setStorageProviderId(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.broker_link.storage_provider_id</code>.
     */
    @Override
    public String getStorageProviderId() {
        return (String) get(1);
    }

    /**
     * Setter for <code>keycloak.broker_link.realm_id</code>.
     */
    @Override
    public BrokerLinkRecord setRealmId(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.broker_link.realm_id</code>.
     */
    @Override
    public String getRealmId() {
        return (String) get(2);
    }

    /**
     * Setter for <code>keycloak.broker_link.broker_user_id</code>.
     */
    @Override
    public BrokerLinkRecord setBrokerUserId(String value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.broker_link.broker_user_id</code>.
     */
    @Override
    public String getBrokerUserId() {
        return (String) get(3);
    }

    /**
     * Setter for <code>keycloak.broker_link.broker_username</code>.
     */
    @Override
    public BrokerLinkRecord setBrokerUsername(String value) {
        set(4, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.broker_link.broker_username</code>.
     */
    @Override
    public String getBrokerUsername() {
        return (String) get(4);
    }

    /**
     * Setter for <code>keycloak.broker_link.token</code>.
     */
    @Override
    public BrokerLinkRecord setToken(String value) {
        set(5, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.broker_link.token</code>.
     */
    @Override
    public String getToken() {
        return (String) get(5);
    }

    /**
     * Setter for <code>keycloak.broker_link.user_id</code>.
     */
    @Override
    public BrokerLinkRecord setUserId(String value) {
        set(6, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.broker_link.user_id</code>.
     */
    @Override
    public String getUserId() {
        return (String) get(6);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record2<String, String> key() {
        return (Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Record7 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row7<String, String, String, String, String, String, String> fieldsRow() {
        return (Row7) super.fieldsRow();
    }

    @Override
    public Row7<String, String, String, String, String, String, String> valuesRow() {
        return (Row7) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return BrokerLink.BROKER_LINK.IDENTITY_PROVIDER;
    }

    @Override
    public Field<String> field2() {
        return BrokerLink.BROKER_LINK.STORAGE_PROVIDER_ID;
    }

    @Override
    public Field<String> field3() {
        return BrokerLink.BROKER_LINK.REALM_ID;
    }

    @Override
    public Field<String> field4() {
        return BrokerLink.BROKER_LINK.BROKER_USER_ID;
    }

    @Override
    public Field<String> field5() {
        return BrokerLink.BROKER_LINK.BROKER_USERNAME;
    }

    @Override
    public Field<String> field6() {
        return BrokerLink.BROKER_LINK.TOKEN;
    }

    @Override
    public Field<String> field7() {
        return BrokerLink.BROKER_LINK.USER_ID;
    }

    @Override
    public String component1() {
        return getIdentityProvider();
    }

    @Override
    public String component2() {
        return getStorageProviderId();
    }

    @Override
    public String component3() {
        return getRealmId();
    }

    @Override
    public String component4() {
        return getBrokerUserId();
    }

    @Override
    public String component5() {
        return getBrokerUsername();
    }

    @Override
    public String component6() {
        return getToken();
    }

    @Override
    public String component7() {
        return getUserId();
    }

    @Override
    public String value1() {
        return getIdentityProvider();
    }

    @Override
    public String value2() {
        return getStorageProviderId();
    }

    @Override
    public String value3() {
        return getRealmId();
    }

    @Override
    public String value4() {
        return getBrokerUserId();
    }

    @Override
    public String value5() {
        return getBrokerUsername();
    }

    @Override
    public String value6() {
        return getToken();
    }

    @Override
    public String value7() {
        return getUserId();
    }

    @Override
    public BrokerLinkRecord value1(String value) {
        setIdentityProvider(value);
        return this;
    }

    @Override
    public BrokerLinkRecord value2(String value) {
        setStorageProviderId(value);
        return this;
    }

    @Override
    public BrokerLinkRecord value3(String value) {
        setRealmId(value);
        return this;
    }

    @Override
    public BrokerLinkRecord value4(String value) {
        setBrokerUserId(value);
        return this;
    }

    @Override
    public BrokerLinkRecord value5(String value) {
        setBrokerUsername(value);
        return this;
    }

    @Override
    public BrokerLinkRecord value6(String value) {
        setToken(value);
        return this;
    }

    @Override
    public BrokerLinkRecord value7(String value) {
        setUserId(value);
        return this;
    }

    @Override
    public BrokerLinkRecord values(String value1, String value2, String value3, String value4, String value5, String value6, String value7) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        return this;
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IBrokerLink from) {
        setIdentityProvider(from.getIdentityProvider());
        setStorageProviderId(from.getStorageProviderId());
        setRealmId(from.getRealmId());
        setBrokerUserId(from.getBrokerUserId());
        setBrokerUsername(from.getBrokerUsername());
        setToken(from.getToken());
        setUserId(from.getUserId());
    }

    @Override
    public <E extends IBrokerLink> E into(E into) {
        into.from(this);
        return into;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached BrokerLinkRecord
     */
    public BrokerLinkRecord() {
        super(BrokerLink.BROKER_LINK);
    }

    /**
     * Create a detached, initialised BrokerLinkRecord
     */
    public BrokerLinkRecord(String identityProvider, String storageProviderId, String realmId, String brokerUserId, String brokerUsername, String token, String userId) {
        super(BrokerLink.BROKER_LINK);

        setIdentityProvider(identityProvider);
        setStorageProviderId(storageProviderId);
        setRealmId(realmId);
        setBrokerUserId(brokerUserId);
        setBrokerUsername(brokerUsername);
        setToken(token);
        setUserId(userId);
    }

    /**
     * Create a detached, initialised BrokerLinkRecord
     */
    public BrokerLinkRecord(com.hiddenswitch.framework.schema.keycloak.tables.pojos.BrokerLink value) {
        super(BrokerLink.BROKER_LINK);

        if (value != null) {
            setIdentityProvider(value.getIdentityProvider());
            setStorageProviderId(value.getStorageProviderId());
            setRealmId(value.getRealmId());
            setBrokerUserId(value.getBrokerUserId());
            setBrokerUsername(value.getBrokerUsername());
            setToken(value.getToken());
            setUserId(value.getUserId());
        }
    }

        public BrokerLinkRecord(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }
}
