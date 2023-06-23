import React, { forwardRef, useEffect, useRef } from "react"
import Blockly, { BlocklyOptions, WorkspaceSvg } from "blockly"
import { Options } from "options"

export interface SimpleReactBlocklyProps {
  wrapperDivClassName: string
  workspaceConfiguration: BlocklyOptions
  workspaceDidChange: () => void
}

export interface SimpleReactBlocklyRef {
  workspace: WorkspaceSvg
  innerBlocklyDiv: HTMLDivElement
}

export default forwardRef<SimpleReactBlocklyRef, SimpleReactBlocklyProps>((props, ref) => {
  const innerBlocklyDiv = useRef<HTMLDivElement>(null)

  const options = props.workspaceConfiguration as Partial<Options>

  useEffect(() => {
    const workspace = Blockly.inject(innerBlocklyDiv.current, props.workspaceConfiguration)
    workspace.addChangeListener(props.workspaceDidChange)

    const state = { workspace, innerBlocklyDiv: innerBlocklyDiv.current }

    if (typeof ref === "function") {
      ref(state)
    } else if (ref) {
      ref.current = state
    }
  }, [])

  useEffect(() => {
    if (ref && typeof ref !== "function") {
      const workspace = ref.current.workspace
      const xml = Blockly.Xml.workspaceToDom(workspace, false)
      workspace.dispose()

      const newWorkspace = Blockly.inject(innerBlocklyDiv.current, props.workspaceConfiguration)
      newWorkspace.addChangeListener(props.workspaceDidChange)
      Blockly.Xml.clearWorkspaceAndLoadFromXml(xml, newWorkspace)

      ref.current = { workspace: newWorkspace, innerBlocklyDiv: innerBlocklyDiv.current }
    }
  }, [options.horizontalLayout, options.toolboxPosition])

  return <div ref={innerBlocklyDiv} className={props.wrapperDivClassName}></div>
})
