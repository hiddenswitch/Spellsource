/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.spellsource.routines;


import com.hiddenswitch.framework.schema.spellsource.Spellsource;
import com.hiddenswitch.framework.schema.spellsource.tables.records.GeneratedArtRecord;

import io.github.jklingsporn.vertx.jooq.shared.postgres.JSONBToJsonObjectConverter;
import io.vertx.core.json.JsonObject;

import org.jooq.Field;
import org.jooq.Parameter;
import org.jooq.impl.AbstractRoutine;
import org.jooq.impl.Internal;
import org.jooq.impl.SQLDataType;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class SaveGeneratedArt extends AbstractRoutine<GeneratedArtRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The parameter <code>spellsource.save_generated_art.RETURN_VALUE</code>.
     */
    public static final Parameter<GeneratedArtRecord> RETURN_VALUE = Internal.createParameter("RETURN_VALUE", com.hiddenswitch.framework.schema.spellsource.tables.GeneratedArt.GENERATED_ART.getDataType(), false, false);

    /**
     * The parameter <code>spellsource.save_generated_art.digest</code>.
     */
    public static final Parameter<String> DIGEST = Internal.createParameter("digest", SQLDataType.CLOB, false, false);

    /**
     * The parameter <code>spellsource.save_generated_art.links</code>.
     */
    public static final Parameter<String[]> LINKS = Internal.createParameter("links", SQLDataType.CLOB.getArrayDataType(), false, false);

    /**
     * The parameter <code>spellsource.save_generated_art.extra_info</code>.
     */
    public static final Parameter<JsonObject> EXTRA_INFO = Internal.createParameter("extra_info", SQLDataType.JSONB, false, false, new JSONBToJsonObjectConverter());

    /**
     * Create a new routine call instance
     */
    public SaveGeneratedArt() {
        super("save_generated_art", Spellsource.SPELLSOURCE, com.hiddenswitch.framework.schema.spellsource.tables.GeneratedArt.GENERATED_ART.getDataType());

        setReturnParameter(RETURN_VALUE);
        addInParameter(DIGEST);
        addInParameter(LINKS);
        addInParameter(EXTRA_INFO);
    }

    /**
     * Set the <code>digest</code> parameter IN value to the routine
     */
    public void setDigest(String value) {
        setValue(DIGEST, value);
    }

    /**
     * Set the <code>digest</code> parameter to the function to be used with a
     * {@link org.jooq.Select} statement
     */
    public SaveGeneratedArt setDigest(Field<String> field) {
        setField(DIGEST, field);
        return this;
    }

    /**
     * Set the <code>links</code> parameter IN value to the routine
     */
    public void setLinks(String[] value) {
        setValue(LINKS, value);
    }

    /**
     * Set the <code>links</code> parameter to the function to be used with a
     * {@link org.jooq.Select} statement
     */
    public SaveGeneratedArt setLinks(Field<String[]> field) {
        setField(LINKS, field);
        return this;
    }

    /**
     * Set the <code>extra_info</code> parameter IN value to the routine
     */
    public void setExtraInfo(JsonObject value) {
        setValue(EXTRA_INFO, value);
    }

    /**
     * Set the <code>extra_info</code> parameter to the function to be used with
     * a {@link org.jooq.Select} statement
     */
    public SaveGeneratedArt setExtraInfo(Field<JsonObject> field) {
        setField(EXTRA_INFO, field);
        return this;
    }
}