/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.daos;


import com.hiddenswitch.framework.schema.keycloak.tables.UserEntity;
import com.hiddenswitch.framework.schema.keycloak.tables.records.UserEntityRecord;

import io.github.jklingsporn.vertx.jooq.shared.reactive.AbstractReactiveVertxDAO;

import java.util.Collection;

import org.jooq.Configuration;


import java.util.List;
import io.vertx.core.Future;
import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicQueryExecutor;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class UserEntityDao extends AbstractReactiveVertxDAO<UserEntityRecord, com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity, String, Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity>>, Future<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity>, Future<Integer>, Future<String>> implements io.github.jklingsporn.vertx.jooq.classic.VertxDAO<UserEntityRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity,String> {

    /**
     * @param configuration Used for rendering, so only SQLDialect must be set and must be one of the POSTGREs types.
     * @param delegate A configured AsyncSQLClient that is used for query execution
     */
    public UserEntityDao(Configuration configuration, io.vertx.sqlclient.SqlClient delegate) {
        super(UserEntity.USER_ENTITY, com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity.class, new ReactiveClassicQueryExecutor<UserEntityRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity,String>(configuration,delegate,com.hiddenswitch.framework.schema.keycloak.tables.mappers.RowMappers.getUserEntityMapper()));
    }

    @Override
    protected String getId(com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity object) {
        return object.getId();
    }

    /**
     * Find records that have <code>email IN (values)</code> asynchronously
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity>> findManyByEmail(Collection<String> values) {
        return findManyByCondition(UserEntity.USER_ENTITY.EMAIL.in(values));
    }

    /**
     * Find records that have <code>email IN (values)</code> asynchronously limited by the given limit
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity>> findManyByEmail(Collection<String> values, int limit) {
        return findManyByCondition(UserEntity.USER_ENTITY.EMAIL.in(values),limit);
    }

    /**
     * Find records that have <code>email_constraint IN (values)</code> asynchronously
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity>> findManyByEmailConstraint(Collection<String> values) {
        return findManyByCondition(UserEntity.USER_ENTITY.EMAIL_CONSTRAINT.in(values));
    }

    /**
     * Find records that have <code>email_constraint IN (values)</code> asynchronously limited by the given limit
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity>> findManyByEmailConstraint(Collection<String> values, int limit) {
        return findManyByCondition(UserEntity.USER_ENTITY.EMAIL_CONSTRAINT.in(values),limit);
    }

    /**
     * Find records that have <code>email_verified IN (values)</code> asynchronously
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity>> findManyByEmailVerified(Collection<Boolean> values) {
        return findManyByCondition(UserEntity.USER_ENTITY.EMAIL_VERIFIED.in(values));
    }

    /**
     * Find records that have <code>email_verified IN (values)</code> asynchronously limited by the given limit
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity>> findManyByEmailVerified(Collection<Boolean> values, int limit) {
        return findManyByCondition(UserEntity.USER_ENTITY.EMAIL_VERIFIED.in(values),limit);
    }

    /**
     * Find records that have <code>enabled IN (values)</code> asynchronously
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity>> findManyByEnabled(Collection<Boolean> values) {
        return findManyByCondition(UserEntity.USER_ENTITY.ENABLED.in(values));
    }

    /**
     * Find records that have <code>enabled IN (values)</code> asynchronously limited by the given limit
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity>> findManyByEnabled(Collection<Boolean> values, int limit) {
        return findManyByCondition(UserEntity.USER_ENTITY.ENABLED.in(values),limit);
    }

    /**
     * Find records that have <code>federation_link IN (values)</code> asynchronously
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity>> findManyByFederationLink(Collection<String> values) {
        return findManyByCondition(UserEntity.USER_ENTITY.FEDERATION_LINK.in(values));
    }

    /**
     * Find records that have <code>federation_link IN (values)</code> asynchronously limited by the given limit
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity>> findManyByFederationLink(Collection<String> values, int limit) {
        return findManyByCondition(UserEntity.USER_ENTITY.FEDERATION_LINK.in(values),limit);
    }

    /**
     * Find records that have <code>first_name IN (values)</code> asynchronously
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity>> findManyByFirstName(Collection<String> values) {
        return findManyByCondition(UserEntity.USER_ENTITY.FIRST_NAME.in(values));
    }

    /**
     * Find records that have <code>first_name IN (values)</code> asynchronously limited by the given limit
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity>> findManyByFirstName(Collection<String> values, int limit) {
        return findManyByCondition(UserEntity.USER_ENTITY.FIRST_NAME.in(values),limit);
    }

    /**
     * Find records that have <code>last_name IN (values)</code> asynchronously
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity>> findManyByLastName(Collection<String> values) {
        return findManyByCondition(UserEntity.USER_ENTITY.LAST_NAME.in(values));
    }

    /**
     * Find records that have <code>last_name IN (values)</code> asynchronously limited by the given limit
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity>> findManyByLastName(Collection<String> values, int limit) {
        return findManyByCondition(UserEntity.USER_ENTITY.LAST_NAME.in(values),limit);
    }

    /**
     * Find records that have <code>realm_id IN (values)</code> asynchronously
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity>> findManyByRealmId(Collection<String> values) {
        return findManyByCondition(UserEntity.USER_ENTITY.REALM_ID.in(values));
    }

    /**
     * Find records that have <code>realm_id IN (values)</code> asynchronously limited by the given limit
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity>> findManyByRealmId(Collection<String> values, int limit) {
        return findManyByCondition(UserEntity.USER_ENTITY.REALM_ID.in(values),limit);
    }

    /**
     * Find records that have <code>username IN (values)</code> asynchronously
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity>> findManyByUsername(Collection<String> values) {
        return findManyByCondition(UserEntity.USER_ENTITY.USERNAME.in(values));
    }

    /**
     * Find records that have <code>username IN (values)</code> asynchronously limited by the given limit
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity>> findManyByUsername(Collection<String> values, int limit) {
        return findManyByCondition(UserEntity.USER_ENTITY.USERNAME.in(values),limit);
    }

    /**
     * Find records that have <code>created_timestamp IN (values)</code> asynchronously
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity>> findManyByCreatedTimestamp(Collection<Long> values) {
        return findManyByCondition(UserEntity.USER_ENTITY.CREATED_TIMESTAMP.in(values));
    }

    /**
     * Find records that have <code>created_timestamp IN (values)</code> asynchronously limited by the given limit
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity>> findManyByCreatedTimestamp(Collection<Long> values, int limit) {
        return findManyByCondition(UserEntity.USER_ENTITY.CREATED_TIMESTAMP.in(values),limit);
    }

    /**
     * Find records that have <code>service_account_client_link IN (values)</code> asynchronously
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity>> findManyByServiceAccountClientLink(Collection<String> values) {
        return findManyByCondition(UserEntity.USER_ENTITY.SERVICE_ACCOUNT_CLIENT_LINK.in(values));
    }

    /**
     * Find records that have <code>service_account_client_link IN (values)</code> asynchronously limited by the given limit
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity>> findManyByServiceAccountClientLink(Collection<String> values, int limit) {
        return findManyByCondition(UserEntity.USER_ENTITY.SERVICE_ACCOUNT_CLIENT_LINK.in(values),limit);
    }

    /**
     * Find records that have <code>not_before IN (values)</code> asynchronously
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity>> findManyByNotBefore(Collection<Integer> values) {
        return findManyByCondition(UserEntity.USER_ENTITY.NOT_BEFORE.in(values));
    }

    /**
     * Find records that have <code>not_before IN (values)</code> asynchronously limited by the given limit
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity>> findManyByNotBefore(Collection<Integer> values, int limit) {
        return findManyByCondition(UserEntity.USER_ENTITY.NOT_BEFORE.in(values),limit);
    }

    @Override
    public ReactiveClassicQueryExecutor<UserEntityRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity,String> queryExecutor(){
        return (ReactiveClassicQueryExecutor<UserEntityRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity,String>) super.queryExecutor();
    }
}