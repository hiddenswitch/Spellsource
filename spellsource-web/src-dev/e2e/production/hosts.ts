import { siteURL } from "../../../playwright.production.config";

const site = new URL(siteURL);

/**
 * The public names a player's browser and the shipped client resolve. The
 * client derives its subscriptions host from the graphql url by string
 * replacement, which turns graphql.<domain> into subscriptions.<domain>, so
 * that name must exist and serve the gateway too.
 */
export const domain = site.hostname.replace(/^www\./, "");
export const siteHosts = [domain, `www.${domain}`, `login.${domain}`, `graphql.${domain}`, `subscriptions.${domain}`];
// A local candidate website can still exercise the deployed gateway.
const backendDomain = process.env.SPELLSOURCE_BACKEND_DOMAIN ?? domain;
export const graphqlHosts = [`graphql.${backendDomain}`, `subscriptions.${backendDomain}`];
