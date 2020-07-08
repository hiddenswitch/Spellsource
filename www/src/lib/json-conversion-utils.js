import Blockly, {isNumber} from "blockly";

export default class JsonConversionUtils {
  static classBlocksDictionary = {}
  static enumBlocksDictionary = {}
  static allArgNames = new Set()

  static addBlockToMap(block) {
    if (block.type.endsWith('SHADOW')) {
      return
    }
    let list = this.argsList(block)
    if (list.length > 0) {
      let className = null

      for (let arg of list) {
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


  static newBlock(workspace, type) {
    let block = workspace.newBlock(type)
    this.manuallyAddShadowBlocks(block, Blockly.Blocks[type].json)
    return block
  }

  static generateCard(workspace, card) {
    let block = this.newBlock(workspace, 'Starter_' + card.type)
    let args = ['baseManaCost', 'name', 'baseAttack', 'baseHp', 'description']
    args.forEach(arg => {
      if (!!card[arg] && !!block.getField(arg)) {
        block.getField(arg).setValue(card[arg])
      }
    })
    block.initSvg()

    let lowestBlock = block

    if (!!card.heroClass) {
      let heroClassBlock = workspace.newBlock('HeroClass_' + card.heroClass)
      block.getInput('heroClass').connection.connect(heroClassBlock.outputConnection)
      heroClassBlock.initSvg()
    }

    if (!!card.rarity) {
      let rarityBlock = workspace.newBlock('Rarity_' + card.rarity)
      block.getInput('rarity').connection.connect(rarityBlock.outputConnection)
      rarityBlock.initSvg()
    }

    this.targetSelection(block, card, workspace)

    if (!!card.spell) {
      this.handleArg(block.getInput('spell').connection, card.spell, 'spell', workspace)
    }

    if (!!card.battlecry) {
      let openerBlock = null
      if (!!card.battlecry.condition) {
        openerBlock = this.newBlock(workspace, 'Property_opener2')
      } else {
        openerBlock = this.newBlock(workspace, 'Property_opener1')
      }
      lowestBlock.nextConnection.connect(openerBlock.previousConnection)
      openerBlock.initSvg()

      this.targetSelection(openerBlock, card.battlecry, workspace)
      this.handleArg(openerBlock.getInput('battlecry.spell').connection, card.battlecry.spell, 'battlecry.spell', workspace, card.battlecry)

      if (!!card.battlecry.condition) {
        this.handleArg(openerBlock.getInput('battlecry.condition').connection, card.battlecry.condition, 'battlecry.condition', workspace, card.battlecry)
      }
      lowestBlock = openerBlock
    }

    if (!!card.deathrattle) {
      let aftermathBlock = this.newBlock(workspace, 'Property_aftermath')
      lowestBlock.nextConnection.connect(aftermathBlock.previousConnection)
      aftermathBlock.initSvg()
      this.handleArg(aftermathBlock.getInput('deathrattle').connection, card.deathrattle, 'deathrattle', workspace, card)
      lowestBlock = aftermathBlock
    }

    if (!!card.triggers || !!card.trigger) {
      let triggersBlock = this.newBlock(workspace, 'Property_triggers')
      lowestBlock.nextConnection.connect(triggersBlock.previousConnection)
      triggersBlock.initSvg()
      this.enchantments(triggersBlock, card, 'triggers', workspace)
      lowestBlock = triggersBlock
    }

    if (!!card.auras || !!card.aura) {
      let aurasBlock = this.newBlock(workspace, 'Property_auras')
      lowestBlock.nextConnection.connect(aurasBlock.previousConnection)
      aurasBlock.initSvg()
      this.auras(aurasBlock, card, workspace)
      lowestBlock = aurasBlock
    }

    if (!!card.attributes) {
      if (!!card.attributes.BATTLECRY) {
        delete card.attributes.BATTLECRY
      }
      if (!!card.attributes.DEATHRATTLES) {
        delete card.attributes.DEATHRATTLES
      }
      if (Object.values(card.attributes).length > 0) {
        let attributesBlock = this.newBlock(workspace, 'Property_attributes')
        lowestBlock.nextConnection.connect(attributesBlock.previousConnection)
        attributesBlock.initSvg()
        let lowestConnection = attributesBlock.getFirstStatementConnection()
        for (let atr in card.attributes) {
          let attributeBlock
          if (isNumber(card.attributes[atr])) {
            attributeBlock = this.newBlock(workspace, 'Property_attributes_int')
            attributesBlock.setFieldValue(card.attributes[atr], 'value')
          } else {
            attributeBlock = this.newBlock(workspace, 'Property_attributes_boolean')
          }
          this.handleArg(attributeBlock.getInput('attribute').connection, atr, 'attribute', workspace, card.attributes)
          attributeBlock.initSvg()
          lowestConnection.connect(attributeBlock.previousConnection)
          lowestConnection = attributeBlock.nextConnection
        }

        lowestBlock = attributesBlock
      }
    }

    workspace.render()

    return block
  }

  static getInput(block, inputName) {
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

  static enchantments(block, json, inputName, workspace) {
    let triggers
    if (!!json.trigger && inputName === 'triggers') {
      triggers = [json.trigger]
    } else {
      triggers = json[inputName]
    }
    let lowestConnection = block.getFirstStatementConnection()
    for (let trigger of triggers) {
      let props = this.relevantProperties(trigger);
      let match = this.getMatch(trigger.eventTrigger, 'eventTrigger', trigger);
      let triggerBlock
      if (props.length === 2 && !!trigger.eventTrigger && !!trigger.spell && !!match
      && !trigger.eventTrigger.fireCondition && !trigger.eventTrigger.queueCondition) {
        triggerBlock = this.newBlock(workspace, 'Enchantment')
      }
      else {
        triggerBlock = this.newBlock(workspace, 'EnchantmentOptions')
        let lowestOptionConnection = triggerBlock.getFirstStatementConnection()
        for (let prop of props) {
          if (prop === 'spell' || prop === 'eventTrigger') {
            continue
          }
          this.handleArg(lowestOptionConnection, json[prop], 'option', workspace, json, true)
          lowestOptionConnection = lowestOptionConnection.targetBlock().nextConnection
        }
        if (!!trigger.eventTrigger.fireCondition) {
          let optionConditionBlock = this.newBlock(workspace, 'EnchantmentOption_fireCondition')
          this.handleArg(optionConditionBlock.getInput('eventTrigger.fireCondition').connection, trigger.eventTrigger.fireCondition, 'fireCondition', workspace, json.eventTrigger)
          lowestOptionConnection.connect(optionConditionBlock.previousConnection)
          optionConditionBlock.initSvg()
          lowestOptionConnection = optionConditionBlock.nextConnection
        }
        if (!!trigger.eventTrigger.queueCondition) {
          let optionConditionBlock = this.newBlock(workspace, 'EnchantmentOption_queueCondition')
          this.handleArg(optionConditionBlock.getInput('eventTrigger.queueCondition').connection, trigger.eventTrigger.queueCondition, 'queueCondition', workspace, json.eventTrigger)
          lowestOptionConnection.connect(optionConditionBlock.previousConnection)
          optionConditionBlock.initSvg()
          lowestOptionConnection = optionConditionBlock.nextConnection
        }
      }
      this.handleArg(triggerBlock.getInput('spell').connection, trigger.spell, 'spell', workspace, trigger)
      this.handleArg(triggerBlock.getInput('eventTrigger').connection, trigger.eventTrigger, 'eventTrigger', workspace, trigger)

      lowestConnection.connect(triggerBlock.previousConnection)
      lowestConnection = triggerBlock.nextConnection
      triggerBlock.initSvg()
    }
  }

  static auras(block, json, workspace) {
    let auras
    if (!!json.aura) {
      auras = [json.aura]
    } else {
      auras = json['auras']
    }
    let lowestConnection = block.getFirstStatementConnection()
    for (let aura of auras) {
      this.handleArg(lowestConnection, aura, 'aura', workspace, auras, true)
      lowestConnection = lowestConnection.targetBlock().nextConnection
    }
  }

  static targetSelection(block, json, workspace) {
    let input = this.getInput(block, 'targetSelection')
    if (input != null) {
      if (!!json.targetSelection) {
        if (!!json.spell && !!json.spell.filter) {
          let match = this.getMatch(json.targetSelection, 'targetSelection', json);
          let real
          if (json.spell.filter.class === 'RaceFilter') {
            if (match.data === 'FRIENDLY_MINIONS') {
              real = this.newBlock(workspace, 'TargetSelection_FRIENDLY_RACE')
            }

            this.handleArg(real.getInput('super.spell.filter.race').connection, json.spell.filter.race, 'race', workspace, json.spell.filter)
          } else {

          }
          input.connection.connect(real.outputConnection)
          real.initSvg()

        } else {
          this.handleArg(input.connection, json.targetSelection, 'targetSelection', workspace, json)
        }
      } else {
        let targetSelection = this.newBlock(workspace, 'TargetSelection_NONE')
        input.connection.connect(targetSelection.outputConnection)
        targetSelection.initSvg()
      }
    }
  }

  static getMatch(json, inputName, parentJson) {
    let matches = null
    let bestMatch = null
    if (typeof json !== 'object') {
      let lookingForType = null
      if (inputName === 'target') {
        lookingForType = 'EntityReference'
      }
      if (inputName === 'race') {
        lookingForType = 'Race'
      }
      if (inputName === 'targetPlayer') {
        lookingForType = 'TargetPlayer'
      }
      if (inputName === 'heroClass') {
        lookingForType = 'HeroClass'
      }
      if (inputName === 'attribute') {
        lookingForType = 'Attribute'
        json = json.toString().replace('AURA_', '')
      }
      if (inputName === 'targetSelection') {
        lookingForType = 'TargetSelection'
      }
      matches = this.enumBlocksDictionary[json]
      if (!matches || matches.length === 0) {
        return
      }
      for (let match of matches) {
        if (!lookingForType || match.type.startsWith(lookingForType)) {
          return match
        }
      }
    } else if (!!json.class) {
      let className = json.class
      matches = this.classBlocksDictionary[className]
      if (!matches || matches.length === 0) {
        return
      }
      let relevantProperties = this.relevantProperties(json)
      let goodMatches = []
      for (let match of matches) { //for each possible match
        let reallyHasIt = true
        for (let property of relevantProperties) { //check the json's relevant properties
          if (relevantProperties.includes('target') && property === 'filter') {
            continue
          }
          if (className.endsWith('Spell') && property === 'targetPlayer') {
            continue
          }
          let hasIt = false
          for (let arg of this.argsList(match)) { //see if the match has a corresponding prop
            if (arg.name.startsWith(property)) {
              hasIt = true
              break
            }
          }
          if (!hasIt) { //if it doesn't, it's not good enough
            reallyHasIt = false
            break
          }
        }
        if (reallyHasIt) { //if it covers all the json's properties, it's good enough
          goodMatches.push(match)
        }
      }
      let fewest = null
      for (let goodMatch of goodMatches) { //choose the one with the fewest extra properties
        let argsList = this.argsList(goodMatch);
        let extras = 0
        for (let arg of argsList) {
          let hasIt = false
          for (let property of relevantProperties) {
            if (arg.name.startsWith(property)) {
              hasIt = true
              break
            }
          }
          if (!hasIt) {
            extras++
          }
        }
        if (!fewest || extras < fewest) {
          bestMatch = goodMatch
          fewest = extras
        }
      }
    }
    return bestMatch
  }

  static handleArg(connection, json, inputName, workspace, parentJson, statement = false) {
    json = this.mutateJson(json)
    let bestMatch = this.getMatch(json, inputName, parentJson)
    if (!!bestMatch) {
      let newBlock = this.newBlock(workspace, bestMatch.type)
      if (json.hasOwnProperty('targetPlayer') && bestMatch.output === 'SpellDesc') {
        let realBlock = this.newBlock(workspace, 'Spell_TargetPlayer')
        realBlock.getInput('super').connection.connect(newBlock.outputConnection)
        this.handleArg(realBlock.getInput('targetPlayer').connection, json.targetPlayer, 'targetPlayer', workspace, json)
        connection.connect(realBlock.outputConnection)
        realBlock.initSvg()
      } else if (inputName === 'target' && !!parentJson.target && !!parentJson.filter) {
        let filterBlock = this.newBlock(workspace, 'EntityReference_FILTER')
        filterBlock.getInput('super').connection.connect(newBlock.outputConnection)
        this.handleArg(filterBlock.getInput('super.filter').connection, parentJson.filter, 'filter', workspace, json)
        connection.connect(filterBlock.outputConnection)
        filterBlock.initSvg()
      } else {
        if (statement) {
          connection.connect(newBlock.previousConnection)
        } else {
          connection.connect(newBlock.outputConnection)
        }
      }
      newBlock.initSvg()

      for (let inputArg of this.inputsList(bestMatch)) {
        if (inputArg.name === 'target' && json.hasOwnProperty('randomTarget')) {
          let randomBlock = this.newBlock(workspace, 'EntityReference_RANDOM')
          this.handleArg(randomBlock.getInput('super').connection, json[inputArg.name], inputArg.name, workspace, json)
          newBlock.getInput(inputArg.name).connection.connect(randomBlock.outputConnection)
          randomBlock.initSvg()
        } else if (json.hasOwnProperty(inputArg.name)) {
          if (typeof json[inputArg.name] !== 'object' && (inputArg.name === 'value' || inputArg.name === 'attackBonus'
            || inputArg.name === 'hpBonus' || inputArg.name === 'howMany'
          )) {
            let valueBlock
            if (!!newBlock.getInput(inputArg.name).connection.targetBlock()
              && newBlock.getInput(inputArg.name).connection.targetBlock().type === 'ValueProvider_int') {
              valueBlock = newBlock.getInput(inputArg.name).connection.targetBlock()
            } else {
              valueBlock = this.newBlock(workspace, 'ValueProvider_int')
              newBlock.getInput(inputArg.name).connection.connect(valueBlock.outputConnection)
              newBlock.initSvg()
            }
            valueBlock.setFieldValue(json[inputArg.name], 'int')
          } else if (inputArg.name === 'spells' || inputArg.name === 'conditions'
          || inputArg.name === 'filters') {
            let thingArray = json[inputArg.name]
            let lowestBlock = newBlock.getFirstStatementConnection().targetBlock()
            this.handleArg(lowestBlock.getInput('i').connection, thingArray[0], 'i', workspace, thingArray)
            for (let i = 1; i < thingArray.length; i++) {
              let thingI
              switch (inputArg.name) {
                case 'conditions':
                  thingI = this.newBlock(workspace, 'Condition_I')
                  break
                case 'filters':
                  thingI = this.newBlock(workspace, 'Filter_I')
                  break
                default:
                  thingI = this.newBlock(workspace, 'Spell_Meta')
                  break
              }
              this.handleArg(thingI.getInput('i').connection, thingArray[i], 'i', workspace, thingArray)
              lowestBlock.nextConnection.connect(thingI.previousConnection)
              thingI.initSvg()
              lowestBlock = thingI
            }

          } else {
            this.handleArg(newBlock.getInput(inputArg.name).connection, json[inputArg.name], inputArg.name, workspace, json)
          }
        }
      }

      for (let dropdown of this.dropdownsList(bestMatch)) {
        if (json.hasOwnProperty(dropdown.name)) {
          newBlock.setFieldValue(json[dropdown.name], dropdown.name)
        }
      }
    }
  }

  static relevantProperties(json) {
    let relevantProperties = []
    for (let property in json) {
      if ((property === 'randomTarget' && !!json.target) || property === 'class'
      || property === 'fireCondition' || property === 'queueCondition') {
        continue
      }
      relevantProperties.push(property)
    }
    return relevantProperties
  }

  static mutateJson(json) {
    if (typeof json !== 'object') {
      return json
    }
    let className = json.class
    let props = this.relevantProperties(json);
    if (className.endsWith('Filter') || className.endsWith('Condition')) {
      if (props.includes('attribute') && props.includes('operation')
        && !props.includes('value') && json.operation === 'HAS') {
        delete json.operation
      }
    }
    if (className === 'CardFilter') {
      if (props.length === 1) {
        if (props[0] === 'race') {
          return {
            class: 'RaceFilter',
            race: json.race
          }
        }
      } else if (!(props.length === 2 && props.includes('attribute') && (props.includes('value') || props.includes('operation')))
      && !(props.length === 3 && props.includes('attribute') && props.includes('operation') && props.includes('value'))
      ) {
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
          if (!json.value && (!json.operation || json.operation === 'HAS')) {
            if ((this.enumBlocksDictionary[json.attribute].output === 'BoolAttribute')) {
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
        return {
          class: 'AndFilter',
          filters: filters
        }
      }
    } else if (className === 'FromDeckToHandSpell') {
      json.class = 'DrawCardSpell'
    } else if (className.endsWith('Aura') && !!json.triggers) {
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
    if (json.targetPlayer === 'SELF' && !json.class.endsWith('Trigger')) {
      delete json.targetPlayer
    }

    return json
  }


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


  static manuallyAddShadowBlocks(thisBlock, block) {
    for (let i = 0; i < 10; i++) {
      if (!!block['args' + i.toString()]) {
        for (let j = 0; j < 10; j++) {
          const arg = block['args' + i.toString()][j]
          if (!!arg) {
            const shadow = arg.shadow
            if (!!shadow) {
              let shadowBlock = this.newBlock(thisBlock.workspace, shadow.type)
              if (shadow.notActuallyShadow) {
                shadowBlock.setMovable(false)
              } else {
                shadowBlock.setShadow(true)
              }
              if (!!shadow.fields) {
                for (let field of shadow.fields) {
                  if (field.valueI !== null) {
                    shadowBlock.setFieldValue(field.valueI, field.name)
                  }
                  if (field.valueS !== null) {
                    shadowBlock.setFieldValue(field.valueS, field.name)
                  }
                  if (field.valueB !== null) {
                    shadowBlock.setFieldValue(field.valueB, field.name)
                  }
                }
              }
              const connection = arg.type.endsWith('statement') ?
                shadowBlock.previousConnection : shadowBlock.outputConnection
              thisBlock.getInput(arg.name).connection.connect(connection)
              shadowBlock.initSvg()
            }
          }
        }
      }
    }


    if (block.type.startsWith('Starter')) {
      let shadowBlock = thisBlock.workspace.newBlock('Property_SHADOW')
      shadowBlock.setShadow(true)
      thisBlock.nextConnection.connect(shadowBlock.previousConnection)
      shadowBlock.initSvg()
    }
  }



}