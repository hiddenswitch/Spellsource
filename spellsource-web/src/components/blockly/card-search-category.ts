import * as Blockly from "blockly/core";
import { SearchCategory } from "./search-category";
import { ToolboxItemInfo } from "blockly/core/utils/toolbox";
import { useGetCollectionCardsLazyQuery } from "../../__generated__/client";
import * as BlocklyMiscUtils from "../../lib/blockly-misc-utils";

/* eslint-disable @typescript-eslint/naming-convention */

/**
 * A toolbox category that provides a search field and displays matching blocks
 * in its flyout.
 */
export class CardSearchCategory extends SearchCategory {
  static readonly SEARCH_CATEGORY_KIND = "cardsearch";

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
    this.categoryKind = CardSearchCategory.SEARCH_CATEGORY_KIND;
    this.defaultMessage = "Type to search for cards";
    this.noResultsMessage = "No matching cards found";
  }

  protected async getBlocks(): Promise<ToolboxItemInfo[]> {
    const query = this.searchField.value;

    if (!query) return [];

    const workspace = this.workspace_;

    const getCollectionCards = workspace["getCollectionCards"] as ReturnType<typeof useGetCollectionCardsLazyQuery>[0];

    const { data, error } = await getCollectionCards({
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

    const cards = data.allCollectionCards?.nodes ?? [];

    return cards.map((card) => ({
      kind: "block",
      type: "Card_REFERENCE",
      fields: {
        id: card.id,
        name: BlocklyMiscUtils.cardMessage(card.cardScript),
      },
      extraState: card.cardScript,
    }));
  }
}
