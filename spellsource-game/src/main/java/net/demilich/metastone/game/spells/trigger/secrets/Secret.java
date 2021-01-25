package net.demilich.metastone.game.spells.trigger.secrets;

import co.paralleluniverse.fibers.Suspendable;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.desc.EnchantmentSerializer;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.aura.SecretsTriggerTwiceAura;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.EventTrigger;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;

@JsonSerialize(using = EnchantmentSerializer.class)
public class Secret extends Enchantment {
	private static final Zones[] ZONES = new Zones[]{Zones.SECRET};

	public Secret(EventTrigger trigger, SpellDesc spell, Card source) {
		super();
		getTriggers().add(trigger);
		setSpell(spell);
		setSourceCard(source);
		setMaxFires(1);
		getAttributes().putAll(source.getAttributes());
	}

	public Secret(EnchantmentDesc desc, Card source) {
		this(desc.getEventTrigger().create(), desc.getSpell(), source);
		setCountUntilCast(desc.getCountUntilCast());
		if (desc.getMaxFires() == null) {
			setMaxFires(1);
		} else {
			setMaxFires(desc.getMaxFires());
		}
		setKeepAfterTransform(desc.isKeepAfterTransform());
		setCountByValue(desc.isCountByValue());
		setPersistentOwner(desc.isPersistentOwner());
	}

	@Override
	@Suspendable
	protected boolean process(int ownerId, SpellDesc spell, GameEvent event) {
		boolean spellCasts = super.process(ownerId, spell, event);
		if (isInPlay() && spellCasts) {
			expire(event.getGameContext());
			Player owner = event.getGameContext().getPlayer(ownerId);
			event.getGameContext().getLogic().secretTriggered(owner, this);
		}
		return spellCasts;
	}

	@Override
	@Suspendable
	public void onGameEvent(GameEvent event) {
		if (event.getGameContext().getActivePlayerId() == getOwner()) {
			return;
		}
		super.onGameEvent(event);
	}

	@Override
	public String getName() {
		return getSourceCard().getName();
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.SECRET;
	}

	@Override
	public Secret clone() {
		return (Secret) super.clone();
	}

	@Override
	@Suspendable
	protected void cast(int ownerId, SpellDesc spell, GameEvent event) {
		if (SpellUtils.hasAura(event.getGameContext(), ownerId, SecretsTriggerTwiceAura.class)) {
			event.getGameContext().getLogic().castSpell(ownerId, spell, hostReference, EntityReference.NONE, TargetSelection.NONE, false, null);
		}
		super.cast(ownerId, spell, event);
	}

	@Override
	public Zones[] getZones() {
		return ZONES;
	}
}
