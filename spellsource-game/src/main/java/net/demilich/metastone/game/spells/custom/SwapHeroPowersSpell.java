package net.demilich.metastone.game.spells.custom;

import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swaps the hero powers of the casting player and opponent. Both players receive each other's hero power.
 */
public class SwapHeroPowersSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(SwapHeroPowersSpell.class);

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Player opponent = context.getOpponent(player);

		Card playerHeroPower = player.getHeroPowerZone().get(0);
		Card opponentHeroPower = opponent.getHeroPowerZone().get(0);

		String playerHeroPowerId = playerHeroPower.getCardId();
		String opponentHeroPowerId = opponentHeroPower.getCardId();

		logger.debug("swapHeroPowers {} {}: Swapping {} (player {}) with {} (player {})",
				context.getGameId(), source,
				playerHeroPowerId, player.getUserId(),
				opponentHeroPowerId, opponent.getUserId());

		// Remove both hero powers
		context.getLogic().removeEnchantments(playerHeroPower);
		playerHeroPower.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
		context.getLogic().removeCard(playerHeroPower);

		context.getLogic().removeEnchantments(opponentHeroPower);
		opponentHeroPower.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
		context.getLogic().removeCard(opponentHeroPower);

		// Create new copies and assign to swapped players
		Card newPlayerHeroPower = context.getCardById(opponentHeroPowerId).getCopy();
		newPlayerHeroPower.setId(context.getLogic().generateId());
		newPlayerHeroPower.setOwner(player.getId());
		if (newPlayerHeroPower.hasHeroClass(HeroClass.INHERIT)) {
			newPlayerHeroPower.setHeroClass(player.getHero().getHeroClass());
		}
		newPlayerHeroPower.moveOrAddTo(context, Zones.HERO_POWER);
		playerHeroPower.getAttributes().put(Attribute.TRANSFORM_REFERENCE, newPlayerHeroPower.getReference());
		context.getLogic().addEnchantments(player, source, newPlayerHeroPower, newPlayerHeroPower);

		Card newOpponentHeroPower = context.getCardById(playerHeroPowerId).getCopy();
		newOpponentHeroPower.setId(context.getLogic().generateId());
		newOpponentHeroPower.setOwner(opponent.getId());
		if (newOpponentHeroPower.hasHeroClass(HeroClass.INHERIT)) {
			newOpponentHeroPower.setHeroClass(opponent.getHero().getHeroClass());
		}
		newOpponentHeroPower.moveOrAddTo(context, Zones.HERO_POWER);
		opponentHeroPower.getAttributes().put(Attribute.TRANSFORM_REFERENCE, newOpponentHeroPower.getReference());
		context.getLogic().addEnchantments(opponent, source, newOpponentHeroPower, newOpponentHeroPower);
	}
}
