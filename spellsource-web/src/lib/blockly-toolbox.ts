import Blockly, { WorkspaceSvg } from "blockly";
import * as BlocklyMiscUtils from "./blockly-spellsource-utils";
import { toTitleCaseCorrected } from "./blockly-spellsource-utils";
import { ContextType } from "react";
import { BlocklyDataContext } from "../pages/card-editor";
import { CardDef } from "../components/collection/card-display";
import { ImageDef } from "../__generated__/client";
import { ToolboxSearchCategory } from "../components/blockly/toolbox-search-category";
import { CardSearchCategory } from "../components/blockly/card-search-category";
import { getBlockInfo } from "./blockly-utils";
import StaticCategoryInfo = Blockly.utils.toolbox.StaticCategoryInfo;
import ToolboxInfo = Blockly.utils.toolbox.ToolboxInfo;
import ToolboxItemInfo = Blockly.utils.toolbox.ToolboxItemInfo;

/**
 * Initializes the necessary callbacks for the Variables tab's CUSTOM dynamic-ness
 * @param workspace
 */
export function initCallbacks(workspace: WorkspaceSvg) {
  workspace.registerToolboxCategoryCallback("SPELLSOURCE_VARIABLES", (workspace) => {
    var xmlList: Element[] = [];
    var button = document.createElement("button");
    button.setAttribute("text", "Create entity variable...");
    button.setAttribute("callbackKey", "CREATE_VARIABLE_ENTITY");
    xmlList.push(button);

    button = document.createElement("button");
    button.setAttribute("text", Blockly["Msg"]["NEW_STRING_VARIABLE"]);
    button.setAttribute("callbackKey", "CREATE_VARIABLE_STRING");
    xmlList.push(button);

    button = document.createElement("button");
    button.setAttribute("text", Blockly["Msg"]["NEW_NUMBER_VARIABLE"]);
    button.setAttribute("callbackKey", "CREATE_VARIABLE_NUMBER");
    xmlList.push(button);

    workspace.registerButtonCallback(
      "CREATE_VARIABLE_STRING",
      Blockly.VariablesDynamic["onCreateVariableButtonClick_String"]
    );
    workspace.registerButtonCallback(
      "CREATE_VARIABLE_NUMBER",
      Blockly.VariablesDynamic["onCreateVariableButtonClick_Number"]
    );
    workspace.registerButtonCallback("CREATE_VARIABLE_ENTITY", () => {
      Blockly.Variables.createVariableButtonHandler(workspace, undefined, "EntityReference");
    });

    var blockList = Blockly.VariablesDynamic.flyoutCategoryBlocks(workspace);
    xmlList = xmlList.concat(blockList);
    return xmlList;
  });
}

/**
 * Constructs the toolbox JSON for the card editor
 * @param results The current search results
 * @param data
 * @returns toolbox json
 */
export function editorToolbox(results: string[] = [], data: ContextType<typeof BlocklyDataContext>): ToolboxInfo {
  return {
    kind: "categoryToolbox",
    contents: [
      {
        kind: ToolboxSearchCategory.registrationName,
        name: "Search Blocks",
        colour: "rgb(85, 119, 238)",
        contents: [],
      },

      {
        kind: CardSearchCategory.registrationName,
        name: "Search Cards",
        colour: "rgb(85, 119, 238)",
        contents: [],
      },

      myCardsCategory(data),

      {
        kind: "sep",
      },

      category(
        "Card Starters",
        "#888888",
        "The core blocks that cards of any type will be built from",
        contents("Starter")
      ),

      category(
        "Card Properties",
        "#888888",
        "Blocks to add additional properties to your card (placed connected to your Starter)",
        contents("Property")
      ),

      category("Card Art", "#888888", "Blocks representing the art that your card can have", [
        /*category("Unused", "#888888", "Art that hasn't yet been used by a card", artContents(false)),
                                                                category("Used", "#888888", "Art that's been used by cards already", artContents(true)),
                                                                category("All", "#888888", "All available art", artContents(null)),*/
        generatedArtCategory(data),
        ...artCategories(),
      ]),

      {
        kind: "sep",
      },
      category("Rarities", "#888888", "Blocks for the different Rarities that cards can have", contents("Rarity")),

      classesCategory(data),

      cardsCategory(data),

      category("Targets", "30", "Blocks for the many different targets that effects can have", [
        category("Ally", "30", "Blocks that target allied things", subContents("EntityReference", "Ally")),

        category("Enemy", "30", "Blocks that target enemy things", subContents("EntityReference", "Enemy")),

        category(
          "Both",
          "30",
          "Blocks that target both allied and enemy things",
          subContents("EntityReference", "Both")
        ),

        category("Misc", "30", "Blocks that target other things", subContents("EntityReference", "Misc")),

        category(
          "Helper",
          "30",
          "Blocks that help you target specific/niche things",
          subContents("EntityReference", "Util")
        ),
      ]),

      category(
        "Choices",
        "60",
        "Blocks for the actions that a card can make the player take when being played",
        contents("TargetSelection")
      ),

      category("Attributes", "200", "Blocks for the many properties of cards/entities", [
        category(
          "Primary",
          "200",
          "Attribute blocks that are frequently used on cards",
          subContents("Attribute", "Frequent")
        ),

        category(
          "Secondary",
          "200",
          "Attribute blocks that aren't commonly put on cards directly, but rather are used by other cards",
          subContents("Attribute", "Infrequent")
        ),

        category("Misc", "200", "Attribute blocks that are used rarely and nichely", subContents("Attribute", "Misc")),
      ]),

      category("Players", "45", "Blocks for specifying (in different ways) the two players", [
        ...exclusionContents("TargetPlayer", "TargetPlayer_1", "TargetPlayer_2"),
        {
          kind: "label",
          text: " ",
        },
        {
          kind: "label",
          text: " ",
        },
        {
          kind: "label",
          text: " ",
        },
        getBlockInfo("TargetPlayer_1"),
        getBlockInfo("TargetPlayer_2"),
      ]),

      category("Tribes", "160", "Blocks for the different tribes that units can be a part of", contents("Race")),

      {
        kind: "sep",
      },

      category(
        "Spells",
        "260",
        "Blocks that are the actual effects cards can cause (not actually related to the 'Spell' card type)",
        [
          category("Buff", "260", "Spell blocks that deal with changing units' stats", subContents("Spell", "Buff")),

          category(
            "Damage",
            "260",
            "Spell blocks that relate to dealing damage (and also the one block for healing)",
            subContents("Spell", "Damage")
          ),

          category("Draw", "260", "Spell blocks that involve drawing/receiving cards", subContents("Spell", "Draw")),

          category(
            "Summon",
            "260",
            "Spell blocks that have to do with the summoning of units",
            subContents("Spell", "Summon")
          ),

          category(
            "Shuffle",
            "260",
            "Spell blocks that handle card shuffling to decks",
            subContents("Spell", "Shuffle")
          ),

          category("Cost", "260", "Spell blocks that create cost modification effects", subContents("Spell", "Cost")),

          category(
            "Mechanics",
            "260",
            "Spell blocks that employ specific custom mechanics",
            subContents("Spell", "Mechanic")
          ),

          category(
            "Enchantment",
            "260",
            "Spell blocks that add/remove enchantments/auras",
            subContents("Spell", "Enchant")
          ),

          category("Attacking", "260", "Spell blocks about enities attacking", subContents("Spell", "Attack")),

          category("Misc", "260", "Spell blocks that aren't otherwise categorized", subContents("Spell", "Misc")),

          category(
            "Helper",
            "260",
            "Spell blocks that help implement complex/combined effects",
            subContents("Spell", "Util")
          ),
        ]
      ),

      category("Values", "340", "Blocks for anything and everything numeric", [
        category(
          "Number of ...",
          "340",
          "Blocks for counting the number of things",
          subContents("ValueProvider", "Number")
        ),

        category(
          "Properties",
          "340",
          "Blocks for numerical properties of things",
          subContents("ValueProvider", "Properties")
        ),

        category("Misc", "340", "Blocks for other values", subContents("ValueProvider", "Misc")),

        category("Helper", "340", "Blocks for calculating more advanced values", subContents("ValueProvider", "Util")),
      ]),

      category(
        "Conditions",
        "100",
        "Blocks that CAN handle the truth, because they evaluate it",
        contents("Condition")
      ),

      category(
        "Filters",
        "120",
        "Blocks for narrowing down lists of entities based on desired properties",
        contents("Filter")
      ),

      category("Enchantment", "280", "Blocks for the creation of ongoing triggered effects", contents("Enchantment")),

      category("Triggers", "300", "Blocks for waiting/listening for specific events in-game", [
        category("Unit", "300", "Trigger blocks that deal with units", subContents("Trigger", "Unit")),

        category("Card", "300", "Trigger blocks that relate to cards", subContents("Trigger", "Card")),
        category("Attack", "300", "Trigger blocks that involve attacks / attacking", subContents("Trigger", "Attack")),

        category(
          "Damage",
          "300",
          "Trigger blocks that have to do with damage being dealt",
          subContents("Trigger", "Damage")
        ),

        category("Turn", "300", "Trigger blocks that handle turns starting / ending", subContents("Trigger", "Turn")),

        category("Misc", "300", "Trigger blocks that aren't otherwise categorized", subContents("Trigger", "Misc")),

        category(
          "Helper",
          "300",
          "Trigger blocks that help with reacting to more specific events",
          subContents("Trigger", "Util")
        ),
      ]),

      category("Auras", "230", "Blocks for the specific type of Enchantment of ongoing effects", contents("Aura")),

      category(
        "Card Sources",
        "10",
        "Blocks for the different places that cards can be generated from",
        contents("Source")
      ),

      category(
        "Cost Modifier",
        "320",
        "Blocks for making more complex lun cost modification effects",
        contents("CostModifier")
      ),

      {
        kind: "sep",
      },

      category("Custom", "-1", "Blocks for representing your own effect from the old JSON system", [
        category("Custom Blocks", "#000000", "", subContents("Custom", "Desc")),

        category("Custom Args", "#000000", "", subContents("Custom", "Arg")),

        category("Custom Enums", "#000000", "", subContents("Custom", "Enum")),
      ]),

      category("Simulation", -1, "Blocks for creating simulation tests to see if your cards work", [
        category(
          "Testing",
          "#888888",
          "The blocks used to start a new test",
          inclusionContents("TestStarter", "TestAssertion")
        ),

        category("Actions", "260", "Blocks for actions that you can simulate players taking", contents("TestAction")),

        category(
          "Variables",
          "310",
          "Blocks to save and refer back to specific targets/values in the simulation",
          null,
          { custom: "SPELLSOURCE_VARIABLES" }
        ),

        {
          kind: "sep",
        },

        category(
          "Logic",
          "210",
          "Blocks to help manage what actions are simulated",
          inclusionContents("logic", "controls_if", "controls_ifelse")
        ),

        category(
          "Loops",
          "120",
          "Blocks to facilitate simulating many actions iteratively",
          exclusionContents("controls", "controls_if", "controls_ifelse", "controls_forEach")
        ),

        category(
          "Math",
          "230",
          "Blocks for doing math in regards to actions. Don't try to use these blocks for cards or effects other than test actions.",
          exclusionContents("math", "math_change", "math_trig", "math_constant", "math_atan2")
        ),

        /* Not sure if these are actually going to be needed
                                                                  category('Text', '160',
                                                                    "Test",
                                                                    simpleContents('text')
                                                                  ),
                                                          
                                                                  category('Lists', '260',
                                                                    "Test",
                                                                    simpleContents('list')
                                                                  ),
                                                                  */
      ]),
    ],
  } as ToolboxInfo;
}

export function myCardsCategory(data: ContextType<typeof BlocklyDataContext>) {
  const setOfSets = new Set([] as string[]);

  for (const myCard of data.myCards) {
    const set = myCard.cardScript.set;
    if (set && set !== "CUSTOM") {
      setOfSets.add(set);
    }
  }

  return category(
    "My Cards",
    "#888888",
    "Cards You've Created",
    [
      {
        kind: "label",
        text: data.userId ? "Cards You've Created" : "Login to be able to save the cards you create",
      },
      ...data.myCards
        .filter((card) => card.cardScript.set === "CUSTOM")
        .map((card) => ({
          kind: "block",
          ...card.blocklyWorkspace,
        })),
      ...[...setOfSets].map((set) => myCardsForSetCategory(set, data)),
    ],
    { toolboxitemid: "My Cards" }
  );
}

export function myCardsForSetCategory(set: string, data: ContextType<typeof BlocklyDataContext>) {
  return category(
    set,
    "#888888",
    `Cards You've Created in the "${set}" Set`,
    [
      {
        kind: "label",
        text: `Cards You've Created in the "${set}" Set`,
      },
      ...data.myCards
        .filter((card) => card.cardScript.set === set)
        .map((card) => ({
          kind: "block",
          blockxml: card.blocklyWorkspace,
        })),
    ],
    { toolboxitemid: `My ${set} Cards` }
  );
}

/**
 * Specifically creates the JSON for the "Classes" category.
 * Defined here so that the category can be easily updated for new WorkspaceHeroClasses
 * @returns category json
 */
export function classesCategory(data: ContextType<typeof BlocklyDataContext>) {
  const myCards =
    data.myCards.map((value) => value.cardScript as CardDef).filter((card) => card && card.type === "CLASS") ?? [];

  const classCards = Object.values(data.classes).filter((card) => card.collectible !== false);

  return category(
    "Classes",
    "#888888",
    "Blocks for the different playable champion classes",
    [
      ...contents("HeroClass"),
      ...(myCards.length > 0
        ? [
            {
              kind: "label",
              text: "Your Classes",
            },
          ]
        : []),
      ...myCards.map((card) => ({
        kind: "block",
        type: "HeroClass_REFERENCE",
        fields: {
          id: card.heroClass,
          name: BlocklyMiscUtils.cardMessage(card),
        },
      })),
      ...(myCards.length > 0
        ? [
            {
              kind: "label",
              text: "Other Classes",
            },
          ]
        : []),
      ...classCards.map((card) => ({
        kind: "block",
        type: "HeroClass_REFERENCE",
        fields: {
          id: card.heroClass,
          name: BlocklyMiscUtils.cardMessage(card),
        },
      })),
    ],
    { toolboxitemid: "Classes" }
  );
}

/**
 * Specifically creates the JSON for the "Cards" category.
 * Defined here so that the category can be easily updated for new WorkspaceCards
 * @returns category json
 */
export function cardsCategory(data: ContextType<typeof BlocklyDataContext>) {
  return category(
    "Cards",
    "#888888",
    "Blocks for referencing the cards you make in the workspace (use 'Search Card Catalogue' to reference existing cards)",
    [
      ...contents("Card"),
      {
        kind: "label",
        text: "Blocks you can use for referencing your own cards",
      },
      ...(data.myCards
        .filter((card) => card.cardScript && card.cardScript.type !== "CLASS")
        .map((card) => ({
          kind: "block",
          type: "Card_REFERENCE",
          fields: {
            id: card.id,
            name: BlocklyMiscUtils.cardMessage(card.cardScript),
          },
        })) ?? []),
    ],
    { toolboxitemid: "Cards" }
  );
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
function category(
  name: string,
  color: string | -1,
  tooltip: string,
  contents: ArrayLike<unknown> | null,
  props: {
    [p: string]: any;
  } | null = null
): StaticCategoryInfo {
  let category: any = {
    kind: "category",
    name: name,
    colour: color,
    tooltip: tooltip,
  };
  if (contents?.length) {
    category.contents = contents;
  }
  if (props) {
    for (let propsKey in props) {
      category[propsKey] = props[propsKey];
    }
  }
  return category;
}

/**
 * Gets all valid blocks that start with a prefix and are part of a certain subcategory
 * @param prefix The prefix to check from
 * @param sub The subcategory to match
 * @returns [category json blocks]
 */
function subContents(prefix: string, sub: string) {
  let contents: Partial<ToolboxItemInfo>[] = [];
  for (let block in Blockly.Blocks) {
    if (defaultTest(block) && block.startsWith(prefix)) {
      let subcategory = Blockly.Blocks[block].json?.subcategory;
      if ((!subcategory && sub === "Misc") || subcategory?.includes(sub)) {
        contents.push(getBlockInfo(block));
      }
    }
  }
  return contents;
}

/**
 * Get all valid blocks that start with a prefix
 * @param prefix The prefix to check from
 * @returns [category json blocks]
 */
export function contents(prefix: string) {
  let contents: Partial<ToolboxItemInfo>[] = [];
  for (let block in Blockly.Blocks) {
    if (defaultTest(block) && block.startsWith(prefix)) {
      contents.push(getBlockInfo(block));
    }
  }
  return contents;
}

/**
 * Get all valid blocks that start with a prefix, minus some exclusions by type
 * @param prefix The prefix to check from
 * @param exclusions Specific blocks to not include
 * @returns [category json blocks]
 */
function exclusionContents(prefix: string, ...exclusions: Array<unknown>) {
  let contents: Partial<ToolboxItemInfo>[] = [];
  for (let block in Blockly.Blocks) {
    if (defaultTest(block) && block.startsWith(prefix) && !exclusions.includes(block)) {
      contents.push(getBlockInfo(block));
    }
  }
  return contents;
}

/**
 * Get all valid blocks that start with a prefix, plus some extras by type
 * @param prefix The prefix to check from
 * @param inclusions Additional blocks to include
 * @returns [category json blocks]
 */
function inclusionContents(prefix: string, ...inclusions: Array<unknown>) {
  let contents: Partial<ToolboxItemInfo>[] = [];
  for (let block in Blockly.Blocks) {
    if (defaultTest(block) && (block.startsWith(prefix) || inclusions.includes(block))) {
      contents.push(getBlockInfo(block));
    }
  }
  return contents;
}

function artCategories() {
  const categoryContents: Record<string, Partial<ToolboxItemInfo>[]> = {};

  for (let block in Blockly.Blocks) {
    if (defaultTest(block) && block.startsWith("Art_") && Blockly.Blocks[block]["art"]) {
      const art = Blockly.Blocks[block]["art"] as ImageDef;

      const folder = art.src.split("/").at(-2) ?? "";

      const name = toTitleCaseCorrected(folder);

      categoryContents[name] ??= [];

      categoryContents[name].push(getBlockInfo(block));
    }
  }

  return Object.entries(categoryContents).map(([name, contents]) => category(name, "#888888", name + " Art", contents));
}

/**
 * All blocks that appear in the toolbox should pass this, which means:
 *  - Their type doesn't end in shadow
 *  - If they're a default blockly block, then their type can't have two '_'s in it
 *      (because of the blocks used in mutators)
 * @param block The block type to check
 * @returns boolean
 */
function defaultTest(block: string) {
  return (
    !block.endsWith("SHADOW") &&
    (!block.match(/^.*_.*_.*/) || BlocklyMiscUtils.isSpellsourceBlock(block)) &&
    !block.endsWith("_REFERENCE")
  );
}

export function generatedArtCategory(data: ContextType<typeof BlocklyDataContext>) {
  return category("Create your Own", "#888888", "Use AI to create your own card art", [
    ...(data.userId ? contents("ArtGen") : []),
    {
      kind: "label",
      text: data.userId ? "Art You've Created" : "Login to be able to generate your own art",
    },
    ...(data.generatedArt ?? []).map((art) => ({
      ...getBlockInfo("Art_Generated"),
      fields: {
        src: art.urls[0],
        hash: art.hash,
      },
    })),
  ]);
}
