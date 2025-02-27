/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.spellsource.tables.pojos;


import com.hiddenswitch.framework.schema.spellsource.tables.interfaces.IBannedDraftCards;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class BannedDraftCards implements VertxPojo, IBannedDraftCards {

    private static final long serialVersionUID = 1L;

    private String cardId;

    public BannedDraftCards() {}

    public BannedDraftCards(IBannedDraftCards value) {
        this.cardId = value.getCardId();
    }

    public BannedDraftCards(
        String cardId
    ) {
        this.cardId = cardId;
    }

        public BannedDraftCards(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }

    /**
     * Getter for <code>spellsource.banned_draft_cards.card_id</code>.
     */
    @Override
    public String getCardId() {
        return this.cardId;
    }

    /**
     * Setter for <code>spellsource.banned_draft_cards.card_id</code>.
     */
    @Override
    public BannedDraftCards setCardId(String cardId) {
        this.cardId = cardId;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final BannedDraftCards other = (BannedDraftCards) obj;
        if (this.cardId == null) {
            if (other.cardId != null)
                return false;
        }
        else if (!this.cardId.equals(other.cardId))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.cardId == null) ? 0 : this.cardId.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("BannedDraftCards (");

        sb.append(cardId);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IBannedDraftCards from) {
        setCardId(from.getCardId());
    }

    @Override
    public <E extends IBannedDraftCards> E into(E into) {
        into.from(this);
        return into;
    }
}
