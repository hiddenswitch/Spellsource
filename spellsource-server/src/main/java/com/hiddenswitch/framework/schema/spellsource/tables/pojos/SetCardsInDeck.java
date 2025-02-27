/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.spellsource.tables.pojos;


import com.hiddenswitch.framework.schema.spellsource.tables.interfaces.ISetCardsInDeck;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class SetCardsInDeck implements VertxPojo, ISetCardsInDeck {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String deckId;
    private String cardId;

    public SetCardsInDeck() {}

    public SetCardsInDeck(ISetCardsInDeck value) {
        this.id = value.getId();
        this.deckId = value.getDeckId();
        this.cardId = value.getCardId();
    }

    public SetCardsInDeck(
        Long id,
        String deckId,
        String cardId
    ) {
        this.id = id;
        this.deckId = deckId;
        this.cardId = cardId;
    }

        public SetCardsInDeck(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }

    /**
     * Getter for <code>spellsource.set_cards_in_deck.id</code>.
     */
    @Override
    public Long getId() {
        return this.id;
    }

    /**
     * Setter for <code>spellsource.set_cards_in_deck.id</code>.
     */
    @Override
    public SetCardsInDeck setId(Long id) {
        this.id = id;
        return this;
    }

    /**
     * Getter for <code>spellsource.set_cards_in_deck.deck_id</code>.
     */
    @Override
    public String getDeckId() {
        return this.deckId;
    }

    /**
     * Setter for <code>spellsource.set_cards_in_deck.deck_id</code>.
     */
    @Override
    public SetCardsInDeck setDeckId(String deckId) {
        this.deckId = deckId;
        return this;
    }

    /**
     * Getter for <code>spellsource.set_cards_in_deck.card_id</code>.
     */
    @Override
    public String getCardId() {
        return this.cardId;
    }

    /**
     * Setter for <code>spellsource.set_cards_in_deck.card_id</code>.
     */
    @Override
    public SetCardsInDeck setCardId(String cardId) {
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
        final SetCardsInDeck other = (SetCardsInDeck) obj;
        if (this.id == null) {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.deckId == null) {
            if (other.deckId != null)
                return false;
        }
        else if (!this.deckId.equals(other.deckId))
            return false;
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
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.deckId == null) ? 0 : this.deckId.hashCode());
        result = prime * result + ((this.cardId == null) ? 0 : this.cardId.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SetCardsInDeck (");

        sb.append(id);
        sb.append(", ").append(deckId);
        sb.append(", ").append(cardId);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(ISetCardsInDeck from) {
        setId(from.getId());
        setDeckId(from.getDeckId());
        setCardId(from.getCardId());
    }

    @Override
    public <E extends ISetCardsInDeck> E into(E into) {
        into.from(this);
        return into;
    }
}
