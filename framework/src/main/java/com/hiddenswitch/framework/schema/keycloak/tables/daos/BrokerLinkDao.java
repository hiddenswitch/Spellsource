/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.daos;


import com.hiddenswitch.framework.schema.keycloak.tables.BrokerLink;
import com.hiddenswitch.framework.schema.keycloak.tables.records.BrokerLinkRecord;

import io.github.jklingsporn.vertx.jooq.shared.reactive.AbstractReactiveVertxDAO;

import java.util.Collection;

import org.jooq.Configuration;
import org.jooq.Record2;


import java.util.List;
import io.vertx.core.Future;
import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicQueryExecutor;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class BrokerLinkDao extends AbstractReactiveVertxDAO<BrokerLinkRecord, com.hiddenswitch.framework.schema.keycloak.tables.pojos.BrokerLink, Record2<String, String>, Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.BrokerLink>>, Future<com.hiddenswitch.framework.schema.keycloak.tables.pojos.BrokerLink>, Future<Integer>, Future<Record2<String, String>>> implements io.github.jklingsporn.vertx.jooq.classic.VertxDAO<BrokerLinkRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.BrokerLink,Record2<String, String>> {

    /**
     * @param configuration Used for rendering, so only SQLDialect must be set and must be one of the POSTGREs types.
     * @param delegate A configured AsyncSQLClient that is used for query execution
     */
    public BrokerLinkDao(Configuration configuration, io.vertx.sqlclient.SqlClient delegate) {
        super(BrokerLink.BROKER_LINK, com.hiddenswitch.framework.schema.keycloak.tables.pojos.BrokerLink.class, new ReactiveClassicQueryExecutor<BrokerLinkRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.BrokerLink,Record2<String, String>>(configuration,delegate,com.hiddenswitch.framework.schema.keycloak.tables.mappers.RowMappers.getBrokerLinkMapper()));
    }

    @Override
    protected Record2<String, String> getId(com.hiddenswitch.framework.schema.keycloak.tables.pojos.BrokerLink object) {
        return compositeKeyRecord(object.getIdentityProvider(), object.getUserId());
    }

    /**
     * Find records that have <code>storage_provider_id IN (values)</code> asynchronously
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.BrokerLink>> findManyByStorageProviderId(Collection<String> values) {
        return findManyByCondition(BrokerLink.BROKER_LINK.STORAGE_PROVIDER_ID.in(values));
    }

    /**
     * Find records that have <code>storage_provider_id IN (values)</code> asynchronously limited by the given limit
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.BrokerLink>> findManyByStorageProviderId(Collection<String> values, int limit) {
        return findManyByCondition(BrokerLink.BROKER_LINK.STORAGE_PROVIDER_ID.in(values),limit);
    }

    /**
     * Find records that have <code>realm_id IN (values)</code> asynchronously
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.BrokerLink>> findManyByRealmId(Collection<String> values) {
        return findManyByCondition(BrokerLink.BROKER_LINK.REALM_ID.in(values));
    }

    /**
     * Find records that have <code>realm_id IN (values)</code> asynchronously limited by the given limit
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.BrokerLink>> findManyByRealmId(Collection<String> values, int limit) {
        return findManyByCondition(BrokerLink.BROKER_LINK.REALM_ID.in(values),limit);
    }

    /**
     * Find records that have <code>broker_user_id IN (values)</code> asynchronously
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.BrokerLink>> findManyByBrokerUserId(Collection<String> values) {
        return findManyByCondition(BrokerLink.BROKER_LINK.BROKER_USER_ID.in(values));
    }

    /**
     * Find records that have <code>broker_user_id IN (values)</code> asynchronously limited by the given limit
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.BrokerLink>> findManyByBrokerUserId(Collection<String> values, int limit) {
        return findManyByCondition(BrokerLink.BROKER_LINK.BROKER_USER_ID.in(values),limit);
    }

    /**
     * Find records that have <code>broker_username IN (values)</code> asynchronously
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.BrokerLink>> findManyByBrokerUsername(Collection<String> values) {
        return findManyByCondition(BrokerLink.BROKER_LINK.BROKER_USERNAME.in(values));
    }

    /**
     * Find records that have <code>broker_username IN (values)</code> asynchronously limited by the given limit
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.BrokerLink>> findManyByBrokerUsername(Collection<String> values, int limit) {
        return findManyByCondition(BrokerLink.BROKER_LINK.BROKER_USERNAME.in(values),limit);
    }

    /**
     * Find records that have <code>token IN (values)</code> asynchronously
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.BrokerLink>> findManyByToken(Collection<String> values) {
        return findManyByCondition(BrokerLink.BROKER_LINK.TOKEN.in(values));
    }

    /**
     * Find records that have <code>token IN (values)</code> asynchronously limited by the given limit
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.BrokerLink>> findManyByToken(Collection<String> values, int limit) {
        return findManyByCondition(BrokerLink.BROKER_LINK.TOKEN.in(values),limit);
    }

    /**
     * Find records that have <code>user_id IN (values)</code> asynchronously
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.BrokerLink>> findManyByUserId(Collection<String> values) {
        return findManyByCondition(BrokerLink.BROKER_LINK.USER_ID.in(values));
    }

    /**
     * Find records that have <code>user_id IN (values)</code> asynchronously limited by the given limit
     */
    public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.BrokerLink>> findManyByUserId(Collection<String> values, int limit) {
        return findManyByCondition(BrokerLink.BROKER_LINK.USER_ID.in(values),limit);
    }

    @Override
    public ReactiveClassicQueryExecutor<BrokerLinkRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.BrokerLink,Record2<String, String>> queryExecutor(){
        return (ReactiveClassicQueryExecutor<BrokerLinkRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.BrokerLink,Record2<String, String>>) super.queryExecutor();
    }
}