/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.spellsource.tables.pojos;


import com.hiddenswitch.framework.schema.spellsource.tables.interfaces.ICardCatalogueGetHardRemovalCards;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class CardCatalogueGetHardRemovalCards implements VertxPojo, ICardCatalogueGetHardRemovalCards {

    private static final long serialVersionUID = 1L;

    private String cardId;

    public CardCatalogueGetHardRemovalCards() {}

    public CardCatalogueGetHardRemovalCards(ICardCatalogueGetHardRemovalCards value) {
        this.cardId = value.getCardId();
    }

    public CardCatalogueGetHardRemovalCards(
        String cardId
    ) {
        this.cardId = cardId;
    }

        public CardCatalogueGetHardRemovalCards(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }

    /**
     * Getter for
     * <code>spellsource.card_catalogue_get_hard_removal_cards.card_id</code>.
     */
    @Override
    public String getCardId() {
        return this.cardId;
    }

    /**
     * Setter for
     * <code>spellsource.card_catalogue_get_hard_removal_cards.card_id</code>.
     */
    @Override
    public CardCatalogueGetHardRemovalCards setCardId(String cardId) {
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
        final CardCatalogueGetHardRemovalCards other = (CardCatalogueGetHardRemovalCards) obj;
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
        StringBuilder sb = new StringBuilder("CardCatalogueGetHardRemovalCards (");

        sb.append(cardId);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(ICardCatalogueGetHardRemovalCards from) {
        setCardId(from.getCardId());
    }

    @Override
    public <E extends ICardCatalogueGetHardRemovalCards> E into(E into) {
        into.from(this);
        return into;
    }
}