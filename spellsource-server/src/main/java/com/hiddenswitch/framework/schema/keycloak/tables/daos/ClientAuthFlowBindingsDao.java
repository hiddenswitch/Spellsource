/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.daos;


import com.hiddenswitch.framework.schema.keycloak.tables.ClientAuthFlowBindings;
import com.hiddenswitch.framework.schema.keycloak.tables.records.ClientAuthFlowBindingsRecord;

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
public class ClientAuthFlowBindingsDao extends AbstractReactiveVertxDAO<ClientAuthFlowBindingsRecord, com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientAuthFlowBindings, Record2<String, String>, Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientAuthFlowBindings>>, Future<com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientAuthFlowBindings>, Future<Integer>, Future<Record2<String, String>>> implements io.github.jklingsporn.vertx.jooq.classic.VertxDAO<ClientAuthFlowBindingsRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientAuthFlowBindings,Record2<String, String>> {

        /**
     * @param configuration Used for rendering, so only SQLDialect must be set
     * and must be one of the POSTGREs types.
     * @param delegate A configured AsyncSQLClient that is used for query
     * execution
     */
        public ClientAuthFlowBindingsDao(Configuration configuration, io.vertx.sqlclient.SqlClient delegate) {
                super(ClientAuthFlowBindings.CLIENT_AUTH_FLOW_BINDINGS, com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientAuthFlowBindings.class, new ReactiveClassicQueryExecutor<ClientAuthFlowBindingsRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientAuthFlowBindings,Record2<String, String>>(configuration,delegate,com.hiddenswitch.framework.schema.keycloak.tables.mappers.RowMappers.getClientAuthFlowBindingsMapper()));
        }

        @Override
        protected Record2<String, String> getId(com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientAuthFlowBindings object) {
                return compositeKeyRecord(object.getClientId(), object.getBindingName());
        }

        /**
     * Find records that have <code>flow_id IN (values)</code> asynchronously
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientAuthFlowBindings>> findManyByFlowId(Collection<String> values) {
                return findManyByCondition(ClientAuthFlowBindings.CLIENT_AUTH_FLOW_BINDINGS.FLOW_ID.in(values));
        }

        /**
     * Find records that have <code>flow_id IN (values)</code> asynchronously
     * limited by the given limit
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientAuthFlowBindings>> findManyByFlowId(Collection<String> values, int limit) {
                return findManyByCondition(ClientAuthFlowBindings.CLIENT_AUTH_FLOW_BINDINGS.FLOW_ID.in(values),limit);
        }

        /**
     * Find records that have <code>binding_name IN (values)</code>
     * asynchronously
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientAuthFlowBindings>> findManyByBindingName(Collection<String> values) {
                return findManyByCondition(ClientAuthFlowBindings.CLIENT_AUTH_FLOW_BINDINGS.BINDING_NAME.in(values));
        }

        /**
     * Find records that have <code>binding_name IN (values)</code>
     * asynchronously limited by the given limit
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientAuthFlowBindings>> findManyByBindingName(Collection<String> values, int limit) {
                return findManyByCondition(ClientAuthFlowBindings.CLIENT_AUTH_FLOW_BINDINGS.BINDING_NAME.in(values),limit);
        }

        @Override
        public ReactiveClassicQueryExecutor<ClientAuthFlowBindingsRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientAuthFlowBindings,Record2<String, String>> queryExecutor(){
                return (ReactiveClassicQueryExecutor<ClientAuthFlowBindingsRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientAuthFlowBindings,Record2<String, String>>) super.queryExecutor();
        }
}