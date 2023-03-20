import Blockly from 'blockly'
import BlocklyMiscUtils from './blockly-misc-utils'
import ToolboxInfo = Blockly.utils.toolbox.ToolboxInfo;

export default class BlocklyToolbox {

  /**
   * Initializes the necessary callbacks for the Variables tab's CUSTOM dynamic-ness
   * @param workspace
   */
  static initCallbacks(workspace) {
    workspace.registerToolboxCategoryCallback('SPELLSOURCE_VARIABLES', (workspace) => {
      var xmlList = [];
      var button = document.createElement('button');
      button.setAttribute('text', 'Create entity variable...');
      button.setAttribute('callbackKey', 'CREATE_VARIABLE_ENTITY');
      xmlList.push(button);

      button = document.createElement('button');
      button.setAttribute('text', Blockly.Msg['NEW_STRING_VARIABLE']);
      button.setAttribute('callbackKey', 'CREATE_VARIABLE_STRING');
      xmlList.push(button);

      button = document.createElement('button');
      button.setAttribute('text', Blockly.Msg['NEW_NUMBER_VARIABLE']);
      button.setAttribute('callbackKey', 'CREATE_VARIABLE_NUMBER');
      xmlList.push(button);

      workspace.registerButtonCallback('CREATE_VARIABLE_STRING',
        Blockly.VariablesDynamic["onCreateVariableButtonClick_String"]);
      workspace.registerButtonCallback('CREATE_VARIABLE_NUMBER',
        Blockly.VariablesDynamic["onCreateVariableButtonClick_Number"]);
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

  /**
   * Constructs the toolbox JSON for the card editor
   * @param results The current search results
   * @returns toolbox json
   */
  static editorToolbox(results = []): any  {
    return {
      kind: 'categoryToolbox',
      contents: [
        this.searchResultsCategory(results),

        {
          "kind": "sep"
        },

        this.category('Card Starters', '#888888',
          "The core blocks that cards of any type will be built from",
          this.contents('Starter')
        ),

        this.category('Card Properties', '#888888',
          "Blocks to add additional properties to your card (placed connected to your Starter)",
          this.contents('Property')
        ),

        this.category('Card Art', '#888888',
          "Blocks representing the art that your card can have",
          [
            this.category('Unused', '#888888', "Art that hasn't yet been used by a card",
              this.artContents(false)
            ),
            this.category('Used', '#888888', "Art that's been used by cards already",
              this.artContents(true)
            ),
            this.category('All', '#888888', "All available art",
              this.artContents(null)
            )
          ]
        ),

        {
          "kind": "sep"
        },

        this.category('Rarities', '#888888',
          "Blocks for the different Rarities that cards can have",
          this.contents('Rarity')
        ),

        this.classesCategory(),

        this.cardsCategory(),

        this.category('Targets', '30',
          "Blocks for the many different targets that effects can have",
          [
            this.category('Ally', '30',
              'Blocks that target allied things',
              this.subContents('EntityReference', 'Ally')
            ),

            this.category('Enemy', '30',
              'Blocks that target enemy things',
              this.subContents('EntityReference', 'Enemy')
            ),

            this.category('Both', '30',
              'Blocks that target both allied and enemy things',
              this.subContents('EntityReference', 'Both')
            ),

            this.category('Misc', '30',
              'Blocks that target other things',
              this.subContents('EntityReference', 'Misc')
            ),

            this.category('Helper', '30',
              'Blocks that help you target specific/niche things',
              this.subContents('EntityReference', 'Util')
            )
          ]
        ),



        this.category('Choices', '60',
          "Blocks for the actions that a card can make the player take when being played",
          this.contents('TargetSelection')
        ),

        this.category('Attributes', '200',
          "Blocks for the many properties of cards/entities",
          [
            this.category('Primary', '200',
              'Attribute blocks that are frequently used on cards',
              this.subContents('Attribute', 'Frequent')
            ),

            this.category('Secondary', '200',
              "Attribute blocks that aren't commonly put on cards directly, but rather are used by other cards",
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
          this.contents('Race')
        ),

        {
          "kind": "sep"
        },

        this.category('Spells', '260',
          "Blocks that are the actual effects cards can cause (not actually related to the 'Spell' card type)",
          [
            this.category('Buff', '260',
              "Spell blocks that deal with changing units' stats",
              this.subContents('Spell', 'Buff')
            ),

            this.category('Damage', '260',
              "Spell blocks that relate to dealing damage (and also the one block for healing)",
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

            this.category('Mechanics', '260',
              "Spell blocks that employ specific custom mechanics",
              this.subContents('Spell', 'Mechanic')
            ),

            this.category('Enchantment', '260',
              "Spell blocks that add/remove enchantments/auras",
              this.subContents('Spell', 'Enchant')
            ),

            this.category('Attacking', '260',
              "Spell blocks about enities attacking",
              this.subContents('Spell', 'Attack')
            ),

            this.category('Misc', '260',
              "Spell blocks that aren't otherwise categorized",
              this.subContents('Spell', 'Misc')
            ),

            this.category('Helper', '260',
              "Spell blocks that help implement complex/combined effects",
              this.subContents('Spell', 'Util')
            ),
          ]
        ),

        this.category('Values', '340',
          "Blocks for anything and everything numeric",
          [
            this.category('Number of ...', '340',
              "Blocks for counting the number of things",
              this.subContents('ValueProvider', 'Number')
            ),

            this.category('Properties', '340',
              "Blocks for numerical properties of things",
              this.subContents('ValueProvider', 'Properties')
            ),

            this.category('Misc', '340',
              "Blocks for other values",
              this.subContents('ValueProvider', 'Misc')
            ),

            this.category('Helper', '340',
              "Blocks for calculating more advanced values",
              this.subContents('ValueProvider', 'Util')
            )
          ]
        ),



        this.category('Conditions', '100',
          "Blocks that CAN handle the truth, because they evaluate it",
          this.contents('Condition')
        ),

        this.category('Filters', '120',
          "Blocks for narrowing down lists of entities based on desired properties",
          this.contents('Filter')
        ),

        this.category('Enchantment', '280',
          "Blocks for the creation of ongoing triggered effects",
          this.contents('Enchantment')
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

            this.category('Misc', '300',
              "Trigger blocks that aren't otherwise categorized",
              this.subContents('Trigger', 'Misc')
            ),

            this.category('Helper', '300',
              "Trigger blocks that help with reacting to more specific events",
              this.subContents('Trigger', 'Util')
            ),
          ]
        ),

        this.category('Auras', '230',
          "Blocks for the specific type of Enchantment of ongoing effects",
          this.contents('Aura')
        ),

        this.category('Card Sources', '10',
          "Blocks for the different places that cards can be generated from",
          this.contents('Source')
        ),

        this.category('Cost Modifier', '320',
          "Blocks for making more complex lun cost modification effects",
          this.contents('CostModifier')
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
            this.category('Testing', '#888888',
              "The blocks used to start a new test",
              this.inclusionContents('TestStarter',
                'TestAssertion')
            ),

            this.category('Actions', '260',
              "Blocks for actions that you can simulate players taking",
              this.contents('TestAction')
            ),

            this.category('Variables', '310',
              "Blocks to save and refer back to specific targets/values in the simulation",
              null, {custom: 'SPELLSOURCE_VARIABLES'}),

            {
              "kind": "sep"
            },

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
              "Blocks for doing math in regards to actions. Don't try to use these blocks for cards or effects other than test actions.",
              this.exclusionContents('math', 'math_change', 'math_trig', 'math_constant', 'math_atan2')
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
          ]
        )
      ]
    }
  }

  /**
   * Specifically creates the JSON for the "Search Results" category.
   * Defined here so that the category can be easily updated for new search results
   * @returns category json
   */
  static searchResultsCategory(results) {
    return this.category('Search Results', '#000000',
      "The relevant blocks from your search will appear here",
      results.map(value => this.getBlock(value.id)), {toolboxitemid: 'Search Results'}
    )
  }

  /**
   * Specifically creates the JSON for the "Classes" category.
   * Defined here so that the category can be easily updated for new WorkspaceHeroClasss
   * @returns category json
   */
  static classesCategory() {
    let workspaceHeroClasses = this.contents('WorkspaceHeroClass')
    let contents
    if (workspaceHeroClasses.length > 0) {
      contents = [
        ...workspaceHeroClasses,
        {
          "kind": "label",
          "text": " "
        },
        ...this.contents('HeroClass')
      ]
    } else {
      contents = this.contents('HeroClass')
    }

    return this.category('Classes', '#888888',
      "Blocks for the different playable champion classes",
      contents, {toolboxitemid: 'Classes'}
    )
  }

  /**
   * Specifically creates the JSON for the "Cards" category.
   * Defined here so that the category can be easily updated for new WorkspaceCards
   * @returns category json
   */
  static cardsCategory() {
    return this.category('Cards', '#888888',
      "Blocks for referencing the cards you make in the workspace (use 'Search Card Catalogue' to reference existing cards)",
      [
        {
          "kind": "label",
          "text": "Blocks for your Workspace Cards will appear here"
        },
        ...this.contents('Card'),
        ...this.contents('WorkspaceCard')
      ], {toolboxitemid: 'Cards'}
    )
  }

  /**
   * Creates the blockly toolbox JSON for a new category
   * @param name The category's name
   * @param color The category's color
   * @param tooltip The category's tooltip
   * @param contents The contents of the category
   * @param props The toolbox CUSTOM field, if it needs one
   * @returns category json
   */
  static category(name, color, tooltip, contents, props = null): any { // TODO category type
    let category: any = {
      kind: 'category',
      name: name,
      colour: color,
      tooltip: tooltip
    }
    if (contents?.length) {
      category.contents = contents
    }
    if (!!props) {
      for (let propsKey in props) {
        category[propsKey] = props[propsKey]
      }
    }
    return category
  }

  /**
   * Gets all valid blocks that start with a prefix and are part of a certain subcategory
   * @param prefix The prefix to check from
   * @param sub The subcategory to match
   * @returns [category json blocks]
   */
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

  /**
   * Get all valid blocks that start with a prefix
   * @param prefix The prefix to check from
   * @returns [category json blocks]
   */
  static contents(prefix) {
    let contents = []
    for (let block in Blockly.Blocks) {
      if (this.defaultTest(block) && (block.startsWith(prefix))) {
        contents.push(this.getBlock(block))
      }
    }
    return contents
  }

  /**
   * Get all valid blocks that start with a prefix, minus some exclusions by type
   * @param prefix The prefix to check from
   * @param exclusions Specific blocks to not include
   * @returns [category json blocks]
   */
  static exclusionContents(prefix, ...exclusions) {
    let contents = []
    for (let block in Blockly.Blocks) {
      if (this.defaultTest(block) && block.startsWith(prefix) && !exclusions.includes(block)) {
        contents.push(this.getBlock(block))
      }
    }
    return contents
  }

  /**
   * Get all valid blocks that start with a prefix, plus some extras by type
   * @param prefix The prefix to check from
   * @param inclusions Additional blocks to include
   * @returns [category json blocks]
   */
  static inclusionContents(prefix, ...inclusions) {
    let contents = []
    for (let block in Blockly.Blocks) {
      if (this.defaultTest(block) && (block.startsWith(prefix) || inclusions.includes(block))) {
        contents.push(this.getBlock(block))
      }
    }
    return contents
  }

  static artContents(used) {
    let contents = []
    for (let block in Blockly.Blocks) {
      if (this.defaultTest(block) && block.startsWith('Art_') &&
        (used === !!Blockly.Blocks[block].used || used === null)) {
        contents.push(this.getBlock(block))
      }
    }
    return contents
  }

  /**
   * All blocks that appear in the toolbox should pass this, which means:
   *  - Their type doesn't end in shadow
   *  - If they're a default blockly block, then their type can't have two '_'s in it
   *      (because of the blocks used in mutators)
   * @param block The block type to check
   * @returns boolean
   */
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
              shadowContents.push(...this.blockContents(arg.shadow.type))

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