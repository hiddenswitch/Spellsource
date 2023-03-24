export const pgPort = parseInt(process.env.PG_PORT || "5432")
export const keycloakPort = parseInt(process.env.KEYCLOAK_PORT || "8080")

export const clientId = "spellsource";
export const clientSecret = "clientsecret"
export const issuer = `http://localhost:${keycloakPort}/realms/hiddenswitch`;
export const baseUrl = "http://localhost:3000"
