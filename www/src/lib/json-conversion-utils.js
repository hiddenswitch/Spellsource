import Blockly, {isNumber} from 'blockly'
import BlocklyMiscUtils from './blockly-misc-utils'
import {isArray} from 'lodash'

export default class JsonConversionUtils {
  static classBlocksDictionary = {} //A dictionary mapping the 'class' argument a block uses to the block itself
  static enumBlocksDictionary = {} //A dictionary mapping the enum value of the block to the block itself
  static allArgNames = new Set() //Every different possible arg name that appears on blocks, (for searching)

  static blockTypeColors = {}

  static errorOnCustom = false

  static customBlocks = {}


  /**
   * Creates a reference for the block's json that's easily accessible by
   * either its output or 'class' argument
   * @param block The block to add
   */
  static addBlockToMap(block) {
    if (block.type.endsWith('SHADOW')) {
      return
    }
    let list = this.argsList(block)
    if (list.length > 0) {
      let className = null

      for (let arg of list) {
        if (!arg.name) {
          continue
        }
        this.allArgNames.add(arg.name)
        if (arg.name.endsWith('class')) {
          className = arg.value
          break
        }
      }
      if (!!className) {
        if (!this.classBlocksDictionary[className]) {
          this.classBlocksDictionary[className] = []
        }
        this.classBlocksDictionary[className].push(block)
      }
    } else {
      if (!this.enumBlocksDictionary[block.data]) {
        this.enumBlocksDictionary[block.data] = []
      }
      this.enumBlocksDictionary[block.data].push(block)
    }

  }

  /**
   * Helper method to make sure added blocks have the correct shadows
   * We still want those in case people decide to pull apart the converted stuff
   * @param workspace The workspace
   * @param type The block type to create
   * @returns The created block
   */
  static newBlock(workspace, type) {
    return BlocklyMiscUtils.newBlock(workspace, type)
  }

  /**
   * OVERALL METHOD TO CREATE THE CARD ON THE WORKSPACE
   * PUBLIC STATIC VOID MAIN OVER HERE, CHAPS
   * @param workspace The workspace
   * @param card The card json to generate from
   * @returns The created starter block
   */
  static generateCard(workspace, card) {
    let type = card.type
    if (!!card.quest) {
      type = 'QUEST'
    } else if (!!card.secret) {
      type = 'SECRET'
    } else if (type === 'HERO' && !card.attributes.hasOwnProperty('HP')) {
      type = 'HERO2'
    }
    let block = this.newBlock(workspace, 'Starter_' + type)
    let args = ['baseManaCost', 'name', 'baseAttack', 'baseHp', 'description', 'countUntilCast',
      'damage', 'durability']
    args.forEach(arg => {
      if (!!card[arg] && !!block.getField(arg)) {
        block.setFieldValue(card[arg], arg)
      }
    })

    if (!!block.initSvg) {
      block.initSvg()
    }
    if (!!card.attributes && !!card.attributes.HP) {
      block.setFieldValue(card.attributes.HP, 'attributes.HP,attributes.MAX_HP')
    }

    if (!!block.getInput('name')) {
      block.getInput('name').connection.targetBlock().setFieldValue(card.name, 'text')
    }
    if (!!block.getInput('description')) {
      block.getInput('description').connection.targetBlock().setFieldValue(card.description, 'text')
    }

    let lowestBlock = block

    for (let arg of ['heroClass', 'rarity', 'spell', 'targetSelection', 'secret', 'quest', 'heroPower', 'hero']) {
      if (card.type === 'CLASS' && arg === 'heroClass') {
        block.getInput('heroClass').connection.targetBlock().setFieldValue(card.heroClass, 'text')
        continue
      }

      if (card.hasOwnProperty(arg) && !!block.getInput(arg)) {
        this.simpleHandleArg(block, arg, card, workspace)
      }
    }

    if (!!card.race && card.type === 'MINION') {
      this.simpleHandleArg(block, 'race', card, workspace)
    }

    if (!!card.countByValue && (card.countByValue === 'TRUE' || card.countByValue === true)) {
      block.setFieldValue('TRUE', 'countByValue')
    }

    if (!!card.battlecry) {
      let openerBlock
      if (!!card.battlecry.condition) {
        openerBlock = this.newBlock(workspace, 'Property_opener2')
      } else {
        openerBlock = this.newBlock(workspace, 'Property_opener1')
      }
      lowestBlock.nextConnection.connect(openerBlock.previousConnection)
      if (!!openerBlock.initSvg) {
        openerBlock.initSvg()
      }

      this.handleArg(openerBlock.getInput('battlecry.targetSelection').connection, card.battlecry.targetSelection, 'battlecry.targetSelection', workspace, card.battlecry)
      this.handleArg(openerBlock.getInput('battlecry.spell').connection, card.battlecry.spell, 'battlecry.spell', workspace, card.battlecry)

      if (!!card.battlecry.condition) {
        this.handleArg(openerBlock.getInput('battlecry.condition').connection, card.battlecry.condition, 'battlecry.condition', workspace, card.battlecry)
      }
      lowestBlock = openerBlock
    }

    if (!!card.deathrattle) {
      let aftermathBlock = this.newBlock(workspace, 'Property_aftermath')
      lowestBlock.nextConnection.connect(aftermathBlock.previousConnection)
      if (!!aftermathBlock.initSvg) {
        aftermathBlock.initSvg()
      }
      this.simpleHandleArg(aftermathBlock, 'deathrattle', card, workspace)
      lowestBlock = aftermathBlock
    }

    const triggers = (trigger, property) => {
      if (!!card[trigger + 's'] || !!card[trigger]) {
        let triggersBlock = this.newBlock(workspace, property)
        lowestBlock.nextConnection.connect(triggersBlock.previousConnection)
        if (!!triggersBlock.initSvg) {
          triggersBlock.initSvg()
        }
        let triggers
        if (!!card[trigger]) {
          triggers = [card[trigger]]
        } else {
          triggers = card[trigger + 's']
        }
        let lowestConnection = triggersBlock.getFirstStatementConnection()
        for (let trigger of triggers) {
          let triggerBlock = this.enchantment(trigger, workspace)
          lowestConnection.connect(triggerBlock.previousConnection)
          lowestConnection = triggerBlock.nextConnection
          if (!!triggerBlock.initSvg) {
            triggerBlock.initSvg()
          }
        }
        lowestBlock = triggersBlock
      }
    }
    triggers('trigger', 'Property_triggers')
    triggers('passiveTrigger', 'Property_triggers2')
    triggers('deckTrigger', 'Property_triggers3')
    triggers( 'gameTrigger', 'Property_triggers4')

    if (!!card.auras || !!card.aura) {
      let aurasBlock = this.newBlock(workspace, 'Property_auras')
      lowestBlock.nextConnection.connect(aurasBlock.previousConnection)
      if (!!aurasBlock.initSvg) {
        aurasBlock.initSvg()
      }
      this.auras(aurasBlock, card, workspace)
      lowestBlock = aurasBlock
    }

    if (!!card.attributes) {
      delete card.attributes.SPELLSOURCE_NAME
      delete card.attributes.BATTLECRY
      delete card.attributes.DEATHRATTLES
      if (card.type === 'HERO') {
        delete card.attributes.HP
        delete card.attributes.MAX_HP
      }
      if (Object.values(card.attributes).length > 0) {
        let attributesBlock = this.newBlock(workspace, 'Property_attributes')
        lowestBlock.nextConnection.connect(attributesBlock.previousConnection)
        if (!!attributesBlock.initSvg) {
          attributesBlock.initSvg()
        }
        let lowestConnection = attributesBlock.getFirstStatementConnection()
        for (let atr in card.attributes) {
          let attributeBlock
          if (isNumber(card.attributes[atr])) {
            attributeBlock = this.newBlock(workspace, 'Property_attributes_int')
            attributeBlock.getField('value').setValue(card.attributes[atr])
          } else {
            attributeBlock = this.newBlock(workspace, 'Property_attributes_boolean')
          }
          this.handleArg(attributeBlock.getInput('attribute').connection, atr, 'attribute', workspace, card.attributes)
          if (!!attributeBlock.initSvg) {
            attributeBlock.initSvg()
          }
          lowestConnection.connect(attributeBlock.previousConnection)
          lowestConnection = attributeBlock.nextConnection
        }

        lowestBlock = attributesBlock
      }
    }

    if (!!card.manaCostModifier) {
      let costyBlock = null
      if (card.manaCostModifier.class === 'ConditionalValueProvider' && card.manaCostModifier.ifFalse === 0) {
        costyBlock = this.newBlock(workspace, 'Property_manaCostModifierConditional')
        this.handleArg(costyBlock.getInput('manaCostModifier.condition').connection, card.manaCostModifier.condition,
          'condition', workspace, card)
        if (typeof card.manaCostModifier.ifTrue === 'object') {
          this.handleArg(costyBlock.getInput('manaCostModifier.ifTrue').connection, card.manaCostModifier.ifTrue,
            'ifTrue', workspace, card)
        } else {
          this.handleIntArg(costyBlock, costyBlock.json.args0[0].name, workspace, card.manaCostModifier.ifTrue)
        }

      } else {
        costyBlock = this.newBlock(workspace, 'Property_manaCostModifier')
        this.handleArg(costyBlock.getInput('manaCostModifier').connection, card.manaCostModifier,
          'manaCostModifier', workspace, card)
      }
      lowestBlock.nextConnection.connect(costyBlock.previousConnection)
      if (!!costyBlock.initSvg) {
        costyBlock.initSvg()
      }
      lowestBlock = costyBlock
    }

    if (!!card.cardCostModifier) {
      let costyBlock = this.newBlock(workspace, 'Property_cardCostModifier')
      lowestBlock.nextConnection.connect(costyBlock.previousConnection)
      if (!!costyBlock.initSvg) {
        costyBlock.initSvg()
      }

      this.costModifier(costyBlock, card.cardCostModifier, workspace)

      lowestBlock = costyBlock
    }

    if (!!card.dynamicDescription) {
      let descriptionsBlock = this.newBlock(workspace, 'Property_descriptions')

      this.dynamicDescription(workspace, descriptionsBlock.getFirstStatementConnection(), card.dynamicDescription, 'i')

      descriptionsBlock.previousConnection.connect(lowestBlock.nextConnection)
      if (!!descriptionsBlock.initSvg) {
        descriptionsBlock.initSvg()
      }
      lowestBlock = descriptionsBlock
    }

    if (card.set !== 'CUSTOM') {
      let setBlock = this.newBlock(workspace, 'Property_set')
      setBlock.setFieldValue(card.set, 'set')
      setBlock.previousConnection.connect(lowestBlock.nextConnection)
      if (!!setBlock.initSvg) {
        setBlock.initSvg()
      }
      lowestBlock = setBlock
    }

    if (!!card.condition) {
      let conditionBlock = this.newBlock(workspace, 'Property_condition')
      this.simpleHandleArg(conditionBlock, 'condition', card, workspace)
      conditionBlock.previousConnection.connect(lowestBlock.nextConnection)
      if (!!conditionBlock.initSvg) {
        conditionBlock.initSvg()
      }
      lowestBlock = conditionBlock
    }

    if ((card.collectible === false || card.collectible === 'FALSE') && type !== 'HERO') {
      let uncollectibleBlock = this.newBlock(workspace, 'Property_uncollectible')
      uncollectibleBlock.previousConnection.connect(lowestBlock.nextConnection)
      if (!!uncollectibleBlock.initSvg) {
        uncollectibleBlock.initSvg()
      }
      lowestBlock = uncollectibleBlock
    }

    if (!!card.art) {
      for (let path of ['art.primary', 'art.secondary', 'art.shadow', 'art.highlight', 'art.body.vertex']) {
        let json = card
        for (let arg of path.split('.')) {
          if (!!json) {
            json = json[arg]
          }
        }

        if (!!json && !!block.getInput(path)) {
          let colorBlock = block.getInput(path).connection.targetBlock()
          for (let i of ['r', 'g', 'b', 'a']) {
            colorBlock.setFieldValue(Math.round(json[i] * 255), i)
          }
        }
      }
      if (!!card.art.glow) {
        let glowBlock = this.newBlock(workspace, 'Property_glow')
        glowBlock.previousConnection.connect(lowestBlock.nextConnection)
        let colorBlock = glowBlock.getInput('art.glow').connection.targetBlock()
        for (let i of ['r', 'g', 'b', 'a']) {
          colorBlock.setFieldValue(Math.round(card.art.glow[i] * 255), i)
        }
        if (!!glowBlock.initSvg) {
          glowBlock.initSvg()
        }
        lowestBlock = glowBlock
      }
    }

    if (!!workspace.render) {
      workspace.render()
    }

    return block
  }

  /**
   * Copies over the dynamic description fields for a card
   *
   * @param workspace The workspace
   * @param connection The statement connection for the list of descriptions
   * @param descriptions The json array of descriptions
   * @param inputName The name of the descriptions input argument on the block
   */
  static dynamicDescription(workspace, connection, descriptions, inputName) {
    for (let dynamicDescription of descriptions) {
      let block = connection.targetBlock()
      if (!block) {
        block = this.newBlock(workspace, 'Property_description')
        connection.connect(block.previousConnection)
        if (!!block.initSvg) {
          block.initSvg()
        }
      }
      if (typeof dynamicDescription === 'string') {
        dynamicDescription = {
          class: 'StringDescription',
          string: dynamicDescription
        }
      }

      let descBlock = this.newBlock(workspace, 'Property_' + dynamicDescription.class)

      if (descBlock === null) {
        continue
      }

      block.getInput(inputName).connection.connect(descBlock.outputConnection)

      if (!!dynamicDescription.value) {
        if (isNumber(dynamicDescription.value)) {
          this.handleIntArg(descBlock, 'value', workspace, dynamicDescription.value)
        } else {
          this.handleArg(descBlock.getInput('value').connection, dynamicDescription.value,
            'value', workspace, dynamicDescription)
        }
      }
      if (!!dynamicDescription.condition) {
        this.handleArg(descBlock.getInput('condition').connection, dynamicDescription.condition,
          'condition', workspace, dynamicDescription)
      }
      if (!!dynamicDescription.string) {
        descBlock.setFieldValue(dynamicDescription.string, 'string')
      }
      if (dynamicDescription.hasOwnProperty('description1')) {
        this.dynamicDescription(workspace, block.getInput(inputName).connection, [dynamicDescription.description1], 'description1')
      }
      if (dynamicDescription.hasOwnProperty('description2')) {
        this.dynamicDescription(workspace, block.getInput(inputName).connection, [dynamicDescription.description2], 'description2')
      }

      if (!!dynamicDescription.descriptions) {
        this.dynamicDescription(workspace, descBlock.getFirstStatementConnection(), dynamicDescription.descriptions, 'i')
      }

      block.getInput(inputName).connection.connect(descBlock.outputConnection)
      connection = block.nextConnection
      if (!!descBlock.initSvg) {
        descBlock.initSvg()
      }
    }
  }

  /**
   * Handles finding an input on a block by what its name ends with
   * @param block The block (not its json definition) to search through
   * @param inputName What the name has to end with
   * @returns The json for the correct input, or null
   */
  static getInputEndsWith(block, inputName) {
    for (let name of this.allArgNames) {
      let input = block.getInput(name)
      if (!!input) {
        if (name.endsWith(inputName)) {
          return input
        }
      }
    }
    return null
  }

  /**
   * Whether we need to use the EnchantmentOptions block and not just the Enchantment block
   *
   * Used to be more complicated, but the trigger conditions got streamlined
   * @param trigger
   * @param props
   * @returns {boolean}
   */
  static enchantmentNeedsOptions(trigger, props) {
    return !(props.length === 2 && !!trigger.eventTrigger && !!trigger.spell)
  }

  /**
   * Enchantments are special enough to need their own method
   * because of the unique option blocks
   *
   * @param trigger The json of the trigger to be blockified
   * @param workspace The workspace
   * @param triggerBlock The enchantment block that may or may not already be
   * present in the list of enchantment statements
   * @returns The created block
   */
  static enchantment(trigger, workspace, triggerBlock = null) {
    let props = this.relevantProperties(trigger)
    if (!this.enchantmentNeedsOptions(trigger, props)) {
      if (!triggerBlock) {
        triggerBlock = this.newBlock(workspace, 'Enchantment')
      }
    } else {
      if (!triggerBlock) {
        triggerBlock = this.newBlock(workspace, 'EnchantmentOptions')
      }
      let lowestOptionConnection = triggerBlock.getFirstStatementConnection()
      for (let prop of props) {
        if (prop === 'spell' || prop === 'eventTrigger') {
          continue
        }
        let match = null
        for (let blockType in Blockly.Blocks) {
          let block = Blockly.Blocks[blockType].json
          if (block.type.startsWith('EnchantmentOption')) {
            for (let arg of this.argsList(block)) {
              if (arg.name === prop) {
                match = block
              }
            }
          }
        }
        let option = this.newBlock(workspace, match.type)
        if (trigger[prop] !== true) {
          option.setFieldValue(trigger[prop], prop)
        }
        if (!!option.initSvg) {
          option.initSvg()
        }
        lowestOptionConnection.connect(option.previousConnection)
        lowestOptionConnection = lowestOptionConnection.targetBlock().nextConnection
      }
    }
    this.handleArg(triggerBlock.getInput('spell').connection, trigger.spell, 'spell', workspace, trigger)
    this.handleArg(triggerBlock.getInput('eventTrigger').connection, trigger.eventTrigger, 'eventTrigger', workspace, trigger)

    return triggerBlock
  }

  /**
   * Whether we need to use the EnchantmentOptions block and not just the Enchantment block
   *
   * Used to be more complicated, but the trigger conditions got streamlined
   * @param costModifier
   * @param props
   * @returns {boolean}
   */
  static costModifierNeedsOptions(costModifier, props) {
    if (costModifier.class === 'OneTurnCostModifier') return true
    for (let prop of props) {
      if (!!Blockly.Blocks['CostModifierOption_' + prop]) {
        return true
      }
    }
    return false
  }

  /**
   * Cost Modifiers are special enough to need their own method
   * because of the unique option blocks
   *
   * @param costModifier The json of the trigger to be blockified
   * @param workspace The workspace
   * @returns The created block
   */
  static costModifier(costyBlock, costModifier, workspace) {
    costModifier = this.mutateJson(costModifier)
    if (typeof costModifier.value !== 'object' && costModifier.value < 0) {
      if (costModifier.operation === 'SUBTRACT') {
        costModifier.operation = 'ADD'
      } else {
        costModifier.operation = 'SUBTRACT'
      }
      costModifier.value *= -1
    }
    let props = this.relevantProperties(costModifier)
    let costModifierBlock
    let lowestOptionConnection
    if (!this.costModifierNeedsOptions(costModifier, props)) {
      costModifierBlock = this.newBlock(workspace, 'CostModifier')
    } else {
      costModifierBlock = this.newBlock(workspace, 'CostModifierOptions')
      lowestOptionConnection = costModifierBlock.getFirstStatementConnection()
      for (let prop of props) {
        if (prop === 'value' || prop === 'operation' || prop === 'target' || prop === 'filter') {
          continue
        }
        let option = this.newBlock(workspace, 'CostModifierOption_' + prop)
        this.handleInputs(Blockly.Blocks['CostModifierOption_' + prop].json, costModifier, option, workspace, null)
        if (!!option.initSvg) {
          option.initSvg()
        }
        lowestOptionConnection.connect(option.previousConnection)
        lowestOptionConnection = lowestOptionConnection.targetBlock().nextConnection
      }
    }
    if (costModifier.class === 'OneTurnCostModifier') {
      let option = this.newBlock(workspace, 'CostModifierOption_oneTurn')
      if (!!option.initSvg) {
        option.initSvg()
      }
      lowestOptionConnection.connect(option.previousConnection)
    }
    if (typeof costModifier.value !== 'object') {
      this.handleIntArg(costModifierBlock, 'value', workspace, costModifier.value)
    } else {
      this.simpleHandleArg(costModifierBlock, 'value', costModifier, workspace)
    }
    if (!!costModifier.target) {
      this.simpleHandleArg(costModifierBlock, 'target', costModifier, workspace)
    }
    if (!!costModifier.operation) {
      costModifierBlock.setFieldValue(costModifier.operation, 'operation')
    }

    if (costModifierBlock.initSvg) {
      costModifierBlock.initSvg()
    }

    costyBlock.getInput('cardCostModifier').connection.connect(costModifierBlock.outputConnection)
  }

  /**
   * Auras are also weird enough to need their own method,
   * since they're the only typical 'Desc' style json object
   * that's statement style and not value style
   *
   * Functions both for json.aura and json.auras
   *
   * @param block The block that has the aura statement input
   * @param json The json of the aura / auras
   * @param workspace The workspace
   */
  static auras(block, json, workspace) {
    let auras
    if (!!json.aura) {
      auras = [json.aura]
    } else {
      auras = json['auras']
    }
    let lowestConnection = block.getFirstStatementConnection()
    for (let aura of auras) {
      this.handleArg(lowestConnection, aura, 'aura', workspace, aura, true)
      lowestConnection = lowestConnection.targetBlock().nextConnection
    }
  }

  /**
   * Finds the best block match for a given bit of json
   *
   * For Enums/Strings, it just simply finds the corresponding Block
   *
   * For actual JSON objects, the tl; dr is it finds the block that:
   *  - covers all the required json properties, determined by this.relevantProperties()
   *  - has a lot of arguments that the json does actually have
   *  - doesn't have too many arguments that the json doesn't have
   *
   * If no blocks match the first criteria, nothing is returned
   * The other criteria are for picking the best block to return
   *
   * @param json The json we need to find a match for
   * @param inputName The name of the argument that json is assigned to
   * @param parentJson The level of json above json
   * @returns The Block that's the best match (its JSON definition), or null if no good matches
   */
  static getMatch(json, inputName, parentJson) {
    let matches = null
    let bestMatch = null
    if (typeof json !== 'object') { //just looking for the correct block with the data of the json string
      let lookingForType = BlocklyMiscUtils.inputNameToBlockType(inputName)
      if (inputName === 'attribute') {
        json = json.toString().replace('AURA_', '')
      }
      matches = this.enumBlocksDictionary[json]
      if (!matches || matches.length === 0) {
        return
      }
      for (let match of matches) {
        if (!lookingForType || match.type.startsWith(lookingForType)
          || match.type.startsWith('CatalogueCard')) {
          return match
        }
      }
    } else if (!!json.class) { //need to find the block that represents that class
      let className = json.class
      if (className === 'AddEnchantmentSpell' && !!json.trigger) {
        if (this.enchantmentNeedsOptions(json.trigger, this.relevantProperties(json.trigger))) {
          if (!!json.revertTrigger) {
            return Blockly.Blocks['Spell_AddEnchantment5'].json
          } else {
            return Blockly.Blocks['Spell_AddEnchantment2'].json
          }
        } else {
          if (!!json.revertTrigger) {
            return Blockly.Blocks['Spell_AddEnchantment4'].json
          } else {
            return Blockly.Blocks['Spell_AddEnchantment'].json
          }
        }
      }
      if (className === 'AddPactSpell') {
        return Blockly.Blocks['Spell_AddPact'].json
      }
      if (className === 'CardCostModifierSpell') {
        return Blockly.Blocks['Spell_CardCostModifier'].json
      }
      if (className.endsWith('CostModifier')) {
        if (this.costModifierNeedsOptions(json, this.relevantProperties(json))) {
          return Blockly.Blocks['CostModifierOptions'].json
        } else {
          return Blockly.Blocks['CostModifier'].json
        }
      }
      matches = this.classBlocksDictionary[className]
      if (!matches || matches.length === 0) {
        return
      }
      let relevantProperties = this.relevantProperties(json)
      let goodMatches = []
      for (let match of matches) { //for each possible match
        let hasAllProps = true
        for (let property of relevantProperties) { //check the json's relevant properties
          if ((relevantProperties.includes('target') || match.output === 'SpellDesc') && property === 'filter') {
            continue
          }
          if ((className.endsWith('Spell')
            || className.endsWith('ValueProvider')
            || className.endsWith('Source')
          ) && property === 'targetPlayer') {
            continue
          }
          if (className.endsWith('Trigger')) {
            if (json.targetPlayer === 'BOTH' && property === 'targetPlayer') {
              continue
            }
            if (property === 'race' || property === 'requiredAttribute') {
              continue
            }
          }

          let hasThisProp = false
          for (let arg of this.argsList(match)) { //see if the match has a corresponding prop
            if (arg.type === 'field_label_plural') {
              continue
            }
            if (arg.name.split('.')[0] === property) { //just a surface level check, not traversing nested args
              if (arg.type === 'field_label_serializable_hidden') {
                if ((arg.value === 'TRUE' ? true : arg.value) === json[property]) {
                  hasThisProp = true
                }
              } else {
                hasThisProp = true
              }
            }

          }
          if (!hasThisProp) { //if it doesn't, it's not good enough
            hasAllProps = false
            break
          }
        }
        if (hasAllProps) { //if it covers all the json's properties, it's good enough
          goodMatches.push(match)
        }
      }
      let bestScore = 0
      for (let goodMatch of goodMatches) { //choose the one with the highest number of correct properties
        let bestScore = null
        for (let goodMatch of matches) {
          let argsList = this.argsList(goodMatch)
          let hasOneNormalArg = argsList.length === 0;
          for (let arg of argsList) {
            if (!arg.name.includes('.') && !arg.name.includes('super')) {
              hasOneNormalArg = true
            }
          }
          if (!hasOneNormalArg) {
            continue
          }
          let score = 0
          for (let arg of argsList) {
            if (arg.type === 'field_label_plural') {
              continue
            }
            let delta = 0
            let property = this.traverseJsonByArgName(arg.name, json, parentJson)
            if (property !== null && property !== undefined) {
              if (arg.type === 'field_label_serializable_hidden') {
                if ((arg.value === 'TRUE' ? true : arg.value) === property) {
                  delta = 2
                } else {
                  delta = -5 // an unchangeable field on the block is wrong... not a good look
                }
              } else {
                delta = 2 //if it's an input, we assume the correct block can be put here
              }
            } else {
              if (arg.type === 'field_label_serializable_hidden') {
                delta = -5
              } else {
                delta = -1 //it's kinda bad to straight up not have the property
              }
            }
            score += delta
          }
          if (score > bestScore || (score >= bestScore && bestMatch?.type.localeCompare(goodMatch.type) > 0)) {
            //for tied scores, do the one that comes alphabetically first
            //e.g. choosing ExampleSpell1 instead of ExampleSpell2
            bestMatch = goodMatch
            bestScore = score
          }
        }
      }
    } else {
      //what the heck could it even be if it doesn't have a class?
    }
    return bestMatch
  }

  /**
   * Handles an argument on a block, recursively constructing the relevant blocks based on the json
   * @param connection The Blockly connection object where the new Block will have to go
   * @param json The json to determine the newBlock from
   * @param inputName The name of the argument that json is assigned to
   * @param workspace The workspace
   * @param parentJson The level of json above json
   * @param statement Whether we're connected a statement rather than an input
   */
  static handleArg(connection, json, inputName, workspace, parentJson, statement = false) {
    json = this.mutateJson(json)

    if (!!connection.targetBlock() && connection.targetBlock().type === 'Property_text_SHADOW') {
      connection.targetBlock().setFieldValue(json, 'text')
      return
    }

    let bestMatch = this.getMatch(json, inputName, parentJson)
    let newBlock
    if (!bestMatch) {
      try {
        this.generateDummyBlock(json, inputName, parentJson)
      } catch (e) {
        //generating this quality of life dummy block is never worth crashing about
        console.log('Tried to generate a dummy block for ' + json + ' but failed because of ' + e)
      }
      if (this.errorOnCustom) {
        throw Error("Couldn't generate without custom blocks")
      }
      newBlock = this.handleNoMatch(json, inputName, parentJson, workspace)
    } else if (!!connection.targetBlock() && connection.targetBlock().type === bestMatch.type) {
      if (!connection.targetBlock().isShadow()) {
        newBlock = connection.targetBlock()
        connection.disconnect()
        //just simpler to disconnect it and then reconnect it
      }
    }
    if (!newBlock) {
      newBlock = this.newBlock(workspace, bestMatch.type)
    }
    if (!!newBlock.initSvg) {
      newBlock.initSvg()
    }

    let outerBlock = this.wrapperBlocks(newBlock, json, inputName, workspace, parentJson, connection, bestMatch)

    if (statement) {
      connection.connect(outerBlock.previousConnection)
    } else {
      connection.connect(outerBlock.outputConnection)
    }

    if (!bestMatch) {
      return //args already taken care of by handleNoMatch
    }

    this.handleInputs(bestMatch, json, newBlock, workspace, parentJson)
  }

  static handleInputs(bestMatch, json, newBlock, workspace, parentJson) {
    //now handle each dropdown on the new block (assumes stuff will just work)
    for (let dropdown of this.dropdownsList(bestMatch)) {
      let jsonElement = json[dropdown.name]
      if (!jsonElement && (dropdown.name.includes('.') || dropdown.name.includes('super') || dropdown.name.includes(','))) {
        jsonElement = this.traverseJsonByArgName(dropdown.name, json, parentJson)
      }

      if (!jsonElement) {
        continue
      }

      newBlock.setFieldValue(jsonElement, dropdown.name)
    }

    //now handle each input on the new block
    for (let inputArg of this.inputsList(bestMatch)) {
      let name = inputArg.name
      let argName = name
      let jsonElement = json[name]
      if (!jsonElement && (name.includes('.') || name.includes('super') || name.includes(','))) {
        jsonElement = this.traverseJsonByArgName(name, json, parentJson)
        name = name.split('.').slice(-1)[0]
      }

      if (jsonElement === null || jsonElement === undefined) {
        if (newBlock.getInput(name)?.connection.targetBlock()?.type === 'EntityReference_SHADOW'
        || newBlock.getInput(name)?.connection.targetBlock()?.type === 'EntityReference_IT') {
          let it = this.newBlock(workspace, 'EntityReference_IT')
          newBlock.getInput(name).connection.connect(it.outputConnection)
          if (it.initSvg) {
            it.initSvg()
          }
        }
        continue
      }

      //if the json has a corresponding argument
      if (typeof jsonElement !== 'object' && BlocklyMiscUtils.inputNameToBlockType(name) === 'ValueProvider') {
        //integer block stuff
        this.handleIntArg(newBlock, inputArg.name, workspace, jsonElement)
      } else if (name === 'spells' || name === 'conditions'
        || name === 'filters' || name === 'cards') { //arrays of things stuff
        this.handleArrayArg(jsonElement, newBlock, workspace, name)
      } else if (name === 'trigger' || name === 'pact') {
        this.enchantment(json[name], workspace, newBlock.getFirstStatementConnection().targetBlock())
      } else if (name === 'aura') {
        this.auras(newBlock, json, workspace)
      } else if (name === 'cardCostModifier') {
        this.costModifier(newBlock, json.cardCostModifier, workspace)
      } else { //default recursion case
        this.handleArg(newBlock.getInput(argName).connection, jsonElement, name, workspace, json)
      }
    }
  }

  static handleArrayArg(jsonElement, newBlock, workspace, name) {
    let thingArray = jsonElement
    let lowestBlock = newBlock.getFirstStatementConnection().targetBlock()
    this.handleArg(lowestBlock.getInput('i').connection, thingArray[0], name.slice(0, -1), workspace, thingArray)
    for (let i = 1; i < thingArray.length; i++) {
      let thingI
      switch (name) {
        case 'conditions':
          thingI = this.newBlock(workspace, 'Condition_I')
          break
        case 'filters':
        case 'cardFilters':
          thingI = this.newBlock(workspace, 'Filter_I')
          break
        case 'cards':
          thingI = this.newBlock(workspace, 'Card_I')
          break
        default:
          thingI = this.newBlock(workspace, 'Spell_I')
          break
      }
      this.handleArg(thingI.getInput('i').connection, thingArray[i], name.slice(0, -1), workspace, thingArray)
      lowestBlock.nextConnection.connect(thingI.previousConnection)
      if (!!thingI.initSvg) {
        thingI.initSvg()
      }
      lowestBlock = thingI
    }
  }

  /**
   * Many blocks serve as 'wrappers' for other blocks, ending up being converted
   * to card json as a modification of what they hold inside (think, random from [Target(s)])
   *
   * This method handles all the wrapper blocks that could be encountered
   * when handling an arg, successfully wrapping even in cases where
   * multipler wrappers are needed.
   * @param block The original block in need of wrapping
   * @param json The json that the block corresponds to
   * @param inputName The name of the argument that json is assigned to
   * @param workspace The workspace
   * @param parentJson The level of json above json
   * @param connection The connection that the block was originally supposed to connect to
   * @param bestMatch The block that was decided to be the best match
   * @returns The eventual outermost block
   */
  static wrapperBlocks(block, json, inputName, workspace, parentJson, connection, bestMatch) {
    const wrap = (blockType, inputName = 'super') => {
      let newOuterBlock = this.newBlock(workspace, blockType)
      newOuterBlock.getInput(inputName).connection.connect(outerBlock.outputConnection)
      if (!!newOuterBlock.initSvg) {
        newOuterBlock.initSvg()
      }
      outerBlock = newOuterBlock
    }
    let outerBlock = block

    if (!!json.targetPlayer && !!bestMatch && (json.targetPlayer !== 'SELF' || json.class === 'ReturnTargetToHandSpell') &&
      (bestMatch.output === 'ValueProviderDesc' || bestMatch.output === 'SpellDesc' || bestMatch.output === 'Source')) {
      switch (bestMatch.output) {
        case 'ValueProviderDesc':
          wrap('ValueProvider_targetPlayer')
          break
        case 'Source':
          wrap('Source_targetPlayer')
          break
        default:
          wrap('Spell_TargetPlayer')
          break
      }
      this.simpleHandleArg(outerBlock, 'targetPlayer', json, workspace)
    }

    if (inputName === 'target' && !!parentJson.target && !!parentJson.filter
      && !this.getInputEndsWith(connection.getSourceBlock(), 'filter')
      && !connection.getSourceBlock().getInput('filter')) {
      wrap('EntityReference_FILTER')
      this.handleArg(outerBlock.getInput('super.filter').connection, parentJson.filter, 'filter', workspace, json)
    }

    if (!!json.invert && !!bestMatch
      && !this.argsList(bestMatch).map(arg => arg.name.split('.').slice(-1)[0]).includes('invert')) {
      if (json.class.endsWith('Filter')) {
        wrap('Filter_NOT')
      } else if (json.class.endsWith('Condition')) {
        wrap('Condition_NOT')
      }
    }

    if (!!json.class && json.class.endsWith('Trigger')) {
      if (!!json.fireCondition) {
        wrap('Trigger_FireCondition')
        this.simpleHandleArg(outerBlock, 'fireCondition', json, workspace)
      }
      if (!!json.queueCondition) {
        wrap('Trigger_QueueCondition')
        this.simpleHandleArg(outerBlock, 'queueCondition', json, workspace)
      }
      if (!!json.race) {
        wrap('Trigger_Race')
        this.simpleHandleArg(outerBlock, 'race', json, workspace)
      }
      if (!!json.requiredAttribute) {
        let match = this.getMatch(json.requiredAttribute, 'attribute', json)
        wrap('Trigger_Attribute')
        this.simpleHandleArg(outerBlock, 'requiredAttribute', json, workspace)
      }
    }

    if (inputName.endsWith('targetSelection') && !!parentJson.targetSelectionCondition
    && !!parentJson.targetSelectionOverride) {
      wrap('TargetSelection_OVERRIDE')
      this.handleArg(outerBlock.getInput('super.targetSelectionCondition').connection, parentJson.targetSelectionCondition, 'targetSelectionCondition', workspace, parentJson)
      this.handleArg(outerBlock.getInput('super.targetSelectionOverride').connection, parentJson.targetSelectionOverride, 'targetSelectionOverride', workspace, parentJson)
    }

    if (inputName.endsWith('targetSelection') && !!parentJson.spell && !!parentJson.spell.filter
      && json !== 'NONE') {
      if (parentJson.spell.filter.class === 'RaceFilter') {
        wrap('TargetSelection_RACE')
        this.handleArg(outerBlock.getInput('super.spell.filter.race').connection, parentJson.spell.filter.race, 'race', workspace, parentJson.spell.filter)
      } else {
        wrap('TargetSelection_FILTER')
        this.handleArg(outerBlock.getInput('super.spell.filter').connection, parentJson.spell.filter, 'filter', workspace, parentJson.spell)
      }
    }

    if (inputName === 'target' && parentJson.randomTarget === true) { //handles the randomTarget arg
      wrap('EntityReference_RANDOM')
    }

    if (!!json.multiplier) {
      wrap('ValueProvider_multiplier')
      if (typeof json.multiplier !== 'object') {
        this.handleIntArg(outerBlock, 'multiplier', workspace, json.multiplier)
      } else {
        this.simpleHandleArg(outerBlock, 'multiplier', json, workspace)
      }
    }

    if (!!json.offset) {
      wrap('ValueProvider_offset')
      if (typeof json.offset !== 'object') {
        this.handleIntArg(outerBlock, 'offset', workspace, json.offset)
      } else {
        this.simpleHandleArg(outerBlock, 'offset', json, workspace)
      }
    }

    if (!!json.distinct) {
      wrap('Source_distinct')
    }

    return outerBlock
  }

  /**
   * In many places specifying everything needed by handleArg is redundent,
   * so this method can handle the simple cases simply
   * @param block The block being connected to
   * @param inputName The name of BOTH the block argument and the json argument
   * @param json The PARENT json in which the real json is found by inputName
   * @param workspace The workspace
   * @returns The block that handleArg returns
   */
  static simpleHandleArg(block, inputName, json, workspace) {
    return this.handleArg(block.getInput(inputName).connection, json[inputName], inputName, workspace, json)
  }

  /**
   * Traverses through the json of card to try to find the element
   * being referred to by a block's argument, which could include
   * features like 'super', '.' and ',' as explained in WorkspaceUtils
   *
   * In cases of ',' this returns the first match it encounters
   * @param name The name to search for, possibly containing special elements
   * @param json The json to search in
   * @param parentJson The level above the json to search in (for super purposes)
   * @returns What's in the correct spot, or undefined if it can't find the right spot
   */
  static traverseJsonByArgName(name, json, parentJson) {
    if (!name || !json) {
      return undefined
    }
    if (name.includes(',')) {
      let names = name.split(',')
      for (let name of names) {
        let elem = this.traverseJsonByArgName(name, json, parentJson)
        if (elem !== undefined) {
          return elem
        }
      }
      return undefined
    } else {
      let i = name.indexOf('.')
      if (i <= 0) {
        return json[name]
      }
      let start = name.substring(0, i)
      let rest = name.substring(i + 1)
      if (start === 'super' && !!parentJson) {
        return this.traverseJsonByArgName(rest, parentJson, null)
      } else {
        return this.traverseJsonByArgName(rest, json[start], json)
      }
    }
  }

  /**
   * Specifically handles a spot where an integer value is needed
   *
   * Uses the shadow int block if it's already there,
   * or makes a nonshadow one if it isn't
   * @param newBlock The block that the int block should be connected to
   * @param inputArg The name of the int argument
   * @param workspace The workspace
   * @param int The number that should actually end up in the int block
   */
  static handleIntArg(newBlock, inputArg, workspace, int) {
    let valueBlock
    if (!!newBlock.getInput(inputArg).connection.targetBlock()
      && newBlock.getInput(inputArg).connection.targetBlock().type === 'ValueProvider_int') {
      valueBlock = newBlock.getInput(inputArg).connection.targetBlock()
    } else {
      valueBlock = this.newBlock(workspace, 'ValueProvider_int')
      newBlock.getInput(inputArg).connection.connect(valueBlock.outputConnection)
      if (!!valueBlock.initSvg) {
        valueBlock.initSvg()
      }
    }
    valueBlock.setFieldValue(int, 'int')
  }

  /**
   * The old way of doing custom blocks, which was actually defining
   * a new block with the needed input types and field values built into it
   *
   * Now, this is only used to generate some json to print to the console
   * that you can use to quickly make the block yourself,
   * but the actual block(s) that end up on the workspace
   * are from handleNoMatch and use stuff from the Custom tab
   *
   * @param json The json of the card that needs its own block
   * @param inputName The name of the argument json is assigned to
   * @param parentJson The level of json that's above json
   * @returns The block it generated
   * */
  static generateDummyBlock(json, inputName, parentJson) {
    inputName = inputName.split('.').slice(-1)[0]
    let type = BlocklyMiscUtils.inputNameToBlockType(inputName)
    let consoleBlock
    if (typeof json !== 'object') {
      let color = this.blockTypeColors[type]
      consoleBlock = {
        type: type + '_' + json.toString(),
        data: json.toString(),
        colour: isNumber(color) ? parseInt(color) : color,
        output: type,
        message0: BlocklyMiscUtils.toHappyFormatting(json.toString())
      }
    } else {
      let props = this.relevantProperties(json)
      let className = json.class
      let messages = []
      let args = []
      for (let prop of props) {
        let shouldBeField = !BlocklyMiscUtils.inputNameToBlockType(prop)
        if (!!json.class && json.class.endsWith('Trigger') &&
          (prop === 'targetPlayer' || prop === 'sourcePlayer')) {
          shouldBeField = true
        }
        let arg = {
          name: prop
        }
        let newMessage = prop + ': %1'
        if (shouldBeField) {
          if (prop === 'operation') {
            if (this.dropdownsList(Blockly.Blocks['ValueProvider_Algebraic'].json)[0]
              .options.map(arr => arr[1]).includes(json[prop])) {
              arg = this.dropdownsList(Blockly.Blocks['ValueProvider_Algebraic'].json)[0]
            } else {
              arg = this.dropdownsList(Blockly.Blocks['Condition_Comparison'].json)[0]
            }
          } else {
            arg.type = 'field_label_serializable_hidden'
            arg.value = json[prop]
            newMessage += '"' + BlocklyMiscUtils.toHappyFormatting(json[prop].toString()) + '"'
          }
        } else {
          arg.type = 'input_value'
          arg.check = (prop === 'attribute' ? (!!parentJson.value ? 'Int' : 'Bool') : '')
            + BlocklyMiscUtils.blockTypeToOuput(BlocklyMiscUtils.inputNameToBlockType(prop))
          arg.shadow = {
            type: prop === 'target' ? 'EntityReference_IT' :
              BlocklyMiscUtils.inputNameToBlockType(prop) +
              (prop === 'attribute' ? (!!parentJson.value ? '_INT_SHADOW' : '_BOOL_SHADOW')
                : '_SHADOW')
          }
        }
        messages.push(newMessage)
        args.push(arg)
      }

      let output = BlocklyMiscUtils.blockTypeToOuput(type)
      let color = this.blockTypeColors[output]
      consoleBlock = {
        type: type + '_' + className.replace(type, ''),
        inputsInline: false,
        output: output,
        colour: isNumber(color) ? parseInt(color) : color,
        message0: className + '%1',
        args0: [{
          type: 'field_label_serializable_hidden',
          name: 'class',
          value: className
        }]
      }
      if (type === 'Aura') {
        delete consoleBlock.output
        consoleBlock.previousStatement = ['Auras']
        consoleBlock.nextStatement = ['Auras']
      }
      for (let j = 1; j <= messages.length; j++) {
        //block['message' + j.toString()] = messages[j - 1]
        //block['args' + j.toString()] = [args[j - 1]]

        consoleBlock.message0 += ' ' + messages[j - 1].replace('%1', '%' + (j + 1).toString())
        consoleBlock.args0.push(args[j - 1])
      }

    }

    console.log('Had to create new block ' + consoleBlock.type)
    console.log(JSON.stringify(consoleBlock, null, 2).toString()
      .replace('"colour": "(\\d+)"', '"colour": $1'))


    let blok = JSON.stringify(consoleBlock, null, 2)
    //blok = consoleBlock.type
    if (!this.customBlocks[blok]) {
      this.customBlocks[blok] = 0
    }
    this.customBlocks[blok]++

    return consoleBlock
  }

  /**
   * Handles the construction of a custom block for a given bit of json
   *
   * No longer actually generates any new blocks, but uses the tools
   * in the Custom tab of the toolbox
   *
   * @param json The json that needs to be turned into a custom block
   * @param inputName The name of the argument json is assigned to
   * @param parentJson The level of json that's above json
   * @param workspace The workspace
   * @returns {Blockly.Block}
   */
  static handleNoMatch(json, inputName, parentJson, workspace) {
    inputName = inputName.split('.').slice(-1)[0]
    let type = BlocklyMiscUtils.inputNameToBlockType(inputName)
    let newBlock = this.newBlock(workspace, 'Custom' + type)
    if (typeof json !== 'object') {
      newBlock.setFieldValue(json, 'value')
    } else if (!!json.class) {
      newBlock.getInput('class').connection.targetBlock().setFieldValue(json.class, 'class')
      let lowestConnection = newBlock.getFirstStatementConnection()

      for (let arg in json) {
        if (arg === 'class') {
          continue
        }

        let blockType = BlocklyMiscUtils.inputNameToBlockType(arg)

        let argValue = json[arg]
        if (!blockType) {
          if (argValue === true || argValue === false) {
            blockType = 'Boolean'
            argValue = argValue.toString().toUpperCase()
          } else if (isArray(argValue)) {
            blockType = BlocklyMiscUtils.inputNameToBlockType(arg.slice(0, -1)) + 's'
          } else {
            blockType = 'text'
          }
        }
        let newArgBlock = this.newBlock(workspace, 'CustomArg_' + blockType)
        newArgBlock.previousConnection.connect(lowestConnection)
        lowestConnection = newArgBlock.nextConnection
        if (!!newArgBlock.initSvg) {
          newArgBlock.initSvg()
        }
        newArgBlock.setFieldValue(arg, 'customArg')

        if (!!newArgBlock.getInput('customValue')) {
          if (isNumber(argValue)) {
            this.handleIntArg(newArgBlock, 'customValue', workspace, argValue)
          } else if (isArray(argValue)) {
            if (arg === 'aura') {
              this.auras(newArgBlock, json, workspace)
            } else this.handleArrayArg(argValue, newArgBlock, workspace, arg)
          } else {
            this.handleArg(newArgBlock.getInput('customValue').connection, argValue, arg,
              workspace, parentJson)
          }
        } else {
          newArgBlock.setFieldValue(argValue, 'customValue')
        }
      }
    }
    return newBlock
  }

  /**
   * The arguments of a desc that should be used in deciding on a block representation
   * @param json
   * @returns array of the relevant properties (strings)
   */
  static relevantProperties(json) {
    let relevantProperties = []
    for (let property in json) {
      if ((property === 'randomTarget' && !!json.target) || property === 'class'
        || property === 'fireCondition' || property === 'queueCondition'
        || property === 'invert' || property === 'offset' || property === 'multiplier'
        || property === 'distinct'
      ) {
        continue //these ones can be handled by other blocks
      }
      if (json[property] !== null && json[property] !== undefined) {
        relevantProperties.push(property)
      }
    }
    return relevantProperties
  }

  /**
   * Deals with places in card json that could be equivalent in function to different json,
   * but need special handling to be creatable in the card editor
   * @param json
   * @returns The mutated json
   */
  static mutateJson(json) {
    if ((json === null || json === undefined)) {
      json = 'NONE'
    }
    if (typeof json !== 'object') {
      return json
    }
    let className = json.class
    let props = this.relevantProperties(json)

    //the 'has' operation can be implied in many cases
    if (className.endsWith('Filter') || className.endsWith('Condition')) {
      if (props.includes('attribute') && props.includes('operation')
        && !props.includes('value') && json.operation === 'HAS') {
        delete json.operation
        props = this.relevantProperties(json)
      }
    }

    if (className === 'CardFilter') {
      if (props.length === 1) { //cardfilters for just a race can be race filters
        if (props[0] === 'race') {
          return {
            class: 'RaceFilter',
            race: json.race,
            invert: json.invert
          }
        }
        if (props[0] === 'attribute') {
          return {
            class: 'AttributeFilter',
            attribute: json.attribute,
            invert: json.invert
          }
        }
      } else if (!(props.length === 2 && props.includes('attribute') && (props.includes('value') || props.includes('operation')))
        && !(props.length === 3 && props.includes('attribute') && props.includes('operation') && props.includes('value'))
      ) { //cardfilters with many different properties need to be split up to a big and filter
        let filters = []
        if (!!json.race) {
          filters.push({
            class: 'RaceFilter',
            race: json.race
          })
        }
        if (!!json.cardType) {
          filters.push({
            class: 'CardFilter',
            cardType: json.cardType
          })
        }
        if (!!json.attribute) {
          if (!json.value && (!json.operation || json.operation === 'HAS') && !!this.enumBlocksDictionary[json.attribute]) {
            if ((this.enumBlocksDictionary[json.attribute][0].output === 'BoolAttribute')) {
              filters.push({
                class: 'AttributeFilter',
                attribute: json.attribute
              })
            } else {
              filters.push({
                class: 'AttributeFilter',
                attribute: json.attribute,
                operation: 'GREATER_OR_EQUAL',
                value: 1
              })
            }
          } else {
            filters.push({
              class: 'AttributeFilter',
              attribute: json.attribute,
              operation: json.operation,
              value: json.value
            })
          }
        }
        if (!!json.manaCost) {
          filters.push({
            class: 'CardFilter',
            manaCost: json.manaCost
          })
        }
        if (!!json.heroClass) {
          filters.push({
            class: 'CardFilter',
            heroClass: json.heroClass
          })
        }
        if (!!json.rarity) {
          filters.push({
            class: 'CardFilter',
            rarity: json.rarity
          })
        }
        return {
          class: 'AndFilter',
          filters: filters,
          invert: json.invert
        }
      }
    }

    //would be redundant to add this functionality separately
    if (className === 'MinionCountCondition') {
      json = {
        class: 'ComparisonCondition',
        value1: {
          class: 'EntityCountValueProvider',
          target: json.targetPlayer === 'OPPONENT' ? 'ENEMY_MINIONS'
            : json.targetPlayer === 'BOTH' ? 'ALL_MINIONS'
              : 'FRIENDLY_MINIONS'
        },
        operation: json.operation,
        value2: json.value
      }
    }
    if (className === 'CardCountCondition') {
      json = {
        class: 'ComparisonCondition',
        value1: {
          class: 'CardCountValueProvider',
          targetPlayer: json.targetPlayer,
          cardFilter: !!json.filter ? json.filter : json.cardFilter
        },
        operation: json.operation,
        value2: json.value
      }
    }
    /*if (className === 'DeckContainsCondition') {
      if (!!json.card) {
        json = {
          class: 'ComparisonCondition',
          value1: {
            class: 'CardCountValueProvider',
            cardSource: {
              class: 'DeckSource'
            },
            cardFilter: {
              class: 'SpecificCardFilter',
              card: json.card
            }
          },
          operation: "GREATER_OR_EQUAL",
          value2: 1
        }
      } else {
        json = {
          class: 'ComparisonCondition',
          value1: {
            class: 'CardCountValueProvider',
            cardSource: {
              class: 'DeckSource'
            },
            cardFilter: json.cardFilter
          },
          operation: "GREATER_OR_EQUAL",
          value2: 1
        }
      }
    }*/

    //some auras specify extra triggers unnecessarily
    if (className.endsWith('Aura') && !!json.triggers) {
      let triggersDontMatter = true
      for (let trigger of json.triggers) {
        if (trigger.class !== 'WillEndSequenceTrigger') {
          triggersDontMatter = false
        }
      }
      if (triggersDontMatter) {
        delete json.triggers
      }
    }

    //in spells, the targetPlayer of 'self' is always redundant
    if ((json.targetPlayer === 'SELF' && json.class.endsWith('Spell')
      && json.class !== 'ReturnTargetToHandSpell')
        //in triggers, it's 'both'
    || (json.targetPlayer === 'BOTH' && json.class.endsWith('Trigger'))) {
      delete json.targetPlayer
    }

    //for certain triggers the Source Player and Target Player are just the same
    if ((className === 'MinionSummonedTrigger' || className === 'MinionPlayedTrigger')
      && !!json.sourcePlayer) {
      json.targetPlayer = json.sourcePlayer
      delete json.sourcePlayer
    }

    if (className === 'TemporaryAttackSpell' && !!json.attackBonus) {
      if (!json.value) {
        json.value = 0
      }
      json.value += json.attackBonus
      delete json.attackBonus
    }

    if (json.sourcePlayer === 'BOTH') {
      delete json.sourcePlayer
    }

    if (className === 'ReduceValueProvider' && json.attribute) {
      json.value1 = {
        class: 'AttributeValueProvider',
        attribute: json.attribute
      }
      delete json.attribute
    }

    if (className === 'DiscoverSpell' && !json.cards && !json.cardSource) {
      json.cardSource = {
        class: 'CatalogueSource'
      }
    }

    if (className === 'AlgebraicValueProvider' && json.operation === 'NEGATE'
    && !json.hasOwnProperty('value2')) {
      json.operation = 'MULTIPLY'
      json.value2 = -1
    }

    if (className === 'RecruitSpell' && !json.cardLocation) {
      json.cardLocation = 'DECK'
    }

    if (className.endsWith('Modifier') && !json.target) {
      json.target = 'FRIENDLY_HAND'
    }

    if (className.endsWith('Aura') && !!json.triggers && json.triggers.length === 1) {
      json.trigger = json.triggers[0]
      delete json.triggers
    }

    return json
  }

  /**
   * Returns a list of the all the arguments in a block's json
   * that are are input args
   * @param block
   * @returns An array of the input args
   */
  static inputsList(block) {
    let inputsList = []
    for (let i = 0; i < 10; i++) {
      if (!!block['args' + i.toString()]) {
        for (let j = 0; j < 10; j++) {
          const arg = block['args' + i.toString()][j]
          if (!!arg && arg.type.includes('input')) {
            inputsList.push(arg)
          }
        }
      }
    }
    return inputsList
  }

  /**
   * Returns a list of the all the arguments in a block's json
   * that are are dropdown args
   * @param block
   * @returns An array of the dropdown args
   */
  static dropdownsList(block) {
    let inputsList = []
    for (let i = 0; i < 10; i++) {
      if (!!block['args' + i.toString()]) {
        for (let j = 0; j < 10; j++) {
          const arg = block['args' + i.toString()][j]
          if (!!arg && arg.type.includes('dropdown')) {
            inputsList.push(arg)
          }
        }
      }
    }
    return inputsList
  }

  /**
   * Returns a list of the all the arguments in a block's json
   * @param block
   * @returns An array of the args
   */
  static argsList(block) {
    let argsList = []
    for (let i = 0; i < 10; i++) {
      if (!!block['args' + i.toString()]) {
        for (let j = 0; j < 10; j++) {
          const arg = block['args' + i.toString()][j]
          if (!!arg) {
            argsList.push(arg)
          }
        }
      }
    }
    return argsList
  }
}