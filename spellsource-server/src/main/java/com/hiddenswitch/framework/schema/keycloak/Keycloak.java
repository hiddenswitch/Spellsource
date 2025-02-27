/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak;


import com.hiddenswitch.framework.schema.DefaultCatalog;
import com.hiddenswitch.framework.schema.keycloak.tables.AdminEventEntity;
import com.hiddenswitch.framework.schema.keycloak.tables.AssociatedPolicy;
import com.hiddenswitch.framework.schema.keycloak.tables.AuthenticationExecution;
import com.hiddenswitch.framework.schema.keycloak.tables.AuthenticationFlow;
import com.hiddenswitch.framework.schema.keycloak.tables.AuthenticatorConfig;
import com.hiddenswitch.framework.schema.keycloak.tables.AuthenticatorConfigEntry;
import com.hiddenswitch.framework.schema.keycloak.tables.BrokerLink;
import com.hiddenswitch.framework.schema.keycloak.tables.Client;
import com.hiddenswitch.framework.schema.keycloak.tables.ClientAttributes;
import com.hiddenswitch.framework.schema.keycloak.tables.ClientAuthFlowBindings;
import com.hiddenswitch.framework.schema.keycloak.tables.ClientInitialAccess;
import com.hiddenswitch.framework.schema.keycloak.tables.ClientNodeRegistrations;
import com.hiddenswitch.framework.schema.keycloak.tables.ClientScope;
import com.hiddenswitch.framework.schema.keycloak.tables.ClientScopeAttributes;
import com.hiddenswitch.framework.schema.keycloak.tables.ClientScopeClient;
import com.hiddenswitch.framework.schema.keycloak.tables.ClientScopeRoleMapping;
import com.hiddenswitch.framework.schema.keycloak.tables.ClientSession;
import com.hiddenswitch.framework.schema.keycloak.tables.ClientSessionAuthStatus;
import com.hiddenswitch.framework.schema.keycloak.tables.ClientSessionNote;
import com.hiddenswitch.framework.schema.keycloak.tables.ClientSessionProtMapper;
import com.hiddenswitch.framework.schema.keycloak.tables.ClientSessionRole;
import com.hiddenswitch.framework.schema.keycloak.tables.ClientUserSessionNote;
import com.hiddenswitch.framework.schema.keycloak.tables.Component;
import com.hiddenswitch.framework.schema.keycloak.tables.ComponentConfig;
import com.hiddenswitch.framework.schema.keycloak.tables.CompositeRole;
import com.hiddenswitch.framework.schema.keycloak.tables.Credential;
import com.hiddenswitch.framework.schema.keycloak.tables.Databasechangelog;
import com.hiddenswitch.framework.schema.keycloak.tables.Databasechangeloglock;
import com.hiddenswitch.framework.schema.keycloak.tables.DefaultClientScope;
import com.hiddenswitch.framework.schema.keycloak.tables.EventEntity;
import com.hiddenswitch.framework.schema.keycloak.tables.FedUserAttribute;
import com.hiddenswitch.framework.schema.keycloak.tables.FedUserConsent;
import com.hiddenswitch.framework.schema.keycloak.tables.FedUserConsentClScope;
import com.hiddenswitch.framework.schema.keycloak.tables.FedUserCredential;
import com.hiddenswitch.framework.schema.keycloak.tables.FedUserGroupMembership;
import com.hiddenswitch.framework.schema.keycloak.tables.FedUserRequiredAction;
import com.hiddenswitch.framework.schema.keycloak.tables.FedUserRoleMapping;
import com.hiddenswitch.framework.schema.keycloak.tables.FederatedIdentity;
import com.hiddenswitch.framework.schema.keycloak.tables.FederatedUser;
import com.hiddenswitch.framework.schema.keycloak.tables.GroupAttribute;
import com.hiddenswitch.framework.schema.keycloak.tables.GroupRoleMapping;
import com.hiddenswitch.framework.schema.keycloak.tables.IdentityProvider;
import com.hiddenswitch.framework.schema.keycloak.tables.IdentityProviderConfig;
import com.hiddenswitch.framework.schema.keycloak.tables.IdentityProviderMapper;
import com.hiddenswitch.framework.schema.keycloak.tables.IdpMapperConfig;
import com.hiddenswitch.framework.schema.keycloak.tables.KeycloakGroup;
import com.hiddenswitch.framework.schema.keycloak.tables.KeycloakRole;
import com.hiddenswitch.framework.schema.keycloak.tables.MigrationModel;
import com.hiddenswitch.framework.schema.keycloak.tables.OfflineClientSession;
import com.hiddenswitch.framework.schema.keycloak.tables.OfflineUserSession;
import com.hiddenswitch.framework.schema.keycloak.tables.PolicyConfig;
import com.hiddenswitch.framework.schema.keycloak.tables.ProtocolMapper;
import com.hiddenswitch.framework.schema.keycloak.tables.ProtocolMapperConfig;
import com.hiddenswitch.framework.schema.keycloak.tables.Realm;
import com.hiddenswitch.framework.schema.keycloak.tables.RealmAttribute;
import com.hiddenswitch.framework.schema.keycloak.tables.RealmDefaultGroups;
import com.hiddenswitch.framework.schema.keycloak.tables.RealmEnabledEventTypes;
import com.hiddenswitch.framework.schema.keycloak.tables.RealmEventsListeners;
import com.hiddenswitch.framework.schema.keycloak.tables.RealmLocalizations;
import com.hiddenswitch.framework.schema.keycloak.tables.RealmRequiredCredential;
import com.hiddenswitch.framework.schema.keycloak.tables.RealmSmtpConfig;
import com.hiddenswitch.framework.schema.keycloak.tables.RealmSupportedLocales;
import com.hiddenswitch.framework.schema.keycloak.tables.RedirectUris;
import com.hiddenswitch.framework.schema.keycloak.tables.RequiredActionConfig;
import com.hiddenswitch.framework.schema.keycloak.tables.RequiredActionProvider;
import com.hiddenswitch.framework.schema.keycloak.tables.ResourceAttribute;
import com.hiddenswitch.framework.schema.keycloak.tables.ResourcePolicy;
import com.hiddenswitch.framework.schema.keycloak.tables.ResourceScope;
import com.hiddenswitch.framework.schema.keycloak.tables.ResourceServer;
import com.hiddenswitch.framework.schema.keycloak.tables.ResourceServerPermTicket;
import com.hiddenswitch.framework.schema.keycloak.tables.ResourceServerPolicy;
import com.hiddenswitch.framework.schema.keycloak.tables.ResourceServerResource;
import com.hiddenswitch.framework.schema.keycloak.tables.ResourceServerScope;
import com.hiddenswitch.framework.schema.keycloak.tables.ResourceUris;
import com.hiddenswitch.framework.schema.keycloak.tables.RoleAttribute;
import com.hiddenswitch.framework.schema.keycloak.tables.ScopeMapping;
import com.hiddenswitch.framework.schema.keycloak.tables.ScopePolicy;
import com.hiddenswitch.framework.schema.keycloak.tables.UserAttribute;
import com.hiddenswitch.framework.schema.keycloak.tables.UserConsent;
import com.hiddenswitch.framework.schema.keycloak.tables.UserConsentClientScope;
import com.hiddenswitch.framework.schema.keycloak.tables.UserEntity;
import com.hiddenswitch.framework.schema.keycloak.tables.UserFederationConfig;
import com.hiddenswitch.framework.schema.keycloak.tables.UserFederationMapper;
import com.hiddenswitch.framework.schema.keycloak.tables.UserFederationMapperConfig;
import com.hiddenswitch.framework.schema.keycloak.tables.UserFederationProvider;
import com.hiddenswitch.framework.schema.keycloak.tables.UserGroupMembership;
import com.hiddenswitch.framework.schema.keycloak.tables.UserRequiredAction;
import com.hiddenswitch.framework.schema.keycloak.tables.UserRoleMapping;
import com.hiddenswitch.framework.schema.keycloak.tables.UserSession;
import com.hiddenswitch.framework.schema.keycloak.tables.UserSessionNote;
import com.hiddenswitch.framework.schema.keycloak.tables.UsernameLoginFailure;
import com.hiddenswitch.framework.schema.keycloak.tables.WebOrigins;

import java.util.Arrays;
import java.util.List;

import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keycloak extends SchemaImpl {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>keycloak</code>
     */
    public static final Keycloak KEYCLOAK = new Keycloak();

    /**
     * The table <code>keycloak.admin_event_entity</code>.
     */
    public final AdminEventEntity ADMIN_EVENT_ENTITY = AdminEventEntity.ADMIN_EVENT_ENTITY;

    /**
     * The table <code>keycloak.associated_policy</code>.
     */
    public final AssociatedPolicy ASSOCIATED_POLICY = AssociatedPolicy.ASSOCIATED_POLICY;

    /**
     * The table <code>keycloak.authentication_execution</code>.
     */
    public final AuthenticationExecution AUTHENTICATION_EXECUTION = AuthenticationExecution.AUTHENTICATION_EXECUTION;

    /**
     * The table <code>keycloak.authentication_flow</code>.
     */
    public final AuthenticationFlow AUTHENTICATION_FLOW = AuthenticationFlow.AUTHENTICATION_FLOW;

    /**
     * The table <code>keycloak.authenticator_config</code>.
     */
    public final AuthenticatorConfig AUTHENTICATOR_CONFIG = AuthenticatorConfig.AUTHENTICATOR_CONFIG;

    /**
     * The table <code>keycloak.authenticator_config_entry</code>.
     */
    public final AuthenticatorConfigEntry AUTHENTICATOR_CONFIG_ENTRY = AuthenticatorConfigEntry.AUTHENTICATOR_CONFIG_ENTRY;

    /**
     * The table <code>keycloak.broker_link</code>.
     */
    public final BrokerLink BROKER_LINK = BrokerLink.BROKER_LINK;

    /**
     * The table <code>keycloak.client</code>.
     */
    public final Client CLIENT = Client.CLIENT;

    /**
     * The table <code>keycloak.client_attributes</code>.
     */
    public final ClientAttributes CLIENT_ATTRIBUTES = ClientAttributes.CLIENT_ATTRIBUTES;

    /**
     * The table <code>keycloak.client_auth_flow_bindings</code>.
     */
    public final ClientAuthFlowBindings CLIENT_AUTH_FLOW_BINDINGS = ClientAuthFlowBindings.CLIENT_AUTH_FLOW_BINDINGS;

    /**
     * The table <code>keycloak.client_initial_access</code>.
     */
    public final ClientInitialAccess CLIENT_INITIAL_ACCESS = ClientInitialAccess.CLIENT_INITIAL_ACCESS;

    /**
     * The table <code>keycloak.client_node_registrations</code>.
     */
    public final ClientNodeRegistrations CLIENT_NODE_REGISTRATIONS = ClientNodeRegistrations.CLIENT_NODE_REGISTRATIONS;

    /**
     * The table <code>keycloak.client_scope</code>.
     */
    public final ClientScope CLIENT_SCOPE = ClientScope.CLIENT_SCOPE;

    /**
     * The table <code>keycloak.client_scope_attributes</code>.
     */
    public final ClientScopeAttributes CLIENT_SCOPE_ATTRIBUTES = ClientScopeAttributes.CLIENT_SCOPE_ATTRIBUTES;

    /**
     * The table <code>keycloak.client_scope_client</code>.
     */
    public final ClientScopeClient CLIENT_SCOPE_CLIENT = ClientScopeClient.CLIENT_SCOPE_CLIENT;

    /**
     * The table <code>keycloak.client_scope_role_mapping</code>.
     */
    public final ClientScopeRoleMapping CLIENT_SCOPE_ROLE_MAPPING = ClientScopeRoleMapping.CLIENT_SCOPE_ROLE_MAPPING;

    /**
     * The table <code>keycloak.client_session</code>.
     */
    public final ClientSession CLIENT_SESSION = ClientSession.CLIENT_SESSION;

    /**
     * The table <code>keycloak.client_session_auth_status</code>.
     */
    public final ClientSessionAuthStatus CLIENT_SESSION_AUTH_STATUS = ClientSessionAuthStatus.CLIENT_SESSION_AUTH_STATUS;

    /**
     * The table <code>keycloak.client_session_note</code>.
     */
    public final ClientSessionNote CLIENT_SESSION_NOTE = ClientSessionNote.CLIENT_SESSION_NOTE;

    /**
     * The table <code>keycloak.client_session_prot_mapper</code>.
     */
    public final ClientSessionProtMapper CLIENT_SESSION_PROT_MAPPER = ClientSessionProtMapper.CLIENT_SESSION_PROT_MAPPER;

    /**
     * The table <code>keycloak.client_session_role</code>.
     */
    public final ClientSessionRole CLIENT_SESSION_ROLE = ClientSessionRole.CLIENT_SESSION_ROLE;

    /**
     * The table <code>keycloak.client_user_session_note</code>.
     */
    public final ClientUserSessionNote CLIENT_USER_SESSION_NOTE = ClientUserSessionNote.CLIENT_USER_SESSION_NOTE;

    /**
     * The table <code>keycloak.component</code>.
     */
    public final Component COMPONENT = Component.COMPONENT;

    /**
     * The table <code>keycloak.component_config</code>.
     */
    public final ComponentConfig COMPONENT_CONFIG = ComponentConfig.COMPONENT_CONFIG;

    /**
     * The table <code>keycloak.composite_role</code>.
     */
    public final CompositeRole COMPOSITE_ROLE = CompositeRole.COMPOSITE_ROLE;

    /**
     * The table <code>keycloak.credential</code>.
     */
    public final Credential CREDENTIAL = Credential.CREDENTIAL;

    /**
     * The table <code>keycloak.databasechangelog</code>.
     */
    public final Databasechangelog DATABASECHANGELOG = Databasechangelog.DATABASECHANGELOG;

    /**
     * The table <code>keycloak.databasechangeloglock</code>.
     */
    public final Databasechangeloglock DATABASECHANGELOGLOCK = Databasechangeloglock.DATABASECHANGELOGLOCK;

    /**
     * The table <code>keycloak.default_client_scope</code>.
     */
    public final DefaultClientScope DEFAULT_CLIENT_SCOPE = DefaultClientScope.DEFAULT_CLIENT_SCOPE;

    /**
     * The table <code>keycloak.event_entity</code>.
     */
    public final EventEntity EVENT_ENTITY = EventEntity.EVENT_ENTITY;

    /**
     * The table <code>keycloak.fed_user_attribute</code>.
     */
    public final FedUserAttribute FED_USER_ATTRIBUTE = FedUserAttribute.FED_USER_ATTRIBUTE;

    /**
     * The table <code>keycloak.fed_user_consent</code>.
     */
    public final FedUserConsent FED_USER_CONSENT = FedUserConsent.FED_USER_CONSENT;

    /**
     * The table <code>keycloak.fed_user_consent_cl_scope</code>.
     */
    public final FedUserConsentClScope FED_USER_CONSENT_CL_SCOPE = FedUserConsentClScope.FED_USER_CONSENT_CL_SCOPE;

    /**
     * The table <code>keycloak.fed_user_credential</code>.
     */
    public final FedUserCredential FED_USER_CREDENTIAL = FedUserCredential.FED_USER_CREDENTIAL;

    /**
     * The table <code>keycloak.fed_user_group_membership</code>.
     */
    public final FedUserGroupMembership FED_USER_GROUP_MEMBERSHIP = FedUserGroupMembership.FED_USER_GROUP_MEMBERSHIP;

    /**
     * The table <code>keycloak.fed_user_required_action</code>.
     */
    public final FedUserRequiredAction FED_USER_REQUIRED_ACTION = FedUserRequiredAction.FED_USER_REQUIRED_ACTION;

    /**
     * The table <code>keycloak.fed_user_role_mapping</code>.
     */
    public final FedUserRoleMapping FED_USER_ROLE_MAPPING = FedUserRoleMapping.FED_USER_ROLE_MAPPING;

    /**
     * The table <code>keycloak.federated_identity</code>.
     */
    public final FederatedIdentity FEDERATED_IDENTITY = FederatedIdentity.FEDERATED_IDENTITY;

    /**
     * The table <code>keycloak.federated_user</code>.
     */
    public final FederatedUser FEDERATED_USER = FederatedUser.FEDERATED_USER;

    /**
     * The table <code>keycloak.group_attribute</code>.
     */
    public final GroupAttribute GROUP_ATTRIBUTE = GroupAttribute.GROUP_ATTRIBUTE;

    /**
     * The table <code>keycloak.group_role_mapping</code>.
     */
    public final GroupRoleMapping GROUP_ROLE_MAPPING = GroupRoleMapping.GROUP_ROLE_MAPPING;

    /**
     * The table <code>keycloak.identity_provider</code>.
     */
    public final IdentityProvider IDENTITY_PROVIDER = IdentityProvider.IDENTITY_PROVIDER;

    /**
     * The table <code>keycloak.identity_provider_config</code>.
     */
    public final IdentityProviderConfig IDENTITY_PROVIDER_CONFIG = IdentityProviderConfig.IDENTITY_PROVIDER_CONFIG;

    /**
     * The table <code>keycloak.identity_provider_mapper</code>.
     */
    public final IdentityProviderMapper IDENTITY_PROVIDER_MAPPER = IdentityProviderMapper.IDENTITY_PROVIDER_MAPPER;

    /**
     * The table <code>keycloak.idp_mapper_config</code>.
     */
    public final IdpMapperConfig IDP_MAPPER_CONFIG = IdpMapperConfig.IDP_MAPPER_CONFIG;

    /**
     * The table <code>keycloak.keycloak_group</code>.
     */
    public final KeycloakGroup KEYCLOAK_GROUP = KeycloakGroup.KEYCLOAK_GROUP;

    /**
     * The table <code>keycloak.keycloak_role</code>.
     */
    public final KeycloakRole KEYCLOAK_ROLE = KeycloakRole.KEYCLOAK_ROLE;

    /**
     * The table <code>keycloak.migration_model</code>.
     */
    public final MigrationModel MIGRATION_MODEL = MigrationModel.MIGRATION_MODEL;

    /**
     * The table <code>keycloak.offline_client_session</code>.
     */
    public final OfflineClientSession OFFLINE_CLIENT_SESSION = OfflineClientSession.OFFLINE_CLIENT_SESSION;

    /**
     * The table <code>keycloak.offline_user_session</code>.
     */
    public final OfflineUserSession OFFLINE_USER_SESSION = OfflineUserSession.OFFLINE_USER_SESSION;

    /**
     * The table <code>keycloak.policy_config</code>.
     */
    public final PolicyConfig POLICY_CONFIG = PolicyConfig.POLICY_CONFIG;

    /**
     * The table <code>keycloak.protocol_mapper</code>.
     */
    public final ProtocolMapper PROTOCOL_MAPPER = ProtocolMapper.PROTOCOL_MAPPER;

    /**
     * The table <code>keycloak.protocol_mapper_config</code>.
     */
    public final ProtocolMapperConfig PROTOCOL_MAPPER_CONFIG = ProtocolMapperConfig.PROTOCOL_MAPPER_CONFIG;

    /**
     * The table <code>keycloak.realm</code>.
     */
    public final Realm REALM = Realm.REALM;

    /**
     * The table <code>keycloak.realm_attribute</code>.
     */
    public final RealmAttribute REALM_ATTRIBUTE = RealmAttribute.REALM_ATTRIBUTE;

    /**
     * The table <code>keycloak.realm_default_groups</code>.
     */
    public final RealmDefaultGroups REALM_DEFAULT_GROUPS = RealmDefaultGroups.REALM_DEFAULT_GROUPS;

    /**
     * The table <code>keycloak.realm_enabled_event_types</code>.
     */
    public final RealmEnabledEventTypes REALM_ENABLED_EVENT_TYPES = RealmEnabledEventTypes.REALM_ENABLED_EVENT_TYPES;

    /**
     * The table <code>keycloak.realm_events_listeners</code>.
     */
    public final RealmEventsListeners REALM_EVENTS_LISTENERS = RealmEventsListeners.REALM_EVENTS_LISTENERS;

    /**
     * The table <code>keycloak.realm_localizations</code>.
     */
    public final RealmLocalizations REALM_LOCALIZATIONS = RealmLocalizations.REALM_LOCALIZATIONS;

    /**
     * The table <code>keycloak.realm_required_credential</code>.
     */
    public final RealmRequiredCredential REALM_REQUIRED_CREDENTIAL = RealmRequiredCredential.REALM_REQUIRED_CREDENTIAL;

    /**
     * The table <code>keycloak.realm_smtp_config</code>.
     */
    public final RealmSmtpConfig REALM_SMTP_CONFIG = RealmSmtpConfig.REALM_SMTP_CONFIG;

    /**
     * The table <code>keycloak.realm_supported_locales</code>.
     */
    public final RealmSupportedLocales REALM_SUPPORTED_LOCALES = RealmSupportedLocales.REALM_SUPPORTED_LOCALES;

    /**
     * The table <code>keycloak.redirect_uris</code>.
     */
    public final RedirectUris REDIRECT_URIS = RedirectUris.REDIRECT_URIS;

    /**
     * The table <code>keycloak.required_action_config</code>.
     */
    public final RequiredActionConfig REQUIRED_ACTION_CONFIG = RequiredActionConfig.REQUIRED_ACTION_CONFIG;

    /**
     * The table <code>keycloak.required_action_provider</code>.
     */
    public final RequiredActionProvider REQUIRED_ACTION_PROVIDER = RequiredActionProvider.REQUIRED_ACTION_PROVIDER;

    /**
     * The table <code>keycloak.resource_attribute</code>.
     */
    public final ResourceAttribute RESOURCE_ATTRIBUTE = ResourceAttribute.RESOURCE_ATTRIBUTE;

    /**
     * The table <code>keycloak.resource_policy</code>.
     */
    public final ResourcePolicy RESOURCE_POLICY = ResourcePolicy.RESOURCE_POLICY;

    /**
     * The table <code>keycloak.resource_scope</code>.
     */
    public final ResourceScope RESOURCE_SCOPE = ResourceScope.RESOURCE_SCOPE;

    /**
     * The table <code>keycloak.resource_server</code>.
     */
    public final ResourceServer RESOURCE_SERVER = ResourceServer.RESOURCE_SERVER;

    /**
     * The table <code>keycloak.resource_server_perm_ticket</code>.
     */
    public final ResourceServerPermTicket RESOURCE_SERVER_PERM_TICKET = ResourceServerPermTicket.RESOURCE_SERVER_PERM_TICKET;

    /**
     * The table <code>keycloak.resource_server_policy</code>.
     */
    public final ResourceServerPolicy RESOURCE_SERVER_POLICY = ResourceServerPolicy.RESOURCE_SERVER_POLICY;

    /**
     * The table <code>keycloak.resource_server_resource</code>.
     */
    public final ResourceServerResource RESOURCE_SERVER_RESOURCE = ResourceServerResource.RESOURCE_SERVER_RESOURCE;

    /**
     * The table <code>keycloak.resource_server_scope</code>.
     */
    public final ResourceServerScope RESOURCE_SERVER_SCOPE = ResourceServerScope.RESOURCE_SERVER_SCOPE;

    /**
     * The table <code>keycloak.resource_uris</code>.
     */
    public final ResourceUris RESOURCE_URIS = ResourceUris.RESOURCE_URIS;

    /**
     * The table <code>keycloak.role_attribute</code>.
     */
    public final RoleAttribute ROLE_ATTRIBUTE = RoleAttribute.ROLE_ATTRIBUTE;

    /**
     * The table <code>keycloak.scope_mapping</code>.
     */
    public final ScopeMapping SCOPE_MAPPING = ScopeMapping.SCOPE_MAPPING;

    /**
     * The table <code>keycloak.scope_policy</code>.
     */
    public final ScopePolicy SCOPE_POLICY = ScopePolicy.SCOPE_POLICY;

    /**
     * The table <code>keycloak.user_attribute</code>.
     */
    public final UserAttribute USER_ATTRIBUTE = UserAttribute.USER_ATTRIBUTE;

    /**
     * The table <code>keycloak.user_consent</code>.
     */
    public final UserConsent USER_CONSENT = UserConsent.USER_CONSENT;

    /**
     * The table <code>keycloak.user_consent_client_scope</code>.
     */
    public final UserConsentClientScope USER_CONSENT_CLIENT_SCOPE = UserConsentClientScope.USER_CONSENT_CLIENT_SCOPE;

    /**
     * The table <code>keycloak.user_entity</code>.
     */
    public final UserEntity USER_ENTITY = UserEntity.USER_ENTITY;

    /**
     * The table <code>keycloak.user_federation_config</code>.
     */
    public final UserFederationConfig USER_FEDERATION_CONFIG = UserFederationConfig.USER_FEDERATION_CONFIG;

    /**
     * The table <code>keycloak.user_federation_mapper</code>.
     */
    public final UserFederationMapper USER_FEDERATION_MAPPER = UserFederationMapper.USER_FEDERATION_MAPPER;

    /**
     * The table <code>keycloak.user_federation_mapper_config</code>.
     */
    public final UserFederationMapperConfig USER_FEDERATION_MAPPER_CONFIG = UserFederationMapperConfig.USER_FEDERATION_MAPPER_CONFIG;

    /**
     * The table <code>keycloak.user_federation_provider</code>.
     */
    public final UserFederationProvider USER_FEDERATION_PROVIDER = UserFederationProvider.USER_FEDERATION_PROVIDER;

    /**
     * The table <code>keycloak.user_group_membership</code>.
     */
    public final UserGroupMembership USER_GROUP_MEMBERSHIP = UserGroupMembership.USER_GROUP_MEMBERSHIP;

    /**
     * The table <code>keycloak.user_required_action</code>.
     */
    public final UserRequiredAction USER_REQUIRED_ACTION = UserRequiredAction.USER_REQUIRED_ACTION;

    /**
     * The table <code>keycloak.user_role_mapping</code>.
     */
    public final UserRoleMapping USER_ROLE_MAPPING = UserRoleMapping.USER_ROLE_MAPPING;

    /**
     * The table <code>keycloak.user_session</code>.
     */
    public final UserSession USER_SESSION = UserSession.USER_SESSION;

    /**
     * The table <code>keycloak.user_session_note</code>.
     */
    public final UserSessionNote USER_SESSION_NOTE = UserSessionNote.USER_SESSION_NOTE;

    /**
     * The table <code>keycloak.username_login_failure</code>.
     */
    public final UsernameLoginFailure USERNAME_LOGIN_FAILURE = UsernameLoginFailure.USERNAME_LOGIN_FAILURE;

    /**
     * The table <code>keycloak.web_origins</code>.
     */
    public final WebOrigins WEB_ORIGINS = WebOrigins.WEB_ORIGINS;

    /**
     * No further instances allowed
     */
    private Keycloak() {
        super("keycloak", null);
    }


    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        return Arrays.asList(
            AdminEventEntity.ADMIN_EVENT_ENTITY,
            AssociatedPolicy.ASSOCIATED_POLICY,
            AuthenticationExecution.AUTHENTICATION_EXECUTION,
            AuthenticationFlow.AUTHENTICATION_FLOW,
            AuthenticatorConfig.AUTHENTICATOR_CONFIG,
            AuthenticatorConfigEntry.AUTHENTICATOR_CONFIG_ENTRY,
            BrokerLink.BROKER_LINK,
            Client.CLIENT,
            ClientAttributes.CLIENT_ATTRIBUTES,
            ClientAuthFlowBindings.CLIENT_AUTH_FLOW_BINDINGS,
            ClientInitialAccess.CLIENT_INITIAL_ACCESS,
            ClientNodeRegistrations.CLIENT_NODE_REGISTRATIONS,
            ClientScope.CLIENT_SCOPE,
            ClientScopeAttributes.CLIENT_SCOPE_ATTRIBUTES,
            ClientScopeClient.CLIENT_SCOPE_CLIENT,
            ClientScopeRoleMapping.CLIENT_SCOPE_ROLE_MAPPING,
            ClientSession.CLIENT_SESSION,
            ClientSessionAuthStatus.CLIENT_SESSION_AUTH_STATUS,
            ClientSessionNote.CLIENT_SESSION_NOTE,
            ClientSessionProtMapper.CLIENT_SESSION_PROT_MAPPER,
            ClientSessionRole.CLIENT_SESSION_ROLE,
            ClientUserSessionNote.CLIENT_USER_SESSION_NOTE,
            Component.COMPONENT,
            ComponentConfig.COMPONENT_CONFIG,
            CompositeRole.COMPOSITE_ROLE,
            Credential.CREDENTIAL,
            Databasechangelog.DATABASECHANGELOG,
            Databasechangeloglock.DATABASECHANGELOGLOCK,
            DefaultClientScope.DEFAULT_CLIENT_SCOPE,
            EventEntity.EVENT_ENTITY,
            FedUserAttribute.FED_USER_ATTRIBUTE,
            FedUserConsent.FED_USER_CONSENT,
            FedUserConsentClScope.FED_USER_CONSENT_CL_SCOPE,
            FedUserCredential.FED_USER_CREDENTIAL,
            FedUserGroupMembership.FED_USER_GROUP_MEMBERSHIP,
            FedUserRequiredAction.FED_USER_REQUIRED_ACTION,
            FedUserRoleMapping.FED_USER_ROLE_MAPPING,
            FederatedIdentity.FEDERATED_IDENTITY,
            FederatedUser.FEDERATED_USER,
            GroupAttribute.GROUP_ATTRIBUTE,
            GroupRoleMapping.GROUP_ROLE_MAPPING,
            IdentityProvider.IDENTITY_PROVIDER,
            IdentityProviderConfig.IDENTITY_PROVIDER_CONFIG,
            IdentityProviderMapper.IDENTITY_PROVIDER_MAPPER,
            IdpMapperConfig.IDP_MAPPER_CONFIG,
            KeycloakGroup.KEYCLOAK_GROUP,
            KeycloakRole.KEYCLOAK_ROLE,
            MigrationModel.MIGRATION_MODEL,
            OfflineClientSession.OFFLINE_CLIENT_SESSION,
            OfflineUserSession.OFFLINE_USER_SESSION,
            PolicyConfig.POLICY_CONFIG,
            ProtocolMapper.PROTOCOL_MAPPER,
            ProtocolMapperConfig.PROTOCOL_MAPPER_CONFIG,
            Realm.REALM,
            RealmAttribute.REALM_ATTRIBUTE,
            RealmDefaultGroups.REALM_DEFAULT_GROUPS,
            RealmEnabledEventTypes.REALM_ENABLED_EVENT_TYPES,
            RealmEventsListeners.REALM_EVENTS_LISTENERS,
            RealmLocalizations.REALM_LOCALIZATIONS,
            RealmRequiredCredential.REALM_REQUIRED_CREDENTIAL,
            RealmSmtpConfig.REALM_SMTP_CONFIG,
            RealmSupportedLocales.REALM_SUPPORTED_LOCALES,
            RedirectUris.REDIRECT_URIS,
            RequiredActionConfig.REQUIRED_ACTION_CONFIG,
            RequiredActionProvider.REQUIRED_ACTION_PROVIDER,
            ResourceAttribute.RESOURCE_ATTRIBUTE,
            ResourcePolicy.RESOURCE_POLICY,
            ResourceScope.RESOURCE_SCOPE,
            ResourceServer.RESOURCE_SERVER,
            ResourceServerPermTicket.RESOURCE_SERVER_PERM_TICKET,
            ResourceServerPolicy.RESOURCE_SERVER_POLICY,
            ResourceServerResource.RESOURCE_SERVER_RESOURCE,
            ResourceServerScope.RESOURCE_SERVER_SCOPE,
            ResourceUris.RESOURCE_URIS,
            RoleAttribute.ROLE_ATTRIBUTE,
            ScopeMapping.SCOPE_MAPPING,
            ScopePolicy.SCOPE_POLICY,
            UserAttribute.USER_ATTRIBUTE,
            UserConsent.USER_CONSENT,
            UserConsentClientScope.USER_CONSENT_CLIENT_SCOPE,
            UserEntity.USER_ENTITY,
            UserFederationConfig.USER_FEDERATION_CONFIG,
            UserFederationMapper.USER_FEDERATION_MAPPER,
            UserFederationMapperConfig.USER_FEDERATION_MAPPER_CONFIG,
            UserFederationProvider.USER_FEDERATION_PROVIDER,
            UserGroupMembership.USER_GROUP_MEMBERSHIP,
            UserRequiredAction.USER_REQUIRED_ACTION,
            UserRoleMapping.USER_ROLE_MAPPING,
            UserSession.USER_SESSION,
            UserSessionNote.USER_SESSION_NOTE,
            UsernameLoginFailure.USERNAME_LOGIN_FAILURE,
            WebOrigins.WEB_ORIGINS
        );
    }
}
