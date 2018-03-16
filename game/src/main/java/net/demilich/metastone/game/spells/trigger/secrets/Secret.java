package net.demilich.metastone.game.spells.trigger.secrets;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.TriggerDesc;
import net.demilich.metastone.game.spells.trigger.EventTrigger;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.utils.AttributeMap;

public class Secret extends Enchantment {
	public Secret(EventTrigger trigger, SpellDesc spell, Card source) {
		super(trigger, spell);
		setSourceCard(source);
		setMaxFires(1);
		setAttributes((AttributeMap) source.getAttributes().clone());
	}

	public Secret(TriggerDesc desc, Card source) {
		this(desc.eventTrigger.create(), desc.spell, source);
		setCountUntilCast(desc.countUntilCast);
		if (desc.maxFires == null) {
			setMaxFires(1);
		} else {
			setMaxFires(desc.maxFires);
		}
		setKeepAfterTransform(desc.keepAfterTransform);
		setCountByValue(desc.countByValue);
		setPersistentOwner(desc.persistentOwner);
	}

	@Override
	@Suspendable
	protected boolean onFire(int ownerId, SpellDesc spell, GameEvent event) {
		boolean spellCasts = super.onFire(ownerId, spell, event);
		if (spellCasts) {
			Player owner = event.getGameContext().getPlayer(ownerId);
			event.getGameContext().getLogic().secretTriggered(owner, this);
			expire();
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
		Secret clone = (Secret) super.clone();
		clone.setSourceCard(getSourceCard());
		return clone;
	}
}
