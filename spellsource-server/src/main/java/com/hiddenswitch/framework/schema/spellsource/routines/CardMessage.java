/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.spellsource.routines;


import com.hiddenswitch.framework.schema.spellsource.Spellsource;
import com.hiddenswitch.framework.schema.spellsource.tables.records.CardsRecord;
import com.hiddenswitch.framework.schema.spellsource.tables.records.ClassesRecord;

import org.jooq.Field;
import org.jooq.Parameter;
import org.jooq.impl.AbstractRoutine;
import org.jooq.impl.Internal;
import org.jooq.impl.SQLDataType;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class CardMessage extends AbstractRoutine<String> {

    private static final long serialVersionUID = 1L;

    /**
     * The parameter <code>spellsource.card_message.RETURN_VALUE</code>.
     */
    public static final Parameter<String> RETURN_VALUE = Internal.createParameter("RETURN_VALUE", SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>spellsource.card_message.card</code>.
     */
    public static final Parameter<CardsRecord> CARD = Internal.createParameter("card", com.hiddenswitch.framework.schema.spellsource.tables.Cards.CARDS.getDataType(), false, false);

    /**
     * The parameter <code>spellsource.card_message.cl</code>.
     */
    public static final Parameter<ClassesRecord> CL = Internal.createParameter("cl", com.hiddenswitch.framework.schema.spellsource.tables.Classes.CLASSES.getDataType(), false, false);

    /**
     * Create a new routine call instance
     */
    public CardMessage() {
        super("card_message", Spellsource.SPELLSOURCE, SQLDataType.CLOB);

        setReturnParameter(RETURN_VALUE);
        addInParameter(CARD);
        addInParameter(CL);
    }

    /**
     * Set the <code>card</code> parameter IN value to the routine
     */
    public void setCard(CardsRecord value) {
        setValue(CARD, value);
    }

    /**
     * Set the <code>card</code> parameter to the function to be used with a
     * {@link org.jooq.Select} statement
     */
    public CardMessage setCard(Field<CardsRecord> field) {
        setField(CARD, field);
        return this;
    }

    /**
     * Set the <code>cl</code> parameter IN value to the routine
     */
    public void setCl(ClassesRecord value) {
        setValue(CL, value);
    }

    /**
     * Set the <code>cl</code> parameter to the function to be used with a
     * {@link org.jooq.Select} statement
     */
    public CardMessage setCl(Field<ClassesRecord> field) {
        setField(CL, field);
        return this;
    }
}