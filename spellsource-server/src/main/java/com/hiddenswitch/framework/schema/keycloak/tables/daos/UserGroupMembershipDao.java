/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.daos;


import com.hiddenswitch.framework.schema.keycloak.tables.UserGroupMembership;
import com.hiddenswitch.framework.schema.keycloak.tables.records.UserGroupMembershipRecord;

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
public class UserGroupMembershipDao extends AbstractReactiveVertxDAO<UserGroupMembershipRecord, com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserGroupMembership, Record2<String, String>, Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserGroupMembership>>, Future<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserGroupMembership>, Future<Integer>, Future<Record2<String, String>>> implements io.github.jklingsporn.vertx.jooq.classic.VertxDAO<UserGroupMembershipRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserGroupMembership,Record2<String, String>> {

        /**
     * @param configuration Used for rendering, so only SQLDialect must be set
     * and must be one of the POSTGREs types.
     * @param delegate A configured AsyncSQLClient that is used for query
     * execution
     */
        public UserGroupMembershipDao(Configuration configuration, io.vertx.sqlclient.SqlClient delegate) {
                super(UserGroupMembership.USER_GROUP_MEMBERSHIP, com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserGroupMembership.class, new ReactiveClassicQueryExecutor<UserGroupMembershipRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserGroupMembership,Record2<String, String>>(configuration,delegate,com.hiddenswitch.framework.schema.keycloak.tables.mappers.RowMappers.getUserGroupMembershipMapper()));
        }

        @Override
        protected Record2<String, String> getId(com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserGroupMembership object) {
                return compositeKeyRecord(object.getGroupId(), object.getUserId());
        }

        /**
     * Find records that have <code>user_id IN (values)</code> asynchronously
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserGroupMembership>> findManyByUserId(Collection<String> values) {
                return findManyByCondition(UserGroupMembership.USER_GROUP_MEMBERSHIP.USER_ID.in(values));
        }

        /**
     * Find records that have <code>user_id IN (values)</code> asynchronously
     * limited by the given limit
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserGroupMembership>> findManyByUserId(Collection<String> values, int limit) {
                return findManyByCondition(UserGroupMembership.USER_GROUP_MEMBERSHIP.USER_ID.in(values),limit);
        }

        @Override
        public ReactiveClassicQueryExecutor<UserGroupMembershipRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserGroupMembership,Record2<String, String>> queryExecutor(){
                return (ReactiveClassicQueryExecutor<UserGroupMembershipRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserGroupMembership,Record2<String, String>>) super.queryExecutor();
        }
}
