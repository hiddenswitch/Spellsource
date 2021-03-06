/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.pojos;


import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IClientSession;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ClientSession implements VertxPojo, IClientSession {

    private static final long serialVersionUID = 1393935565;

    private String  id;
    private String  clientId;
    private String  redirectUri;
    private String  state;
    private Integer timestamp;
    private String  sessionId;
    private String  authMethod;
    private String  realmId;
    private String  authUserId;
    private String  currentAction;

    public ClientSession() {}

    public ClientSession(IClientSession value) {
        this.id = value.getId();
        this.clientId = value.getClientId();
        this.redirectUri = value.getRedirectUri();
        this.state = value.getState();
        this.timestamp = value.getTimestamp();
        this.sessionId = value.getSessionId();
        this.authMethod = value.getAuthMethod();
        this.realmId = value.getRealmId();
        this.authUserId = value.getAuthUserId();
        this.currentAction = value.getCurrentAction();
    }

    public ClientSession(
        String  id,
        String  clientId,
        String  redirectUri,
        String  state,
        Integer timestamp,
        String  sessionId,
        String  authMethod,
        String  realmId,
        String  authUserId,
        String  currentAction
    ) {
        this.id = id;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.state = state;
        this.timestamp = timestamp;
        this.sessionId = sessionId;
        this.authMethod = authMethod;
        this.realmId = realmId;
        this.authUserId = authUserId;
        this.currentAction = currentAction;
    }

    public ClientSession(io.vertx.core.json.JsonObject json) {
        this();
        fromJson(json);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public ClientSession setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String getClientId() {
        return this.clientId;
    }

    @Override
    public ClientSession setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    @Override
    public String getRedirectUri() {
        return this.redirectUri;
    }

    @Override
    public ClientSession setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
        return this;
    }

    @Override
    public String getState() {
        return this.state;
    }

    @Override
    public ClientSession setState(String state) {
        this.state = state;
        return this;
    }

    @Override
    public Integer getTimestamp() {
        return this.timestamp;
    }

    @Override
    public ClientSession setTimestamp(Integer timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public String getSessionId() {
        return this.sessionId;
    }

    @Override
    public ClientSession setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    @Override
    public String getAuthMethod() {
        return this.authMethod;
    }

    @Override
    public ClientSession setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
        return this;
    }

    @Override
    public String getRealmId() {
        return this.realmId;
    }

    @Override
    public ClientSession setRealmId(String realmId) {
        this.realmId = realmId;
        return this;
    }

    @Override
    public String getAuthUserId() {
        return this.authUserId;
    }

    @Override
    public ClientSession setAuthUserId(String authUserId) {
        this.authUserId = authUserId;
        return this;
    }

    @Override
    public String getCurrentAction() {
        return this.currentAction;
    }

    @Override
    public ClientSession setCurrentAction(String currentAction) {
        this.currentAction = currentAction;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ClientSession (");

        sb.append(id);
        sb.append(", ").append(clientId);
        sb.append(", ").append(redirectUri);
        sb.append(", ").append(state);
        sb.append(", ").append(timestamp);
        sb.append(", ").append(sessionId);
        sb.append(", ").append(authMethod);
        sb.append(", ").append(realmId);
        sb.append(", ").append(authUserId);
        sb.append(", ").append(currentAction);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IClientSession from) {
        setId(from.getId());
        setClientId(from.getClientId());
        setRedirectUri(from.getRedirectUri());
        setState(from.getState());
        setTimestamp(from.getTimestamp());
        setSessionId(from.getSessionId());
        setAuthMethod(from.getAuthMethod());
        setRealmId(from.getRealmId());
        setAuthUserId(from.getAuthUserId());
        setCurrentAction(from.getCurrentAction());
    }

    @Override
    public <E extends IClientSession> E into(E into) {
        into.from(this);
        return into;
    }
}
