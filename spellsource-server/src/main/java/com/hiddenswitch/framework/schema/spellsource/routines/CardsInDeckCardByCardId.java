/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.spellsource.routines;


import com.hiddenswitch.framework.schema.spellsource.Spellsource;
import com.hiddenswitch.framework.schema.spellsource.tables.records.CardsInDeckRecord;
import com.hiddenswitch.framework.schema.spellsource.tables.records.CardsRecord;

import org.jooq.Field;
import org.jooq.Parameter;
import org.jooq.impl.AbstractRoutine;
import org.jooq.impl.Internal;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class CardsInDeckCardByCardId extends AbstractRoutine<CardsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The parameter
     * <code>spellsource.cards_in_deck_card_by_card_id.RETURN_VALUE</code>.
     */
    public static final Parameter<CardsRecord> RETURN_VALUE = Internal.createParameter("RETURN_VALUE", com.hiddenswitch.framework.schema.spellsource.tables.Cards.CARDS.getDataType(), false, false);

    /**
     * The parameter
     * <code>spellsource.cards_in_deck_card_by_card_id.cards_in_deck</code>.
     */
    public static final Parameter<CardsInDeckRecord> CARDS_IN_DECK = Internal.createParameter("cards_in_deck", com.hiddenswitch.framework.schema.spellsource.tables.CardsInDeck.CARDS_IN_DECK.getDataType(), false, false);

    /**
     * Create a new routine call instance
     */
    public CardsInDeckCardByCardId() {
        super("cards_in_deck_card_by_card_id", Spellsource.SPELLSOURCE, com.hiddenswitch.framework.schema.spellsource.tables.Cards.CARDS.getDataType());

        setReturnParameter(RETURN_VALUE);
        addInParameter(CARDS_IN_DECK);
    }

    /**
     * Set the <code>cards_in_deck</code> parameter IN value to the routine
     */
    public void setCardsInDeck(CardsInDeckRecord value) {
        setValue(CARDS_IN_DECK, value);
    }

    /**
     * Set the <code>cards_in_deck</code> parameter to the function to be used
     * with a {@link org.jooq.Select} statement
     */
    public CardsInDeckCardByCardId setCardsInDeck(Field<CardsInDeckRecord> field) {
        setField(CARDS_IN_DECK, field);
        return this;
    }
}
