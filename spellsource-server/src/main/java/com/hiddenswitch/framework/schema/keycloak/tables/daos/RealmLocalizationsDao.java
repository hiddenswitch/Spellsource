/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.daos;


import com.hiddenswitch.framework.schema.keycloak.tables.RealmLocalizations;
import com.hiddenswitch.framework.schema.keycloak.tables.records.RealmLocalizationsRecord;

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
public class RealmLocalizationsDao extends AbstractReactiveVertxDAO<RealmLocalizationsRecord, com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmLocalizations, Record2<String, String>, Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmLocalizations>>, Future<com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmLocalizations>, Future<Integer>, Future<Record2<String, String>>> implements io.github.jklingsporn.vertx.jooq.classic.VertxDAO<RealmLocalizationsRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmLocalizations,Record2<String, String>> {

        /**
     * @param configuration Used for rendering, so only SQLDialect must be set
     * and must be one of the POSTGREs types.
     * @param delegate A configured AsyncSQLClient that is used for query
     * execution
     */
        public RealmLocalizationsDao(Configuration configuration, io.vertx.sqlclient.SqlClient delegate) {
                super(RealmLocalizations.REALM_LOCALIZATIONS, com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmLocalizations.class, new ReactiveClassicQueryExecutor<RealmLocalizationsRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmLocalizations,Record2<String, String>>(configuration,delegate,com.hiddenswitch.framework.schema.keycloak.tables.mappers.RowMappers.getRealmLocalizationsMapper()));
        }

        @Override
        protected Record2<String, String> getId(com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmLocalizations object) {
                return compositeKeyRecord(object.getRealmId(), object.getLocale());
        }

        /**
     * Find records that have <code>locale IN (values)</code> asynchronously
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmLocalizations>> findManyByLocale(Collection<String> values) {
                return findManyByCondition(RealmLocalizations.REALM_LOCALIZATIONS.LOCALE.in(values));
        }

        /**
     * Find records that have <code>locale IN (values)</code> asynchronously
     * limited by the given limit
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmLocalizations>> findManyByLocale(Collection<String> values, int limit) {
                return findManyByCondition(RealmLocalizations.REALM_LOCALIZATIONS.LOCALE.in(values),limit);
        }

        /**
     * Find records that have <code>texts IN (values)</code> asynchronously
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmLocalizations>> findManyByTexts(Collection<String> values) {
                return findManyByCondition(RealmLocalizations.REALM_LOCALIZATIONS.TEXTS.in(values));
        }

        /**
     * Find records that have <code>texts IN (values)</code> asynchronously
     * limited by the given limit
     */
        public Future<List<com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmLocalizations>> findManyByTexts(Collection<String> values, int limit) {
                return findManyByCondition(RealmLocalizations.REALM_LOCALIZATIONS.TEXTS.in(values),limit);
        }

        @Override
        public ReactiveClassicQueryExecutor<RealmLocalizationsRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmLocalizations,Record2<String, String>> queryExecutor(){
                return (ReactiveClassicQueryExecutor<RealmLocalizationsRecord,com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmLocalizations,Record2<String, String>>) super.queryExecutor();
        }
}
