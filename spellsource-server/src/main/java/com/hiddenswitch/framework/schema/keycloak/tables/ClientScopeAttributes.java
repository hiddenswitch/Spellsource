/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables;


import com.hiddenswitch.framework.schema.keycloak.Indexes;
import com.hiddenswitch.framework.schema.keycloak.Keycloak;
import com.hiddenswitch.framework.schema.keycloak.Keys;
import com.hiddenswitch.framework.schema.keycloak.tables.records.ClientScopeAttributesRecord;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Function3;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Records;
import org.jooq.Row3;
import org.jooq.Schema;
import org.jooq.SelectField;
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
public class ClientScopeAttributes extends TableImpl<ClientScopeAttributesRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>keycloak.client_scope_attributes</code>
     */
    public static final ClientScopeAttributes CLIENT_SCOPE_ATTRIBUTES = new ClientScopeAttributes();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ClientScopeAttributesRecord> getRecordType() {
        return ClientScopeAttributesRecord.class;
    }

    /**
     * The column <code>keycloak.client_scope_attributes.scope_id</code>.
     */
    public final TableField<ClientScopeAttributesRecord, String> SCOPE_ID = createField(DSL.name("scope_id"), SQLDataType.VARCHAR(36).nullable(false), this, "");

    /**
     * The column <code>keycloak.client_scope_attributes.value</code>.
     */
    public final TableField<ClientScopeAttributesRecord, String> VALUE = createField(DSL.name("value"), SQLDataType.VARCHAR(2048), this, "");

    /**
     * The column <code>keycloak.client_scope_attributes.name</code>.
     */
    public final TableField<ClientScopeAttributesRecord, String> NAME = createField(DSL.name("name"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    private ClientScopeAttributes(Name alias, Table<ClientScopeAttributesRecord> aliased) {
        this(alias, aliased, null);
    }

    private ClientScopeAttributes(Name alias, Table<ClientScopeAttributesRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>keycloak.client_scope_attributes</code> table
     * reference
     */
    public ClientScopeAttributes(String alias) {
        this(DSL.name(alias), CLIENT_SCOPE_ATTRIBUTES);
    }

    /**
     * Create an aliased <code>keycloak.client_scope_attributes</code> table
     * reference
     */
    public ClientScopeAttributes(Name alias) {
        this(alias, CLIENT_SCOPE_ATTRIBUTES);
    }

    /**
     * Create a <code>keycloak.client_scope_attributes</code> table reference
     */
    public ClientScopeAttributes() {
        this(DSL.name("client_scope_attributes"), null);
    }

    public <O extends Record> ClientScopeAttributes(Table<O> child, ForeignKey<O, ClientScopeAttributesRecord> key) {
        super(child, key, CLIENT_SCOPE_ATTRIBUTES);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Keycloak.KEYCLOAK;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.asList(Indexes.IDX_CLSCOPE_ATTRS);
    }

    @Override
    public UniqueKey<ClientScopeAttributesRecord> getPrimaryKey() {
        return Keys.PK_CL_TMPL_ATTR;
    }

    @Override
    public List<ForeignKey<ClientScopeAttributesRecord, ?>> getReferences() {
        return Arrays.asList(Keys.CLIENT_SCOPE_ATTRIBUTES__FK_CL_SCOPE_ATTR_SCOPE);
    }

    private transient ClientScope _clientScope;

    /**
     * Get the implicit join path to the <code>keycloak.client_scope</code>
     * table.
     */
    public ClientScope clientScope() {
        if (_clientScope == null)
            _clientScope = new ClientScope(this, Keys.CLIENT_SCOPE_ATTRIBUTES__FK_CL_SCOPE_ATTR_SCOPE);

        return _clientScope;
    }

    @Override
    public ClientScopeAttributes as(String alias) {
        return new ClientScopeAttributes(DSL.name(alias), this);
    }

    @Override
    public ClientScopeAttributes as(Name alias) {
        return new ClientScopeAttributes(alias, this);
    }

    @Override
    public ClientScopeAttributes as(Table<?> alias) {
        return new ClientScopeAttributes(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public ClientScopeAttributes rename(String name) {
        return new ClientScopeAttributes(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public ClientScopeAttributes rename(Name name) {
        return new ClientScopeAttributes(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public ClientScopeAttributes rename(Table<?> name) {
        return new ClientScopeAttributes(name.getQualifiedName(), null);
    }

    // -------------------------------------------------------------------------
    // Row3 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row3<String, String, String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function3<? super String, ? super String, ? super String, ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function3<? super String, ? super String, ? super String, ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}