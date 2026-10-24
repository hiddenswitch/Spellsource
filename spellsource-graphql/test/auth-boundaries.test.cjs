const assert = require('node:assert/strict');
const { test } = require('node:test');
delete process.env.PG_JWT_SECRET;
const { authenticate, databaseSettings } = require('../src/auth');

test('anonymous database requests use the restricted website role', () => {
  assert.deepEqual(databaseSettings({}), { role: 'website', 'user.id': '' });
});

test('authenticated database requests preserve the verified subject', () => {
  assert.deepEqual(databaseSettings({ auth: { sub: 'test-user' } }), { role: 'website', 'user.id': 'test-user' });
});

test('the former fallback admin token is rejected', async () => {
  const request = { header: () => 'Bearer Ice Block' };
  await new Promise((resolve, reject) => {
    const response = { status(code) { assert.equal(code, 401); return this; }, json() { resolve(); } };
    authenticate(request, response, () => reject(new Error('Fallback token bypassed verification')));
  });
  assert.equal(request.admin, undefined);
});
