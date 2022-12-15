package com.hiddenswitch.framework.schema.keycloak.tables.mappers;

import io.vertx.sqlclient.Row;
import java.util.function.Function;

public class RowMappers {

        private RowMappers(){}

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.AdminEventEntity> getAdminEventEntityMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.AdminEventEntity pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.AdminEventEntity();
                        pojo.setId(row.getString("id"));
                        pojo.setAdminEventTime(row.getLong("admin_event_time"));
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setOperationType(row.getString("operation_type"));
                        pojo.setAuthRealmId(row.getString("auth_realm_id"));
                        pojo.setAuthClientId(row.getString("auth_client_id"));
                        pojo.setAuthUserId(row.getString("auth_user_id"));
                        pojo.setIpAddress(row.getString("ip_address"));
                        pojo.setResourcePath(row.getString("resource_path"));
                        pojo.setRepresentation(row.getString("representation"));
                        pojo.setError(row.getString("error"));
                        pojo.setResourceType(row.getString("resource_type"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.AssociatedPolicy> getAssociatedPolicyMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.AssociatedPolicy pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.AssociatedPolicy();
                        pojo.setPolicyId(row.getString("policy_id"));
                        pojo.setAssociatedPolicyId(row.getString("associated_policy_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.AuthenticationExecution> getAuthenticationExecutionMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.AuthenticationExecution pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.AuthenticationExecution();
                        pojo.setId(row.getString("id"));
                        pojo.setAlias(row.getString("alias"));
                        pojo.setAuthenticator(row.getString("authenticator"));
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setFlowId(row.getString("flow_id"));
                        pojo.setRequirement(row.getInteger("requirement"));
                        pojo.setPriority(row.getInteger("priority"));
                        pojo.setAuthenticatorFlow(row.getBoolean("authenticator_flow"));
                        pojo.setAuthFlowId(row.getString("auth_flow_id"));
                        pojo.setAuthConfig(row.getString("auth_config"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.AuthenticationFlow> getAuthenticationFlowMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.AuthenticationFlow pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.AuthenticationFlow();
                        pojo.setId(row.getString("id"));
                        pojo.setAlias(row.getString("alias"));
                        pojo.setDescription(row.getString("description"));
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setProviderId(row.getString("provider_id"));
                        pojo.setTopLevel(row.getBoolean("top_level"));
                        pojo.setBuiltIn(row.getBoolean("built_in"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.AuthenticatorConfig> getAuthenticatorConfigMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.AuthenticatorConfig pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.AuthenticatorConfig();
                        pojo.setId(row.getString("id"));
                        pojo.setAlias(row.getString("alias"));
                        pojo.setRealmId(row.getString("realm_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.AuthenticatorConfigEntry> getAuthenticatorConfigEntryMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.AuthenticatorConfigEntry pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.AuthenticatorConfigEntry();
                        pojo.setAuthenticatorId(row.getString("authenticator_id"));
                        pojo.setValue(row.getString("value"));
                        pojo.setName(row.getString("name"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.BrokerLink> getBrokerLinkMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.BrokerLink pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.BrokerLink();
                        pojo.setIdentityProvider(row.getString("identity_provider"));
                        pojo.setStorageProviderId(row.getString("storage_provider_id"));
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setBrokerUserId(row.getString("broker_user_id"));
                        pojo.setBrokerUsername(row.getString("broker_username"));
                        pojo.setToken(row.getString("token"));
                        pojo.setUserId(row.getString("user_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.Client> getClientMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.Client pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.Client();
                        pojo.setId(row.getString("id"));
                        pojo.setEnabled(row.getBoolean("enabled"));
                        pojo.setFullScopeAllowed(row.getBoolean("full_scope_allowed"));
                        pojo.setClientId(row.getString("client_id"));
                        pojo.setNotBefore(row.getInteger("not_before"));
                        pojo.setPublicClient(row.getBoolean("public_client"));
                        pojo.setSecret(row.getString("secret"));
                        pojo.setBaseUrl(row.getString("base_url"));
                        pojo.setBearerOnly(row.getBoolean("bearer_only"));
                        pojo.setManagementUrl(row.getString("management_url"));
                        pojo.setSurrogateAuthRequired(row.getBoolean("surrogate_auth_required"));
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setProtocol(row.getString("protocol"));
                        pojo.setNodeReregTimeout(row.getInteger("node_rereg_timeout"));
                        pojo.setFrontchannelLogout(row.getBoolean("frontchannel_logout"));
                        pojo.setConsentRequired(row.getBoolean("consent_required"));
                        pojo.setName(row.getString("name"));
                        pojo.setServiceAccountsEnabled(row.getBoolean("service_accounts_enabled"));
                        pojo.setClientAuthenticatorType(row.getString("client_authenticator_type"));
                        pojo.setRootUrl(row.getString("root_url"));
                        pojo.setDescription(row.getString("description"));
                        pojo.setRegistrationToken(row.getString("registration_token"));
                        pojo.setStandardFlowEnabled(row.getBoolean("standard_flow_enabled"));
                        pojo.setImplicitFlowEnabled(row.getBoolean("implicit_flow_enabled"));
                        pojo.setDirectAccessGrantsEnabled(row.getBoolean("direct_access_grants_enabled"));
                        pojo.setAlwaysDisplayInConsole(row.getBoolean("always_display_in_console"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientAttributes> getClientAttributesMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientAttributes pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientAttributes();
                        pojo.setClientId(row.getString("client_id"));
                        pojo.setName(row.getString("name"));
                        pojo.setValue(row.getString("value"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientAuthFlowBindings> getClientAuthFlowBindingsMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientAuthFlowBindings pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientAuthFlowBindings();
                        pojo.setClientId(row.getString("client_id"));
                        pojo.setFlowId(row.getString("flow_id"));
                        pojo.setBindingName(row.getString("binding_name"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientInitialAccess> getClientInitialAccessMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientInitialAccess pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientInitialAccess();
                        pojo.setId(row.getString("id"));
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setTimestamp(row.getInteger("timestamp"));
                        pojo.setExpiration(row.getInteger("expiration"));
                        pojo.setCount(row.getInteger("count"));
                        pojo.setRemainingCount(row.getInteger("remaining_count"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientNodeRegistrations> getClientNodeRegistrationsMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientNodeRegistrations pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientNodeRegistrations();
                        pojo.setClientId(row.getString("client_id"));
                        pojo.setValue(row.getInteger("value"));
                        pojo.setName(row.getString("name"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientScope> getClientScopeMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientScope pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientScope();
                        pojo.setId(row.getString("id"));
                        pojo.setName(row.getString("name"));
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setDescription(row.getString("description"));
                        pojo.setProtocol(row.getString("protocol"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientScopeAttributes> getClientScopeAttributesMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientScopeAttributes pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientScopeAttributes();
                        pojo.setScopeId(row.getString("scope_id"));
                        pojo.setValue(row.getString("value"));
                        pojo.setName(row.getString("name"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientScopeClient> getClientScopeClientMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientScopeClient pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientScopeClient();
                        pojo.setClientId(row.getString("client_id"));
                        pojo.setScopeId(row.getString("scope_id"));
                        pojo.setDefaultScope(row.getBoolean("default_scope"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientScopeRoleMapping> getClientScopeRoleMappingMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientScopeRoleMapping pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientScopeRoleMapping();
                        pojo.setScopeId(row.getString("scope_id"));
                        pojo.setRoleId(row.getString("role_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession> getClientSessionMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSession();
                        pojo.setId(row.getString("id"));
                        pojo.setClientId(row.getString("client_id"));
                        pojo.setRedirectUri(row.getString("redirect_uri"));
                        pojo.setState(row.getString("state"));
                        pojo.setTimestamp(row.getInteger("timestamp"));
                        pojo.setSessionId(row.getString("session_id"));
                        pojo.setAuthMethod(row.getString("auth_method"));
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setAuthUserId(row.getString("auth_user_id"));
                        pojo.setCurrentAction(row.getString("current_action"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSessionAuthStatus> getClientSessionAuthStatusMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSessionAuthStatus pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSessionAuthStatus();
                        pojo.setAuthenticator(row.getString("authenticator"));
                        pojo.setStatus(row.getInteger("status"));
                        pojo.setClientSession(row.getString("client_session"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSessionNote> getClientSessionNoteMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSessionNote pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSessionNote();
                        pojo.setName(row.getString("name"));
                        pojo.setValue(row.getString("value"));
                        pojo.setClientSession(row.getString("client_session"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSessionProtMapper> getClientSessionProtMapperMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSessionProtMapper pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSessionProtMapper();
                        pojo.setProtocolMapperId(row.getString("protocol_mapper_id"));
                        pojo.setClientSession(row.getString("client_session"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSessionRole> getClientSessionRoleMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSessionRole pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSessionRole();
                        pojo.setRoleId(row.getString("role_id"));
                        pojo.setClientSession(row.getString("client_session"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientUserSessionNote> getClientUserSessionNoteMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientUserSessionNote pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientUserSessionNote();
                        pojo.setName(row.getString("name"));
                        pojo.setValue(row.getString("value"));
                        pojo.setClientSession(row.getString("client_session"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.Component> getComponentMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.Component pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.Component();
                        pojo.setId(row.getString("id"));
                        pojo.setName(row.getString("name"));
                        pojo.setParentId(row.getString("parent_id"));
                        pojo.setProviderId(row.getString("provider_id"));
                        pojo.setProviderType(row.getString("provider_type"));
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setSubType(row.getString("sub_type"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ComponentConfig> getComponentConfigMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ComponentConfig pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ComponentConfig();
                        pojo.setId(row.getString("id"));
                        pojo.setComponentId(row.getString("component_id"));
                        pojo.setName(row.getString("name"));
                        pojo.setValue(row.getString("value"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.CompositeRole> getCompositeRoleMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.CompositeRole pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.CompositeRole();
                        pojo.setComposite(row.getString("composite"));
                        pojo.setChildRole(row.getString("child_role"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.Credential> getCredentialMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.Credential pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.Credential();
                        pojo.setId(row.getString("id"));
                        io.vertx.core.buffer.Buffer saltBuffer = row.getBuffer("salt");
                        pojo.setSalt(saltBuffer == null?null:saltBuffer.getBytes());
                        pojo.setType(row.getString("type"));
                        pojo.setUserId(row.getString("user_id"));
                        pojo.setCreatedDate(row.getLong("created_date"));
                        pojo.setUserLabel(row.getString("user_label"));
                        pojo.setSecretData(row.getString("secret_data"));
                        pojo.setCredentialData(row.getString("credential_data"));
                        pojo.setPriority(row.getInteger("priority"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.Databasechangeloglock> getDatabasechangeloglockMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.Databasechangeloglock pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.Databasechangeloglock();
                        pojo.setId(row.getInteger("id"));
                        pojo.setLocked(row.getBoolean("locked"));
                        pojo.setLockgranted(row.getLocalDateTime("lockgranted"));
                        pojo.setLockedby(row.getString("lockedby"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.DefaultClientScope> getDefaultClientScopeMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.DefaultClientScope pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.DefaultClientScope();
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setScopeId(row.getString("scope_id"));
                        pojo.setDefaultScope(row.getBoolean("default_scope"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.EventEntity> getEventEntityMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.EventEntity pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.EventEntity();
                        pojo.setId(row.getString("id"));
                        pojo.setClientId(row.getString("client_id"));
                        pojo.setDetailsJson(row.getString("details_json"));
                        pojo.setError(row.getString("error"));
                        pojo.setIpAddress(row.getString("ip_address"));
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setSessionId(row.getString("session_id"));
                        pojo.setEventTime(row.getLong("event_time"));
                        pojo.setType(row.getString("type"));
                        pojo.setUserId(row.getString("user_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserAttribute> getFedUserAttributeMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserAttribute pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserAttribute();
                        pojo.setId(row.getString("id"));
                        pojo.setName(row.getString("name"));
                        pojo.setUserId(row.getString("user_id"));
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setStorageProviderId(row.getString("storage_provider_id"));
                        pojo.setValue(row.getString("value"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserConsent> getFedUserConsentMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserConsent pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserConsent();
                        pojo.setId(row.getString("id"));
                        pojo.setClientId(row.getString("client_id"));
                        pojo.setUserId(row.getString("user_id"));
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setStorageProviderId(row.getString("storage_provider_id"));
                        pojo.setCreatedDate(row.getLong("created_date"));
                        pojo.setLastUpdatedDate(row.getLong("last_updated_date"));
                        pojo.setClientStorageProvider(row.getString("client_storage_provider"));
                        pojo.setExternalClientId(row.getString("external_client_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserConsentClScope> getFedUserConsentClScopeMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserConsentClScope pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserConsentClScope();
                        pojo.setUserConsentId(row.getString("user_consent_id"));
                        pojo.setScopeId(row.getString("scope_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserCredential> getFedUserCredentialMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserCredential pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserCredential();
                        pojo.setId(row.getString("id"));
                        io.vertx.core.buffer.Buffer saltBuffer = row.getBuffer("salt");
                        pojo.setSalt(saltBuffer == null?null:saltBuffer.getBytes());
                        pojo.setType(row.getString("type"));
                        pojo.setCreatedDate(row.getLong("created_date"));
                        pojo.setUserId(row.getString("user_id"));
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setStorageProviderId(row.getString("storage_provider_id"));
                        pojo.setUserLabel(row.getString("user_label"));
                        pojo.setSecretData(row.getString("secret_data"));
                        pojo.setCredentialData(row.getString("credential_data"));
                        pojo.setPriority(row.getInteger("priority"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserGroupMembership> getFedUserGroupMembershipMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserGroupMembership pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserGroupMembership();
                        pojo.setGroupId(row.getString("group_id"));
                        pojo.setUserId(row.getString("user_id"));
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setStorageProviderId(row.getString("storage_provider_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserRequiredAction> getFedUserRequiredActionMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserRequiredAction pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserRequiredAction();
                        pojo.setRequiredAction(row.getString("required_action"));
                        pojo.setUserId(row.getString("user_id"));
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setStorageProviderId(row.getString("storage_provider_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserRoleMapping> getFedUserRoleMappingMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserRoleMapping pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.FedUserRoleMapping();
                        pojo.setRoleId(row.getString("role_id"));
                        pojo.setUserId(row.getString("user_id"));
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setStorageProviderId(row.getString("storage_provider_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.FederatedIdentity> getFederatedIdentityMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.FederatedIdentity pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.FederatedIdentity();
                        pojo.setIdentityProvider(row.getString("identity_provider"));
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setFederatedUserId(row.getString("federated_user_id"));
                        pojo.setFederatedUsername(row.getString("federated_username"));
                        pojo.setToken(row.getString("token"));
                        pojo.setUserId(row.getString("user_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.FederatedUser> getFederatedUserMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.FederatedUser pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.FederatedUser();
                        pojo.setId(row.getString("id"));
                        pojo.setStorageProviderId(row.getString("storage_provider_id"));
                        pojo.setRealmId(row.getString("realm_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.GroupAttribute> getGroupAttributeMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.GroupAttribute pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.GroupAttribute();
                        pojo.setId(row.getString("id"));
                        pojo.setName(row.getString("name"));
                        pojo.setValue(row.getString("value"));
                        pojo.setGroupId(row.getString("group_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.GroupRoleMapping> getGroupRoleMappingMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.GroupRoleMapping pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.GroupRoleMapping();
                        pojo.setRoleId(row.getString("role_id"));
                        pojo.setGroupId(row.getString("group_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.IdentityProvider> getIdentityProviderMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.IdentityProvider pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.IdentityProvider();
                        pojo.setInternalId(row.getString("internal_id"));
                        pojo.setEnabled(row.getBoolean("enabled"));
                        pojo.setProviderAlias(row.getString("provider_alias"));
                        pojo.setProviderId(row.getString("provider_id"));
                        pojo.setStoreToken(row.getBoolean("store_token"));
                        pojo.setAuthenticateByDefault(row.getBoolean("authenticate_by_default"));
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setAddTokenRole(row.getBoolean("add_token_role"));
                        pojo.setTrustEmail(row.getBoolean("trust_email"));
                        pojo.setFirstBrokerLoginFlowId(row.getString("first_broker_login_flow_id"));
                        pojo.setPostBrokerLoginFlowId(row.getString("post_broker_login_flow_id"));
                        pojo.setProviderDisplayName(row.getString("provider_display_name"));
                        pojo.setLinkOnly(row.getBoolean("link_only"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.IdentityProviderConfig> getIdentityProviderConfigMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.IdentityProviderConfig pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.IdentityProviderConfig();
                        pojo.setIdentityProviderId(row.getString("identity_provider_id"));
                        pojo.setValue(row.getString("value"));
                        pojo.setName(row.getString("name"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.IdentityProviderMapper> getIdentityProviderMapperMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.IdentityProviderMapper pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.IdentityProviderMapper();
                        pojo.setId(row.getString("id"));
                        pojo.setName(row.getString("name"));
                        pojo.setIdpAlias(row.getString("idp_alias"));
                        pojo.setIdpMapperName(row.getString("idp_mapper_name"));
                        pojo.setRealmId(row.getString("realm_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.IdpMapperConfig> getIdpMapperConfigMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.IdpMapperConfig pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.IdpMapperConfig();
                        pojo.setIdpMapperId(row.getString("idp_mapper_id"));
                        pojo.setValue(row.getString("value"));
                        pojo.setName(row.getString("name"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.KeycloakGroup> getKeycloakGroupMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.KeycloakGroup pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.KeycloakGroup();
                        pojo.setId(row.getString("id"));
                        pojo.setName(row.getString("name"));
                        pojo.setParentGroup(row.getString("parent_group"));
                        pojo.setRealmId(row.getString("realm_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.KeycloakRole> getKeycloakRoleMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.KeycloakRole pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.KeycloakRole();
                        pojo.setId(row.getString("id"));
                        pojo.setClientRealmConstraint(row.getString("client_realm_constraint"));
                        pojo.setClientRole(row.getBoolean("client_role"));
                        pojo.setDescription(row.getString("description"));
                        pojo.setName(row.getString("name"));
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setClient(row.getString("client"));
                        pojo.setRealm(row.getString("realm"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.MigrationModel> getMigrationModelMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.MigrationModel pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.MigrationModel();
                        pojo.setId(row.getString("id"));
                        pojo.setVersion(row.getString("version"));
                        pojo.setUpdateTime(row.getLong("update_time"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.OfflineClientSession> getOfflineClientSessionMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.OfflineClientSession pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.OfflineClientSession();
                        pojo.setUserSessionId(row.getString("user_session_id"));
                        pojo.setClientId(row.getString("client_id"));
                        pojo.setOfflineFlag(row.getString("offline_flag"));
                        pojo.setTimestamp(row.getInteger("timestamp"));
                        pojo.setData(row.getString("data"));
                        pojo.setClientStorageProvider(row.getString("client_storage_provider"));
                        pojo.setExternalClientId(row.getString("external_client_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.OfflineUserSession> getOfflineUserSessionMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.OfflineUserSession pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.OfflineUserSession();
                        pojo.setUserSessionId(row.getString("user_session_id"));
                        pojo.setUserId(row.getString("user_id"));
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setCreatedOn(row.getInteger("created_on"));
                        pojo.setOfflineFlag(row.getString("offline_flag"));
                        pojo.setData(row.getString("data"));
                        pojo.setLastSessionRefresh(row.getInteger("last_session_refresh"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.PolicyConfig> getPolicyConfigMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.PolicyConfig pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.PolicyConfig();
                        pojo.setPolicyId(row.getString("policy_id"));
                        pojo.setName(row.getString("name"));
                        pojo.setValue(row.getString("value"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ProtocolMapper> getProtocolMapperMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ProtocolMapper pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ProtocolMapper();
                        pojo.setId(row.getString("id"));
                        pojo.setName(row.getString("name"));
                        pojo.setProtocol(row.getString("protocol"));
                        pojo.setProtocolMapperName(row.getString("protocol_mapper_name"));
                        pojo.setClientId(row.getString("client_id"));
                        pojo.setClientScopeId(row.getString("client_scope_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ProtocolMapperConfig> getProtocolMapperConfigMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ProtocolMapperConfig pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ProtocolMapperConfig();
                        pojo.setProtocolMapperId(row.getString("protocol_mapper_id"));
                        pojo.setValue(row.getString("value"));
                        pojo.setName(row.getString("name"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.Realm> getRealmMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.Realm pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.Realm();
                        pojo.setId(row.getString("id"));
                        pojo.setAccessCodeLifespan(row.getInteger("access_code_lifespan"));
                        pojo.setUserActionLifespan(row.getInteger("user_action_lifespan"));
                        pojo.setAccessTokenLifespan(row.getInteger("access_token_lifespan"));
                        pojo.setAccountTheme(row.getString("account_theme"));
                        pojo.setAdminTheme(row.getString("admin_theme"));
                        pojo.setEmailTheme(row.getString("email_theme"));
                        pojo.setEnabled(row.getBoolean("enabled"));
                        pojo.setEventsEnabled(row.getBoolean("events_enabled"));
                        pojo.setEventsExpiration(row.getLong("events_expiration"));
                        pojo.setLoginTheme(row.getString("login_theme"));
                        pojo.setName(row.getString("name"));
                        pojo.setNotBefore(row.getInteger("not_before"));
                        pojo.setPasswordPolicy(row.getString("password_policy"));
                        pojo.setRegistrationAllowed(row.getBoolean("registration_allowed"));
                        pojo.setRememberMe(row.getBoolean("remember_me"));
                        pojo.setResetPasswordAllowed(row.getBoolean("reset_password_allowed"));
                        pojo.setSocial(row.getBoolean("social"));
                        pojo.setSslRequired(row.getString("ssl_required"));
                        pojo.setSsoIdleTimeout(row.getInteger("sso_idle_timeout"));
                        pojo.setSsoMaxLifespan(row.getInteger("sso_max_lifespan"));
                        pojo.setUpdateProfileOnSocLogin(row.getBoolean("update_profile_on_soc_login"));
                        pojo.setVerifyEmail(row.getBoolean("verify_email"));
                        pojo.setMasterAdminClient(row.getString("master_admin_client"));
                        pojo.setLoginLifespan(row.getInteger("login_lifespan"));
                        pojo.setInternationalizationEnabled(row.getBoolean("internationalization_enabled"));
                        pojo.setDefaultLocale(row.getString("default_locale"));
                        pojo.setRegEmailAsUsername(row.getBoolean("reg_email_as_username"));
                        pojo.setAdminEventsEnabled(row.getBoolean("admin_events_enabled"));
                        pojo.setAdminEventsDetailsEnabled(row.getBoolean("admin_events_details_enabled"));
                        pojo.setEditUsernameAllowed(row.getBoolean("edit_username_allowed"));
                        pojo.setOtpPolicyCounter(row.getInteger("otp_policy_counter"));
                        pojo.setOtpPolicyWindow(row.getInteger("otp_policy_window"));
                        pojo.setOtpPolicyPeriod(row.getInteger("otp_policy_period"));
                        pojo.setOtpPolicyDigits(row.getInteger("otp_policy_digits"));
                        pojo.setOtpPolicyAlg(row.getString("otp_policy_alg"));
                        pojo.setOtpPolicyType(row.getString("otp_policy_type"));
                        pojo.setBrowserFlow(row.getString("browser_flow"));
                        pojo.setRegistrationFlow(row.getString("registration_flow"));
                        pojo.setDirectGrantFlow(row.getString("direct_grant_flow"));
                        pojo.setResetCredentialsFlow(row.getString("reset_credentials_flow"));
                        pojo.setClientAuthFlow(row.getString("client_auth_flow"));
                        pojo.setOfflineSessionIdleTimeout(row.getInteger("offline_session_idle_timeout"));
                        pojo.setRevokeRefreshToken(row.getBoolean("revoke_refresh_token"));
                        pojo.setAccessTokenLifeImplicit(row.getInteger("access_token_life_implicit"));
                        pojo.setLoginWithEmailAllowed(row.getBoolean("login_with_email_allowed"));
                        pojo.setDuplicateEmailsAllowed(row.getBoolean("duplicate_emails_allowed"));
                        pojo.setDockerAuthFlow(row.getString("docker_auth_flow"));
                        pojo.setRefreshTokenMaxReuse(row.getInteger("refresh_token_max_reuse"));
                        pojo.setAllowUserManagedAccess(row.getBoolean("allow_user_managed_access"));
                        pojo.setSsoMaxLifespanRememberMe(row.getInteger("sso_max_lifespan_remember_me"));
                        pojo.setSsoIdleTimeoutRememberMe(row.getInteger("sso_idle_timeout_remember_me"));
                        pojo.setDefaultRole(row.getString("default_role"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmAttribute> getRealmAttributeMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmAttribute pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmAttribute();
                        pojo.setName(row.getString("name"));
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setValue(row.getString("value"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmDefaultGroups> getRealmDefaultGroupsMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmDefaultGroups pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmDefaultGroups();
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setGroupId(row.getString("group_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmEnabledEventTypes> getRealmEnabledEventTypesMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmEnabledEventTypes pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmEnabledEventTypes();
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setValue(row.getString("value"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmEventsListeners> getRealmEventsListenersMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmEventsListeners pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmEventsListeners();
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setValue(row.getString("value"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmLocalizations> getRealmLocalizationsMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmLocalizations pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmLocalizations();
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setLocale(row.getString("locale"));
                        pojo.setTexts(row.getString("texts"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmRequiredCredential> getRealmRequiredCredentialMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmRequiredCredential pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmRequiredCredential();
                        pojo.setType(row.getString("type"));
                        pojo.setFormLabel(row.getString("form_label"));
                        pojo.setInput(row.getBoolean("input"));
                        pojo.setSecret(row.getBoolean("secret"));
                        pojo.setRealmId(row.getString("realm_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmSmtpConfig> getRealmSmtpConfigMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmSmtpConfig pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmSmtpConfig();
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setValue(row.getString("value"));
                        pojo.setName(row.getString("name"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmSupportedLocales> getRealmSupportedLocalesMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmSupportedLocales pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.RealmSupportedLocales();
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setValue(row.getString("value"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.RedirectUris> getRedirectUrisMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.RedirectUris pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.RedirectUris();
                        pojo.setClientId(row.getString("client_id"));
                        pojo.setValue(row.getString("value"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.RequiredActionConfig> getRequiredActionConfigMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.RequiredActionConfig pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.RequiredActionConfig();
                        pojo.setRequiredActionId(row.getString("required_action_id"));
                        pojo.setValue(row.getString("value"));
                        pojo.setName(row.getString("name"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.RequiredActionProvider> getRequiredActionProviderMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.RequiredActionProvider pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.RequiredActionProvider();
                        pojo.setId(row.getString("id"));
                        pojo.setAlias(row.getString("alias"));
                        pojo.setName(row.getString("name"));
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setEnabled(row.getBoolean("enabled"));
                        pojo.setDefaultAction(row.getBoolean("default_action"));
                        pojo.setProviderId(row.getString("provider_id"));
                        pojo.setPriority(row.getInteger("priority"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ResourceAttribute> getResourceAttributeMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ResourceAttribute pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ResourceAttribute();
                        pojo.setId(row.getString("id"));
                        pojo.setName(row.getString("name"));
                        pojo.setValue(row.getString("value"));
                        pojo.setResourceId(row.getString("resource_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ResourcePolicy> getResourcePolicyMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ResourcePolicy pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ResourcePolicy();
                        pojo.setResourceId(row.getString("resource_id"));
                        pojo.setPolicyId(row.getString("policy_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ResourceScope> getResourceScopeMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ResourceScope pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ResourceScope();
                        pojo.setResourceId(row.getString("resource_id"));
                        pojo.setScopeId(row.getString("scope_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ResourceServer> getResourceServerMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ResourceServer pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ResourceServer();
                        pojo.setId(row.getString("id"));
                        pojo.setAllowRsRemoteMgmt(row.getBoolean("allow_rs_remote_mgmt"));
                        pojo.setPolicyEnforceMode(row.getString("policy_enforce_mode"));
                        pojo.setDecisionStrategy(row.getShort("decision_strategy"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ResourceServerPermTicket> getResourceServerPermTicketMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ResourceServerPermTicket pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ResourceServerPermTicket();
                        pojo.setId(row.getString("id"));
                        pojo.setOwner(row.getString("owner"));
                        pojo.setRequester(row.getString("requester"));
                        pojo.setCreatedTimestamp(row.getLong("created_timestamp"));
                        pojo.setGrantedTimestamp(row.getLong("granted_timestamp"));
                        pojo.setResourceId(row.getString("resource_id"));
                        pojo.setScopeId(row.getString("scope_id"));
                        pojo.setResourceServerId(row.getString("resource_server_id"));
                        pojo.setPolicyId(row.getString("policy_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ResourceServerPolicy> getResourceServerPolicyMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ResourceServerPolicy pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ResourceServerPolicy();
                        pojo.setId(row.getString("id"));
                        pojo.setName(row.getString("name"));
                        pojo.setDescription(row.getString("description"));
                        pojo.setType(row.getString("type"));
                        pojo.setDecisionStrategy(row.getString("decision_strategy"));
                        pojo.setLogic(row.getString("logic"));
                        pojo.setResourceServerId(row.getString("resource_server_id"));
                        pojo.setOwner(row.getString("owner"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ResourceServerResource> getResourceServerResourceMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ResourceServerResource pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ResourceServerResource();
                        pojo.setId(row.getString("id"));
                        pojo.setName(row.getString("name"));
                        pojo.setType(row.getString("type"));
                        pojo.setIconUri(row.getString("icon_uri"));
                        pojo.setOwner(row.getString("owner"));
                        pojo.setResourceServerId(row.getString("resource_server_id"));
                        pojo.setOwnerManagedAccess(row.getBoolean("owner_managed_access"));
                        pojo.setDisplayName(row.getString("display_name"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ResourceServerScope> getResourceServerScopeMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ResourceServerScope pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ResourceServerScope();
                        pojo.setId(row.getString("id"));
                        pojo.setName(row.getString("name"));
                        pojo.setIconUri(row.getString("icon_uri"));
                        pojo.setResourceServerId(row.getString("resource_server_id"));
                        pojo.setDisplayName(row.getString("display_name"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ResourceUris> getResourceUrisMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ResourceUris pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ResourceUris();
                        pojo.setResourceId(row.getString("resource_id"));
                        pojo.setValue(row.getString("value"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.RoleAttribute> getRoleAttributeMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.RoleAttribute pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.RoleAttribute();
                        pojo.setId(row.getString("id"));
                        pojo.setRoleId(row.getString("role_id"));
                        pojo.setName(row.getString("name"));
                        pojo.setValue(row.getString("value"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ScopeMapping> getScopeMappingMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ScopeMapping pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ScopeMapping();
                        pojo.setClientId(row.getString("client_id"));
                        pojo.setRoleId(row.getString("role_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.ScopePolicy> getScopePolicyMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.ScopePolicy pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.ScopePolicy();
                        pojo.setScopeId(row.getString("scope_id"));
                        pojo.setPolicyId(row.getString("policy_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserAttribute> getUserAttributeMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserAttribute pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserAttribute();
                        pojo.setName(row.getString("name"));
                        pojo.setValue(row.getString("value"));
                        pojo.setUserId(row.getString("user_id"));
                        pojo.setId(row.getString("id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserConsent> getUserConsentMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserConsent pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserConsent();
                        pojo.setId(row.getString("id"));
                        pojo.setClientId(row.getString("client_id"));
                        pojo.setUserId(row.getString("user_id"));
                        pojo.setCreatedDate(row.getLong("created_date"));
                        pojo.setLastUpdatedDate(row.getLong("last_updated_date"));
                        pojo.setClientStorageProvider(row.getString("client_storage_provider"));
                        pojo.setExternalClientId(row.getString("external_client_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserConsentClientScope> getUserConsentClientScopeMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserConsentClientScope pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserConsentClientScope();
                        pojo.setUserConsentId(row.getString("user_consent_id"));
                        pojo.setScopeId(row.getString("scope_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity> getUserEntityMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity();
                        pojo.setId(row.getString("id"));
                        pojo.setEmail(row.getString("email"));
                        pojo.setEmailConstraint(row.getString("email_constraint"));
                        pojo.setEmailVerified(row.getBoolean("email_verified"));
                        pojo.setEnabled(row.getBoolean("enabled"));
                        pojo.setFederationLink(row.getString("federation_link"));
                        pojo.setFirstName(row.getString("first_name"));
                        pojo.setLastName(row.getString("last_name"));
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setUsername(row.getString("username"));
                        pojo.setCreatedTimestamp(row.getLong("created_timestamp"));
                        pojo.setServiceAccountClientLink(row.getString("service_account_client_link"));
                        pojo.setNotBefore(row.getInteger("not_before"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserFederationConfig> getUserFederationConfigMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserFederationConfig pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserFederationConfig();
                        pojo.setUserFederationProviderId(row.getString("user_federation_provider_id"));
                        pojo.setValue(row.getString("value"));
                        pojo.setName(row.getString("name"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserFederationMapper> getUserFederationMapperMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserFederationMapper pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserFederationMapper();
                        pojo.setId(row.getString("id"));
                        pojo.setName(row.getString("name"));
                        pojo.setFederationProviderId(row.getString("federation_provider_id"));
                        pojo.setFederationMapperType(row.getString("federation_mapper_type"));
                        pojo.setRealmId(row.getString("realm_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserFederationMapperConfig> getUserFederationMapperConfigMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserFederationMapperConfig pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserFederationMapperConfig();
                        pojo.setUserFederationMapperId(row.getString("user_federation_mapper_id"));
                        pojo.setValue(row.getString("value"));
                        pojo.setName(row.getString("name"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserFederationProvider> getUserFederationProviderMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserFederationProvider pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserFederationProvider();
                        pojo.setId(row.getString("id"));
                        pojo.setChangedSyncPeriod(row.getInteger("changed_sync_period"));
                        pojo.setDisplayName(row.getString("display_name"));
                        pojo.setFullSyncPeriod(row.getInteger("full_sync_period"));
                        pojo.setLastSync(row.getInteger("last_sync"));
                        pojo.setPriority(row.getInteger("priority"));
                        pojo.setProviderName(row.getString("provider_name"));
                        pojo.setRealmId(row.getString("realm_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserGroupMembership> getUserGroupMembershipMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserGroupMembership pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserGroupMembership();
                        pojo.setGroupId(row.getString("group_id"));
                        pojo.setUserId(row.getString("user_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserRequiredAction> getUserRequiredActionMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserRequiredAction pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserRequiredAction();
                        pojo.setUserId(row.getString("user_id"));
                        pojo.setRequiredAction(row.getString("required_action"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserRoleMapping> getUserRoleMappingMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserRoleMapping pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserRoleMapping();
                        pojo.setRoleId(row.getString("role_id"));
                        pojo.setUserId(row.getString("user_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserSession> getUserSessionMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserSession pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserSession();
                        pojo.setId(row.getString("id"));
                        pojo.setAuthMethod(row.getString("auth_method"));
                        pojo.setIpAddress(row.getString("ip_address"));
                        pojo.setLastSessionRefresh(row.getInteger("last_session_refresh"));
                        pojo.setLoginUsername(row.getString("login_username"));
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setRememberMe(row.getBoolean("remember_me"));
                        pojo.setStarted(row.getInteger("started"));
                        pojo.setUserId(row.getString("user_id"));
                        pojo.setUserSessionState(row.getInteger("user_session_state"));
                        pojo.setBrokerSessionId(row.getString("broker_session_id"));
                        pojo.setBrokerUserId(row.getString("broker_user_id"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserSessionNote> getUserSessionNoteMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserSessionNote pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserSessionNote();
                        pojo.setUserSession(row.getString("user_session"));
                        pojo.setName(row.getString("name"));
                        pojo.setValue(row.getString("value"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.UsernameLoginFailure> getUsernameLoginFailureMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.UsernameLoginFailure pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.UsernameLoginFailure();
                        pojo.setRealmId(row.getString("realm_id"));
                        pojo.setUsername(row.getString("username"));
                        pojo.setFailedLoginNotBefore(row.getInteger("failed_login_not_before"));
                        pojo.setLastFailure(row.getLong("last_failure"));
                        pojo.setLastIpFailure(row.getString("last_ip_failure"));
                        pojo.setNumFailures(row.getInteger("num_failures"));
                        return pojo;
                };
        }

        public static Function<Row,com.hiddenswitch.framework.schema.keycloak.tables.pojos.WebOrigins> getWebOriginsMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.keycloak.tables.pojos.WebOrigins pojo = new com.hiddenswitch.framework.schema.keycloak.tables.pojos.WebOrigins();
                        pojo.setClientId(row.getString("client_id"));
                        pojo.setValue(row.getString("value"));
                        return pojo;
                };
        }

}
