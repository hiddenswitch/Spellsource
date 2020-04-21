package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.CardType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Changes the hero of {@link SpellArg#TARGET_PLAYER} to the specified hero card ID in {@link SpellArg#CARD}. If {@link
 * SpellArg#CARDS} or {@link SpellArg#CARD_FILTER} or {@link SpellArg#CARD_SOURCE} are specified, a random hero card
 * will be chosen from the lists of cards.
 * <p>
 * This spell activates the {@link net.demilich.metastone.game.cards.desc.CardDesc#battlecry} by default. To disable it,
 * set {@link SpellArg#EXCLUSIVE} to {@code true}.
 * <p>
 * For <b>example,</b> to turn the casting player's hero into Ragnaros:
 * <pre>
 *     {
 *         "class": "ChangeHeroSpell",
 *         "card": "hero_ragnaros",
 *         "targetPlayer": "SELF"
 *     }
 * </pre>
 * Casts the {@link SpellArg#SPELL} sub-spell with the {@link net.demilich.metastone.game.targeting.EntityReference#OUTPUT}
 * set to the new hero.
 */
public class ChangeHeroSpell extends Spell {
	private static Logger logger = LoggerFactory.getLogger(ChangeHeroSpell.class);

	/**
	 * Changes the casting player's hero to the specified card ID.
	 *
	 * @param heroCardId A hero card ({@link CardType#HERO}.
	 * @return The spell
	 */
	public static SpellDesc create(String heroCardId) {
		Map<SpellArg, Object> arguments = new SpellDesc(ChangeHeroSpell.class);
		arguments.put(SpellArg.CARD, heroCardId);
		return new SpellDesc(arguments);
	}

	/**
	 * Changes the specified player's hero the specified card ID.
	 *
	 * @param player     The player whose hero should be changed.
	 * @param heroCardId A hero card ({@link CardType#HERO}.
	 * @return The spell
	 */
	public static SpellDesc create(TargetPlayer player, String heroCardId) {
		Map<SpellArg, Object> arguments = new SpellDesc(ChangeHeroSpell.class);
		arguments.put(SpellArg.TARGET_PLAYER, player);
		arguments.put(SpellArg.CARD, heroCardId);
		return new SpellDesc(arguments);
	}

	/**
	 * Changes the specified player's hero with the specified card ID, resolving the battlecry if specified.
	 *
	 * @param player           the player
	 * @param heroCardId       the hero card
	 * @param resolveBattlecry {@code true} if the hero card should have its battlecry resolved
	 * @return the spell
	 */
	public static SpellDesc create(TargetPlayer player, String heroCardId, boolean resolveBattlecry) {
		Map<SpellArg, Object> arguments = new SpellDesc(ChangeHeroSpell.class);
		arguments.put(SpellArg.TARGET_PLAYER, player);
		arguments.put(SpellArg.CARD, heroCardId);
		arguments.put(SpellArg.EXCLUSIVE, !resolveBattlecry);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.CARD, SpellArg.CARDS, SpellArg.CARD_FILTER, SpellArg.CARD_SOURCE, SpellArg.SPELL, SpellArg.EXCLUSIVE);
		CardList heroCards = SpellUtils.getCards(context, player, target, source, desc);
		if (heroCards.size() == 0) {
			logger.error("onCast {} {}: Requires hero card ID, none specified.", context.getGameId(), source);
			return;
		}

		Card heroCard = heroCards.get(0);
		Hero hero = heroCard.hero();
		context.getLogic().changeHero(player, source, hero, !(boolean) desc.getOrDefault(SpellArg.EXCLUSIVE, false));

		List<SpellDesc> spellDescs = desc.subSpells(0);
		for (SpellDesc subSpell : spellDescs) {
			SpellUtils.castChildSpell(context, player, subSpell, source, target, hero);
		}
	}
}
