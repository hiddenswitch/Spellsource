/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.daos;


import com.hiddenswitch.framework.schema.keycloak.tables.ClientSession;
import com.hiddenswitch.framework.schema.keycloak.tables.records.ClientSessionRecord;

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
public class ClientSessionDao extends AbstractReactiveVertxDAO<ClientSessionRecord, com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession, String, Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession>>, Future<com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession>, Future<Integer>, Future<String>> implements io.github.jklingsporn.vertx.jooq.classic.VertxDAO<ClientSessionRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession,String> {

        /**
     * @param configuration Used for rendering, so only SQLDialect must be set
     * and must be one of the POSTGREs types.
     * @param delegate A configured AsyncSQLClient that is used for query
     * execution
     */
        public ClientSessionDao(Configuration configuration, io.vertx.sqlclient.SqlClient delegate) {
                super(ClientSession.CLIENT_SESSION, com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession.class, new ReactiveClassicQueryExecutor<ClientSessionRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession,String>(configuration,delegate,com.hiddenswitch.framework.schema.keycloak.tables.mappers.RowMappers.getClientSessionMapper()));
        }

        @Override
        protected String getId(com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession object) {
                return object.getId();
        }

        /**
     * Find records that have <code>client_id IN (values)</code> asynchronously
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession>> findManyByClientId(Collection<String> values) {
                return findManyByCondition(ClientSession.CLIENT_SESSION.CLIENT_ID.in(values));
        }

        /**
     * Find records that have <code>client_id IN (values)</code> asynchronously
     * limited by the given limit
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession>> findManyByClientId(Collection<String> values, int limit) {
                return findManyByCondition(ClientSession.CLIENT_SESSION.CLIENT_ID.in(values),limit);
        }

        /**
     * Find records that have <code>redirect_uri IN (values)</code>
     * asynchronously
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession>> findManyByRedirectUri(Collection<String> values) {
                return findManyByCondition(ClientSession.CLIENT_SESSION.REDIRECT_URI.in(values));
        }

        /**
     * Find records that have <code>redirect_uri IN (values)</code>
     * asynchronously limited by the given limit
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession>> findManyByRedirectUri(Collection<String> values, int limit) {
                return findManyByCondition(ClientSession.CLIENT_SESSION.REDIRECT_URI.in(values),limit);
        }

        /**
     * Find records that have <code>state IN (values)</code> asynchronously
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession>> findManyByState(Collection<String> values) {
                return findManyByCondition(ClientSession.CLIENT_SESSION.STATE.in(values));
        }

        /**
     * Find records that have <code>state IN (values)</code> asynchronously
     * limited by the given limit
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession>> findManyByState(Collection<String> values, int limit) {
                return findManyByCondition(ClientSession.CLIENT_SESSION.STATE.in(values),limit);
        }

        /**
     * Find records that have <code>timestamp IN (values)</code> asynchronously
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession>> findManyByTimestamp(Collection<Integer> values) {
                return findManyByCondition(ClientSession.CLIENT_SESSION.TIMESTAMP.in(values));
        }

        /**
     * Find records that have <code>timestamp IN (values)</code> asynchronously
     * limited by the given limit
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession>> findManyByTimestamp(Collection<Integer> values, int limit) {
                return findManyByCondition(ClientSession.CLIENT_SESSION.TIMESTAMP.in(values),limit);
        }

        /**
     * Find records that have <code>session_id IN (values)</code> asynchronously
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession>> findManyBySessionId(Collection<String> values) {
                return findManyByCondition(ClientSession.CLIENT_SESSION.SESSION_ID.in(values));
        }

        /**
     * Find records that have <code>session_id IN (values)</code> asynchronously
     * limited by the given limit
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession>> findManyBySessionId(Collection<String> values, int limit) {
                return findManyByCondition(ClientSession.CLIENT_SESSION.SESSION_ID.in(values),limit);
        }

        /**
     * Find records that have <code>auth_method IN (values)</code>
     * asynchronously
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession>> findManyByAuthMethod(Collection<String> values) {
                return findManyByCondition(ClientSession.CLIENT_SESSION.AUTH_METHOD.in(values));
        }

        /**
     * Find records that have <code>auth_method IN (values)</code>
     * asynchronously limited by the given limit
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession>> findManyByAuthMethod(Collection<String> values, int limit) {
                return findManyByCondition(ClientSession.CLIENT_SESSION.AUTH_METHOD.in(values),limit);
        }

        /**
     * Find records that have <code>realm_id IN (values)</code> asynchronously
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession>> findManyByRealmId(Collection<String> values) {
                return findManyByCondition(ClientSession.CLIENT_SESSION.REALM_ID.in(values));
        }

        /**
     * Find records that have <code>realm_id IN (values)</code> asynchronously
     * limited by the given limit
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession>> findManyByRealmId(Collection<String> values, int limit) {
                return findManyByCondition(ClientSession.CLIENT_SESSION.REALM_ID.in(values),limit);
        }

        /**
     * Find records that have <code>auth_user_id IN (values)</code>
     * asynchronously
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession>> findManyByAuthUserId(Collection<String> values) {
                return findManyByCondition(ClientSession.CLIENT_SESSION.AUTH_USER_ID.in(values));
        }

        /**
     * Find records that have <code>auth_user_id IN (values)</code>
     * asynchronously limited by the given limit
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession>> findManyByAuthUserId(Collection<String> values, int limit) {
                return findManyByCondition(ClientSession.CLIENT_SESSION.AUTH_USER_ID.in(values),limit);
        }

        /**
     * Find records that have <code>current_action IN (values)</code>
     * asynchronously
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession>> findManyByCurrentAction(Collection<String> values) {
                return findManyByCondition(ClientSession.CLIENT_SESSION.CURRENT_ACTION.in(values));
        }

        /**
     * Find records that have <code>current_action IN (values)</code>
     * asynchronously limited by the given limit
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession>> findManyByCurrentAction(Collection<String> values, int limit) {
                return findManyByCondition(ClientSession.CLIENT_SESSION.CURRENT_ACTION.in(values),limit);
        }

        @Override
        public ReactiveClassicQueryExecutor<ClientSessionRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession,String> queryExecutor(){
                return (ReactiveClassicQueryExecutor<ClientSessionRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession,String>) super.queryExecutor();
        }
}
