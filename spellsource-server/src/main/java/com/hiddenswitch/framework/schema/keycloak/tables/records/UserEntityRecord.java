/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.records;


import com.hiddenswitch.framework.schema.keycloak.tables.UserEntity;
import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IUserEntity;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record13;
import org.jooq.Row13;
import org.jooq.impl.UpdatableRecordImpl;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class UserEntityRecord extends UpdatableRecordImpl<UserEntityRecord> implements VertxPojo, Record13<String, String, String, Boolean, Boolean, String, String, String, String, String, Long, String, Integer>, IUserEntity {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>keycloak.user_entity.id</code>.
     */
    @Override
    public UserEntityRecord setId(String value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.user_entity.id</code>.
     */
    @Override
    public String getId() {
        return (String) get(0);
    }

    /**
     * Setter for <code>keycloak.user_entity.email</code>.
     */
    @Override
    public UserEntityRecord setEmail(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.user_entity.email</code>.
     */
    @Override
    public String getEmail() {
        return (String) get(1);
    }

    /**
     * Setter for <code>keycloak.user_entity.email_constraint</code>.
     */
    @Override
    public UserEntityRecord setEmailConstraint(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.user_entity.email_constraint</code>.
     */
    @Override
    public String getEmailConstraint() {
        return (String) get(2);
    }

    /**
     * Setter for <code>keycloak.user_entity.email_verified</code>.
     */
    @Override
    public UserEntityRecord setEmailVerified(Boolean value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.user_entity.email_verified</code>.
     */
    @Override
    public Boolean getEmailVerified() {
        return (Boolean) get(3);
    }

    /**
     * Setter for <code>keycloak.user_entity.enabled</code>.
     */
    @Override
    public UserEntityRecord setEnabled(Boolean value) {
        set(4, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.user_entity.enabled</code>.
     */
    @Override
    public Boolean getEnabled() {
        return (Boolean) get(4);
    }

    /**
     * Setter for <code>keycloak.user_entity.federation_link</code>.
     */
    @Override
    public UserEntityRecord setFederationLink(String value) {
        set(5, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.user_entity.federation_link</code>.
     */
    @Override
    public String getFederationLink() {
        return (String) get(5);
    }

    /**
     * Setter for <code>keycloak.user_entity.first_name</code>.
     */
    @Override
    public UserEntityRecord setFirstName(String value) {
        set(6, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.user_entity.first_name</code>.
     */
    @Override
    public String getFirstName() {
        return (String) get(6);
    }

    /**
     * Setter for <code>keycloak.user_entity.last_name</code>.
     */
    @Override
    public UserEntityRecord setLastName(String value) {
        set(7, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.user_entity.last_name</code>.
     */
    @Override
    public String getLastName() {
        return (String) get(7);
    }

    /**
     * Setter for <code>keycloak.user_entity.realm_id</code>.
     */
    @Override
    public UserEntityRecord setRealmId(String value) {
        set(8, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.user_entity.realm_id</code>.
     */
    @Override
    public String getRealmId() {
        return (String) get(8);
    }

    /**
     * Setter for <code>keycloak.user_entity.username</code>.
     */
    @Override
    public UserEntityRecord setUsername(String value) {
        set(9, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.user_entity.username</code>.
     */
    @Override
    public String getUsername() {
        return (String) get(9);
    }

    /**
     * Setter for <code>keycloak.user_entity.created_timestamp</code>.
     */
    @Override
    public UserEntityRecord setCreatedTimestamp(Long value) {
        set(10, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.user_entity.created_timestamp</code>.
     */
    @Override
    public Long getCreatedTimestamp() {
        return (Long) get(10);
    }

    /**
     * Setter for <code>keycloak.user_entity.service_account_client_link</code>.
     */
    @Override
    public UserEntityRecord setServiceAccountClientLink(String value) {
        set(11, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.user_entity.service_account_client_link</code>.
     */
    @Override
    public String getServiceAccountClientLink() {
        return (String) get(11);
    }

    /**
     * Setter for <code>keycloak.user_entity.not_before</code>.
     */
    @Override
    public UserEntityRecord setNotBefore(Integer value) {
        set(12, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.user_entity.not_before</code>.
     */
    @Override
    public Integer getNotBefore() {
        return (Integer) get(12);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<String> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record13 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row13<String, String, String, Boolean, Boolean, String, String, String, String, String, Long, String, Integer> fieldsRow() {
        return (Row13) super.fieldsRow();
    }

    @Override
    public Row13<String, String, String, Boolean, Boolean, String, String, String, String, String, Long, String, Integer> valuesRow() {
        return (Row13) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return UserEntity.USER_ENTITY.ID;
    }

    @Override
    public Field<String> field2() {
        return UserEntity.USER_ENTITY.EMAIL;
    }

    @Override
    public Field<String> field3() {
        return UserEntity.USER_ENTITY.EMAIL_CONSTRAINT;
    }

    @Override
    public Field<Boolean> field4() {
        return UserEntity.USER_ENTITY.EMAIL_VERIFIED;
    }

    @Override
    public Field<Boolean> field5() {
        return UserEntity.USER_ENTITY.ENABLED;
    }

    @Override
    public Field<String> field6() {
        return UserEntity.USER_ENTITY.FEDERATION_LINK;
    }

    @Override
    public Field<String> field7() {
        return UserEntity.USER_ENTITY.FIRST_NAME;
    }

    @Override
    public Field<String> field8() {
        return UserEntity.USER_ENTITY.LAST_NAME;
    }

    @Override
    public Field<String> field9() {
        return UserEntity.USER_ENTITY.REALM_ID;
    }

    @Override
    public Field<String> field10() {
        return UserEntity.USER_ENTITY.USERNAME;
    }

    @Override
    public Field<Long> field11() {
        return UserEntity.USER_ENTITY.CREATED_TIMESTAMP;
    }

    @Override
    public Field<String> field12() {
        return UserEntity.USER_ENTITY.SERVICE_ACCOUNT_CLIENT_LINK;
    }

    @Override
    public Field<Integer> field13() {
        return UserEntity.USER_ENTITY.NOT_BEFORE;
    }

    @Override
    public String component1() {
        return getId();
    }

    @Override
    public String component2() {
        return getEmail();
    }

    @Override
    public String component3() {
        return getEmailConstraint();
    }

    @Override
    public Boolean component4() {
        return getEmailVerified();
    }

    @Override
    public Boolean component5() {
        return getEnabled();
    }

    @Override
    public String component6() {
        return getFederationLink();
    }

    @Override
    public String component7() {
        return getFirstName();
    }

    @Override
    public String component8() {
        return getLastName();
    }

    @Override
    public String component9() {
        return getRealmId();
    }

    @Override
    public String component10() {
        return getUsername();
    }

    @Override
    public Long component11() {
        return getCreatedTimestamp();
    }

    @Override
    public String component12() {
        return getServiceAccountClientLink();
    }

    @Override
    public Integer component13() {
        return getNotBefore();
    }

    @Override
    public String value1() {
        return getId();
    }

    @Override
    public String value2() {
        return getEmail();
    }

    @Override
    public String value3() {
        return getEmailConstraint();
    }

    @Override
    public Boolean value4() {
        return getEmailVerified();
    }

    @Override
    public Boolean value5() {
        return getEnabled();
    }

    @Override
    public String value6() {
        return getFederationLink();
    }

    @Override
    public String value7() {
        return getFirstName();
    }

    @Override
    public String value8() {
        return getLastName();
    }

    @Override
    public String value9() {
        return getRealmId();
    }

    @Override
    public String value10() {
        return getUsername();
    }

    @Override
    public Long value11() {
        return getCreatedTimestamp();
    }

    @Override
    public String value12() {
        return getServiceAccountClientLink();
    }

    @Override
    public Integer value13() {
        return getNotBefore();
    }

    @Override
    public UserEntityRecord value1(String value) {
        setId(value);
        return this;
    }

    @Override
    public UserEntityRecord value2(String value) {
        setEmail(value);
        return this;
    }

    @Override
    public UserEntityRecord value3(String value) {
        setEmailConstraint(value);
        return this;
    }

    @Override
    public UserEntityRecord value4(Boolean value) {
        setEmailVerified(value);
        return this;
    }

    @Override
    public UserEntityRecord value5(Boolean value) {
        setEnabled(value);
        return this;
    }

    @Override
    public UserEntityRecord value6(String value) {
        setFederationLink(value);
        return this;
    }

    @Override
    public UserEntityRecord value7(String value) {
        setFirstName(value);
        return this;
    }

    @Override
    public UserEntityRecord value8(String value) {
        setLastName(value);
        return this;
    }

    @Override
    public UserEntityRecord value9(String value) {
        setRealmId(value);
        return this;
    }

    @Override
    public UserEntityRecord value10(String value) {
        setUsername(value);
        return this;
    }

    @Override
    public UserEntityRecord value11(Long value) {
        setCreatedTimestamp(value);
        return this;
    }

    @Override
    public UserEntityRecord value12(String value) {
        setServiceAccountClientLink(value);
        return this;
    }

    @Override
    public UserEntityRecord value13(Integer value) {
        setNotBefore(value);
        return this;
    }

    @Override
    public UserEntityRecord values(String value1, String value2, String value3, Boolean value4, Boolean value5, String value6, String value7, String value8, String value9, String value10, Long value11, String value12, Integer value13) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        value9(value9);
        value10(value10);
        value11(value11);
        value12(value12);
        value13(value13);
        return this;
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IUserEntity from) {
        setId(from.getId());
        setEmail(from.getEmail());
        setEmailConstraint(from.getEmailConstraint());
        setEmailVerified(from.getEmailVerified());
        setEnabled(from.getEnabled());
        setFederationLink(from.getFederationLink());
        setFirstName(from.getFirstName());
        setLastName(from.getLastName());
        setRealmId(from.getRealmId());
        setUsername(from.getUsername());
        setCreatedTimestamp(from.getCreatedTimestamp());
        setServiceAccountClientLink(from.getServiceAccountClientLink());
        setNotBefore(from.getNotBefore());
    }

    @Override
    public <E extends IUserEntity> E into(E into) {
        into.from(this);
        return into;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached UserEntityRecord
     */
    public UserEntityRecord() {
        super(UserEntity.USER_ENTITY);
    }

    /**
     * Create a detached, initialised UserEntityRecord
     */
    public UserEntityRecord(String id, String email, String emailConstraint, Boolean emailVerified, Boolean enabled, String federationLink, String firstName, String lastName, String realmId, String username, Long createdTimestamp, String serviceAccountClientLink, Integer notBefore) {
        super(UserEntity.USER_ENTITY);

        setId(id);
        setEmail(email);
        setEmailConstraint(emailConstraint);
        setEmailVerified(emailVerified);
        setEnabled(enabled);
        setFederationLink(federationLink);
        setFirstName(firstName);
        setLastName(lastName);
        setRealmId(realmId);
        setUsername(username);
        setCreatedTimestamp(createdTimestamp);
        setServiceAccountClientLink(serviceAccountClientLink);
        setNotBefore(notBefore);
    }

    /**
     * Create a detached, initialised UserEntityRecord
     */
    public UserEntityRecord(com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity value) {
        super(UserEntity.USER_ENTITY);

        if (value != null) {
            setId(value.getId());
            setEmail(value.getEmail());
            setEmailConstraint(value.getEmailConstraint());
            setEmailVerified(value.getEmailVerified());
            setEnabled(value.getEnabled());
            setFederationLink(value.getFederationLink());
            setFirstName(value.getFirstName());
            setLastName(value.getLastName());
            setRealmId(value.getRealmId());
            setUsername(value.getUsername());
            setCreatedTimestamp(value.getCreatedTimestamp());
            setServiceAccountClientLink(value.getServiceAccountClientLink());
            setNotBefore(value.getNotBefore());
        }
    }

        public UserEntityRecord(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }
}
