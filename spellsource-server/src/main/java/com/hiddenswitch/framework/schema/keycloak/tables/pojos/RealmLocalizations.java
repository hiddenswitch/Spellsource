/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.pojos;


import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IRealmLocalizations;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class RealmLocalizations implements VertxPojo, IRealmLocalizations {

    private static final long serialVersionUID = 1L;

    private String realmId;
    private String locale;
    private String texts;

    public RealmLocalizations() {}

    public RealmLocalizations(IRealmLocalizations value) {
        this.realmId = value.getRealmId();
        this.locale = value.getLocale();
        this.texts = value.getTexts();
    }

    public RealmLocalizations(
        String realmId,
        String locale,
        String texts
    ) {
        this.realmId = realmId;
        this.locale = locale;
        this.texts = texts;
    }

        public RealmLocalizations(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }

    /**
     * Getter for <code>keycloak.realm_localizations.realm_id</code>.
     */
    @Override
    public String getRealmId() {
        return this.realmId;
    }

    /**
     * Setter for <code>keycloak.realm_localizations.realm_id</code>.
     */
    @Override
    public RealmLocalizations setRealmId(String realmId) {
        this.realmId = realmId;
        return this;
    }

    /**
     * Getter for <code>keycloak.realm_localizations.locale</code>.
     */
    @Override
    public String getLocale() {
        return this.locale;
    }

    /**
     * Setter for <code>keycloak.realm_localizations.locale</code>.
     */
    @Override
    public RealmLocalizations setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    /**
     * Getter for <code>keycloak.realm_localizations.texts</code>.
     */
    @Override
    public String getTexts() {
        return this.texts;
    }

    /**
     * Setter for <code>keycloak.realm_localizations.texts</code>.
     */
    @Override
    public RealmLocalizations setTexts(String texts) {
        this.texts = texts;
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
        final RealmLocalizations other = (RealmLocalizations) obj;
        if (this.realmId == null) {
            if (other.realmId != null)
                return false;
        }
        else if (!this.realmId.equals(other.realmId))
            return false;
        if (this.locale == null) {
            if (other.locale != null)
                return false;
        }
        else if (!this.locale.equals(other.locale))
            return false;
        if (this.texts == null) {
            if (other.texts != null)
                return false;
        }
        else if (!this.texts.equals(other.texts))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.realmId == null) ? 0 : this.realmId.hashCode());
        result = prime * result + ((this.locale == null) ? 0 : this.locale.hashCode());
        result = prime * result + ((this.texts == null) ? 0 : this.texts.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RealmLocalizations (");

        sb.append(realmId);
        sb.append(", ").append(locale);
        sb.append(", ").append(texts);

        sb.append(")");
        return sb.toString();
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
}