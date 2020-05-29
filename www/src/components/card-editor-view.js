import React, { useState } from 'react'
import WorkspaceUtils from '../lib/workspace-utils'
import { useStaticQuery, graphql } from 'gatsby'
import styles from './card-editor-view.module.css'
import ReactBlocklyComponent from 'react-blockly'
import Blockly from 'blockly'
import { has, filter } from 'lodash'
import recursiveOmitBy from 'recursive-omit-by'
import AceEditor from 'react-ace'
import 'ace-builds/src-noconflict/mode-json'
import 'ace-builds/src-noconflict/mode-xml'
import 'ace-builds/src-noconflict/theme-github'
import { Xml, FieldLabelSerializable } from 'blockly'

class FieldLabelSerializableHidden extends FieldLabelSerializable {
  constructor (opt_value, opt_validator, opt_config) {super(opt_value, opt_validator, opt_config)}

  static fromJson (options) {
    const field = new FieldLabelSerializableHidden()
    field.setValue(options.value)
    return field
  }

  getSize() {
    let size = super.getSize();
    size.width = -10;
    return size;
  }

  getDisplayText_ () {
    return ''
  }
}

Blockly.fieldRegistry.register('field_label_serializable_hidden', FieldLabelSerializableHidden)
Blockly.HSV_SATURATION = .65;

const CardEditorView = () => {
  const [code, setCode] = useState(``)
  const [xml, setXml] = useState(``)
  const data = useStaticQuery(graphql`
  query {
    toolbox {
      BlockCategoryList {
        BlockTypePrefix
        CategoryName
        ColorHex
      }
    }
    allBlock {
      edges {
        node {
          id
          args {
            i
            args {
              type
              check
              name
              valueS
              valueI
              min
              max
              int
              text
              options
            }
          }
          inputsInline
          colour
          messages
          nextStatement
          output
          previousStatement
          type
          data
        }
      }
    }
  }`)

  const toolboxCategories = data.toolbox.BlockCategoryList.map(({
    BlockTypePrefix, CategoryName, ColorHex
  }) => {
    return {
      name: CategoryName,
      blocks: filter(data.allBlock.edges, edge => edge.node.type.startsWith(BlockTypePrefix))
        .map(edge => {return { type: edge.node.type }})
    }
  })

  data.allBlock.edges.forEach(edge => {
    if (has(Blockly.Blocks, edge.node.type)) {
      return
    }

    const block = recursiveOmitBy(edge.node, ({ node }) => node === null)

    // Patch back in values from union type
    if (!!block.args) {
      block.args.forEach(args => {
        args.args.forEach(arg => {
          if (!!arg.valueI) {
            arg.value = arg.valueI
            delete arg.valueI
          }
          if (!!arg.valueS) {
            arg.value = arg.valueS
            delete arg.valueS
          }
        })

        block['args' + args.i.toString()] = args.args
      })
      delete block.args
    }

    if (!!block.messages) {
      block.messages.forEach((message, i) => {
        block['message' + i.toString()] = message
      })
      delete block.messages
    }

    Blockly.Blocks[block.type] = {
      init: function () {
        this.jsonInit(block)
        if (!!block.data) {
          this.data = block.data
        }
      }
    }
  })

  function onCodeEditorChanged (newValue) {

  }

  function onWorkspaceChanged (workspace) {
    setXml(('<xml>' + Xml.workspaceToDom(workspace).innerHTML + '</xml>').replace(/>/gi,'>\n'))
    setCode(JSON.stringify(WorkspaceUtils.workspaceToDictionary(workspace), null, 2))
  }

  return (<span>
    <ReactBlocklyComponent.BlocklyEditor
      workspaceDidChange={onWorkspaceChanged}
      wrapperDivClassName={styles.codeEditor}
      toolboxCategories={toolboxCategories}/>
    <AceEditor
      width={'100%'}
      mode="json"
      theme="github"
      setOptions={{
        'wrap': true
      }}
      readOnly={true}
      onChange={onCodeEditorChanged}
      value={code}
      editorProps={{ $blockScrolling: true }}
    /></span>)
}

export default CardEditorView