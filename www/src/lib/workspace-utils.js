import {isNumber, Xml} from 'blockly'
import {extend, filter, find, fromPairs, isArray, isPlainObject, isEmpty, map, merge} from 'lodash'
import format from 'string-format'

export default class WorkspaceUtils {
  static BLOCKLY_BOOLEAN_ATTRIBUTE_TRUE = 'BLOCKLY_BOOLEAN_ATTRIBUTE_TRUE'
  static BLOCKLY_INT_ATTRIBUTE = 'BLOCKLY_INT_ATTRIBUTE'
  static BLOCKLY_ARRAY_ELEMENT = 'BLOCKLY_ARRAY_ELEMENT'
  static BLOCKLY_EXTEND_PREVIOUS = 'BLOCKLY_EXTEND_PREVIOUS'

  /**
   * Process a given piece of XML, returning a "CardScript" JSON token that corresponds to it.
   *
   * When given an XML document, this will descend, depth-first, to recursive calls to this function, building the
   * object bottom-up.
   *
   * For example,
   *
   * <block 1> xmlToDictionary(1) {
   *   <statement 2> xmlToDictionary(2) {
   *     <block 3 ...> xmlToDictionary(3) {return reduce 2, 3}
   *       <next>
   *         <block 4 ...> xmlToDictionary(4) {return reduce 3, 4}
   *       </next>
   *   </statement> return reduce 2, 1 }
   * </block> return 1 }
   *
   * The way we signal the time and objects to reduce with is using the <data> element for blocks.
   *
   * attributes block (something + an attributes object calculated from the crap we received)
   *   int attribute (something + int attribute + bool attribute + int attribute)
   *     bool attribute (something + int attribute + bool attribute)
   *       int attribute add this int attribute to whatever was previous to it (something + int attribute)
   *
   * @param xml
   * @param prev
   * @param parent
   * @returns {{}|*|{}|[]}
   */
  static xmlToCardScript (xml, prev = null, parent = null) {
    let nextNode = null
    let next = null
    switch (xml.nodeName) {
      case '#document':
        if (!!xml.firstElementChild) {
          return WorkspaceUtils.xmlToCardScript(xml.firstElementChild)
        }
        break
      case 'xml':
        if (!!xml.firstElementChild) {
          const elementNodes = filter(Array.from(xml.childNodes), cn => cn.nodeType === Node.ELEMENT_NODE)
          if (elementNodes.length === 1) {
            return WorkspaceUtils.xmlToCardScript(elementNodes[0])
          }
          return map(elementNodes, cn => WorkspaceUtils.xmlToCardScript(cn))
        }
        break
      case 'shadow':
      case 'block':
        const obj = {}
        if (!xml.hasChildNodes()) {
          return obj
        }
        const childNodes = Array.from(xml.childNodes)
        const length = childNodes.length
        for (let i = 0; i < length; i++) {
          const childNode = childNodes[i]
          switch (childNode.nodeName) {
            case '#text':
              // i.e. if childElementCount === 0
              if (length === 1) {
                return xml.innerHTML
              }
              break
            case 'field':
              obj[childNode.attributes['name'].value] = !isNaN(childNode.innerHTML) ? +childNode.innerHTML : childNode.innerHTML
              break
            case 'statement':
            case 'value':
              if (childNode.firstElementChild.nodeName === 'shadow' && childNode.lastElementChild !== childNode.firstElementChild) {
                obj[childNode.attributes['name'].value] = WorkspaceUtils.xmlToCardScript(childNode.lastElementChild, null, obj)
              } else {
                obj[childNode.attributes['name'].value] = WorkspaceUtils.xmlToCardScript(childNode.firstElementChild, null, obj)
              }
              break
            case 'next':
              if (!!childNode.firstElementChild && childNode.firstElementChild.nodeName === 'block') {
                nextNode = childNode.firstElementChild
              }
              break
          }
        }

        if (!!nextNode) {
          next = WorkspaceUtils.xmlToCardScript(nextNode, obj)
        }

        const hasData = find(childNodes, cn => cn.nodeName === 'data')
        if (hasData) {
          const values = hasData.innerHTML.split(',')
          let retValue = null
          for (let i = 0; i < values.length; i++) {
            const value = values[i]
            switch (value) {
              case WorkspaceUtils.BLOCKLY_EXTEND_PREVIOUS:
                if (!!obj.customArg && !!obj.customValue) {
                  obj[obj.customArg] = obj.customValue
                  delete obj.customArg
                  delete obj.customValue
                }
                if (!!prev) {
                  merge(prev, obj)
                }
                retValue = obj
                break
              case WorkspaceUtils.BLOCKLY_BOOLEAN_ATTRIBUTE_TRUE:
                if (!obj.attribute) {
                  return {}
                }
                const boolAttribute = { [obj.attribute]: true }
                if (!!next) {
                  extend(boolAttribute, next)
                }
                retValue = boolAttribute
                break
              case WorkspaceUtils.BLOCKLY_INT_ATTRIBUTE:
                if (!obj.attribute) {
                  return {}
                }
                const intAttribute = { [obj.attribute]: obj.value }
                if (!!next) {
                  extend(intAttribute, next)
                }
                retValue = intAttribute
                break
              case this.BLOCKLY_ARRAY_ELEMENT:
                // Handle every array statement on this block
                retValue = [obj]
                if (!!obj.i) {
                  retValue = [obj.i]
                }
                if (!!next) {
                  if (isArray(next)) {
                    retValue = retValue.concat(next)
                  } else {
                    retValue = retValue.concat([next])
                  }
                }
                break
              default:
                const allValues = filter(childNodes, cn =>
                  cn.nodeName === 'field')
                const valuesObj = fromPairs(map(allValues, cn =>
                  [cn.attributes['name'].value, cn.innerHTML])
                )

                const res = format(value, valuesObj)
                retValue = !isNaN(res) ? +res : res
                break
            }
          }
          if (retValue !== null) {
            return this.postProcessCardScript(retValue)
          }
        }
        return this.postProcessCardScript(obj)
      default:
        throw new Error('invalid block type to pass here')
    }
  }

  /**
   * Makes final changes to the cardScript to make it valid
   *
   * Input value names that contain '.'s will be rearranged,
   * as defined by the rearrangeInputValues method
   *
   * Cards that have opener(battlecry) and/or aftermath(deathrattle)
   * properties will be given their respective attributes
   *
   * Boolean values are also fixed here
   *
   * @param cardScript
   * @returns the modified cardScript
   */
  static postProcessCardScript (cardScript) {
    if (isArray(cardScript)) {
      for (const cardScriptElement of cardScript) {
        this.postProcessCardScript(cardScriptElement)
      }
      return cardScript
    }
    this.rearrangeInputValues(cardScript)
    if (!!cardScript.card && !(typeof cardScript.card === 'string')) {
      delete cardScript.card
    }
    if (cardScript.target === 'IT') {
      delete cardScript.target
    }
    if (cardScript.cardType === 'ANY') {
      delete cardScript.cardType
    }

    if (!!cardScript.battlecry) {
      if (!cardScript.attributes) {
        cardScript.attributes = {}
      }
      cardScript.attributes.BATTLECRY = true
    }

    if (!!cardScript.deathrattle) {
      if (!cardScript.attributes) {
        cardScript.attributes = {}
      }
      cardScript.attributes.DEATHRATTLES = true
    }

    if (!!cardScript.class && cardScript.class.endsWith('Aura')
      && !!cardScript.attribute && !cardScript.attribute.startsWith('AURA_')) {
      cardScript.attribute = 'AURA_' + cardScript.attribute
    }

    if (!!cardScript.triggers && cardScript.triggers.length === 1) {
      cardScript.trigger = cardScript.triggers[0]
      delete cardScript.triggers
    }

    if (!!cardScript.aura && isArray(cardScript.aura)) {
      cardScript.aura = cardScript.aura[0]
    }

    for (let arg of ['r', 'g', 'b', 'a']) {
      if (cardScript.hasOwnProperty(arg) && isNumber(cardScript[arg])) {
        cardScript[arg] = Math.round(1000 * cardScript[arg] / 255) / 1000
      }
    }

    return cardScript
  }

  /**
   * Usage:
   *    ...
   *    {
   *      "super.X": "value"
   *      ...
   *    }
   *
   *    super tries to move the argument up a level,
   *    so that the level above will look like
   *
   *    ...
   *    "X": "value",
   *    {
   *      ...
   *    }
   *
   *    -----------------------------------------------------------------------
   *
   *    {
   *      ...
   *      "X.Y": "value"
   *    }
   *
   *    other uses of '.' try to move the argument down a level,
   *    so that it will look like
   *
   *    {
   *      ...
   *      "X": {
   *        "Y": "value"
   *      }
   *    }
   *
   *    if "X" is already present, then "Y" will simply be put in as an argument
   *    if "X" isn't there already, it will be created
   *
   *    -----------------------------------------------------------------------
   *
   *    ...
   *    "X,Y.Z": "value"
   *    ...
   *
   *    ',' will put a value into multiple different places,
   *    so that it will look like
   *
   *    ...
   *    "X": "value",
   *    "Y.Z": "value"
   *    ...
   *
   *    which will be split as shown above, turning into
   *
   *    ...
   *    "X": "value",
   *    "Y": {
   *      "Z": "value"
   *    }
   * @param cardScript
   */
  static rearrangeInputValues (cardScript) {
    if (typeof cardScript === 'string') {
      return
    }

    //first, split up any args with ','
    for (const cardScriptKey in cardScript) {
      if (cardScriptKey.includes(',')) {
        let newKeys = cardScriptKey.split(',')
        for (const key of newKeys) {
          cardScript[key] = cardScript[cardScriptKey]
        }
        delete cardScript[cardScriptKey]
      }
    }

    //go through the children to bring super.* up
    for (const cardScriptKey in cardScript) {
      if (cardScript.propertyIsEnumerable(cardScriptKey)) {
        //first time go through all the ones that definitely won't override what we're working with
        for (const cardScriptElementKey in cardScript[cardScriptKey]) {
          if (cardScriptElementKey.startsWith('super.')) {
            let newKey = cardScriptElementKey.substring(cardScriptElementKey.indexOf('.') + 1)
            if (cardScriptKey.includes('.')) {
              let correctPrefix = cardScriptKey.split('.').slice(0, -1).join('.')
              cardScript[correctPrefix + '.' + newKey] = cardScript[cardScriptKey][cardScriptElementKey]
            } else {
              cardScript[newKey] = cardScript[cardScriptKey][cardScriptElementKey]
            }
            delete cardScript[cardScriptKey][cardScriptElementKey]
          }
        }
        //then do the last one that might override what we're working with
        if (!!cardScript[cardScriptKey]['super']
          && typeof cardScript[cardScriptKey]['super'] === 'string') {
          cardScript[cardScriptKey] = cardScript[cardScriptKey]['super']
        } else if (!!cardScript['super'] && cardScript.propertyIsEnumerable('super')
          && typeof cardScript['super'] !== 'string') {
          let andWhenEveryonesSuper = !!cardScript.super.super
          merge(cardScript, cardScript['super'])
          if (andWhenEveryonesSuper) {
            //no one will be
          } else {
            delete cardScript['super']
          }
        }
      }
    }

    //go through the keys here to bring down any *.*
    for (const cardScriptKey in cardScript) {
      if (!cardScriptKey.startsWith('super') && cardScriptKey.includes('.')) {
        let newKey = cardScriptKey.substring(0, cardScriptKey.indexOf('.'))
        let newKey2 = cardScriptKey.substring(cardScriptKey.indexOf('.') + 1)
        if (!cardScript.hasOwnProperty(newKey)) {
          cardScript[newKey] = {}
        }
        if (cardScript.propertyIsEnumerable(newKey)) {
          cardScript[newKey][newKey2] = cardScript[cardScriptKey]
          delete cardScript[cardScriptKey]
          this.postProcessCardScript(cardScript[newKey])
        }
      }

      //gotta do this because it seems like the original block -> xml conversion hates booleans
      if (cardScript[cardScriptKey] === 'TRUE') {
        cardScript[cardScriptKey] = true
      }
      if (cardScript[cardScriptKey] === 'FALSE') {
        cardScript[cardScriptKey] = false
      }
      if (isPlainObject(cardScript[cardScriptKey]) && isEmpty(cardScript[cardScriptKey])) {
        delete cardScript[cardScriptKey]
      }
    }
  }

  static workspaceToCardScript (workspace) {
    const xml = Xml.workspaceToDom(workspace)
    return WorkspaceUtils.xmlToCardScript(xml)
  }
}