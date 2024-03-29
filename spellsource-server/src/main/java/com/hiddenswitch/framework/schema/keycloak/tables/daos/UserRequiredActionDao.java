/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.daos;


import com.hiddenswitch.framework.schema.keycloak.tables.UserRequiredAction;
import com.hiddenswitch.framework.schema.keycloak.tables.records.UserRequiredActionRecord;

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
public class UserRequiredActionDao extends AbstractReactiveVertxDAO<UserRequiredActionRecord, com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserRequiredAction, Record2<String, String>, Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserRequiredAction>>, Future<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserRequiredAction>, Future<Integer>, Future<Record2<String, String>>> implements io.github.jklingsporn.vertx.jooq.classic.VertxDAO<UserRequiredActionRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserRequiredAction,Record2<String, String>> {

        /**
     * @param configuration Used for rendering, so only SQLDialect must be set
     * and must be one of the POSTGREs types.
     * @param delegate A configured AsyncSQLClient that is used for query
     * execution
     */
        public UserRequiredActionDao(Configuration configuration, io.vertx.sqlclient.SqlClient delegate) {
                super(UserRequiredAction.USER_REQUIRED_ACTION, com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserRequiredAction.class, new ReactiveClassicQueryExecutor<UserRequiredActionRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserRequiredAction,Record2<String, String>>(configuration,delegate,com.hiddenswitch.framework.schema.keycloak.tables.mappers.RowMappers.getUserRequiredActionMapper()));
        }

        @Override
        protected Record2<String, String> getId(com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserRequiredAction object) {
                return compositeKeyRecord(object.getRequiredAction(), object.getUserId());
        }

        /**
     * Find records that have <code>user_id IN (values)</code> asynchronously
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserRequiredAction>> findManyByUserId(Collection<String> values) {
                return findManyByCondition(UserRequiredAction.USER_REQUIRED_ACTION.USER_ID.in(values));
        }

        /**
     * Find records that have <code>user_id IN (values)</code> asynchronously
     * limited by the given limit
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserRequiredAction>> findManyByUserId(Collection<String> values, int limit) {
                return findManyByCondition(UserRequiredAction.USER_REQUIRED_ACTION.USER_ID.in(values),limit);
        }

        @Override
        public ReactiveClassicQueryExecutor<UserRequiredActionRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserRequiredAction,Record2<String, String>> queryExecutor(){
                return (ReactiveClassicQueryExecutor<UserRequiredActionRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserRequiredAction,Record2<String, String>>) super.queryExecutor();
        }
}
