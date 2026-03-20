import { Client } from "../../game";
import type { TokenStore } from "../../game/client/token-store";
import type { LoginOrCreateReply } from "spellsource-protos/dist/experimental/client/hiddenswitch";

// Mock all proto client impls
jest.mock("spellsource-protos/dist/experimental/client/hiddenswitch", () => {
  const actual = jest.requireActual("spellsource-protos/dist/experimental/client/hiddenswitch");
  return {
    ...actual,
    UnauthenticatedClientImpl: jest.fn().mockImplementation(() => ({
      login: jest.fn(),
      createAccount: jest.fn(),
      verifyToken: jest.fn(),
      getConfiguration: jest.fn(),
    })),
    UnauthenticatedCardsClientImpl: jest.fn().mockImplementation(() => ({
      getCards: jest.fn(),
    })),
    AuthenticatedCardsClientImpl: jest.fn().mockImplementation(() => ({
      getCardsByUser: jest.fn(),
    })),
  };
});

jest.mock("spellsource-protos/dist/experimental/client/spellsource", () => {
  const actual = jest.requireActual("spellsource-protos/dist/experimental/client/spellsource");
  return {
    ...actual,
    MatchmakingClientImpl: jest.fn().mockImplementation(() => ({
      matchmakingGet: jest.fn(),
      matchmakingDelete: jest.fn(),
    })),
    HiddenSwitchSpellsourceAPIServiceClientImpl: jest.fn().mockImplementation(() => ({
      decksGetAll: jest.fn(),
      decksGet: jest.fn(),
      decksPut: jest.fn(),
      decksUpdate: jest.fn(),
      decksDelete: jest.fn(),
      duplicateDeck: jest.fn(),
      draftsGet: jest.fn(),
      draftsPost: jest.fn(),
      draftsChooseHero: jest.fn(),
      draftsChooseCard: jest.fn(),
      subscribeMatch: jest.fn(),
    })),
  };
});

import { UnauthenticatedClientImpl } from "spellsource-protos/dist/experimental/client/hiddenswitch";
const MockUnauthenticated = UnauthenticatedClientImpl as jest.MockedClass<typeof UnauthenticatedClientImpl>;

function mockStorage(): Storage {
  const store = new Map<string, string>();
  return {
    getItem: (key: string) => store.get(key) ?? null,
    setItem: (key: string, value: string) => {
      store.set(key, value);
    },
    removeItem: (key: string) => {
      store.delete(key);
    },
    clear: () => store.clear(),
    get length() {
      return store.size;
    },
    key: (index: number) => [...store.keys()][index] ?? null,
  };
}

function mockTokenStore(): TokenStore {
  const storage = mockStorage();
  return {
    save: (reply) => storage.setItem("token", JSON.stringify(reply)),
    load: () => {
      const raw = storage.getItem("token");
      return raw ? JSON.parse(raw) : null;
    },
    clear: () => storage.removeItem("token"),
  };
}

const sampleReply: LoginOrCreateReply = {
  accessTokenResponse: { token: "test-token-123" },
  userEntity: { id: "user-1", email: "test@example.com", username: "testuser", privacyToken: "" },
};

// Mock WebSocket for GameClient
class MockWebSocket {
  static instances: MockWebSocket[] = [];
  url: string;
  binaryType = "blob";
  readyState = 0;
  onopen: ((ev: Event) => void) | null = null;
  onmessage: ((ev: MessageEvent) => void) | null = null;
  onerror: ((ev: Event) => void) | null = null;
  onclose: ((ev: CloseEvent) => void) | null = null;
  sentMessages: Uint8Array[] = [];
  constructor(url: string) {
    this.url = url;
    MockWebSocket.instances.push(this);
  }
  send(data: Uint8Array) {
    this.sentMessages.push(data);
  }
  close() {
    this.readyState = 3;
  }
  static get OPEN() {
    return 1;
  }
  static get CONNECTING() {
    return 0;
  }
  static get CLOSING() {
    return 2;
  }
  static get CLOSED() {
    return 3;
  }
}
(global as any).WebSocket = MockWebSocket;

describe("Client facade", () => {
  let client: Client;

  beforeEach(() => {
    MockUnauthenticated.mockClear();
    MockWebSocket.instances = [];
    client = new Client({ tokenStore: mockTokenStore() });
  });

  afterEach(() => {
    client.dispose();
  });

  function getAuthMock() {
    return MockUnauthenticated.mock.results[0].value;
  }

  describe("authentication", () => {
    it("login delegates to auth client and emits authenticated", async () => {
      const mock = getAuthMock();
      mock.login.mockResolvedValue(sampleReply);

      const states: boolean[] = [];
      client.isAuthenticated$.subscribe((v) => states.push(v));

      await client.login("test@example.com", "password123");

      expect(client.auth.isAuthenticated()).toBe(true);
      expect(states).toContain(true);
    });

    it("createAccount delegates and emits authenticated", async () => {
      const mock = getAuthMock();
      mock.createAccount.mockResolvedValue(sampleReply);

      await client.createAccount("e@e.com", "user", "pass");

      expect(client.auth.isAuthenticated()).toBe(true);
    });

    it("restoreSession returns true when token is valid", async () => {
      const mock = getAuthMock();
      mock.login.mockResolvedValue(sampleReply);
      await client.login("test@example.com", "pass");

      // Create a new client with the same token store
      const tokenStore = mockTokenStore();
      tokenStore.save(sampleReply);
      const client2 = new Client({ tokenStore });
      const mock2 = MockUnauthenticated.mock.results[MockUnauthenticated.mock.results.length - 1].value;
      mock2.verifyToken.mockResolvedValue({ value: true });

      const ok = await client2.restoreSession();
      expect(ok).toBe(true);
      client2.dispose();
    });

    it("restoreSession returns false when no token stored", async () => {
      const ok = await client.restoreSession();
      expect(ok).toBe(false);
    });

    it("logout clears auth and emits not authenticated", async () => {
      const mock = getAuthMock();
      mock.login.mockResolvedValue(sampleReply);
      await client.login("test@example.com", "pass");

      const states: boolean[] = [];
      client.isAuthenticated$.subscribe((v) => states.push(v));

      client.logout();

      expect(client.auth.isAuthenticated()).toBe(false);
      expect(states[states.length - 1]).toBe(false);
    });
  });

  describe("sub-client access", () => {
    it("exposes collection, matchmaking, game, and stateManager", () => {
      expect(client.collection).toBeDefined();
      expect(client.matchmaking).toBeDefined();
      expect(client.game).toBeDefined();
      expect(client.stateManager).toBeDefined();
    });
  });

  describe("startGame", () => {
    it("subscribes state manager and opens WebSocket", async () => {
      const mock = getAuthMock();
      mock.login.mockResolvedValue(sampleReply);
      await client.login("test@example.com", "pass");

      client.startGame({ playerKey: "key", playerSecret: "secret" });

      expect(MockWebSocket.instances).toHaveLength(1);
      expect(MockWebSocket.instances[0].url).toContain("token=test-token-123");
    });
  });

  describe("disconnect", () => {
    it("disconnects game without logging out", async () => {
      const mock = getAuthMock();
      mock.login.mockResolvedValue(sampleReply);
      await client.login("test@example.com", "pass");

      client.startGame({ playerKey: "k", playerSecret: "s" });
      client.disconnect();

      // Still authenticated
      expect(client.auth.isAuthenticated()).toBe(true);
    });
  });
});
