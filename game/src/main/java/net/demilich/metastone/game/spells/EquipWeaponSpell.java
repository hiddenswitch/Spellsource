package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.CardFilter;
import net.demilich.metastone.game.spells.desc.source.CatalogueSource;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.Zones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Equips the specified weapon in {@link SpellArg#CARD} or chooses a random one based on the {@link
 * SpellArg#CARD_FILTER} and {@link SpellArg#CARD_SOURCE} for the specified {@link SpellArg#TARGET_PLAYER}.
 * <p>
 * Equipping weapons this way does not trigger battlecries.
 * <p>
 * When the weapon is equipped, the subspell specified in {@link SpellArg#SPELL} will be cast with the {@link
 * EntityReference#OUTPUT} set to the newly equipped weapon. This is necessary because some effects may result in
 * multiple weapons being equipped and destroyed in a row, and a specific effect applying not to the weapon equipped at
 * the end of the sequence but at the weapon the particular effect equips.
 * <p>
 * For example, to equip a random weapon:
 * <pre>
 *     {
 *         "class": "EquipWeaponSpell",
 *         "cardFilter": {
 *             "class": "CardFilter",
 *             "cardType": "WEAPON"
 *         }
 *     }
 * </pre>
 * To equip a Wicked Knife:
 * <pre>
 *     {
 *         "class": "EquipWeaponSpell",
 *         "card": "weapon_wicked_knife"
 *     }
 * </pre>
 * This replaces {@link EquipRandomWeaponSpell}.
 */
public class EquipWeaponSpell extends Spell {

	private Logger logger = LoggerFactory.getLogger(EquipWeaponSpell.class);

	public static SpellDesc create(String cardId) {
		Map<SpellArg, Object> arguments = new SpellDesc(EquipWeaponSpell.class);
		arguments.put(SpellArg.CARD, cardId);
		arguments.put(SpellArg.TARGET, EntityReference.NONE);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(TargetPlayer targetPlayer, Card weaponCard) {
		Map<SpellArg, Object> arguments = new SpellDesc(EquipWeaponSpell.class);
		arguments.put(SpellArg.CARD, weaponCard);
		arguments.put(SpellArg.TARGET, EntityReference.NONE);
		if (targetPlayer != null) {
			arguments.put(SpellArg.TARGET_PLAYER, targetPlayer);
		}
		return new SpellDesc(arguments);
	}

	/**
	 * Creates this spell. It will equip a random weapon for the casting player.
	 *
	 * @return This spell
	 */
	public static SpellDesc create() {
		Map<SpellArg, Object> arguments = new SpellDesc(EquipWeaponSpell.class);
		arguments.put(SpellArg.CARD_SOURCE, CatalogueSource.create());
		arguments.put(SpellArg.CARD_FILTER, CardFilter.create(CardType.WEAPON));
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(Card weaponCard) {
		return create(TargetPlayer.SELF, weaponCard);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.CARD, SpellArg.CARD_FILTER, SpellArg.CARD_SOURCE, SpellArg.TARGET_PLAYER, SpellArg.SPELL, SpellArg.SPELLS);
		String cardId = (String) desc.get(SpellArg.CARD);
		CardList results = new CardArrayList();
		if (cardId != null) {
			final Card cardById = context.getCardById(cardId);
			if (cardById == null) {
				logger.error("onCast {} {}: The CARD {} was not found.", context.getGameId(), source, cardId);
				return;
			}
			if (!(cardById.getCardType() == CardType.WEAPON)) {
				logger.error("onCast {} {}: The CARD {} is not a weapon that can be equipped.", context.getGameId(), source, cardById);
				return;
			}

			Card weaponCard = cardById;

			results.add(weaponCard);
		} else if (desc.getEntityFilter() != null || desc.getCardSource() != null) {
			results.addAll(desc.getFilteredCards(context, player, source));
			if (results.stream().anyMatch(c -> !(c.getCardType() == CardType.WEAPON))) {
				logger.error("onCast {} {}: The CARD_FILTER {} and CARD_SOURCE {} produced cards that aren't weapon cards", context.getGameId(), source, desc.getCardFilter(), desc.getCardSource());
				return;
			}
		} else {
			logger.error("onCast {} {}: Neither a CARD nor a CARD_SOURCE/CARD_FILTER were specified for this spell", context.getGameId(), source);
			return;
		}

		if (results.isEmpty()) {
			return;
		}

		Weapon weapon = context.getLogic().getRandom(results).createWeapon();
		context.getLogic().equipWeapon(player.getId(), weapon, null, false);
		weapon = player.getHero().getWeapon();
		if (weapon != null && weapon.getZone() == Zones.WEAPON) {
			for (SpellDesc spellDesc : desc.subSpells(0)) {
				if (weapon.isDestroyed()) {
					logger.error("onCast {} {}: Something destroyed the weapon {} before the subspell {} could be cast on it", context.getGameId(), source, weapon, spellDesc);
					continue;
				}

				SpellUtils.castChildSpell(context, player, spellDesc, source, target, weapon);
			}
		}
	}


}
