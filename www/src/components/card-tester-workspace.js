import ReactBlocklyComponent from 'react-blockly'
import Blockly from 'blockly'
import styles from "./card-editor-view.module.css";
import React, {forwardRef, useEffect, useState} from "react";
import useBlocklyData from "../hooks/use-blockly-data";
import BlocklyMiscUtils from "../lib/blockly-misc-utils";

const CardTesterWorkspace = forwardRef((props, blocklyEditor) => {
  const data = useBlocklyData()

  const mainWorkspace = () => {
    return blocklyEditor.current.workspace.state.workspace
  }

  useEffect(() => {
    mainWorkspace().getTheme().setStartHats(true)
  }, [])

  useEffect(() => {
    BlocklyMiscUtils.switchRenderer(props.renderer, mainWorkspace())
  }, [props.renderer])

  const getToolboxCategories = () => {

    return data.testingToolbox.BlockCategoryList.map((
      {
        BlockTypePrefix, CategoryName, ColorHex, Subcategories, Tooltip, Subtooltips
      }) => {

    })
  }

  const [toolboxCategories, setToolboxCategories] = useState(getToolboxCategories())

  const onWorkspaceChanged = (workspace) => {

  }


  return (<span>
    <ReactBlocklyComponent.BlocklyEditor
      workspaceDidChange={onWorkspaceChanged}
      wrapperDivClassName={styles.cardEditor}
      toolboxCategories={toolboxCategories}
      workspaceConfiguration={
        {
          disable: false,
          zoom: {
            controls: true,
            minScale: .5,
            maxScale: 2.0,
            pinch: true
          },
          move: {
            wheel: true
          },
          renderer: props.renderer || 'spellsource'
        }
      }
      ref={blocklyEditor}
    />
   </span>
  )
})

export default CardTesterWorkspace