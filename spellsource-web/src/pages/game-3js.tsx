import { useState } from 'react';
import dynamic from 'next/dynamic';
import Head from 'next/head';
import { signIn, useSession } from 'next-auth/react';
import { useRouter } from 'next/router';
import { graphqlHost } from '../lib/config';

const Client = dynamic(() => import('spellsource-threejs').then(module => module.SpellsourceThreeClient), { ssr: false });

export default function ThreeGamePage() {
  const { data: session, status } = useSession();
  const router = useRouter();
  const [gameInstance, setGameInstance] = useState(0);
  const demo = router.query.demo === 'true';
  if (status === 'loading' || !router.isReady) return <p>Loading…</p>;
  if (!session && !demo) return <main><h1>Spellsource browser preview</h1>
    <button onClick={() => signIn('keycloak')}>Sign in to play</button>
    <button onClick={() => router.push('/game-3js?demo=true')}>Try the demo</button>
    <a href="/game">Play as a guest in the Unity client</a></main>;
  const endpoint = new URL('/graphql', process.env.NEXT_PUBLIC_GRAPHQL_HOST || graphqlHost);
  const subscriptions = new URL('/subscriptions', endpoint);
  subscriptions.protocol = endpoint.protocol === 'https:' ? 'wss:' : 'ws:';
  return <><Head><title>Spellsource browser preview</title></Head><main style={{ height: '100dvh' }}>
    <Client key={`${gameInstance}:${demo}:${session?.token?.accessToken ?? ''}`} mode={demo ? 'demo' : 'live'} graphqlUrl={endpoint.href}
      subscriptionsUrl={subscriptions.href} accessToken={session?.token?.accessToken}
      onExit={() => { setGameInstance(value => value + 1); void router.push('/game-3js'); }} />
  </main></>;
}
