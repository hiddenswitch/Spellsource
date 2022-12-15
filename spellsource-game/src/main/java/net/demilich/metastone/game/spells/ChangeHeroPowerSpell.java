package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Changes the {@link SpellArg#TARGET_PLAYER} hero power to a random hero power retrieved from {@link
 * SpellUtils#getCards(GameContext, Player, Entity, Entity, SpellDesc)}.
 *
 * @see CopyHeroPower to copy the opponent's hero power.
 * @see net.demilich.metastone.game.spells.aura.CardAura to temporarily change a card, like a hero power, to another
 * card.
 */
public class ChangeHeroPowerSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(ChangeHeroPowerSpell.class);

	public static SpellDesc create(String card) {
		Map<SpellArg, Object> arguments = new SpellDesc(ChangeHeroPowerSpell.class);
		arguments.put(SpellArg.CARD, card);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.CARD, SpellArg.CARDS, SpellArg.CARD_SOURCE, SpellArg.CARD_FILTER);
		List<Card> cards = SpellUtils.getCards(context, player, target, source, desc, 1);
		Hero hero = player.getHero();

		if (cards.isEmpty()) {
			logger.error("changeHeroPower {} {}: No cards were specified.", context.getGameId(), source);
			return;
		}

		Card heroPower = context.getLogic().getRandom(cards).getCopy();

		heroPower.setId(context.getLogic().generateId());
		heroPower.setOwner(hero.getOwner());
		logger.debug("changeHeroPower {} {}: {}'s hero power was changed to {}", context.getGameId(), source, hero.getName(), heroPower);
		// The old hero power should be removed from play.
		var oldHeroPower = player.getHeroPowerZone().get(0);
		context.getLogic().removeEnchantments(oldHeroPower);
		oldHeroPower.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
		context.getLogic().removeCard(oldHeroPower);
		if (heroPower.getHeroClass().equals(HeroClass.INHERIT)) {
			heroPower.setHeroClass(hero.getHeroClass());
		}
		heroPower.moveOrAddTo(context, Zones.HERO_POWER);
		oldHeroPower.getAttributes().put(Attribute.TRANSFORM_REFERENCE, heroPower.getReference());
		context.getLogic().addEnchantments(player, source, heroPower, heroPower);
	}
}
