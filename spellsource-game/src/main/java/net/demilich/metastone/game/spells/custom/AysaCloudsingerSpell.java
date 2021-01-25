package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.entities.minions.BoardPositionRelative;
import net.demilich.metastone.game.spells.*;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.*;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.filter.SpecificCardFilter;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;
import net.demilich.metastone.game.spells.desc.valueprovider.AttributeValueProvider;
import net.demilich.metastone.game.spells.trigger.FatalDamageTrigger;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Changes your current hero to the first card in the {@link net.demilich.metastone.game.spells.desc.SpellArg#CARDS}
 * array.
 * <p>
 * Summons a token form the second card in the {@link net.demilich.metastone.game.spells.desc.SpellArg#CARDS} array,
 * sets its max health to your hero's max health, and sets its HP to your hero's HP.
 * <p>
 * Gives your player an enchantment that prevents fatal damage to your hero if it's (1) the first card still, and (2)
 * the token (any copy) exists in play. If it does, removes one of the tokens from play, and changes the hero to the
 * <b>third</b> card in the cards array. Sets the HP of that hero back to the token's current HP (or 30 if the token is
 * no longer an Actor).
 * <p>
 * If no token exists in play, the fatal damage trigger doesn't fire, the replacement hero dies and you lose.
 */
public final class AysaCloudsingerSpell extends Spell {

	private static Logger LOGGER = LoggerFactory.getLogger(AysaCloudsingerSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(LOGGER, context, source, desc, SpellArg.CARDS);
		Card[] cards = SpellUtils.getCards(context, desc);
		if (cards == null || cards.length < 3) {
			LOGGER.error("onCast {} {}: Requires at least 3 cards", context.getGameId(), source);
			return;
		}
		Card replacementHero = cards[0];
		Card token = cards[1];
		Card originalHero = cards[2];
		if (replacementHero.getCardType() != CardType.HERO) {
			LOGGER.error("onCast {} {}: Replacement hero {} must be a hero card", context.getGameId(), source, replacementHero);
			return;
		}
		if (token.getCardType() != CardType.MINION) {
			LOGGER.error("onCast {} {}: Token {} must be a minion", context.getGameId(), source, token);
			return;
		}
		if (originalHero.getCardType() != CardType.HERO) {
			LOGGER.error("onCast {} {}: Original hero {} must be a hero card", context.getGameId(), source, originalHero);
			return;
		}
		int currentHp = player.getHero().getHp();
		int currentMaxHp = player.getHero().getMaxHp();

		// Token effects group
		SpellDesc summonSpell = SummonSpell.create(BoardPositionRelative.RIGHT, token);
		summonSpell.put(SpellArg.SPELL, MetaSpell.create(EntityReference.OUTPUT, false, SetAttributeSpell.create(Attribute.MAX_HP, currentMaxHp), SetAttributeSpell.create(Attribute.HP, currentHp)));

		// Hero changing group
		SpellDesc changeHeroSpell = ChangeHeroSpell.create(TargetPlayer.SELF, replacementHero.getCardId(), false);
		changeHeroSpell.put(SpellArg.SPELL, OverrideTargetSpell.create(EntityReference.OUTPUT));
		SpellDesc removeSelfSpell = RemoveActorPeacefullySpell.create(EntityReference.SELF, null, false);

		// Fatal damage trigger group
		EnchantmentDesc switchingEnchantment = new EnchantmentDesc();

		// Case 1: token is on battlefield,
		EntityFilter matchesToken = SpecificCardFilter.create(token.getCardId()).create();
		Condition tokenOnBattlefield = AnyMatchFilterCondition.create(EntityReference.FRIENDLY_MINIONS, matchesToken);
		SpellDesc removeToken = RemoveActorPeacefullySpell.create(EntityReference.FRIENDLY_MINIONS, matchesToken, true,
				// Retrieve the hp from the token in play right now
				SetAttributeSpell.create(EntityReference.FRIENDLY_HERO, Attribute.HP, AttributeValueProvider.create(Attribute.HP, EntityReference.OUTPUT).create()));
		// Case 2: token in hand
		Condition tokenInHand = AnyMatchFilterCondition.create(EntityReference.FRIENDLY_HAND, matchesToken);
		SpellDesc removeHandCard = RemoveCardSpell.create(EntityReference.FRIENDLY_HAND, matchesToken, true);
		// Case 3: token in deck
		Condition tokenInDeck = AnyMatchFilterCondition.create(EntityReference.FRIENDLY_DECK, matchesToken);
		SpellDesc removeDeckCard = RemoveCardSpell.create(EntityReference.FRIENDLY_DECK, matchesToken, true);

		SpellDesc removeSpell = ConditionalSpell.create(new Condition[]{
				tokenOnBattlefield,
				tokenInHand,
				tokenInDeck
		}, new SpellDesc[]{
				removeToken,
				removeHandCard,
				removeDeckCard
		});

		// Construct the spell
		switchingEnchantment.setSpell(MetaSpell.create(
				// Prevents fatal damage
				ModifyDamageSpell.create(0, AlgebraicOperation.SET),
				CastAfterSequenceSpell.create(MetaSpell.create(
						// Then changes the hero
						ChangeHeroSpell.create(TargetPlayer.SELF, originalHero.getCardId(), false),
						// The remove spell is responsible for setting the HP in the event the token is on the battlefield
						removeSpell))));

		switchingEnchantment.setMaxFires(1);

		// The fatal damage is only prevented if the token is in play, the hand or the deck
		switchingEnchantment.setEventTrigger(FatalDamageTrigger.create(TargetPlayer.BOTH, TargetPlayer.SELF, EntityType.HERO));
		switchingEnchantment.getEventTrigger().put(EventTriggerArg.QUEUE_CONDITION, OrCondition.create(tokenOnBattlefield, tokenInHand, tokenInDeck));
		SpellDesc addEnchantmentSpell = AddEnchantmentSpell.create(EntityReference.FRIENDLY_PLAYER, switchingEnchantment);

		// Final spell
		for (SpellDesc spell : new SpellDesc[]{summonSpell, changeHeroSpell, addEnchantmentSpell, removeSelfSpell}) {
			SpellUtils.castChildSpell(context, player, spell, source, target);
		}
	}
}
