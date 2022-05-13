/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables;


import com.hiddenswitch.framework.schema.keycloak.Keycloak;
import com.hiddenswitch.framework.schema.keycloak.Keys;
import com.hiddenswitch.framework.schema.keycloak.tables.records.ClientTemplateAttributesRecord;

import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row3;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ClientTemplateAttributes extends TableImpl<ClientTemplateAttributesRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of
     * <code>keycloak.client_template_attributes</code>
     */
    public static final ClientTemplateAttributes CLIENT_TEMPLATE_ATTRIBUTES = new ClientTemplateAttributes();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ClientTemplateAttributesRecord> getRecordType() {
        return ClientTemplateAttributesRecord.class;
    }

    /**
     * The column <code>keycloak.client_template_attributes.template_id</code>.
     */
    public final TableField<ClientTemplateAttributesRecord, String> TEMPLATE_ID = createField(DSL.name("template_id"), SQLDataType.VARCHAR(36).nullable(false), this, "");

    /**
     * The column <code>keycloak.client_template_attributes.value</code>.
     */
    public final TableField<ClientTemplateAttributesRecord, String> VALUE = createField(DSL.name("value"), SQLDataType.VARCHAR(2048), this, "");

    /**
     * The column <code>keycloak.client_template_attributes.name</code>.
     */
    public final TableField<ClientTemplateAttributesRecord, String> NAME = createField(DSL.name("name"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    private ClientTemplateAttributes(Name alias, Table<ClientTemplateAttributesRecord> aliased) {
        this(alias, aliased, null);
    }

    private ClientTemplateAttributes(Name alias, Table<ClientTemplateAttributesRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>keycloak.client_template_attributes</code> table
     * reference
     */
    public ClientTemplateAttributes(String alias) {
        this(DSL.name(alias), CLIENT_TEMPLATE_ATTRIBUTES);
    }

    /**
     * Create an aliased <code>keycloak.client_template_attributes</code> table
     * reference
     */
    public ClientTemplateAttributes(Name alias) {
        this(alias, CLIENT_TEMPLATE_ATTRIBUTES);
    }

    /**
     * Create a <code>keycloak.client_template_attributes</code> table reference
     */
    public ClientTemplateAttributes() {
        this(DSL.name("client_template_attributes"), null);
    }

    public <O extends Record> ClientTemplateAttributes(Table<O> child, ForeignKey<O, ClientTemplateAttributesRecord> key) {
        super(child, key, CLIENT_TEMPLATE_ATTRIBUTES);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Keycloak.KEYCLOAK;
    }

    @Override
    public UniqueKey<ClientTemplateAttributesRecord> getPrimaryKey() {
        return Keys.PK_CL_TMPL_ATTR;
    }

    @Override
    public List<ForeignKey<ClientTemplateAttributesRecord, ?>> getReferences() {
        return Arrays.asList(Keys.CLIENT_TEMPLATE_ATTRIBUTES__FK_CL_TEMPL_ATTR_TEMPL);
    }

    private transient ClientTemplate _clientTemplate;

    /**
     * Get the implicit join path to the <code>keycloak.client_template</code>
     * table.
     */
    public ClientTemplate clientTemplate() {
        if (_clientTemplate == null)
            _clientTemplate = new ClientTemplate(this, Keys.CLIENT_TEMPLATE_ATTRIBUTES__FK_CL_TEMPL_ATTR_TEMPL);

        return _clientTemplate;
    }

    @Override
    public ClientTemplateAttributes as(String alias) {
        return new ClientTemplateAttributes(DSL.name(alias), this);
    }

    @Override
    public ClientTemplateAttributes as(Name alias) {
        return new ClientTemplateAttributes(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public ClientTemplateAttributes rename(String name) {
        return new ClientTemplateAttributes(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public ClientTemplateAttributes rename(Name name) {
        return new ClientTemplateAttributes(name, null);
    }

    // -------------------------------------------------------------------------
    // Row3 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row3<String, String, String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }
}