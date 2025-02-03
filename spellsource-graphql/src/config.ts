import { config as dotenv } from "dotenv";

dotenv({ path: `.env` });
dotenv({ path: `.env.${process.env.NODE_ENV}` });
dotenv({ path: `.env.local` });
dotenv({ path: `.env.${process.env.NODE_ENV}.local` });

export const graphqlPort = parseInt(process.env.GRAPHQL_PORT || "5678");
export const graphqlHost = process.env.GRAPHQL_HOST || "0.0.0.0";

export const pgHost = process.env.PGHOST || process.env.PG_HOST || "localhost";
export const pgPort = parseInt(process.env.PGPORT || process.env.PG_PORT || "5432");
export const pgUser = process.env.PGUSER || process.env.PG_USER || "admin";
export const pgPassword = process.env.PGPASSWORD || process.env.PG_PASSWORD || "password";
export const pgDatabase = process.env.PGDATABASE || process.env.PG_DATABASE || "spellsource";

export const keycloakUrl = process.env.KEYCLOAK_URL || `http://localhost:${parseInt(process.env.KEYCLOAK_PORT || "8080")}`;

export const clientId = process.env.KEYCLOAK_CLIENT_ID || "spellsource";
export const clientSecret = process.env.KEYCLOAK_CLIENT_SECRET || "clientsecret";
export const realm = process.env.KEYCLOAK_REALM || "hiddenswitch";
export const issuer = process.env.KEYCLOAK_ISSUER || `${keycloakUrl}/realms/${realm}`;

export const pgJwtSecret = process.env.PG_JWT_SECRET || "Ice Block";
