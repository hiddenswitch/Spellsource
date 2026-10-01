const assert = require('node:assert/strict');
const { test } = require('node:test');
const { createServer } = require('node:http');
const { once } = require('node:events');
const { setTimeout: delay } = require('node:timers/promises');
const { parse } = require('graphql');
const { WebSocketServer } = require('ws');

test('upstream subscription lifecycle', async (t) => {
  const server = createServer();
  server.listen(0, '127.0.0.1');
  await once(server, 'listening');
  t.after(() => server.close());
  process.env.SPELLSOURCE_HOST = '127.0.0.1';
  process.env.SPELLSOURCE_PORT = String(server.address().port);
  const { wsExecutor } = require('../src/schema/spellsource');
  const request = { document: parse('subscription { gameMessages }'), context: { token: 'test-token' } };

  await t.test('cancelling a pending connection tolerates the upstream disappearing', async () => {
    const upgraded = once(server, 'upgrade');
    const iterator = wsExecutor(request);
    const [, socket] = await upgraded;
    await iterator.return();
    // The client has cancelled while graphql-ws is still awaiting its handshake.
    // Reject that handshake as a backend restart would, then let cleanup settle.
    socket.destroy();
    await delay(100);
    assert.deepEqual(await iterator.next(), { value: undefined, done: true });
  });

  await t.test('forwards authentication and game frames, then closes on cancellation', async () => {
    const wss = new WebSocketServer({ noServer: true });
    t.after(() => wss.close());
    server.once('upgrade', (req, socket, head) => wss.handleUpgrade(req, socket, head, ws => wss.emit('connection', ws)));
    const connected = once(wss, 'connection');
    const iterator = wsExecutor(request);
    const [ws] = await connected;
    const closed = once(ws, 'close');
    const [init] = await once(ws, 'message');
    assert.deepEqual(JSON.parse(init), { type: 'connection_init', payload: { Authorization: 'Bearer test-token' } });
    ws.send(JSON.stringify({ type: 'connection_ack' }));
    const [subscribe] = await once(ws, 'message');
    const operation = JSON.parse(subscribe);
    assert.equal(operation.type, 'subscribe');
    ws.send(JSON.stringify({ id: operation.id, type: 'next', payload: { data: { gameMessages: 'frame' } } }));
    assert.deepEqual((await iterator.next()).value, { data: { gameMessages: 'frame' } });
    await iterator.return();
    await closed;
  });
});
