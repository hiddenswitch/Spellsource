import React, { useEffect, useRef, useState } from 'react'
import type { GameEvent } from '../../../__generated__/client'
import styles from './overlay.module.css'

interface PowerHistoryProps {
  lastEvent: GameEvent | undefined
}

interface HistoryEntry {
  id: number
  text: string
}

let nextId = 0

function eventToText(event: GameEvent): string {
  if (event.description) return event.description
  return event.eventType || 'Event'
}

export const PowerHistory: React.FC<PowerHistoryProps> = ({ lastEvent }) => {
  const [entries, setEntries] = useState<HistoryEntry[]>([])
  const scrollRef = useRef<HTMLDivElement>(null)
  const prevEventRef = useRef<GameEvent | undefined>(undefined)

  useEffect(() => {
    if (lastEvent && lastEvent !== prevEventRef.current) {
      prevEventRef.current = lastEvent
      setEntries((prev) => [...prev, { id: nextId++, text: eventToText(lastEvent) }])
    }
  }, [lastEvent])

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight
    }
  }, [entries])

  if (entries.length === 0) return null

  return (
    <div className={styles.powerHistory} ref={scrollRef}>
      {entries.map((e) => (
        <div key={e.id} className={styles.historyEntry}>
          {e.text}
        </div>
      ))}
    </div>
  )
}
