/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.spellsource.routines;


import com.hiddenswitch.framework.schema.spellsource.Spellsource;

import org.jooq.Field;
import org.jooq.Parameter;
import org.jooq.impl.AbstractRoutine;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;
import org.jooq.impl.SQLDataType;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class GetUserAttribute extends AbstractRoutine<String> {

    private static final long serialVersionUID = 1L;

    /**
     * The parameter <code>spellsource.get_user_attribute.RETURN_VALUE</code>.
     */
    public static final Parameter<String> RETURN_VALUE = Internal.createParameter("RETURN_VALUE", SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>spellsource.get_user_attribute.id_user</code>.
     */
    public static final Parameter<String> ID_USER = Internal.createParameter("id_user", SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>spellsource.get_user_attribute.attribute</code>.
     */
    public static final Parameter<String> ATTRIBUTE = Internal.createParameter("attribute", SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>spellsource.get_user_attribute.or_default</code>.
     */
    public static final Parameter<String> OR_DEFAULT = Internal.createParameter("or_default", SQLDataType.CLOB.defaultValue(DSL.field("'null'::text", SQLDataType.CLOB)), true, false);

    /**
     * Create a new routine call instance
     */
    public GetUserAttribute() {
        super("get_user_attribute", Spellsource.SPELLSOURCE, SQLDataType.CLOB);

        setReturnParameter(RETURN_VALUE);
        addInParameter(ID_USER);
        addInParameter(ATTRIBUTE);
        addInParameter(OR_DEFAULT);
    }

    /**
     * Set the <code>id_user</code> parameter IN value to the routine
     */
    public void setIdUser(String value) {
        setValue(ID_USER, value);
    }

    /**
     * Set the <code>id_user</code> parameter to the function to be used with a
     * {@link org.jooq.Select} statement
     */
    public GetUserAttribute setIdUser(Field<String> field) {
        setField(ID_USER, field);
        return this;
    }

    /**
     * Set the <code>attribute</code> parameter IN value to the routine
     */
    public void setAttribute(String value) {
        setValue(ATTRIBUTE, value);
    }

    /**
     * Set the <code>attribute</code> parameter to the function to be used with
     * a {@link org.jooq.Select} statement
     */
    public GetUserAttribute setAttribute(Field<String> field) {
        setField(ATTRIBUTE, field);
        return this;
    }

    /**
     * Set the <code>or_default</code> parameter IN value to the routine
     */
    public void setOrDefault(String value) {
        setValue(OR_DEFAULT, value);
    }

    /**
     * Set the <code>or_default</code> parameter to the function to be used with
     * a {@link org.jooq.Select} statement
     */
    public GetUserAttribute setOrDefault(Field<String> field) {
        setField(OR_DEFAULT, field);
        return this;
    }
}