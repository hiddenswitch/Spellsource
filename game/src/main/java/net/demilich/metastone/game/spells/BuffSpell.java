package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.spells.aura.ModifyBuffSpellAura;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Gives the {@code target} a stats boost of either *+ {@link SpellArg#VALUE} / + {@link SpellArg#VALUE}) or (+ {@link
 * SpellArg#ATTACK_BONUS} / + {@link SpellArg#HP_BONUS} ). If the target is a {@link Hero}, like {@link
 * EntityReference#FRIENDLY_HERO}, {@link SpellArg#ARMOR_BONUS} will give the hero armor.
 * <p>
 * If a {@link net.demilich.metastone.game.spells.aura.ModifyBuffSpellAura} is in play and matches the {@code source},
 * this effect will add the aura's specified attack and HP bonuses to the buff given by this spell <b>only</b> if the
 * buff spell has the specified key.
 * <ul>
 * <li>If this spell has {@link SpellArg#VALUE}, both the aura's {@link AuraArg#ATTACK_BONUS}, {@link AuraArg#HP_BONUS}
 * and {@link AuraArg#VALUE} values will be used.</li>
 * <li>If this spell has {@link SpellArg#ATTACK_BONUS}, the aura's {@link AuraArg#ATTACK_BONUS} will be used.</li>
 * <li>If this spell has {@link SpellArg#HP_BONUS}, the aura's {@link AuraArg#HP_BONUS} will be used</li>
 * <li>{@link AuraArg#VALUE} is always added.</li>
 * </ul>
 * When a {@link ModifyBuffSpellAura} is in play, this effect sets the {@link GameValue#SPELL_VALUE} retrieved by
 * {@link net.demilich.metastone.game.spells.desc.valueprovider.GameValueProvider} (1) to the HP bonus specified on this
 * effect when querying the aura for the amount of additional HP bonus to apply, and then (2) to the attack bonus
 * specified on this effect when querying the aura for the amount of additional attack bonus to apply.
 * <p>
 * For example, this trigger implements "Whenever you cast a spell, gain Armor equal to its Cost:"
 * <pre>
 *   "trigger": {
 *     "eventTrigger": {
 *       "class": "SpellCastedTrigger",
 *       "sourcePlayer": "SELF"
 *     },
 *     "spell": {
 *       "class": "BuffSpell",
 *       "target": "FRIENDLY_HERO",
 *       "armorBonus": {
 *         "class": "ManaCostProvider",
 *         "target": "EVENT_TARGET"
 *       }
 *     }
 *   }
 * </pre>
 * Observe that the {@code "armorBonus"} can be a {@link net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider}.
 * <p>
 * Or, in this example "Battlecry: Shuffle a friendly minion into your deck and give it +3/+3.":
 * <pre>
 *   "battlecry": {
 *     "targetSelection": "FRIENDLY_MINIONS",
 *     "spell": {
 *       "class": "ShuffleMinionToDeckSpell",
 *       "spell": {
 *         "class": "BuffSpell",
 *         "target": "OUTPUT",
 *         "attackBonus": 3,
 *         "hpBonus": 3
 *       },
 *       "howMany": 1
 *     }
 *   },
 * </pre>
 * Here, the target is {@link EntityReference#OUTPUT}, which refers to the card that was shuffled into the player's
 * deck, and the bonuses are expressed as integer values.
 * <p>
 * Weapons will interpret the HP bonus as a benefit to durability. For example, "Give your weapon +1/+1":
 * <pre>
 *     "spell": {
 *       "class": "BuffSpell",
 *       "target": "FRIENDLY_WEAPON",
 *       "attackBonus": 1,
 *       "hpBonus": 1
 *     }
 * </pre>
 *
 * @see AddAttributeSpell to "buff" attributes.
 */
public class BuffSpell extends RevertableSpell {

	private static Logger LOGGER = LoggerFactory.getLogger(BuffSpell.class);

	public static SpellDesc create(EntityReference target, int value) {
		Map<SpellArg, Object> arguments = new SpellDesc(BuffSpell.class);
		arguments.put(SpellArg.VALUE, value);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(EntityReference target, int attackBonus, int hpBonus) {
		Map<SpellArg, Object> arguments = new SpellDesc(BuffSpell.class);
		arguments.put(SpellArg.ATTACK_BONUS, attackBonus);
		arguments.put(SpellArg.HP_BONUS, hpBonus);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(EntityReference target, ValueProvider attackBonus, ValueProvider hpBonus) {
		Map<SpellArg, Object> arguments = new SpellDesc(BuffSpell.class);
		if (attackBonus != null) {
			arguments.put(SpellArg.ATTACK_BONUS, attackBonus);
		}
		if (hpBonus != null) {
			arguments.put(SpellArg.HP_BONUS, hpBonus);
		}
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	@Override
	protected SpellDesc getReverseSpell(GameContext context, Player player, Entity source, SpellDesc desc, EntityReference target) {
		SpellDesc reverse = new SpellDesc(BuffSpell.class, target, null, false);
		if (desc.get(SpellArg.VALUE) instanceof ValueProvider
				|| desc.get(SpellArg.ATTACK_BONUS) instanceof ValueProvider
				|| desc.get(SpellArg.HP_BONUS) instanceof ValueProvider
				|| desc.get(SpellArg.ARMOR_BONUS) instanceof ValueProvider) {
			throw new UnsupportedOperationException("Cannot create a revertTrigger on a BuffSpell that uses a ValueProvider in any of its value fields");
		}
		if (desc.containsKey(SpellArg.VALUE)) {
			reverse.put(SpellArg.VALUE, -desc.getInt(SpellArg.VALUE, 0));
		}
		if (desc.containsKey(SpellArg.ATTACK_BONUS)) {
			reverse.put(SpellArg.ATTACK_BONUS, -desc.getInt(SpellArg.ATTACK_BONUS, 0));
		}
		if (desc.containsKey(SpellArg.HP_BONUS)) {
			reverse.put(SpellArg.HP_BONUS, -desc.getInt(SpellArg.HP_BONUS, 0));
		}
		if (desc.containsKey(SpellArg.ARMOR_BONUS)) {
			reverse.put(SpellArg.ARMOR_BONUS, -desc.getInt(SpellArg.ARMOR_BONUS, 0));
		}
		return reverse;
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		super.onCast(context, player, desc, source, target);
		Objects.requireNonNull(target, source.getSourceCard().getCardId());
		checkArguments(LOGGER, context, source, desc, SpellArg.ATTACK_BONUS, SpellArg.HP_BONUS, SpellArg.ARMOR_BONUS, SpellArg.VALUE, SpellArg.REVERT_TRIGGER);

		int attackBonus = desc.getValue(SpellArg.ATTACK_BONUS, context, player, target, source, 0);
		int hpBonus = desc.getValue(SpellArg.HP_BONUS, context, player, target, source, 0);
		int totalArmorBonus = desc.getValue(SpellArg.ARMOR_BONUS, context, player, target, source, 0);
		int value = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);

		if (value != 0) {
			if (target instanceof Hero) {
				attackBonus = totalArmorBonus = value;
			} else {
				attackBonus = hpBonus = value;
			}
		}

		// Read aura bonuses
		List<ModifyBuffSpellAura> auras = SpellUtils.getAuras(context, ModifyBuffSpellAura.class, source);

		for (ModifyBuffSpellAura aura : auras) {
			context.getSpellValueStack().push(attackBonus);
			int auraAttackBonus = aura.getDesc().getValue(AuraArg.ATTACK_BONUS, context, player, target, source, 0);
			context.getSpellValueStack().pop();
			context.getSpellValueStack().push(hpBonus);
			int auraHpBonus = aura.getDesc().getValue(AuraArg.HP_BONUS, context, player, target, source, 0);
			context.getSpellValueStack().pop();
			int auraValueBonus = aura.getDesc().getValue(AuraArg.VALUE, context, player, target, source, 0);
			if (desc.containsKey(SpellArg.VALUE)) {
				attackBonus += auraAttackBonus + auraValueBonus;
				hpBonus += auraHpBonus + auraValueBonus;
			} else {
				if (desc.containsKey(SpellArg.ATTACK_BONUS)) {
					attackBonus += auraAttackBonus + auraValueBonus;
					hpBonus += auraValueBonus;
				}
				if (desc.containsKey(SpellArg.HP_BONUS)) {
					attackBonus += auraValueBonus;
					hpBonus += auraHpBonus + auraValueBonus;
				}
			}
		}


		if (attackBonus != 0) {
			if (target instanceof Hero) {
				target.modifyAttribute(Attribute.TEMPORARY_ATTACK_BONUS, attackBonus);
			} else {
				target.modifyAttribute(Attribute.ATTACK_BONUS, attackBonus);
			}
		}

		if (hpBonus != 0) {
			if (target instanceof Weapon) {
				context.getLogic().modifyDurability((Weapon) target, hpBonus);
			} else {
				context.getLogic().modifyHpSpell(source, target, hpBonus);
			}
		}

		if (totalArmorBonus != 0) {
			if (target != null && target.getEntityType() == EntityType.HERO) {
				context.getLogic().gainArmor(context.getPlayer(target.getOwner()), totalArmorBonus);
			} else {
				if (target == null) {
					LOGGER.warn("onCast {} {}: Applying armor and calling with a null target", context.getGameId(), source);
				} else if (target.getOwner() != player.getId()) {
					LOGGER.warn("onCast {} {}: Applying armor and calling without a hero target, but a target {} whose owner" +
							" differs from the player {}", context.getGameId(), source, target, player);
				}
				context.getLogic().gainArmor(player, totalArmorBonus);
			}
		}
	}

}

