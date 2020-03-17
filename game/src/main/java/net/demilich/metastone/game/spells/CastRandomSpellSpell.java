package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.CardFilter;
import net.demilich.metastone.game.spells.desc.filter.SpecificCardFilter;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Casts a random spell from the {@link SpellArg#CARD_SOURCE}, {@link SpellArg#CARD_FILTER} and {@link SpellArg#CARDS}
 * provided.
 * <p>
 * If {@link SpellArg#EXCLUSIVE} is {@code true}, the {@code source} does <b>not</b> have to be in play in order for the
 * spell to be cast.
 * <p>
 * The {@code source} of the spell written on the card that is randomly chosen is the <b>entity doing the casting</b>,
 * not the card itself.
 * <p>
 * For example, to implement "Whenever a player casts a spell, cast a copy of it for them with random targets.":
 * <pre>
 *   {
 *     "eventTrigger": {
 *       "class": "SpellCastedTrigger",
 *       "sourcePlayer": "BOTH"
 *     },
 *     "spell": {
 *       "class": "CastRandomSpellSpell",
 *       "cardFilter": {
 *         "class": "AndFilter",
 *         "filters": [
 *           {
 *             "class": "SpecificCardFilter",
 *             "secondaryTarget": "EVENT_SOURCE"
 *           },
 *           {
 *             "class": "CardFilter",
 *             "cardType": "SPELL"
 *           }
 *         ]
 *       },
 *       "cardSource": {
 *         "class": "UncollectibleCatalogueSource"
 *       },
 *       "targetPlayer": "ACTIVE"
 *     }
 *   }
 * </pre>
 * Observe that the {@link CardFilter} is a {@link SpecificCardFilter} that is configured, using its {@code
 * "secondaryTarget"} option, to only be {@code true} for cards that are equal to {@link EntityReference#EVENT_SOURCE},
 * i.e., the card that was casted. Only one card (the card that was casted) will match, so that's the card that will be
 * randomly cast.
 *
 * @see net.demilich.metastone.game.spells.custom.PlayCardsRandomlySpell for a similar spell that works with minions,
 * 		weapons and heroes and uses the card as the source of the effects
 */
public class CastRandomSpellSpell extends Spell {
	Logger logger = LoggerFactory.getLogger(CastRandomSpellSpell.class);

	public static SpellDesc create(int value) {
		Map<SpellArg, Object> arguments = new SpellDesc(CastRandomSpellSpell.class);
		arguments.put(SpellArg.VALUE, value);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.VALUE, SpellArg.CARD, SpellArg.CARDS, SpellArg.CARD_SOURCE, SpellArg.CARD_FILTER);
		TargetPlayer castingTargetPlayer = desc.getTargetPlayer() == null ? TargetPlayer.OWNER : desc.getTargetPlayer();
		player.modifyAttribute(Attribute.RANDOM_CHOICES, 1);
		int numberOfSpellsToCast = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		CardList spells = SpellUtils.getCards(context, player, target, source, desc, numberOfSpellsToCast);
		for (int i = 0; i < numberOfSpellsToCast; i++) {
			if (spells.isEmpty()) {
				logger.warn("onCast {} {}: An empty number of spells were found with the filter {} and source {}", context.getGameId(), source, desc.getCardFilter(), desc.getCardSource());
				break;
			}
			SpellUtils.DetermineCastingPlayer determineCastingPlayer = SpellUtils.determineCastingPlayer(context, player, source, castingTargetPlayer);
			boolean mustBeInPlay = !desc.getBool(SpellArg.EXCLUSIVE);
			if (mustBeInPlay && !determineCastingPlayer.isSourceInPlay()) {
				break;
			}
			Player castingPlayer = determineCastingPlayer.getCastingPlayer();

			// Must retrieve a copy because castWithRandomTargets mutates the incoming spell card
			Card randomCard = context.getLogic().getRandom(spells).getCopy();
			logger.debug("onCast {} {}: Casting random spell {}", context.getGameId(), source, randomCard);
			RandomCardTargetSpell.castCardWithRandomTargets(context, castingPlayer, source, randomCard);
			context.getLogic().endOfSequence();
		}

		player.modifyAttribute(Attribute.RANDOM_CHOICES, -1);
	}
}
