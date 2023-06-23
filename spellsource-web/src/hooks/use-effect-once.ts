import { useEffect, useRef } from "react"

export const useEffectOnce = (effect: () => void | (() => void)) => {
  const ref = useRef(true)

  useEffect(() => {
    if (ref.current) {
      ref.current = false
      return effect()
    }
  }, [])
}
