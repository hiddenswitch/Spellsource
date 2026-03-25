import { useCallback, useEffect, useRef, useState } from 'react'
import type { ServerGameMessage, GameEvent, Entity } from '../../__generated__/client'
import { MessageType, GameEventType } from '../../__generated__/client'

export interface RevealedCard {
  entity: Entity
  createdAt: number
}

/** How long a revealed card stays visible (ms) */
const REVEAL_DURATION = 3000

/** Duration in ms to wait after dispatching each message type */
function messageDuration(msg: ServerGameMessage): number {
  switch (msg.messageType) {
    case MessageType.OnUpdate:
      return 200
    case MessageType.OnGameEvent:
      return eventDuration(msg.event)
    case MessageType.OnGameEnd:
    case MessageType.OnRequestAction:
    case MessageType.OnMulligan:
    case MessageType.Timer:
      return 0
    default:
      return 0
  }
}

/** Visible gameplay events get delays; internal bookkeeping events get 0. */
function eventDuration(event: GameEvent | null | undefined): number {
  if (!event) return 0
  switch (event.eventType) {
    // Visible combat events
    case GameEventType.PhysicalAttack:
      return 450
    case GameEventType.Damage:
      return 200
    case GameEventType.Heal:
      return 150
    case GameEventType.Kill:
      return 250

    // Visible card/board events
    case GameEventType.Summon:
      return 200
    case GameEventType.DrawCard:
      return 120
    case GameEventType.PlayCard:
      return 250
    case GameEventType.SpellCasted:
      return 200
    case GameEventType.WeaponEquipped:
      return 150
    case GameEventType.SecretPlayed:
      return 150
    case GameEventType.SecretRevealed:
      return 300
    case GameEventType.HeroPowerUsed:
      return 150
    case GameEventType.WeaponDestroyed:
      return 150

    // Events with no visual — zero delay
    case GameEventType.AfterPhysicalAttack:
    case GameEventType.AfterPlayCard:
    case GameEventType.AfterSpellCasted:
    case GameEventType.AfterSummon:
    case GameEventType.BeforePhysicalAttack:
    case GameEventType.BeforeSummon:
    case GameEventType.BoardChanged:
    case GameEventType.AttributeApplied:
    case GameEventType.DidEndSequence:
    case GameEventType.WillEndSequence:
    case GameEventType.EnrageChanged:
    case GameEventType.EntityTouched:
    case GameEventType.EntityUntouched:
    case GameEventType.PreDamage:
    case GameEventType.PreGameStart:
    case GameEventType.GameStart:
    case GameEventType.GameInitialized:
    case GameEventType.TargetAcquisition:
    case GameEventType.PerformedGameAction:
    case GameEventType.TriggerFired:
    case GameEventType.ManaModified:
    case GameEventType.MaxMana:
    case GameEventType.MaxHpIncreased:
    case GameEventType.LoseDeflect:
    case GameEventType.LoseDivineShield:
    case GameEventType.LoseStealth:
    case GameEventType.Overload:
    case GameEventType.DestroyWillQueue:
    case GameEventType.Decay:
    case GameEventType.All:
      return 0

    // Minor visible events — short delay
    case GameEventType.TurnEnd:
    case GameEventType.TurnStart:
      return 100
    case GameEventType.ArmorGained:
      return 100
    case GameEventType.Silence:
      return 150
    case GameEventType.Fatigue:
      return 150
    case GameEventType.Discard:
    case GameEventType.Roasted:
      return 100
    case GameEventType.MissileFired:
      return 80

    default:
      return 0
  }
}

export interface ActiveEffect {
  id: number
  type: 'damage' | 'heal' | 'attack' | 'summon' | 'death'
  /** Entity ID the effect is attached to */
  entityId: number
  /** Optional target entity for attack animations */
  targetEntityId?: number
  /** Numeric value (damage/heal amount) */
  value?: number
  /** When this effect was created (Date.now()) */
  createdAt: number
  /** How long this effect lasts in ms */
  duration: number
}

let nextEffectId = 0

function effectsFromEvent(event: GameEvent): ActiveEffect[] {
  const effects: ActiveEffect[] = []
  const now = Date.now()

  switch (event.eventType) {
    case GameEventType.Damage:
      if (event.target) {
        effects.push({
          id: nextEffectId++,
          type: 'damage',
          entityId: event.target.id,
          value: event.value ?? 0,
          createdAt: now,
          duration: 800,
        })
      }
      break

    case GameEventType.Heal:
      if (event.target) {
        effects.push({
          id: nextEffectId++,
          type: 'heal',
          entityId: event.target.id,
          value: event.value ?? 0,
          createdAt: now,
          duration: 800,
        })
      }
      break

    case GameEventType.PhysicalAttack:
      if (event.source && event.target) {
        effects.push({
          id: nextEffectId++,
          type: 'attack',
          entityId: event.source.id,
          targetEntityId: event.target.id,
          createdAt: now,
          duration: 400,
        })
      }
      break

    case GameEventType.Summon:
      if (event.target) {
        effects.push({
          id: nextEffectId++,
          type: 'summon',
          entityId: event.target.id,
          createdAt: now,
          duration: 300,
        })
      }
      break

    case GameEventType.Kill:
      if (event.target) {
        effects.push({
          id: nextEffectId++,
          type: 'death',
          entityId: event.target.id,
          createdAt: now,
          duration: 300,
        })
      }
      break
  }

  return effects
}

interface AnimationQueueOptions {
  dispatch: (msg: ServerGameMessage) => void
}

export function useAnimationQueue({ dispatch }: AnimationQueueOptions) {
  const queueRef = useRef<ServerGameMessage[]>([])
  const processingRef = useRef(false)
  const dispatchRef = useRef(dispatch)
  dispatchRef.current = dispatch
  const [activeEffects, setActiveEffects] = useState<ActiveEffect[]>([])
  const [revealedCard, setRevealedCard] = useState<RevealedCard | null>(null)

  // Garbage collect expired effects and revealed card
  useEffect(() => {
    if (activeEffects.length === 0 && !revealedCard) return
    const interval = setInterval(() => {
      const now = Date.now()
      setActiveEffects((prev) =>
        prev.filter((e) => now - e.createdAt < e.duration)
      )
      setRevealedCard((prev) =>
        prev && now - prev.createdAt < REVEAL_DURATION ? prev : null
      )
    }, 100)
    return () => clearInterval(interval)
  }, [activeEffects.length, !!revealedCard])

  const processQueue = useCallback(() => {
    if (processingRef.current) return
    processingRef.current = true

    function step() {
      if (queueRef.current.length === 0) {
        processingRef.current = false
        return
      }

      const msg = queueRef.current.shift()!

      // Extract effects and card reveals from game events before dispatching
      if (msg.messageType === MessageType.OnGameEvent && msg.event) {
        const newEffects = effectsFromEvent(msg.event)
        if (newEffects.length > 0) {
          setActiveEffects((prev) => [...prev, ...newEffects])
        }

        // Show opponent's played card in the reveal panel
        const evt = msg.event
        if (
          !evt.isSourcePlayerLocal &&
          (evt.eventType === GameEventType.PlayCard ||
            evt.eventType === GameEventType.SpellCasted ||
            evt.eventType === GameEventType.WeaponEquipped ||
            evt.eventType === GameEventType.HeroPowerUsed ||
            evt.eventType === GameEventType.SecretRevealed)
        ) {
          // The played card is in target (not source) for these events
          const card = evt.target ?? evt.source
          if (card && card.name && card.name.length > 0 && card.cardId !== 'hidden') {
            setRevealedCard({ entity: card, createdAt: Date.now() })
          }
        }
      }

      dispatchRef.current(msg)

      const dur = messageDuration(msg)
      if (dur > 0) {
        setTimeout(step, dur)
      } else {
        // Zero-delay messages process immediately but yield to allow React to render
        // before processing the next message
        setTimeout(step, 0)
      }
    }

    step()
  }, [])

  const enqueue = useCallback(
    (msg: ServerGameMessage) => {
      // Timer messages bypass the queue entirely (they just update a timer display)
      if (msg.messageType === MessageType.Timer) {
        dispatchRef.current(msg)
        return
      }

      // All other messages go through the queue for proper sequencing
      queueRef.current.push(msg)
      processQueue()
    },
    [processQueue]
  )

  return { enqueue, activeEffects, revealedCard }
}
