/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.spellsource.routines;


import com.hiddenswitch.framework.schema.spellsource.Spellsource;
import com.hiddenswitch.framework.schema.spellsource.tables.records.CardsRecord;

import org.jooq.Field;
import org.jooq.Parameter;
import org.jooq.impl.AbstractRoutine;
import org.jooq.impl.Internal;
import org.jooq.impl.SQLDataType;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class GetLatestCard extends AbstractRoutine<CardsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The parameter <code>spellsource.get_latest_card.RETURN_VALUE</code>.
     */
    public static final Parameter<CardsRecord> RETURN_VALUE = Internal.createParameter("RETURN_VALUE", com.hiddenswitch.framework.schema.spellsource.tables.Cards.CARDS.getDataType(), false, false);

    /**
     * The parameter <code>spellsource.get_latest_card.card_id</code>.
     */
    public static final Parameter<String> CARD_ID = Internal.createParameter("card_id", SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>spellsource.get_latest_card.published</code>.
     */
    public static final Parameter<Boolean> PUBLISHED = Internal.createParameter("published", SQLDataType.BOOLEAN, false, false);

    /**
     * Create a new routine call instance
     */
    public GetLatestCard() {
        super("get_latest_card", Spellsource.SPELLSOURCE, com.hiddenswitch.framework.schema.spellsource.tables.Cards.CARDS.getDataType());

        setReturnParameter(RETURN_VALUE);
        addInParameter(CARD_ID);
        addInParameter(PUBLISHED);
    }

    /**
     * Set the <code>card_id</code> parameter IN value to the routine
     */
    public void setCardId(String value) {
        setValue(CARD_ID, value);
    }

    /**
     * Set the <code>card_id</code> parameter to the function to be used with a
     * {@link org.jooq.Select} statement
     */
    public GetLatestCard setCardId(Field<String> field) {
        setField(CARD_ID, field);
        return this;
    }

    /**
     * Set the <code>published</code> parameter IN value to the routine
     */
    public void setPublished(Boolean value) {
        setValue(PUBLISHED, value);
    }

    /**
     * Set the <code>published</code> parameter to the function to be used with
     * a {@link org.jooq.Select} statement
     */
    public GetLatestCard setPublished(Field<Boolean> field) {
        setField(PUBLISHED, field);
        return this;
    }
}
