import * as Blockly from "blockly/core";
import {SearchCategory} from "./search-category";
import {ToolboxItemInfo} from "blockly/core/utils/toolbox";
import * as BlocklyMiscUtils from "../../lib/blockly-spellsource-utils";
import {ContextType} from "react";
import {BlocklyDataContext} from "../../pages/card-editor";

/* eslint-disable @typescript-eslint/naming-convention */

/**
 * A toolbox category that provides a search field and displays matching blocks
 * in its flyout.
 */
export class CardSearchCategory extends SearchCategory {
  static readonly registrationName = "cardsearch";

  /**
   * Initializes a ToolboxSearchCategory.
   * @param categoryDef The information needed to create a category in the
   *     toolbox.
   * @param parentToolbox The parent toolbox for the category.
   * @param opt_parent The parent category or null if the category does not have
   *     a parent.
   */
  constructor(
    categoryDef: Blockly.utils.toolbox.StaticCategoryInfo,
    parentToolbox: Blockly.IToolbox,
    opt_parent?: Blockly.ICollapsibleToolboxItem
  ) {
    super(categoryDef, parentToolbox, opt_parent);
    this.categoryKind = CardSearchCategory.registrationName;
    this.defaultMessage = "Type to search for cards";
    this.noResultsMessage = "No matching cards found";
  }

  protected async getBlocks(): Promise<ToolboxItemInfo[]> {
    const query = this.searchField!.value;

    if (!query) return [];

    const workspace: Blockly.WorkspaceSvg & {
      _data?: ContextType<typeof BlocklyDataContext>;
    } = this.workspace_;

    const { getCollectionCards } = workspace["_data"]!;

    const { data, error } = await getCollectionCards!({
      variables: {
        filter: { searchMessage: { includesInsensitive: query.trim() } },
        limit: 25,
      },
    });

    if (error) {
      console.error(error);
      return [
        {
          kind: "label",
          text: "Encountered an error while searching",
        },
      ];
    }

    const cards = data!.allCollectionCards?.nodes ?? [];

    return cards.map((card) => ({
      kind: "block",
      type: "Card_REFERENCE",
      fields: {
        id: card!.id,
        name: BlocklyMiscUtils.cardMessage(card!.cardScript),
      },
      extraState: card!.cardScript,
    }));
  }
}
