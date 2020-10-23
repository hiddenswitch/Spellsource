import Blockly from 'blockly'
import BlocklyMiscUtils from './blockly-misc-utils'

export default class BlocklyToolbox {

  static initCallbacks(workspace) {
    workspace.registerToolboxCategoryCallback('SPELLSOURCE_VARIABLES', (workspace) => {
      var xmlList = [];
      var button = document.createElement('button');
      button.setAttribute('text', Blockly.Msg['NEW_STRING_VARIABLE']);
      button.setAttribute('callbackKey', 'CREATE_VARIABLE_STRING');
      xmlList.push(button);
      button = document.createElement('button');
      button.setAttribute('text', Blockly.Msg['NEW_NUMBER_VARIABLE']);
      button.setAttribute('callbackKey', 'CREATE_VARIABLE_NUMBER');
      xmlList.push(button);
      button = document.createElement('button');
      button.setAttribute('text', 'Create entity variable...');
      button.setAttribute('callbackKey', 'CREATE_VARIABLE_ENTITY');
      xmlList.push(button);

      workspace.registerButtonCallback('CREATE_VARIABLE_STRING',
        Blockly.VariablesDynamic.onCreateVariableButtonClick_String);
      workspace.registerButtonCallback('CREATE_VARIABLE_NUMBER',
        Blockly.VariablesDynamic.onCreateVariableButtonClick_Number);
      workspace.registerButtonCallback('CREATE_VARIABLE_ENTITY',
        () => {
          Blockly.Variables.createVariableButtonHandler(workspace,
            undefined, 'EntityReference')
        });


      var blockList = Blockly.VariablesDynamic.flyoutCategoryBlocks(workspace);
      xmlList = xmlList.concat(blockList);
      return xmlList;
    })

  }

  static editorToolbox(results, workspace = null) {
    return {
      kind: 'categoryToolbox',
      contents: [
        this.category('Search Results', '#000000',
          "The relevant blocks from your search will appear here",
          results.map(value => this.getBlock(value.id))
        ),

        {
          "kind": "sep"
        },

        this.category('Card Starters', '#888888',
          "The core blocks that cards of any type will be built from",
          this.simpleContents('Starter')
        ),

        this.category('Card Properties', '#888888',
          "Blocks to add additional properties to your card (placed connected to your Starter)",
          this.simpleContents('Property')
        ),

        this.category('Rarities', '#888888',
          "Blocks for the different Rarities that cards can have",
          this.simpleContents('Rarity')
        ),

        this.category('Classes', '#888888',
          "Blocks for the different playable champion classes",
          this.simpleContents('HeroClass', 'WorkspaceHeroClass')
        ),

        this.category('Cards', '#888888',
          "Blocks for referencing the cards you make in the workspace (use 'Search Card Catalogue' to reference existing cards)",
          [
            {
              "kind": "label",
              "text": "Blocks for your Workspace Cards will appear here"
            },
            ...this.simpleContents('Card', 'WorkspaceCard')
          ]
        ),

        this.category('Targets', '30',
          "Blocks for the many different targets that effects can have",
          this.simpleContents('EntityReference')
        ),

        this.category('Choices', '60',
          "Blocks for the actions that a card can make the player take when being played",
          this.simpleContents('TargetSelection')
        ),

        this.category('Attributes', '200',
          "Blocks for the many properties of cards/entities",
          [
            this.category('Frequent', '200',
              'Attribute blocks that are commonly used on cards',
              this.subContents('Attribute', 'Frequent')
            ),

            this.category('Infrequent', '200',
              'Attribute blocks that are used uncommonly, but are still often useful',
              this.subContents('Attribute', 'Infrequent')
            ),

            this.category('Misc', '200',
              'Attribute blocks that are used rarely and nichely',
              this.subContents('Attribute', 'Misc')
            ),
          ]
        ),

        this.category('Players', '45',
          "Blocks for specifying (in different ways) the two players",
          [
            ...this.exclusionContents('TargetPlayer', 'TargetPlayer_1',
              'TargetPlayer_2'),
            {
              "kind": "label",
              "text": " "
            },
            {
              "kind": "label",
              "text": " "
            },
            {
              "kind": "label",
              "text": " "
            },
            this.getBlock('TargetPlayer_1'),
            this.getBlock('TargetPlayer_2')
          ]

        ),

        this.category('Tribes', '160',
          "Blocks for the different tribes that units can be a part of",
          this.simpleContents('Race')
        ),

        this.category('Spells', '260',
          "Blocks that are the actual effects cards can cause (not actually related to the 'Spell' card type)",
          [
            this.category('Buff', '260',
              "Spell blocks that deal with changing units' stats",
              this.subContents('Spell', 'Buff')
            ),

            this.category('Damage', '260',
              "Spell blocks that relate to dealing damage",
              this.subContents('Spell', 'Damage')
            ),

            this.category('Draw', '260',
              "Spell blocks that involve drawing/receiving cards",
              this.subContents('Spell', 'Draw')
            ),

            this.category('Summon', '260',
              "Spell blocks that have to do with the summoning of units",
              this.subContents('Spell', 'Summon')
            ),

            this.category('Shuffle', '260',
              "Spell blocks that handle card shuffling to decks",
              this.subContents('Spell', 'Shuffle')
            ),

            this.category('Cost', '260',
              "Spell blocks that create cost modification effects",
              this.subContents('Spell', 'Cost')
            ),

            this.category('Util', '260',
              "Spell blocks that help implement complex/combined effects",
              this.subContents('Spell', 'Util')
            ),

            this.category('Misc', '260',
              "Spell blocks that aren't otherwise categorized",
              this.subContents('Spell', 'Misc')
            ),
          ]
        ),

        this.category('Values', '340',
          "Blocks for anything and everything numeric",
          this.simpleContents('ValueProvider')
        ),

        this.category('Conditions', '100',
          "Blocks that CAN handle the truth, because they evaluate it",
          this.simpleContents('Condition')
        ),

        this.category('Filters', '120',
          "Blocks for narrowing down lists of entities based on desired properties",
          this.simpleContents('Filter')
        ),

        this.category('Enchantment', '280',
          "Blocks for the creation of ongoing triggered effects",
          this.simpleContents('Enchantment')
        ),

        this.category('Triggers', '300',
          "Blocks for waiting/listening for specific events in-game",
          [
            this.category('Unit', '300',
              "Trigger blocks that deal with units",
              this.subContents('Trigger', 'Unit')
            ),

            this.category('Card', '300',
              "Trigger blocks that relate to cards",
              this.subContents('Trigger', 'Card'))
            ,

            this.category('Attack', '300',
              "Trigger blocks that involve attacks / attacking",
              this.subContents('Trigger', 'Attack')
            ),

            this.category('Damage', '300',
              "Trigger blocks that have to do with damage being dealt",
              this.subContents('Trigger', 'Damage')
            ),

            this.category('Turn', '300',
              "Trigger blocks that handle turns starting / ending",
              this.subContents('Trigger', 'Turn')
            ),

            this.category('Util', '300',
              "Trigger blocks that help with reacting to more specific events",
              this.subContents('Trigger', 'Util')
            ),

            this.category('Misc', '300',
              "Trigger blocks that aren't otherwise categorized",
              this.subContents('Trigger', 'Misc')
            ),
          ]
        ),

        this.category('Auras', '230',
          "Blocks for the specific type of Enchantment of ongoing effects",
          this.simpleContents('Aura')
        ),

        this.category('Card Sources', '10',
          "Blocks for the different places that cards can be generated from",
          this.simpleContents('Source')
        ),

        this.category('Cost Modifier', '320',
          "Blocks for making more complex lun cost modification effects",
          this.simpleContents('CostModifier')
        ),

        {
          "kind": "sep"
        },

        this.category('Custom', '-1',
          "Blocks for representing your own effect from the old JSON system",
          [
            this.category('Custom Blocks', '#000000',
              "",
              this.subContents("Custom", "Desc")
            ),

            this.category('Custom Args', '#000000',
              "",
              this.subContents("Custom", "Arg")
            ),

            this.category('Custom Enums', '#000000',
              "",
              this.subContents("Custom", "Enum")
            )
          ]
        ),

        this.category('Simulation', -1,
          "Blocks for creating simulation tests to see if your cards work",
          [
            this.category('Test Starters', '#888888',
              "The blocks used to start a new test",
              this.simpleContents('TestStarter')
            ),

            this.category('Actions', '260',
              "Blocks for actions that you can simulate players taking",
              this.simpleContents('TestAction')
            ),

            this.category('Assertions', '260',
              "Blocks for actions that you can simulate players taking",
              this.simpleContents('TestAssertion')
            ),

            this.category('Logic', '210',
              "Blocks to help manage what actions are simulated",
              this.inclusionContents('logic',
                'controls_if', 'controls_ifelse')
            ),

            this.category('Loops', '120',
              "Blocks to facilitate simulating many actions iteratively",
              this.exclusionContents('controls',
                'controls_if', 'controls_ifelse')
            ),

            this.category('Math', '230',
              "Test",
              this.exclusionContents('math',
                'math_change')
            ),

            /* Not sure if these are actually going to be needed
            this.category('Text', '160',
              "Test",
              this.simpleContents('text')
            ),

            this.category('Lists', '260',
              "Test",
              this.simpleContents('list')
            ),
            */

            this.category('Variables', '310',
              "Blocks to save and refer back to specific targets/values in the simulation",
              null, 'SPELLSOURCE_VARIABLES')
          ]
        )
      ]
    }
  }

  static category(name, color, tooltip, contents, custom = null) {
    let category = {
      kind: 'category',
      name: name,
      colour: color,
      tooltip: tooltip
    }
    if (contents?.length) {
      category.contents = contents
    }
    if (custom) {
      category.custom = custom
    }
    return category
  }

  static subContents(prefix, sub) {
    let contents = []
    for (let block in Blockly.Blocks) {
      if (this.defaultTest(block) && block.startsWith(prefix)) {
        let subcategory = Blockly.Blocks[block].json?.subcategory
        if ((!subcategory && sub === 'Misc') || subcategory?.includes(sub)) {
          contents.push(this.getBlock(block))
        }
      }
    }
    return contents
  }

  static simpleContents(prefix, prefix2 = null) {
    let contents = []
    for (let block in Blockly.Blocks) {
      if (this.defaultTest(block) && (block.startsWith(prefix) || (!!prefix2 && block.startsWith(prefix2)))) {
        contents.push(this.getBlock(block))
      }
    }
    return contents
  }

  static exclusionContents(prefix, ...exclusions) {
    let contents = []
    for (let block in Blockly.Blocks) {
      if (this.defaultTest(block) && block.startsWith(prefix) && !exclusions.includes(block)) {
        contents.push(this.getBlock(block))
      }
    }
    return contents
  }

  static inclusionContents(prefix, ...inclusions) {
    let contents = []
    for (let block in Blockly.Blocks) {
      if (this.defaultTest(block) && (block.startsWith(prefix) || inclusions.includes(block))) {
        contents.push(this.getBlock(block))
      }
    }
    return contents
  }
  
  static defaultTest(block) {
    return !block.endsWith('SHADOW') && (!block.match(/^.*_.*_.*/) || BlocklyMiscUtils.isSpellsourceBlock(block))
  }

  static getBlock(type) {
    let block = Blockly.Blocks[type]
    return {
      kind: 'block',
      type: type,
      contents: this.blockContents(type)
    }
  }

  static xmlToJson(element) {
    return {
      "kind": "block",
      "blockxml": element.outerHTML
    }
  }


  //Turns our own json formatting for shadow blocks into the formatting
  //that's used for specifying toolbox categories (recursively)
  static blockContents (type) {
    let block = Blockly.Blocks[type]
    let contents = []
    if (!!block && !!block.json) {
      let json = block.json
      for (let i = 0; i < 10; i++) {
        if (!!json['args' + i.toString()]) {
          for (let j = 0; j < 10; j++) {
            const arg = json['args' + i.toString()][j]
            if (!!arg && !!arg.shadow) {
              let shadowContents = []
              if (!!arg.shadow.fields) {
                for (let field of arg.shadow.fields) {
                  shadowContents.push({
                    kind: 'field',
                    name: field.name,
                    value: field.valueI || field.valueS || field.valueB
                  })
                }
              }
              shadowContents.push(this.blockContents(arg.shadow.type))

              contents.push({
                kind: 'value',
                name: arg.name,
                contents: [
                  {
                    kind: arg.shadow.notActuallyShadow ? 'block' : 'shadow',
                    type: arg.shadow.type,
                    contents: shadowContents
                  }
                ]
              })
            }
          }
        }
      }
    }
    return contents
  }

}