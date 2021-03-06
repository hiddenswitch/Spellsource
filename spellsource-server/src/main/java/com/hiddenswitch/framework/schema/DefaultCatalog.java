/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema;


import com.hiddenswitch.framework.schema.hiddenswitch.Hiddenswitch;
import com.hiddenswitch.framework.schema.keycloak.Keycloak;
import com.hiddenswitch.framework.schema.spellsource.Spellsource;

import java.util.Arrays;
import java.util.List;

import org.jooq.Schema;
import org.jooq.impl.CatalogImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class DefaultCatalog extends CatalogImpl {

    private static final long serialVersionUID = 1235014137;

    /**
     * The reference instance of <code>DEFAULT_CATALOG</code>
     */
    public static final DefaultCatalog DEFAULT_CATALOG = new DefaultCatalog();

    /**
     * The schema <code>hiddenswitch</code>.
     */
    public final Hiddenswitch HIDDENSWITCH = Hiddenswitch.HIDDENSWITCH;

    /**
     * The schema <code>keycloak</code>.
     */
    public final Keycloak KEYCLOAK = Keycloak.KEYCLOAK;

    /**
     * The schema <code>spellsource</code>.
     */
    public final Spellsource SPELLSOURCE = Spellsource.SPELLSOURCE;

    /**
     * No further instances allowed
     */
    private DefaultCatalog() {
        super("");
    }

    @Override
    public final List<Schema> getSchemas() {
        return Arrays.<Schema>asList(
            Hiddenswitch.HIDDENSWITCH,
            Keycloak.KEYCLOAK,
            Spellsource.SPELLSOURCE);
    }
}
