import type { CardDef } from "../components/collection/card-display";

export interface CatalogueEntry {
  id: string;
  createdBy?: string | null;
  cardScript: CardDef;
  lastModified?: string | null;
}

export function collectionClasses(cards: readonly CatalogueEntry[]) {
  return cards.filter(({ cardScript: card }) => card.type === "CLASS" && card.heroClass !== "ANY" && card.collectible !== false);
}

export function filterCatalogue(cards: readonly CatalogueEntry[], options: {
  types: readonly string[];
  heroClass?: string;
  mainHeroClass?: string | null;
  search?: string;
  uncollectible: boolean;
  ownOnly: boolean;
  user?: string;
  orderBy?: string;
}) {
  const classes = new Map(cards.filter(({ cardScript }) => cardScript.type === "CLASS").map(({ cardScript }) => [cardScript.heroClass, cardScript]));
  const search = options.search?.trim().toLocaleLowerCase() ?? "";
  const filtered = cards.filter(({ cardScript: card, createdBy }) => {
    const hero = classes.get(card.heroClass);
    const collectible = card.collectible !== false && (card.heroClass === "ANY" || !!hero && hero.collectible !== false);
    if (collectible === options.uncollectible || !options.types.includes(card.type)) return false;
    if (options.ownOnly && (!options.user || createdBy !== options.user)) return false;
    if (options.heroClass === "ALLOWED" && options.mainHeroClass) {
      if (card.heroClass !== options.mainHeroClass && card.heroClass !== "ANY") return false;
    } else if (options.heroClass && card.heroClass !== options.heroClass) return false;
    const message = [String(card.baseManaCost ?? 0), card.name, card.description?.replace(/[$#]/g, ""), card.type, card.heroClass, hero?.name, card.rarity, card.race, card.set].filter(Boolean).join(" ").toLocaleLowerCase();
    return !search || message.includes(search);
  });
  return filtered.sort((a, b) => {
    const cost = (a.cardScript.baseManaCost ?? 0) - (b.cardScript.baseManaCost ?? 0);
    const name = a.cardScript.name.localeCompare(b.cardScript.name);
    let primary = cost;
    switch (options.orderBy) {
      case "COST_DESC": primary = -cost; break;
      case "NAME_ASC": primary = name; break;
      case "NAME_DESC": primary = -name; break;
      case "TYPE_ASC": primary = a.cardScript.type.localeCompare(b.cardScript.type); break;
      case "LAST_MODIFIED_DESC": primary = (Date.parse(b.lastModified ?? "") || 0) - (Date.parse(a.lastModified ?? "") || 0); break;
    }
    return primary || cost || name || a.id.localeCompare(b.id);
  });
}
