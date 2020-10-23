import React from 'react'
import Blockly from 'blockly'

export default class SimpleReactBlockly extends React.Component {
  render() {
    return(
      <div>
        <div ref={ ele => this.innerBlocklyDiv = ele }
             className={this.props.wrapperDivClassName}
        />
      </div>
    )
  }
  componentDidMount() {
    this.workspace = Blockly.inject(this.innerBlocklyDiv,
      this.props.workspaceConfiguration);

    this.workspace.addChangeListener(this.props.workspaceDidChange)
  }
}