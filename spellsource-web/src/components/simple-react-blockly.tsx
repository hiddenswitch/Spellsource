import React from 'react'
import Blockly, {BlocklyOptions, WorkspaceSvg} from 'blockly'

interface SimpleReactBlocklyProps {
  wrapperDivClassName: string
  workspaceConfiguration: BlocklyOptions
  workspaceDidChange: () => void
}

export default class SimpleReactBlockly extends React.Component<SimpleReactBlocklyProps> {
  workspace: WorkspaceSvg
  innerBlocklyDiv: HTMLDivElement

  render() {
    return (
      <div ref={ele => this.innerBlocklyDiv = ele}
           className={this.props.wrapperDivClassName}
      />
    )
  }

  componentDidMount() {
    this.workspace = Blockly.inject(this.innerBlocklyDiv, this.props.workspaceConfiguration);
    this.workspace.addChangeListener(this.props.workspaceDidChange);
  }
}
