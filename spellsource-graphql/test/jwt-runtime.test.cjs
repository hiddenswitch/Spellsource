const assert = require('node:assert/strict');
const { test } = require('node:test');
const jwt = require('jsonwebtoken');
const { generateKeyPairSync } = require('node:crypto');

test('JWT verification accepts valid signatures and rejects tampering on the supported Node runtime', () => {
  const { privateKey, publicKey } = generateKeyPairSync('rsa', { modulusLength: 2048 });
  const token = jwt.sign({ sub: 'runtime-test' }, privateKey, { algorithm: 'RS256', expiresIn: '1m' });
  assert.equal(jwt.verify(token, publicKey, { algorithms: ['RS256'] }).sub, 'runtime-test');
  const parts = token.split('.');
  parts[1] = Buffer.from(JSON.stringify({ sub: 'different-user' })).toString('base64url');
  assert.throws(() => jwt.verify(parts.join('.'), publicKey, { algorithms: ['RS256'] }), /invalid signature/);
  const hmac = jwt.sign({ sub: 'runtime-test' }, 'test-only-signing-key', { algorithm: 'HS256' });
  assert.equal(jwt.verify(hmac, 'test-only-signing-key', { algorithms: ['HS256'] }).sub, 'runtime-test');
  assert.throws(() => jwt.verify(hmac, 'different-signing-key', { algorithms: ['HS256'] }), /invalid signature/);
});
