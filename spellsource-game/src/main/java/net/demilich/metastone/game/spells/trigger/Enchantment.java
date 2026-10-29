package net.demilich.metastone.game.spells.trigger;

import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.desc.HasEntrySet;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityLocation;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.HasValue;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.AddEnchantmentSpell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.aura.DoubleTurnEndTriggersAura;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.IdFactory;
import net.demilich.metastone.game.targeting.TargetSelection;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static net.demilich.metastone.game.GameContext.PLAYER_1;
import static net.demilich.metastone.game.GameContext.PLAYER_2;

/**
 * An enchantment is a type of entity that reacts to certain events using a trigger by casting a spell.
 * <p>
 * Enchantments live inside {@link GameLogic#getActiveTriggers(EntityReference)}; some enchantments, like {@link Secret}
 * and {@link Quest}, live in their respective zones {@link Zones#QUEST} and {@link Zones#SECRET}.
 * <p>
 * Enchantments consistent of two important features: a {@link EventTrigger} built from an {@link EventTriggerDesc}, and
 * a {@link SpellDesc} indicating which spell should be cast when the enchantment's {@link EventTrigger} and its
 * conditions {@link EventTrigger#innerQueues(GameEvent, Enchantment, Entity)}.
 * <p>
 * Enchantments are specified by a {@link EnchantmentDesc} in the card JSON. They can also be specified as fields in
 * e.g., {@link AddEnchantmentSpell}.
 * <p>
 * The lifecycle of an enchantment looks like the following:
 * <ul>
 * <li>{@code onAdd}: Called when the enchantment comes into play. At this moment, the number of times the
 * enchantment has been fired is set to zero; it is not {@code expired}, and </li>
 * <li>{@code onGameEvent}: Gives the enchantment a chance to look at a {@link GameEvent}, evaluate a {@link
 * EventTrigger} against it, and determine whether its {@link EnchantmentDesc#getSpell()} should be cast.</li>
 * <li>{@code onRemove}: Expires the enchantment, ensuring it will never fire again.</li>
 * </ul>
 * An enchantment's lifecycle matches the entity it is hosted by, like an {@link Actor}, the {@link Player} entity (when
 * e.g. {@link AddEnchantmentSpell} has a {@link SpellArg#TARGET} of {@link EntityReference#FRIENDLY_PLAYER}) or itself
 * (in the case of a quest and secret). This means that {@link EntityReference#FRIENDLY_PLAYER} is a natural host for
 * {@link Enchantment} objects that should not be removed for any reason like minion death.
 *
 * @see EnchantmentDesc for a description of the format of an enchantment.
 */
public class Enchantment extends Entity implements Trigger {
	private static final EventTriggerDesc[] DEFAULT_TRIGGERS = new EventTriggerDesc[0];
	private static final Zones[] DEFAULT_BATTLEFIELD_ZONES = new Zones[]{Zones.BATTLEFIELD, Zones.HERO, Zones.HERO_POWER, Zones.WEAPON, Zones.PLAYER, Zones.QUEST, Zones.SECRET};
	private static final Zones[] DEFAULT_PASSIVE_ZONES = new Zones[]{Zones.HERO_POWER, Zones.HAND};
	private static final Zones[] DEFAULT_GAME_ZONES = new Zones[]{Zones.DISCOVER, Zones.SET_ASIDE_ZONE, Zones.DECK, Zones.HAND};
	private static final Zones[] DEFAULT_DECK_ZONES = new Zones[]{Zones.DECK};
	private static final Set<Zones> DEFAULT_BATTLEFIELD_ZONES_SET = Arrays.stream(Enchantment.getDefaultBattlefieldZones()).collect(Collectors.toSet());
	private final HasEntrySet<?, ?> entrySet;
	protected List<EventTrigger> triggers = new ArrayList<>();
	protected List<EventTrigger> expirationTriggers = new ArrayList<>();
	protected List<EventTrigger> activationTriggers = new ArrayList<>();
	protected SpellDesc spell;
	protected EntityReference hostReference;
	protected boolean oneTurn;
	protected boolean activated = true;
	protected boolean expired;
	protected boolean persistentOwner;
	private Integer maxFires;
	private int fires;
	protected boolean keepAfterTransform;
	protected Integer countUntilCast;
	protected boolean countByValue;
	protected boolean usesSpellTrigger = true;
	protected Integer maxFiresPerSequence;
	protected int firesThisSequence;
	protected boolean copyToActor;
	protected Zones[] zones;
	protected boolean added;
	protected int attackBonus;
	protected int hpBonus;
	protected int armorBonus;
	protected int temporaryAttackBonus;
	protected Integer setAttack;
	protected Integer setHp;
	protected Attribute appliedAttribute;
	protected boolean hadNativeAttribute;
	protected Attribute removedAttribute;
	protected Attribute modifiedAttribute;
	protected int attributeDelta;
	protected Attribute cardIdAttribute;
	protected boolean hasRevertableModification;

	public Enchantment() {
		this.entrySet = null;
	}

	private Enchantment(boolean hasTriggers) {
		this.entrySet = null;
		this.expirationTriggers = java.util.Collections.emptyList();
		this.activationTriggers = java.util.Collections.emptyList();
		this.triggers = hasTriggers ? new ArrayList<>(2) : java.util.Collections.emptyList();
	}

	public Enchantment(HasEntrySet<?, ?> entrySet) {
		this.entrySet = entrySet;
	}

	@Override
	public Enchantment clone() {
		Enchantment clone = (Enchantment) super.clone();
		if (this.added) {
			if (this.spell != null && !this.spell.isReadOnly()) {
				clone.spell = this.spell.clone();
			}
			return clone;
		}
		int nTrigs = this.getTriggers().size();
		if (nTrigs == 0) {
			clone.setTriggers(java.util.Collections.emptyList());
		} else {
			List<EventTrigger> clonedTrigs = new ArrayList<>(nTrigs);
			for (int i = 0; i < nTrigs; i++) {
				clonedTrigs.add(this.getTriggers().get(i).clone());
			}
			clone.setTriggers(clonedTrigs);
		}
		int nExpTrigs = this.getExpirationTriggers().size();
		if (nExpTrigs == 0) {
			clone.setExpirationTriggers(java.util.Collections.emptyList());
		} else {
			List<EventTrigger> clonedExpTrigs = new ArrayList<>(nExpTrigs);
			for (int i = 0; i < nExpTrigs; i++) {
				clonedExpTrigs.add(this.getExpirationTriggers().get(i).clone());
			}
			clone.setExpirationTriggers(clonedExpTrigs);
		}
		int nActTrigs = this.getActivationTriggers().size();
		if (nActTrigs == 0) {
			clone.setActivationTriggers(java.util.Collections.emptyList());
		} else {
			List<EventTrigger> clonedActTrigs = new ArrayList<>(nActTrigs);
			for (int i = 0; i < nActTrigs; i++) {
				clonedActTrigs.add(this.getActivationTriggers().get(i).clone());
			}
			clone.setActivationTriggers(clonedActTrigs);
		}
		if (getSpell() != null) {
			clone.spell = getSpell().isReadOnly() ? getSpell() : getSpell().clone();
		}
		return clone;
	}

	protected EventTriggerDesc[] getDefaultTriggers() {
		return DEFAULT_TRIGGERS;
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.ENCHANTMENT;
	}

	public void onRemovedBySilence(GameContext context, Entity host) {
		if (spell != null && getFires() == 0 && host != null && host.isInPlay() && !host.isDestroyed()
				&& getTriggers().stream().anyMatch(t -> t instanceof SilenceTrigger)) {
			setFires(1);
			SpellUtils.castChildSpell(context, context.getPlayer(getOwner()), spell, host, host);
		}
	}

	public void clearRevertableModification() {
		this.hasRevertableModification = false;
	}

	public void expire(GameContext context) {
		if (expired) {
			return;
		}
		expired = true;
		if (context != null) {
			onRemove(context);
		}
	}

	@Override
	public EntityReference getHostReference() {
		return hostReference;
	}

	public SpellDesc getSpell() {
		return spell;
	}

	@Override
	public boolean interestedIn(GameEventType eventType) {
		if (!usesSpellTrigger) {
			return false;
		}
		var trigs = getTriggers();
		for (int i = 0, n = trigs.size(); i < n; i++) {
			var trigger = trigs.get(i);
			var interested = trigger.interestedIn();
			if (interested == eventType || interested == GameEventType.ALL) {
				return true;
			}
		}
		var expTrigs = getExpirationTriggers();
		for (int i = 0, n = expTrigs.size(); i < n; i++) {
			var trigger = expTrigs.get(i);
			var interested = trigger.interestedIn();
			if (interested == eventType || interested == GameEventType.ALL) {
				return true;
			}
		}
		var actTrigs = getActivationTriggers();
		for (int i = 0, n = actTrigs.size(); i < n; i++) {
			var trigger = actTrigs.get(i);
			var interested = trigger.interestedIn();
			if (interested == eventType || interested == GameEventType.ALL) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isExpired() {
		return expired;
	}

	@Override
	public void onAdd(GameContext context, Player player, Entity source, Entity host) {
		added = true;
	}

	/**
	 * Casts the effects of this enchantment.
	 *
	 * @param ownerId
	 * @param spell
	 * @param event
	 * @return {@code true} if processing succeeded.
	 */
	protected boolean process(int ownerId, SpellDesc spell, GameEvent event) {
		if (!usesSpellTrigger) {
			return false;
		}
		// Maybe have to add a thing to prevent the maxfires from counting twice because of TURN_END_TRIGGERS_TWICE
		if (countByValue && event instanceof HasValue) {
			var value = ((HasValue) event).getValue();
			setFires(getFires() + value);
			firesThisSequence += value;
		} else {
			setFires(getFires() + 1);
			firesThisSequence++;
		}

		var spellCasts = true;

		// Prevents infinite looping
		if (maxFiresPerSequence != null && firesThisSequence > maxFiresPerSequence) {
			spellCasts = false;
		}

		if (countUntilCast != null && getFires() < countUntilCast) {
			spellCasts = false;
		}

		if (spellCasts) {
			if (spell != null) {
				cast(ownerId, spell, event);
			} else if (hasRevertableModification) {
				onRemove(event.getGameContext());
			}
		}

		if (getMaxFires() != null
				&& getFires() >= getMaxFires()) {
			expire(event.getGameContext());
		}
		return spellCasts;
	}

	public void onRemove(GameContext context) {
		if (!hasRevertableModification || context == null || hostReference == null) {
			return;
		}
		hasRevertableModification = false;
		Entity host = context.resolveSingleTarget(hostReference, false);
		if (host == null || host.hasAttribute(Attribute.SILENCED)) {
			return;
		}
		if (spell != null && getFires() == 0 && host.isInPlay() && !host.isDestroyed()
				&& getTriggers().stream().anyMatch(t -> t instanceof SilenceTrigger)) {
			setFires(1);
			SpellUtils.castChildSpell(context, context.getPlayer(getOwner()), spell, host, host);
		}
		if (attackBonus != 0) {
			if (host instanceof Hero) {
				if (host.hasAttribute(Attribute.TEMPORARY_ATTACK_BONUS)) {
					int next = host.getAttributeValue(Attribute.TEMPORARY_ATTACK_BONUS) - attackBonus;
					if (next == 0) {
						host.getAttributes().remove(Attribute.TEMPORARY_ATTACK_BONUS);
					} else {
						host.setAttribute(Attribute.TEMPORARY_ATTACK_BONUS, next);
					}
				}
			} else {
				host.modifyAttribute(Attribute.ATTACK_BONUS, -attackBonus);
			}
		}
		if (temporaryAttackBonus != 0) {
			if (host.hasAttribute(Attribute.TEMPORARY_ATTACK_BONUS)) {
				int next = host.getAttributeValue(Attribute.TEMPORARY_ATTACK_BONUS) - temporaryAttackBonus;
				if (next == 0) {
					host.getAttributes().remove(Attribute.TEMPORARY_ATTACK_BONUS);
				} else {
					host.setAttribute(Attribute.TEMPORARY_ATTACK_BONUS, next);
				}
			}
		}
		if (setAttack != null) {
			host.setAttribute(Attribute.ATTACK, host.getAttributeValue(Attribute.BASE_ATTACK));
		}
		if (hpBonus != 0) {
			if (host instanceof Weapon weapon) {
				context.getLogic().modifyDurability(weapon, -hpBonus);
			} else {
				context.getLogic().modifyHpSpell(this, host, -hpBonus);
			}
		}
		if (setHp != null) {
			if (host instanceof Actor actor) {
				int baseHp = actor.getAttributeValue(Attribute.BASE_HP);
				int oldMaxHp = actor.getMaxHp();
				actor.setMaxHp(baseHp);
				if (actor.getHp() > baseHp) {
					actor.setHp(baseHp);
				} else if (oldMaxHp < baseHp) {
					actor.setHp(actor.getHp() + baseHp - oldMaxHp);
				}
			} else if (host instanceof Card) {
				host.getAttributes().remove(Attribute.HP);
			}
		}
		if (armorBonus != 0) {
			if (host.getEntityType() == EntityType.HERO) {
				context.getLogic().gainArmor(context.getPlayer(host.getOwner()), -armorBonus);
			} else if (getOwner() >= 0) {
				context.getLogic().gainArmor(context.getPlayer(getOwner()), -armorBonus);
			}
		}
		if (appliedAttribute != null) {
			Attribute attrToRemove = appliedAttribute;
			appliedAttribute = null;
			if (!hadNativeAttribute && !hasOtherActiveAttributeEnchantment(context, host, attrToRemove, this)) {
				Player ownerPlayer = getOwner() >= 0 ? context.getPlayer(getOwner()) : context.getActivePlayer();
				context.getLogic().removeAttribute(ownerPlayer, null, host, attrToRemove);
			}
		}
		if (removedAttribute != null) {
			context.getLogic().applyAttribute(host, removedAttribute, this);
		}
		if (modifiedAttribute != null && attributeDelta != 0) {
			host.modifyAttribute(modifiedAttribute, -attributeDelta);
		}
		if (cardIdAttribute != null) {
			Player ownerPlayer = getOwner() >= 0 ? context.getPlayer(getOwner()) : context.getActivePlayer();
			context.getLogic().removeAttribute(ownerPlayer, null, host, cardIdAttribute);
		}
	}

	public void setHasRevertableModification(boolean hasRevertableModification) {
		this.hasRevertableModification = hasRevertableModification;
	}

	public static boolean hasActiveAttributeEnchantment(GameContext context, Entity target, Attribute attribute) {
		if (context == null || target == null || attribute == null) {
			return false;
		}
		for (Trigger t : context.getLogic().getActiveTriggers(target.getReference())) {
			if (t instanceof Enchantment ench && !ench.isExpired() && ench.appliedAttribute == attribute) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasOtherActiveAttributeEnchantment(GameContext context, Entity target, Attribute attribute, Enchantment self) {
		if (context == null || target == null || attribute == null) {
			return false;
		}
		for (Trigger t : context.getLogic().getActiveTriggers(target.getReference())) {
			if (t instanceof Enchantment ench && ench != self && !ench.isExpired() && ench.appliedAttribute == attribute) {
				return true;
			}
		}
		return false;
	}

	public static void clearAttackEnchantmentsOnTarget(GameContext context, Entity target) {
		if (context == null || target == null) {
			return;
		}
		for (Trigger t : context.getLogic().getActiveTriggers(target.getReference())) {
			if (t instanceof Enchantment ench && !ench.isExpired()) {
				ench.attackBonus = 0;
				ench.temporaryAttackBonus = 0;
				ench.setAttack = null;
			}
		}
	}

	public static void clearHpEnchantmentsOnTarget(GameContext context, Entity target) {
		if (context == null || target == null) {
			return;
		}
		for (Trigger t : context.getLogic().getActiveTriggers(target.getReference())) {
			if (t instanceof Enchantment ench && !ench.isExpired()) {
				ench.hpBonus = 0;
				ench.setHp = null;
			}
		}
	}

	public static void onAttributeRemovedFromTarget(GameContext context, Entity target, Attribute attribute) {
		if (context == null || target == null || attribute == null) {
			return;
		}
		if (attribute.name().startsWith("AURA_")) {
			return;
		}
		for (Trigger t : context.getLogic().getActiveTriggers(target.getReference())) {
			if (t instanceof Enchantment ench && !ench.isExpired() && ench.appliedAttribute == attribute) {
				ench.appliedAttribute = null;
				if (!ench.usesSpellTrigger
						&& ench.attackBonus == 0
						&& ench.hpBonus == 0
						&& ench.armorBonus == 0
						&& ench.temporaryAttackBonus == 0
						&& ench.setAttack == null
						&& ench.setHp == null
						&& ench.removedAttribute == null
						&& ench.modifiedAttribute == null
						&& ench.cardIdAttribute == null) {
					ench.hasRevertableModification = false;
					ench.expired = true;
				}
			}
		}
	}

	private static Enchantment createInlineEnchantment(SpellDesc desc, Entity source, Entity target) {
		Object rawTrigger = desc != null ? desc.get(SpellArg.REVERT_TRIGGER) : null;
		Object rawSecondTrigger = desc != null ? desc.get(SpellArg.SECOND_REVERT_TRIGGER) : null;
		boolean hasTriggers = rawTrigger != null || rawSecondTrigger != null;
		Enchantment ench = new Enchantment(hasTriggers);
		if (hasTriggers) {
			if (rawTrigger instanceof EventTriggerDesc triggerDesc) {
				ench.getTriggers().add(triggerDesc.create());
			} else if (rawTrigger instanceof EventTrigger trigger) {
				ench.getTriggers().add(trigger.clone());
			}
			if (rawSecondTrigger instanceof EventTriggerDesc secondTriggerDesc) {
				ench.getTriggers().add(secondTriggerDesc.create());
			} else if (rawSecondTrigger instanceof EventTrigger secondTrigger) {
				ench.getTriggers().add(secondTrigger.clone());
			}
			ench.setUsesSpellTrigger(true);
			ench.setMaxFires(1);
		} else {
			ench.setUsesSpellTrigger(false);
			if (target instanceof Card) {
				ench.setCopyToActor(true);
			}
		}
		ench.hasRevertableModification = true;
		Card synthCard = desc != null ? desc.getSynthesizedEnchantmentCard() : null;
		if (synthCard != null) {
			ench.setSourceCard(synthCard);
			ench.setName(synthCard.getName());
		} else if (source != null && source.getSourceCard() != null) {
			ench.setSourceCard(source.getSourceCard());
			ench.setName(source.getSourceCard().getName());
		}
		if (desc != null) {
			String explicitName = desc.getString(SpellArg.NAME);
			if (explicitName != null && !explicitName.isEmpty()) {
				ench.setName(explicitName);
			}
		}
		return ench;
	}

	public static void attachBuffEnchantment(
			GameContext context,
			Player player,
			SpellDesc desc,
			Entity source,
			Entity target,
			int attackBonus,
			int hpBonus,
			int armorBonus) {
		if (target == null
				|| source instanceof Aura
				|| (attackBonus == 0 && hpBonus == 0 && armorBonus == 0)
				|| target.hasAttribute(Attribute.CANT_GAIN_ENCHANTMENTS)) {
			return;
		}
		if (context.getTriggers().size() >= 128
				&& (desc == null || (!desc.containsKey(SpellArg.REVERT_TRIGGER) && !desc.containsKey(SpellArg.SECOND_REVERT_TRIGGER)))) {
			return;
		}
		Enchantment ench = createInlineEnchantment(desc, source, target);
		ench.attackBonus = attackBonus;
		ench.hpBonus = hpBonus;
		ench.armorBonus = armorBonus;
		if (target instanceof Hero && !ench.getUsesSpellTrigger() && attackBonus != 0 && hpBonus == 0 && armorBonus == 0) {
			ench.setOneTurn(true);
		}
		context.getLogic().addEnchantment(player, ench, source, target);
	}

	public static void attachTemporaryAttackEnchantment(
			GameContext context,
			Player player,
			SpellDesc desc,
			Entity source,
			Entity target,
			int temporaryAttackBonus) {
		if (target == null || source instanceof Aura || temporaryAttackBonus == 0 || target.hasAttribute(Attribute.CANT_GAIN_ENCHANTMENTS)) {
			return;
		}
		Enchantment ench = createInlineEnchantment(desc, source, target);
		ench.temporaryAttackBonus = temporaryAttackBonus;
		ench.setOneTurn(true);
		context.getLogic().addEnchantment(player, ench, source, target);
	}

	public static void attachSetAttackEnchantment(
			GameContext context,
			Player player,
			SpellDesc desc,
			Entity source,
			Entity target,
			int attackValue) {
		if (target == null || source instanceof Aura || target.hasAttribute(Attribute.CANT_GAIN_ENCHANTMENTS)) {
			return;
		}
		clearAttackEnchantmentsOnTarget(context, target);
		Enchantment ench = createInlineEnchantment(desc, source, target);
		ench.setAttack = attackValue;
		context.getLogic().addEnchantment(player, ench, source, target);
	}

	public static void attachSetHpEnchantment(
			GameContext context,
			Player player,
			SpellDesc desc,
			Entity source,
			Entity target,
			int hpValue) {
		if (target == null || source instanceof Aura || target.hasAttribute(Attribute.CANT_GAIN_ENCHANTMENTS)) {
			return;
		}
		clearHpEnchantmentsOnTarget(context, target);
		Enchantment ench = createInlineEnchantment(desc, source, target);
		ench.setHp = hpValue;
		context.getLogic().addEnchantment(player, ench, source, target);
	}

	public static void attachAddAttributeEnchantment(
			GameContext context,
			Player player,
			SpellDesc desc,
			Entity source,
			Entity target,
			Attribute attribute) {
		attachAddAttributeEnchantment(context, player, desc, source, target, attribute, false);
	}

	public static void attachAddAttributeEnchantment(
			GameContext context,
			Player player,
			SpellDesc desc,
			Entity source,
			Entity target,
			Attribute attribute,
			boolean alreadyHadAttribute) {
		if (target == null || source instanceof Aura || attribute == null || target.hasAttribute(Attribute.CANT_GAIN_ENCHANTMENTS)) {
			return;
		}
		boolean hasRevert = desc != null && (desc.containsKey(SpellArg.REVERT_TRIGGER) || desc.containsKey(SpellArg.SECOND_REVERT_TRIGGER));
		if (!hasRevert && (attribute.name().startsWith("AURA_") || context.getTriggers().size() >= 128)) {
			return;
		}
		boolean hadNative = alreadyHadAttribute && !hasActiveAttributeEnchantment(context, target, attribute);
		Enchantment ench = createInlineEnchantment(desc, source, target);
		ench.appliedAttribute = attribute;
		ench.hadNativeAttribute = hadNative;
		context.getLogic().addEnchantment(player, ench, source, target);
	}

	public static void attachRemoveAttributeEnchantment(
			GameContext context,
			Player player,
			SpellDesc desc,
			Entity source,
			Entity target,
			Attribute attribute) {
		if (target == null || source instanceof Aura || attribute == null || target.hasAttribute(Attribute.CANT_GAIN_ENCHANTMENTS)) {
			return;
		}
		Enchantment ench = createInlineEnchantment(desc, source, target);
		ench.removedAttribute = attribute;
		context.getLogic().addEnchantment(player, ench, source, target);
	}

	public static void attachModifyAttributeEnchantment(
			GameContext context,
			Player player,
			SpellDesc desc,
			Entity source,
			Entity target,
			Attribute attribute,
			int delta) {
		if (target == null || source instanceof Aura || attribute == null || target.hasAttribute(Attribute.CANT_GAIN_ENCHANTMENTS)) {
			return;
		}
		if (context.getTriggers().size() >= 128
				&& (desc == null || (!desc.containsKey(SpellArg.REVERT_TRIGGER) && !desc.containsKey(SpellArg.SECOND_REVERT_TRIGGER)))) {
			return;
		}
		Enchantment ench = createInlineEnchantment(desc, source, target);
		ench.modifiedAttribute = attribute;
		ench.attributeDelta = delta;
		context.getLogic().addEnchantment(player, ench, source, target);
	}

	public static void attachSetCardEnchantment(
			GameContext context,
			Player player,
			SpellDesc desc,
			Entity source,
			Entity target,
			Attribute cardIdAttribute) {
		if (target == null || source instanceof Aura || cardIdAttribute == null) {
			return;
		}
		Enchantment ench = createInlineEnchantment(desc, source, target);
		ench.cardIdAttribute = cardIdAttribute;
		context.getLogic().addEnchantment(player, ench, source, target);
	}

	protected void cast(int ownerId, SpellDesc spell, GameEvent event) {
		if (event.getEventType().equals(GameEventType.TURN_END) && SpellUtils.getAuras(event.getGameContext(), ownerId, DoubleTurnEndTriggersAura.class).size() > 0) {
			event.getGameContext().getLogic().castSpell(ownerId, spell, hostReference, EntityReference.NONE, TargetSelection.NONE, false, null);
		}
		event.getGameContext().getLogic().castSpell(ownerId, spell, hostReference, EntityReference.NONE, TargetSelection.NONE, false, null);
	}

	@Override
	public void onGameEvent(GameEvent event) {
		if (!isActivated() || isExpired() || !usesSpellTrigger) {
			return;
		}

		int ownerId = getOwner();

		// Expire the trigger beforehand, in case of copying minion (Echoing
		// Ooze). Since this method should only be called
		// after being checked to be played, copying one-turn triggers should no
		// longer matter.
		// But let's check to make sure we don't accidentally expire something
		// that's still using it.
		if (oneTurn
				&& (event.getEventType() == GameEventType.TURN_END || event.getEventType() == GameEventType.TURN_START)) {
			expire(event.getGameContext());
		}

		// Notify the game context that a spell trigger was successfully fired, as long as it wasn't due to a
		// board changed event.
		if (shouldNotifyEnchantmentFired(event)) {
			event.getGameContext().onEnchantmentFired(this);
		}
		process(ownerId, getSpell(), event);
	}

	protected boolean shouldNotifyEnchantmentFired(GameEvent event) {
		return event.getEventType() != GameEventType.BOARD_CHANGED
				&& event.getEventType() != GameEventType.WILL_END_SEQUENCE
				&& getTriggers().stream().noneMatch(trigger -> trigger.interestedIn() == GameEventType.ALL)
				&& hostReference != null
				&& !(hostReference.equals(new EntityReference(PLAYER_1))
				|| hostReference.equals(new EntityReference(PLAYER_2)));
	}

	@Override
	public void setHostReference(EntityReference reference) {
		hostReference = reference;
	}

	@Override
	public Enchantment getCopy() {
		var clone = this.clone();
		clone.setId(IdFactory.UNASSIGNED);
		clone.setEntityLocation(EntityLocation.UNASSIGNED);
		clone.hostReference = EntityReference.NONE;
		clone.added = false;
		clone.expired = false;
		clone.fires = 0;
		if (!isPersistentOwner()) {
			clone.setOwner(NO_OWNER);
		}
		return clone;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("host", getHostReference())
				.append("id", getId())
				.append("sourceCard", getSourceCard())
				.toString();
	}

	@Override
	public final boolean queues(GameEvent event) {
		// Expired triggers do not queue
		if (isExpired()) {
			return false;
		}

		Entity host = event.getGameContext().resolveSingleTarget(hostReference, false);

		// If the host of the enchantment has been removed from play, this enchantment should not queue
		if (host.getZone() == Zones.REMOVED_FROM_PLAY) {
			return false;
		}

		if (!innerQueues(event, host)) {
			return false;
		}

		var eventType = event.getEventType();
		if (!isActivated()) {
			var actTrigs = getActivationTriggers();
			for (int i = 0, n = actTrigs.size(); i < n; i++) {
				EventTrigger trigger = actTrigs.get(i);
				if (trigger == null) {
					continue;
				}
				var interested = trigger.interestedIn();
				if (interested != eventType && interested != GameEventType.ALL) {
					continue;
				}
				if (trigger.queues(event, this, host, getOwner())) {
					activate();
					break;
				}
			}
		}

		if (!isActivated()) {
			return false;
		}

		// Check for expiration next
		// Must have activated in order to expire
		var expTrigs = getExpirationTriggers();
		if (!isExpired() && !expTrigs.isEmpty()) {
			for (int i = 0, n = expTrigs.size(); i < n; i++) {
				EventTrigger trigger = expTrigs.get(i);
				if (trigger == null) {
					continue;
				}
				var interested = trigger.interestedIn();
				if (interested != eventType && interested != GameEventType.ALL) {
					continue;
				}
				if (trigger.queues(event, this, host, getOwner())) {
					expire(event.getGameContext());
					return false;
				}
			}
		}

		var trigs = getTriggers();
		for (int i = 0, n = trigs.size(); i < n; i++) {
			EventTrigger trigger = trigs.get(i);
			if (trigger == null) {
				continue;
			}
			var interested = trigger.interestedIn();
			if (interested != eventType && interested != GameEventType.ALL) {
				continue;
			}
			if (trigger.queues(event, this, host, getOwner())) {
				return true;
			}
		}

		return false;
	}

	protected boolean innerQueues(GameEvent event, Entity host) {
		var hostZone = host.getZone();
		for (var z : getZones()) {
			if (z == hostZone) {
				return true;
			}
		}
		return false;
	}

	public boolean isPersistentOwner() {
		return persistentOwner;
	}

	public void setPersistentOwner(boolean persistentOwner) {
		this.persistentOwner = persistentOwner;
	}

	public boolean oneTurnOnly() {
		return oneTurn;
	}

	public boolean fires(GameEvent event) {
		// Expired
		if (!isActivated() || isExpired()) {
			return false;
		}
		var host = event.getGameContext().resolveSingleTarget(hostReference, false);

		var trigs = getTriggers();
		for (int i = 0, n = trigs.size(); i < n; i++) {
			if (trigs.get(i).fires(event, host, getOwner())) {
				return true;
			}
		}

		return false;
	}

	public Card getSourceCard() {
		return sourceCard;
	}

	public int getFires() {
		return fires;
	}

	public Integer getMaxFires() {
		return maxFires;
	}

	public void setMaxFires(Integer maxFires) {
		this.maxFires = maxFires;
	}

	public void setKeepAfterTransform(boolean keepAfterTransform) {
		this.keepAfterTransform = keepAfterTransform;
	}

	public boolean isKeptAfterTransform() {
		return keepAfterTransform;
	}

	public void setCountUntilCast(Integer countUntilCast) {
		this.countUntilCast = countUntilCast;
	}

	public Integer getCountUntilCast() {
		return countUntilCast;
	}

	public Enchantment setSpell(SpellDesc spell) {
		this.spell = spell;
		return this;
	}

	public void setCountByValue(boolean countByValue) {
		this.countByValue = countByValue;
	}

	/**
	 * Indicates that the {@link HasValue#getValue()} of the event should be used to increment the enchantment's counter
	 * (typically its {@link #fires}) instead of the value 1 (i.e., every event that causes a trigger to fire increments
	 * the number of fires by 1).
	 *
	 * @return {@code true} if the enchantment increases its fires counter by the event's value.
	 */
	public boolean isCountByValue() {
		return countByValue;
	}

	/**
	 * Get the triggers that fire this enchantment.
	 *
	 * @return A list of triggers.
	 */
	public List<EventTrigger> getTriggers() {
		return triggers;
	}

	public void setMaxFiresPerSequence(Integer maxFiresPerSequence) {
		this.maxFiresPerSequence = maxFiresPerSequence;
	}

	public Integer getMaxFiresPerSequence() {
		return maxFiresPerSequence;
	}

	/**
	 * Signals to the enchantment that the currently processing sequence is over.
	 */
	public void endOfSequence() {
		firesThisSequence = 0;
	}

	public int getFiresThisSequence() {
		return firesThisSequence;
	}

	public Enchantment setTriggers(List<EventTrigger> triggers) {
		this.triggers = triggers;
		return this;
	}

	public void setExpirationTriggers(List<EventTrigger> expirationTriggers) {
		this.expirationTriggers = expirationTriggers;
	}

	public List<EventTrigger> getExpirationTriggers() {
		return expirationTriggers;
	}

	@Override
	public boolean isActivated() {
		return activated;
	}

	public void activate() {
		activated = true;
	}

	public List<EventTrigger> getActivationTriggers() {
		return activationTriggers;
	}

	public void setActivationTriggers(List<EventTrigger> activationTriggers) {
		this.activationTriggers = activationTriggers;
	}

	public Enchantment setActivated(boolean activated) {
		this.activated = activated;
		return this;
	}

	public Zones[] getZones() {
		return (zones == null || zones.length == 0) ? getDefaultZones() : zones;
	}

	public Enchantment setZones(Zones[] zones) {
		this.zones = zones;
		return this;
	}

	protected Zones[] getDefaultZones() {
		return DEFAULT_BATTLEFIELD_ZONES;
	}

	public Enchantment setOneTurn(boolean oneTurn) {
		this.oneTurn = oneTurn;
		return this;
	}

	public boolean getOneTurn() {
		return oneTurn;
	}

	public void setUsesSpellTrigger(boolean usesSpellTrigger) {
		this.usesSpellTrigger = usesSpellTrigger;
	}

	public boolean getUsesSpellTrigger() {
		return usesSpellTrigger;
	}

	protected Enchantment setFires(int fires) {
		this.fires = fires;
		return this;
	}


	public static Zones[] getDefaultBattlefieldZones() {
		return DEFAULT_BATTLEFIELD_ZONES;
	}

	public static Zones[] getDefaultPassiveZones() {
		return DEFAULT_PASSIVE_ZONES;
	}

	public static Zones[] getDefaultGameZones() {
		return DEFAULT_GAME_ZONES;
	}

	public static Zones[] getDefaultDeckZones() {
		return DEFAULT_DECK_ZONES;
	}

	public static Set<Zones> getDefaultBattlefieldZonesSet() {
		return DEFAULT_BATTLEFIELD_ZONES_SET;
	}

	public boolean isCopyToActor() {
		return copyToActor;
	}

	public Enchantment setCopyToActor(boolean giveToActor) {
		this.copyToActor = giveToActor;
		return this;
	}

	public HasEntrySet<?, ?> getEntrySet() {
		return entrySet;
	}

	public int getAttackBonus() {
		return attackBonus;
	}

	public int getHpBonus() {
		return hpBonus;
	}

	public int getArmorBonus() {
		return armorBonus;
	}

	public int getTemporaryAttackBonus() {
		return temporaryAttackBonus;
	}

	public Integer getSetAttack() {
		return setAttack;
	}

	public Integer getSetHp() {
		return setHp;
	}

	public Attribute getAppliedAttribute() {
		return appliedAttribute;
	}

	public Attribute getRemovedAttribute() {
		return removedAttribute;
	}

	public Attribute getModifiedAttribute() {
		return modifiedAttribute;
	}

	public int getAttributeDelta() {
		return attributeDelta;
	}

	public Attribute getCardIdAttribute() {
		return cardIdAttribute;
	}

	public String getRuntimeDescription() {
		if (attackBonus != 0 && hpBonus != 0) {
			return (attackBonus >= 0 ? "+" : "") + attackBonus + "/" + (hpBonus >= 0 ? "+" : "") + hpBonus + ".";
		}
		if (attackBonus != 0) {
			return (attackBonus >= 0 ? "+" : "") + attackBonus + " Attack.";
		}
		if (hpBonus != 0) {
			return (hpBonus >= 0 ? "+" : "") + hpBonus + " Health.";
		}
		if (temporaryAttackBonus != 0) {
			return (temporaryAttackBonus >= 0 ? "+" : "") + temporaryAttackBonus + " Attack this turn.";
		}
		if (armorBonus != 0) {
			return (armorBonus >= 0 ? "+" : "") + armorBonus + " Armor.";
		}
		if (setAttack != null && setHp != null) {
			return "Stats set to " + setAttack + "/" + setHp + ".";
		}
		if (setAttack != null) {
			return "Attack set to " + setAttack + ".";
		}
		if (setHp != null) {
			return "Health set to " + setHp + ".";
		}
		if (appliedAttribute != null) {
			return formatAttributeName(appliedAttribute);
		}
		if (modifiedAttribute != null && attributeDelta != 0) {
			return (attributeDelta >= 0 ? "+" : "") + attributeDelta + " " + formatAttributeName(modifiedAttribute) + ".";
		}
		if (hasAttribute(Attribute.DESCRIPTION) && getAttribute(Attribute.DESCRIPTION) instanceof String customDesc && !customDesc.isEmpty()) {
			return customDesc;
		}
		return null;
	}

	private static String formatAttributeName(Attribute attr) {
		return switch (attr) {
			case TAUNT, AURA_TAUNT -> "Taunt.";
			case DIVINE_SHIELD -> "Divine Shield.";
			case WINDFURY, AURA_WINDFURY -> "Windfury.";
			case POISONOUS, AURA_POISONOUS -> "Poisonous.";
			case LIFESTEAL, AURA_LIFESTEAL -> "Lifesteal.";
			case STEALTH, AURA_STEALTH -> "Stealth.";
			case FROZEN -> "Frozen.";
			case RUSH, AURA_RUSH -> "Rush.";
			case CHARGE, AURA_CHARGE -> "Charge.";
			case IMMUNE, AURA_IMMUNE -> "Immune.";
			case DEFLECT -> "Deflect.";
			case UNTARGETABLE_BY_SPELLS, AURA_UNTARGETABLE_BY_SPELLS -> "Elusive.";
			case CANNOT_ATTACK, AURA_CANNOT_ATTACK -> "Can't Attack.";
			case SPELL_DAMAGE, AURA_SPELL_DAMAGE -> "Spell Damage.";
			default -> {
				String[] words = attr.name().toLowerCase().split("_");
				StringBuilder sb = new StringBuilder();
				for (String w : words) {
					if (w.isEmpty()) continue;
					if (!sb.isEmpty()) sb.append(' ');
					sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
				}
				yield sb.append('.').toString();
			}
		};
	}
}