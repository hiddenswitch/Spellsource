import { grpc } from '@improbable-eng/grpc-web'
import { BrowserHeaders } from 'browser-headers'
import { GrpcWebImpl } from 'spellsource-protos/dist/experimental/client/hiddenswitch'
import { GrpcWebImpl as SpellsourceGrpcWebImpl } from 'spellsource-protos/dist/experimental/client/spellsource'

export const DEFAULT_ENDPOINT = 'https://spellsource-api-v0.appmana.com:443'

export interface TransportOptions {
  endpoint?: string
  debug?: boolean
  metadata?: grpc.Metadata
}

/**
 * Create a GrpcWebImpl for the hiddenswitch services (auth, accounts, cards, games).
 */
export function createHiddenswitchRpc(options: TransportOptions = {}): GrpcWebImpl {
  const { endpoint = DEFAULT_ENDPOINT, debug = false, metadata } = options
  return new GrpcWebImpl(endpoint, { debug, metadata })
}

/**
 * Create a GrpcWebImpl for the spellsource services (matchmaking, game subscription).
 */
export function createSpellsourceRpc(options: TransportOptions = {}): SpellsourceGrpcWebImpl {
  const { endpoint = DEFAULT_ENDPOINT, debug = false, metadata } = options
  return new SpellsourceGrpcWebImpl(endpoint, { debug, metadata })
}

/**
 * Build gRPC metadata with a Bearer token for authenticated requests.
 */
export function createAuthMetadata(token: string): BrowserHeaders {
  return new BrowserHeaders({ Authorization: `Bearer ${token}` })
}
