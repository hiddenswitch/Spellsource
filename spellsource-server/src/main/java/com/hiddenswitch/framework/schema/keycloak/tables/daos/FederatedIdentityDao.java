/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.daos;


import com.hiddenswitch.framework.schema.keycloak.tables.FederatedIdentity;
import com.hiddenswitch.framework.schema.keycloak.tables.records.FederatedIdentityRecord;

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
public class FederatedIdentityDao extends AbstractReactiveVertxDAO<FederatedIdentityRecord, com.hiddenswitch.framework.schema.keycloak.tables.pojos.FederatedIdentity, Record2<String, String>, Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.FederatedIdentity>>, Future<com.hiddenswitch.framework.schema.keycloak.tables.pojos.FederatedIdentity>, Future<Integer>, Future<Record2<String, String>>> implements io.github.jklingsporn.vertx.jooq.classic.VertxDAO<FederatedIdentityRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.FederatedIdentity,Record2<String, String>> {

        /**
     * @param configuration Used for rendering, so only SQLDialect must be set
     * and must be one of the POSTGREs types.
     * @param delegate A configured AsyncSQLClient that is used for query
     * execution
     */
        public FederatedIdentityDao(Configuration configuration, io.vertx.sqlclient.SqlClient delegate) {
                super(FederatedIdentity.FEDERATED_IDENTITY, com.hiddenswitch.framework.schema.keycloak.tables.pojos.FederatedIdentity.class, new ReactiveClassicQueryExecutor<FederatedIdentityRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.FederatedIdentity,Record2<String, String>>(configuration,delegate,com.hiddenswitch.framework.schema.keycloak.tables.mappers.RowMappers.getFederatedIdentityMapper()));
        }

        @Override
        protected Record2<String, String> getId(com.hiddenswitch.framework.schema.keycloak.tables.pojos.FederatedIdentity object) {
                return compositeKeyRecord(object.getIdentityProvider(), object.getUserId());
        }

        /**
     * Find records that have <code>realm_id IN (values)</code> asynchronously
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.FederatedIdentity>> findManyByRealmId(Collection<String> values) {
                return findManyByCondition(FederatedIdentity.FEDERATED_IDENTITY.REALM_ID.in(values));
        }

        /**
     * Find records that have <code>realm_id IN (values)</code> asynchronously
     * limited by the given limit
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.FederatedIdentity>> findManyByRealmId(Collection<String> values, int limit) {
                return findManyByCondition(FederatedIdentity.FEDERATED_IDENTITY.REALM_ID.in(values),limit);
        }

        /**
     * Find records that have <code>federated_user_id IN (values)</code>
     * asynchronously
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.FederatedIdentity>> findManyByFederatedUserId(Collection<String> values) {
                return findManyByCondition(FederatedIdentity.FEDERATED_IDENTITY.FEDERATED_USER_ID.in(values));
        }

        /**
     * Find records that have <code>federated_user_id IN (values)</code>
     * asynchronously limited by the given limit
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.FederatedIdentity>> findManyByFederatedUserId(Collection<String> values, int limit) {
                return findManyByCondition(FederatedIdentity.FEDERATED_IDENTITY.FEDERATED_USER_ID.in(values),limit);
        }

        /**
     * Find records that have <code>federated_username IN (values)</code>
     * asynchronously
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.FederatedIdentity>> findManyByFederatedUsername(Collection<String> values) {
                return findManyByCondition(FederatedIdentity.FEDERATED_IDENTITY.FEDERATED_USERNAME.in(values));
        }

        /**
     * Find records that have <code>federated_username IN (values)</code>
     * asynchronously limited by the given limit
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.FederatedIdentity>> findManyByFederatedUsername(Collection<String> values, int limit) {
                return findManyByCondition(FederatedIdentity.FEDERATED_IDENTITY.FEDERATED_USERNAME.in(values),limit);
        }

        /**
     * Find records that have <code>token IN (values)</code> asynchronously
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.FederatedIdentity>> findManyByToken(Collection<String> values) {
                return findManyByCondition(FederatedIdentity.FEDERATED_IDENTITY.TOKEN.in(values));
        }

        /**
     * Find records that have <code>token IN (values)</code> asynchronously
     * limited by the given limit
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.FederatedIdentity>> findManyByToken(Collection<String> values, int limit) {
                return findManyByCondition(FederatedIdentity.FEDERATED_IDENTITY.TOKEN.in(values),limit);
        }

        /**
     * Find records that have <code>user_id IN (values)</code> asynchronously
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.FederatedIdentity>> findManyByUserId(Collection<String> values) {
                return findManyByCondition(FederatedIdentity.FEDERATED_IDENTITY.USER_ID.in(values));
        }

        /**
     * Find records that have <code>user_id IN (values)</code> asynchronously
     * limited by the given limit
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.FederatedIdentity>> findManyByUserId(Collection<String> values, int limit) {
                return findManyByCondition(FederatedIdentity.FEDERATED_IDENTITY.USER_ID.in(values),limit);
        }

        @Override
        public ReactiveClassicQueryExecutor<FederatedIdentityRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.FederatedIdentity,Record2<String, String>> queryExecutor(){
                return (ReactiveClassicQueryExecutor<FederatedIdentityRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.FederatedIdentity,Record2<String, String>>) super.queryExecutor();
        }
}
