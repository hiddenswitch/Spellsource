/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.spellsource.tables.daos;


import com.hiddenswitch.framework.schema.spellsource.tables.HardRemovalCards;
import com.hiddenswitch.framework.schema.spellsource.tables.records.HardRemovalCardsRecord;

import io.github.jklingsporn.vertx.jooq.shared.reactive.AbstractReactiveVertxDAO;

import org.jooq.Configuration;


import java.util.List;
import io.vertx.core.Future;
import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicQueryExecutor;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class HardRemovalCardsDao extends AbstractReactiveVertxDAO<HardRemovalCardsRecord, com.hiddenswitch.framework.schema.spellsource.tables.pojos.HardRemovalCards, String, Future<List<com.hiddenswitch.framework.schema.spellsource.tables.pojos.HardRemovalCards>>, Future<com.hiddenswitch.framework.schema.spellsource.tables.pojos.HardRemovalCards>, Future<Integer>, Future<String>> implements io.github.jklingsporn.vertx.jooq.classic.VertxDAO<HardRemovalCardsRecord,com.hiddenswitch.framework.schema.spellsource.tables.pojos.HardRemovalCards,String> {

        /**
     * @param configuration Used for rendering, so only SQLDialect must be set
     * and must be one of the POSTGREs types.
     * @param delegate A configured AsyncSQLClient that is used for query
     * execution
     */
        public HardRemovalCardsDao(Configuration configuration, io.vertx.sqlclient.SqlClient delegate) {
                super(HardRemovalCards.HARD_REMOVAL_CARDS, com.hiddenswitch.framework.schema.spellsource.tables.pojos.HardRemovalCards.class, new ReactiveClassicQueryExecutor<HardRemovalCardsRecord,com.hiddenswitch.framework.schema.spellsource.tables.pojos.HardRemovalCards,String>(configuration,delegate,com.hiddenswitch.framework.schema.spellsource.tables.mappers.RowMappers.getHardRemovalCardsMapper()));
        }

        @Override
        protected String getId(com.hiddenswitch.framework.schema.spellsource.tables.pojos.HardRemovalCards object) {
                return object.getCardId();
        }

        @Override
        public ReactiveClassicQueryExecutor<HardRemovalCardsRecord,com.hiddenswitch.framework.schema.spellsource.tables.pojos.HardRemovalCards,String> queryExecutor(){
                return (ReactiveClassicQueryExecutor<HardRemovalCardsRecord,com.hiddenswitch.framework.schema.spellsource.tables.pojos.HardRemovalCards,String>) super.queryExecutor();
        }
}
