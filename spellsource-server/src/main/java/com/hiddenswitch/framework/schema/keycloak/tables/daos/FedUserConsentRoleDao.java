/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.daos;


import com.hiddenswitch.framework.schema.keycloak.tables.FedUserConsentRole;
import com.hiddenswitch.framework.schema.keycloak.tables.records.FedUserConsentRoleRecord;

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
public class FedUserConsentRoleDao extends AbstractReactiveVertxDAO<FedUserConsentRoleRecord, com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserConsentRole, Record2<String, String>, Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserConsentRole>>, Future<com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserConsentRole>, Future<Integer>, Future<Record2<String, String>>> implements io.github.jklingsporn.vertx.jooq.classic.VertxDAO<FedUserConsentRoleRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserConsentRole,Record2<String, String>> {

        /**
     * @param configuration Used for rendering, so only SQLDialect must be set
     * and must be one of the POSTGREs types.
     * @param delegate A configured AsyncSQLClient that is used for query
     * execution
     */
        public FedUserConsentRoleDao(Configuration configuration, io.vertx.sqlclient.SqlClient delegate) {
                super(FedUserConsentRole.FED_USER_CONSENT_ROLE, com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserConsentRole.class, new ReactiveClassicQueryExecutor<FedUserConsentRoleRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserConsentRole,Record2<String, String>>(configuration,delegate,com.hiddenswitch.framework.schema.keycloak.tables.mappers.RowMappers.getFedUserConsentRoleMapper()));
        }

        @Override
        protected Record2<String, String> getId(com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserConsentRole object) {
                return compositeKeyRecord(object.getUserConsentId(), object.getRoleId());
        }

        /**
     * Find records that have <code>role_id IN (values)</code> asynchronously
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserConsentRole>> findManyByRoleId(Collection<String> values) {
                return findManyByCondition(FedUserConsentRole.FED_USER_CONSENT_ROLE.ROLE_ID.in(values));
        }

        /**
     * Find records that have <code>role_id IN (values)</code> asynchronously
     * limited by the given limit
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserConsentRole>> findManyByRoleId(Collection<String> values, int limit) {
                return findManyByCondition(FedUserConsentRole.FED_USER_CONSENT_ROLE.ROLE_ID.in(values),limit);
        }

        @Override
        public ReactiveClassicQueryExecutor<FedUserConsentRoleRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserConsentRole,Record2<String, String>> queryExecutor(){
                return (ReactiveClassicQueryExecutor<FedUserConsentRoleRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserConsentRole,Record2<String, String>>) super.queryExecutor();
        }
}