/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.pojos;


import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IFedUserRequiredAction;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class FedUserRequiredAction implements VertxPojo, IFedUserRequiredAction {

    private static final long serialVersionUID = 1584948154;

    private String requiredAction;
    private String userId;
    private String realmId;
    private String storageProviderId;

    public FedUserRequiredAction() {}

    public FedUserRequiredAction(IFedUserRequiredAction value) {
        this.requiredAction = value.getRequiredAction();
        this.userId = value.getUserId();
        this.realmId = value.getRealmId();
        this.storageProviderId = value.getStorageProviderId();
    }

    public FedUserRequiredAction(
        String requiredAction,
        String userId,
        String realmId,
        String storageProviderId
    ) {
        this.requiredAction = requiredAction;
        this.userId = userId;
        this.realmId = realmId;
        this.storageProviderId = storageProviderId;
    }

    public FedUserRequiredAction(io.vertx.core.json.JsonObject json) {
        this();
        fromJson(json);
    }

    @Override
    public String getRequiredAction() {
        return this.requiredAction;
    }

    @Override
    public FedUserRequiredAction setRequiredAction(String requiredAction) {
        this.requiredAction = requiredAction;
        return this;
    }

    @Override
    public String getUserId() {
        return this.userId;
    }

    @Override
    public FedUserRequiredAction setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    @Override
    public String getRealmId() {
        return this.realmId;
    }

    @Override
    public FedUserRequiredAction setRealmId(String realmId) {
        this.realmId = realmId;
        return this;
    }

    @Override
    public String getStorageProviderId() {
        return this.storageProviderId;
    }

    @Override
    public FedUserRequiredAction setStorageProviderId(String storageProviderId) {
        this.storageProviderId = storageProviderId;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("FedUserRequiredAction (");

        sb.append(requiredAction);
        sb.append(", ").append(userId);
        sb.append(", ").append(realmId);
        sb.append(", ").append(storageProviderId);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IFedUserRequiredAction from) {
        setRequiredAction(from.getRequiredAction());
        setUserId(from.getUserId());
        setRealmId(from.getRealmId());
        setStorageProviderId(from.getStorageProviderId());
    }

    @Override
    public <E extends IFedUserRequiredAction> E into(E into) {
        into.from(this);
        return into;
    }
}
