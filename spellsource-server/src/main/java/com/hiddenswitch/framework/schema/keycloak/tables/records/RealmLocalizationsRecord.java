/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.records;


import com.hiddenswitch.framework.schema.keycloak.tables.RealmLocalizations;
import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IRealmLocalizations;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class RealmLocalizationsRecord extends UpdatableRecordImpl<RealmLocalizationsRecord> implements VertxPojo, Record3<String, String, String>, IRealmLocalizations {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>keycloak.realm_localizations.realm_id</code>.
     */
    @Override
    public RealmLocalizationsRecord setRealmId(String value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.realm_localizations.realm_id</code>.
     */
    @Override
    public String getRealmId() {
        return (String) get(0);
    }

    /**
     * Setter for <code>keycloak.realm_localizations.locale</code>.
     */
    @Override
    public RealmLocalizationsRecord setLocale(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.realm_localizations.locale</code>.
     */
    @Override
    public String getLocale() {
        return (String) get(1);
    }

    /**
     * Setter for <code>keycloak.realm_localizations.texts</code>.
     */
    @Override
    public RealmLocalizationsRecord setTexts(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.realm_localizations.texts</code>.
     */
    @Override
    public String getTexts() {
        return (String) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record2<String, String> key() {
        return (Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<String, String, String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<String, String, String> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return RealmLocalizations.REALM_LOCALIZATIONS.REALM_ID;
    }

    @Override
    public Field<String> field2() {
        return RealmLocalizations.REALM_LOCALIZATIONS.LOCALE;
    }

    @Override
    public Field<String> field3() {
        return RealmLocalizations.REALM_LOCALIZATIONS.TEXTS;
    }

    @Override
    public String component1() {
        return getRealmId();
    }

    @Override
    public String component2() {
        return getLocale();
    }

    @Override
    public String component3() {
        return getTexts();
    }

    @Override
    public String value1() {
        return getRealmId();
    }

    @Override
    public String value2() {
        return getLocale();
    }

    @Override
    public String value3() {
        return getTexts();
    }

    @Override
    public RealmLocalizationsRecord value1(String value) {
        setRealmId(value);
        return this;
    }

    @Override
    public RealmLocalizationsRecord value2(String value) {
        setLocale(value);
        return this;
    }

    @Override
    public RealmLocalizationsRecord value3(String value) {
        setTexts(value);
        return this;
    }

    @Override
    public RealmLocalizationsRecord values(String value1, String value2, String value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IRealmLocalizations from) {
        setRealmId(from.getRealmId());
        setLocale(from.getLocale());
        setTexts(from.getTexts());
    }

    @Override
    public <E extends IRealmLocalizations> E into(E into) {
        into.from(this);
        return into;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached RealmLocalizationsRecord
     */
    public RealmLocalizationsRecord() {
        super(RealmLocalizations.REALM_LOCALIZATIONS);
    }

    /**
     * Create a detached, initialised RealmLocalizationsRecord
     */
    public RealmLocalizationsRecord(String realmId, String locale, String texts) {
        super(RealmLocalizations.REALM_LOCALIZATIONS);

        setRealmId(realmId);
        setLocale(locale);
        setTexts(texts);
    }

    /**
     * Create a detached, initialised RealmLocalizationsRecord
     */
    public RealmLocalizationsRecord(com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmLocalizations value) {
        super(RealmLocalizations.REALM_LOCALIZATIONS);

        if (value != null) {
            setRealmId(value.getRealmId());
            setLocale(value.getLocale());
            setTexts(value.getTexts());
        }
    }

        public RealmLocalizationsRecord(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }
}