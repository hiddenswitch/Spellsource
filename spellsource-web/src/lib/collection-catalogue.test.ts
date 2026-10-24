import { CatalogueEntry, collectionClasses, filterCatalogue } from "./collection-catalogue";

const entry = (id: string, script: Record<string, unknown>, metadata = {}): CatalogueEntry => ({
  id, cardScript: { id, name: id, type: "MINION", heroClass: "ANY", collectible: true, baseManaCost: 1, ...script } as any, ...metadata,
});
const cards = [
  entry("class_red", { type: "CLASS", heroClass: "RED", name: "Red class" }),
  entry("class_hidden", { type: "CLASS", heroClass: "HIDDEN", collectible: false }),
  entry("git", { heroClass: "RED", baseManaCost: 2, description: "Deal $3 damage.", set: "BASIC" }),
  entry("neutral", { baseManaCost: 0 }),
  entry("custom", { heroClass: "RED" }, { createdBy: "user", lastModified: "2026-10-24T10:00:00Z" }),
  entry("old_custom", { heroClass: "RED" }, { createdBy: "user", lastModified: "2026-09-24T10:00:00Z" }),
  entry("hidden", { heroClass: "HIDDEN" }),
  entry("token", { collectible: false }),
];
const options = { types: ["MINION"], uncollectible: false, ownOnly: false };
const ids = (result: CatalogueEntry[]) => result.map(c => c.id);

test("Git and SQL cards appear together without mutating the source", () => {
  const before = [...cards];
  expect(ids(filterCatalogue(cards, options))).toEqual(["neutral", "custom", "old_custom", "git"]);
  expect(cards).toEqual(before);
});
test("class availability determines collectibility and class choices", () => {
  expect(ids(collectionClasses(cards))).toEqual(["class_red"]);
  expect(ids(filterCatalogue(cards, { ...options, uncollectible: true }))).toEqual(["hidden", "token"]);
});
test("deck allowed filter includes neutral and matching class cards", () => {
  expect(ids(filterCatalogue(cards, { ...options, heroClass: "ALLOWED", mainHeroClass: "BLUE" }))).toEqual(["neutral"]);
});
test("own cards requires a signed-in matching author", () => {
  expect(filterCatalogue(cards, { ...options, ownOnly: true })).toEqual([]);
  expect(ids(filterCatalogue(cards, { ...options, ownOnly: true, user: "user" }))).toEqual(["custom", "old_custom"]);
});
test("search retains readable description and set matching", () => {
  expect(ids(filterCatalogue(cards, { ...options, search: "DEAL 3 damage" }))).toEqual(["git"]);
  expect(ids(filterCatalogue(cards, { ...options, search: "basic" }))).toEqual(["git"]);
});
test("recent custom changes sort ahead of undated Git cards", () => {
  expect(ids(filterCatalogue(cards, { ...options, orderBy: "LAST_MODIFIED_DESC" }))).toEqual(["custom", "old_custom", "neutral", "git"]);
});
