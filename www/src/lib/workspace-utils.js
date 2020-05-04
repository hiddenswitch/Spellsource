import { Xml } from 'blockly'
import { isArray, isUndefined } from 'lodash'

export default class WorkspaceUtils {
  static xmlToDictionary (xml) {
    // Create the return object
    let obj = {}

    if (xml.nodeType === 1) { // element
      // do attributes
      if (xml.attributes.length > 0) {
        obj['@attributes'] = {}
        for (let j = 0; j < xml.attributes.length; j++) {
          const attribute = xml.attributes.item(j)
          obj['@attributes'][attribute.nodeName] = attribute.nodeValue
        }
      }
    } else if (xml.nodeType === 3) { // text
      obj = xml.nodeValue
    }

    // do children
    if (xml.hasChildNodes()) {
      for (let i = 0; i < xml.childNodes.length; i++) {
        const item = xml.childNodes.item(i)
        const nodeName = item.nodeName
        if (isUndefined(obj[nodeName])) {
          obj[nodeName] = WorkspaceUtils.xmlToDictionary(item)
        } else {
          if (isUndefined(obj[nodeName].push)) {
            const old = obj[nodeName]
            obj[nodeName] = []
            obj[nodeName].push(old)
          }
          obj[nodeName].push(WorkspaceUtils.xmlToDictionary(item))
        }
      }
    }
    return obj
  }

  static workspaceToDictionary (workspace) {
    const xml = Xml.workspaceToDom(workspace)
    const dictionary = WorkspaceUtils.xmlToDictionary(xml)

    let output = {}

    WorkspaceUtils.append(output, dictionary.block)

    return output
  }

  static append (output, block) {
    if (!block) {
      return output
    }

    // Handle the first block
    if (!!block.field) {
      if (!isArray(block.field)) {
        block.field = [block.field]
      }

      block.field.forEach((field) => {
        output[field['@attributes'].name] = field['#text']
      })
    }

    if (!!block.value) {
      if (!isArray(block.value)) {
        block.value = [block.value]
      }

      block.value.forEach((value) => {
        const key = value['@attributes'].name
        if (!!value.block) {
          value = value.block
        }
        if (value.field && value.field['@attributes']) {
          if (value.field['@attributes'].name === 'int') {
            output[key] = parseInt(value.field['#text'])
            return
          } else if (!!value.field.data) {
            output[key] = value.field.data
          }
        }

        output[key] = WorkspaceUtils.append({}, value)

      })
    }

    if (!!block.next) {
      // Continue appending to current output
      WorkspaceUtils.append(output, block.next.block)
    }

    // TODO: What happens when there's a next AND a statement??

    if (!!block.statement) {
      if (!isArray(block.statement)) {
        block.statement = [block.statement]
      }

      block.statement.forEach((statement) => {
        output[statement['@attributes'].name] = WorkspaceUtils.append({}, statement.block)
      })
    }

    return output
  }
}