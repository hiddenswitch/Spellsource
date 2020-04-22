/**
 * <b>The core engine code for Spellsource.</b>
 * <p>
 * {@link net.demilich.metastone.game.GameContext} contains the state, player delegates and state-manipulation code
 * (i.e. the {@link net.demilich.metastone.game.logic.GameLogic}). This is the starting point for interacting with the
 * engine. You'll create an instance of a {@code GameContext} for nearly anything you want to do.
 * <p>
 * To create new player behaviours like AIs, explore the {@link net.demilich.metastone.game.behaviour.Behaviour}
 * hierarchy of classes. In particular, {@link net.demilich.metastone.game.behaviour.GameStateValueBehaviour}
 * represents a complex AI.
 * <p>
 * The {@link net.demilich.metastone.game.cards.CardCatalogue} contains references to the cards. They are accessed by
 * their IDs, which correspond to their filenames in the {@code cards/src/main/resources/cards} directory, minus the
 * {@code ".json"} extension.
 * <p>
 * Cards are deserialized from {@link net.demilich.metastone.game.cards.desc.CardDesc} objects. The type system for the
 * card JSON is not straight forward. Some types, like {@link net.demilich.metastone.game.cards.desc.CardDesc}, {@link
 * net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc} and {@link net.demilich.metastone.game.spells.desc.OpenerDesc}
 * deserialize conventionally, where each field in the class corresponds exactly in name and type to a field in a JSON
 * object. However, all other types are modeled as interable maps that subclass {@link
 * net.demilich.metastone.game.cards.desc.Desc}. These have corresponding key enums, like {@link
 * net.demilich.metastone.game.spells.desc.SpellArg}, whose {@code camelCased} version appears in the JSON. The type of
 * the value can be discovered by looking at the corresponding {@link net.demilich.metastone.game.cards.desc.DescDeserializer}
 * subclass's {@link net.demilich.metastone.game.cards.desc.DescDeserializer#init(net.demilich.metastone.game.cards.desc.DescDeserializer.SerializationContext)}
 * method and cross-referencing the {@link net.demilich.metastone.game.cards.desc.ParseValueType} that appears as a
 * value of an enum, ParseValueType key pair with {@link net.demilich.metastone.game.cards.desc.ParseUtils#parse(com.fasterxml.jackson.databind.JsonNode,
 * net.demilich.metastone.game.cards.desc.ParseValueType, com.fasterxml.jackson.databind.DeserializationContext)}.
 * <p>
 *
 * @see net.demilich.metastone.game.GameContext for more about how to start a Spellsource game and how game state is
 * 		stored and manipulated.
 * @see net.demilich.metastone.game.logic.GameLogic for more about how the rules of the game are implemented
 * @see net.demilich.metastone.game.behaviour.Behaviour for more about how to get player actions
 * @see net.demilich.metastone.game.cards.desc.CardDesc for more about the card JSON format
 * @see net.demilich.metastone.game.spells.Spell for more about spell effects
 * @see net.demilich.metastone.game.entities.Entity for more about how data is stored in the game
 */
package net.demilich.metastone.game;