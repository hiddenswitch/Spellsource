/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.daos;


import com.hiddenswitch.framework.schema.keycloak.tables.GroupAttribute;
import com.hiddenswitch.framework.schema.keycloak.tables.records.GroupAttributeRecord;

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
public class GroupAttributeDao extends AbstractReactiveVertxDAO<GroupAttributeRecord, com.hiddenswitch.framework.schema.keycloak.tables.pojos.GroupAttribute, String, Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.GroupAttribute>>, Future<com.hiddenswitch.framework.schema.keycloak.tables.pojos.GroupAttribute>, Future<Integer>, Future<String>> implements io.github.jklingsporn.vertx.jooq.classic.VertxDAO<GroupAttributeRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.GroupAttribute,String> {

        /**
     * @param configuration Used for rendering, so only SQLDialect must be set
     * and must be one of the POSTGREs types.
     * @param delegate A configured AsyncSQLClient that is used for query
     * execution
     */
        public GroupAttributeDao(Configuration configuration, io.vertx.sqlclient.SqlClient delegate) {
                super(GroupAttribute.GROUP_ATTRIBUTE, com.hiddenswitch.framework.schema.keycloak.tables.pojos.GroupAttribute.class, new ReactiveClassicQueryExecutor<GroupAttributeRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.GroupAttribute,String>(configuration,delegate,com.hiddenswitch.framework.schema.keycloak.tables.mappers.RowMappers.getGroupAttributeMapper()));
        }

        @Override
        protected String getId(com.hiddenswitch.framework.schema.keycloak.tables.pojos.GroupAttribute object) {
                return object.getId();
        }

        /**
     * Find records that have <code>name IN (values)</code> asynchronously
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.GroupAttribute>> findManyByName(Collection<String> values) {
                return findManyByCondition(GroupAttribute.GROUP_ATTRIBUTE.NAME.in(values));
        }

        /**
     * Find records that have <code>name IN (values)</code> asynchronously
     * limited by the given limit
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.GroupAttribute>> findManyByName(Collection<String> values, int limit) {
                return findManyByCondition(GroupAttribute.GROUP_ATTRIBUTE.NAME.in(values),limit);
        }

        /**
     * Find records that have <code>value IN (values)</code> asynchronously
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.GroupAttribute>> findManyByValue(Collection<String> values) {
                return findManyByCondition(GroupAttribute.GROUP_ATTRIBUTE.VALUE.in(values));
        }

        /**
     * Find records that have <code>value IN (values)</code> asynchronously
     * limited by the given limit
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.GroupAttribute>> findManyByValue(Collection<String> values, int limit) {
                return findManyByCondition(GroupAttribute.GROUP_ATTRIBUTE.VALUE.in(values),limit);
        }

        /**
     * Find records that have <code>group_id IN (values)</code> asynchronously
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.GroupAttribute>> findManyByGroupId(Collection<String> values) {
                return findManyByCondition(GroupAttribute.GROUP_ATTRIBUTE.GROUP_ID.in(values));
        }

        /**
     * Find records that have <code>group_id IN (values)</code> asynchronously
     * limited by the given limit
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.GroupAttribute>> findManyByGroupId(Collection<String> values, int limit) {
                return findManyByCondition(GroupAttribute.GROUP_ATTRIBUTE.GROUP_ID.in(values),limit);
        }

        @Override
        public ReactiveClassicQueryExecutor<GroupAttributeRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.GroupAttribute,String> queryExecutor(){
                return (ReactiveClassicQueryExecutor<GroupAttributeRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.GroupAttribute,String>) super.queryExecutor();
        }
}
