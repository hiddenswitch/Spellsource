package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.logic.GameLogic;
import com.hiddenswitch.spellsource.rpc.Spellsource.DamageTypeMessage.DamageType;
import net.demilich.metastone.game.spells.ReceiveCardSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

import java.util.Collections;
import java.util.List;

/**
 * Throws two darts that each deal {@link SpellArg#VALUE} damage to random enemy minions. If both darts hit the same
 * minion, adds a Coin to the player's hand.
 * <p>
 * Implements Dart Throw.
 */
public final class DartThrowSpell extends Spell {

	private static final Logger logger = LoggerFactory.getLogger(DartThrowSpell.class);

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
	}

	@Override
	public void cast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> targets) {
		int damage = desc.getValue(SpellArg.VALUE, context, player, null, source, 2);

		if ((source.getEntityType() == EntityType.CARD && GameLogic.isCardType(((Card) source).getCardType(), CardType.SPELL))) {
			damage = context.getLogic().applySpellpower(player, source, damage);
			damage = context.getLogic().applyAmplify(player, damage, Attribute.SPELL_DAMAGE_AMPLIFY_MULTIPLIER);
		}

		List<Entity> validTargets = SpellUtils.getValidRandomTargets(targets);

		if (validTargets.isEmpty()) {
			return;
		}

		Actor firstTarget = (Actor) context.getLogic().getRandom(validTargets);
		EnumSet<DamageType> damageType = EnumSet.of(DamageType.MAGICAL);
		GameLogic.fireMissileEvent(context, player, source, Collections.singletonList(firstTarget), damageType);
		context.getLogic().damage(player, firstTarget, damage, source, true);

		// Recheck valid targets after first dart
		validTargets = SpellUtils.getValidRandomTargets(targets);
		if (validTargets.isEmpty()) {
			return;
		}

		Actor secondTarget = (Actor) context.getLogic().getRandom(validTargets);
		GameLogic.fireMissileEvent(context, player, source, Collections.singletonList(secondTarget), damageType);
		context.getLogic().damage(player, secondTarget, damage, source, true);

		// If both darts hit the same minion, get a Coin
		if (firstTarget.getId() == secondTarget.getId()) {
			SpellDesc coinSpell = ReceiveCardSpell.create("spell_the_coin");
			SpellUtils.castChildSpell(context, player, coinSpell, source, null);
		}
	}
}
