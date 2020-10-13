package com.hiddenswitch.framework.schema.tables.mappers;

import io.vertx.sqlclient.Row;
import java.util.function.Function;

public class RowMappers {

    private RowMappers(){}

    public static Function<Row,com.hiddenswitch.framework.schema.tables.pojos.CardDesc> getCardDescMapper() {
        return row -> {
            com.hiddenswitch.framework.schema.tables.pojos.CardDesc pojo = new com.hiddenswitch.framework.schema.tables.pojos.CardDesc();
            pojo.setId(row.getLong("id"));
            pojo.setUri(row.getString("uri"));
            // Omitting unrecognized type DataType [ t=xml; p=0; s=0; u="pg_catalog"."xml"; j=null ] (java.lang.Object) for column blockly_workspace!
            // Omitting unrecognized type DataType [ t=jsonb; p=0; s=0; u="pg_catalog"."jsonb"; j=null ] (org.jooq.JSONB) for column card_script!
            return pojo;
        };
    }

    public static Function<Row,com.hiddenswitch.framework.schema.tables.pojos.Cards> getCardsMapper() {
        return row -> {
            com.hiddenswitch.framework.schema.tables.pojos.Cards pojo = new com.hiddenswitch.framework.schema.tables.pojos.Cards();
            pojo.setId(row.getLong("id"));
            pojo.setCardId(row.getString("card_id"));
            pojo.setCardDesc(row.getLong("card_desc"));
            return pojo;
        };
    }

    public static Function<Row,com.hiddenswitch.framework.schema.tables.pojos.CardsInDeck> getCardsInDeckMapper() {
        return row -> {
            com.hiddenswitch.framework.schema.tables.pojos.CardsInDeck pojo = new com.hiddenswitch.framework.schema.tables.pojos.CardsInDeck();
            pojo.setId(row.getLong("id"));
            pojo.setDeckId(row.getLong("deck_id"));
            pojo.setCardId(row.getString("card_id"));
            pojo.setVersion(row.getLong("version"));
            return pojo;
        };
    }

    public static Function<Row,com.hiddenswitch.framework.schema.tables.pojos.Decks> getDecksMapper() {
        return row -> {
            com.hiddenswitch.framework.schema.tables.pojos.Decks pojo = new com.hiddenswitch.framework.schema.tables.pojos.Decks();
            pojo.setId(row.getLong("id"));
            pojo.setCreatedBy(row.getString("created_by"));
            pojo.setLastEditedBy(row.getString("last_edited_by"));
            pojo.setName(row.getString("name"));
            pojo.setHeroClass(row.getString("hero_class"));
            pojo.setTrashed(row.getBoolean("trashed"));
            return pojo;
        };
    }

}
