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
public interface IHardRemovalCards extends VertxPojo, Serializable {

    /**
     * Setter for <code>spellsource.hard_removal_cards.card_id</code>.
     */
    public IHardRemovalCards setCardId(String value);

    /**
     * Getter for <code>spellsource.hard_removal_cards.card_id</code>.
     */
    public String getCardId();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common
     * interface IHardRemovalCards
     */
    public void from(IHardRemovalCards from);

    /**
     * Copy data into another generated Record/POJO implementing the common
     * interface IHardRemovalCards
     */
    public <E extends IHardRemovalCards> E into(E into);

        @Override
        public default IHardRemovalCards fromJson(io.vertx.core.json.JsonObject json) {
                setOrThrow(this::setCardId,json::getString,"card_id","java.lang.String");
                return this;
        }


        @Override
        public default io.vertx.core.json.JsonObject toJson() {
                io.vertx.core.json.JsonObject json = new io.vertx.core.json.JsonObject();
                json.put("card_id",getCardId());
                return json;
        }

}