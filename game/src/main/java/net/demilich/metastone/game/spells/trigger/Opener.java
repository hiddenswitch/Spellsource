package net.demilich.metastone.game.spells.trigger;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.OpenerDesc;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public final class Opener extends Enchantment {

	private final OpenerDesc openerDesc;

	public Opener(OpenerDesc desc, Card sourceCard, Entity host) {
		super();
		setHostReference(host.getReference());
		setSourceCard(sourceCard);
		openerDesc = desc;
	}

	public OpenerDesc getOpenerDesc() {
		return openerDesc;
	}

	@Override
	protected boolean innerQueues(GameEvent event, Entity host) {
		// Openers never fire like regular enchantments
		return false;
	}

	@Override
	@Suspendable
	protected void cast(int ownerId, SpellDesc spell, GameEvent event) {
	}

	@Override
	public boolean isCopyToActor() {
		return true;
	}
}
