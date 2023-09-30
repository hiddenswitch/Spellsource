import { useRef } from "react";

export default function useComponentDidMount(func: () => void) {
  const willMount = useRef(true);

  if (willMount.current) {
    func();
  }

  willMount.current = false;
}
