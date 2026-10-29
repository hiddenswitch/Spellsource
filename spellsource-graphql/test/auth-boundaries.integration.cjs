const assert = require('node:assert/strict');
const { test } = require('node:test');
const endpoint = process.env.AUTH_TEST_GRAPHQL_URL;
if (!endpoint) throw new Error('Set AUTH_TEST_GRAPHQL_URL to the isolated GraphQL server');

test('anonymous requests cannot read private decks', async () => {
  const response = await fetch(endpoint, {
    method: 'POST', headers: { 'content-type': 'application/json' },
    body: JSON.stringify({ query: '{allDecks(condition:{isPremade:false}){totalCount}}' }),
  });
  assert.equal(response.status, 200);
  const result = await response.json();
  assert.equal(result.errors, undefined);
  assert.equal(result.data.allDecks.totalCount, 0);
});

test('the former default credential is rejected by the running gateway', async () => {
  const response = await fetch(endpoint, {
    method: 'POST', headers: { 'content-type': 'application/json', Authorization: 'Bearer Ice Block' },
    body: JSON.stringify({ query: '{__typename}' }),
  });
  assert.equal(response.status, 401);
});
