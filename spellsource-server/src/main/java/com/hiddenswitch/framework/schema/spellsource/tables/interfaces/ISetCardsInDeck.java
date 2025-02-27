/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.spellsource.tables.interfaces;


import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import java.io.Serializable;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public interface ISetCardsInDeck extends VertxPojo, Serializable {

    /**
     * Setter for <code>spellsource.set_cards_in_deck.id</code>.
     */
    public ISetCardsInDeck setId(Long value);

    /**
     * Getter for <code>spellsource.set_cards_in_deck.id</code>.
     */
    public Long getId();

    /**
     * Setter for <code>spellsource.set_cards_in_deck.deck_id</code>.
     */
    public ISetCardsInDeck setDeckId(String value);

    /**
     * Getter for <code>spellsource.set_cards_in_deck.deck_id</code>.
     */
    public String getDeckId();

    /**
     * Setter for <code>spellsource.set_cards_in_deck.card_id</code>.
     */
    public ISetCardsInDeck setCardId(String value);

    /**
     * Getter for <code>spellsource.set_cards_in_deck.card_id</code>.
     */
    public String getCardId();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common
     * interface ISetCardsInDeck
     */
    public void from(ISetCardsInDeck from);

    /**
     * Copy data into another generated Record/POJO implementing the common
     * interface ISetCardsInDeck
     */
    public <E extends ISetCardsInDeck> E into(E into);

        @Override
        public default ISetCardsInDeck fromJson(io.vertx.core.json.JsonObject json) {
                setOrThrow(this::setId,json::getLong,"id","java.lang.Long");
                setOrThrow(this::setDeckId,json::getString,"deck_id","java.lang.String");
                setOrThrow(this::setCardId,json::getString,"card_id","java.lang.String");
                return this;
        }


        @Override
        public default io.vertx.core.json.JsonObject toJson() {
                io.vertx.core.json.JsonObject json = new io.vertx.core.json.JsonObject();
                json.put("id",getId());
                json.put("deck_id",getDeckId());
                json.put("card_id",getCardId());
                return json;
        }

}
