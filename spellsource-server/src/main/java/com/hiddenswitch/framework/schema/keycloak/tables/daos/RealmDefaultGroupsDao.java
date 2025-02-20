/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.daos;


import com.hiddenswitch.framework.schema.keycloak.tables.RealmDefaultGroups;
import com.hiddenswitch.framework.schema.keycloak.tables.records.RealmDefaultGroupsRecord;

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
public class RealmDefaultGroupsDao extends AbstractReactiveVertxDAO<RealmDefaultGroupsRecord, com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmDefaultGroups, Record2<String, String>, Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmDefaultGroups>>, Future<com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmDefaultGroups>, Future<Integer>, Future<Record2<String, String>>> implements io.github.jklingsporn.vertx.jooq.classic.VertxDAO<RealmDefaultGroupsRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmDefaultGroups,Record2<String, String>> {

        /**
     * @param configuration Used for rendering, so only SQLDialect must be set
     * and must be one of the POSTGREs types.
     * @param delegate A configured AsyncSQLClient that is used for query
     * execution
     */
        public RealmDefaultGroupsDao(Configuration configuration, io.vertx.sqlclient.SqlClient delegate) {
                super(RealmDefaultGroups.REALM_DEFAULT_GROUPS, com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmDefaultGroups.class, new ReactiveClassicQueryExecutor<RealmDefaultGroupsRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmDefaultGroups,Record2<String, String>>(configuration,delegate,com.hiddenswitch.framework.schema.keycloak.tables.mappers.RowMappers.getRealmDefaultGroupsMapper()));
        }

        @Override
        protected Record2<String, String> getId(com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmDefaultGroups object) {
                return compositeKeyRecord(object.getRealmId(), object.getGroupId());
        }

        /**
     * Find records that have <code>group_id IN (values)</code> asynchronously
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmDefaultGroups>> findManyByGroupId(Collection<String> values) {
                return findManyByCondition(RealmDefaultGroups.REALM_DEFAULT_GROUPS.GROUP_ID.in(values));
        }

        /**
     * Find records that have <code>group_id IN (values)</code> asynchronously
     * limited by the given limit
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmDefaultGroups>> findManyByGroupId(Collection<String> values, int limit) {
                return findManyByCondition(RealmDefaultGroups.REALM_DEFAULT_GROUPS.GROUP_ID.in(values),limit);
        }

        @Override
        public ReactiveClassicQueryExecutor<RealmDefaultGroupsRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmDefaultGroups,Record2<String, String>> queryExecutor(){
                return (ReactiveClassicQueryExecutor<RealmDefaultGroupsRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmDefaultGroups,Record2<String, String>>) super.queryExecutor();
        }
}
