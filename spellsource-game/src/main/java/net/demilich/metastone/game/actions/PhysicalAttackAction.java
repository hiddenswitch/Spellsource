package net.demilich.metastone.game.actions;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.rpc.Spellsource.ActionTypeMessage.ActionType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.cards.Attribute;

/**
 * Indicates an attack between {@link #getAttackerReference()} and {@link #getTargetReference()}.
 */
public class PhysicalAttackAction extends GameAction {

	private PhysicalAttackAction() {
		super();
		setActionType(ActionType.PHYSICAL_ATTACK);
		setTargetRequirement(TargetSelection.ENEMY_CHARACTERS);
	}

	@Override
	public PhysicalAttackAction clone() {
		return (PhysicalAttackAction) super.clone();
	}

	/**
	 * Creates a physical attack with the specified {@code attackerReference} as the attacker. This should be an {@code
	 * Actor}.
	 *
	 * @param attackerReference
	 */
	public PhysicalAttackAction(EntityReference attackerReference) {
		this();
		this.setSourceReference(attackerReference);
	}

	@Override
	public boolean canBeExecutedOn(GameContext context, Player player, Entity entity) {
		if (!super.canBeExecutedOn(context, player, entity)) {
			return false;
		}
		if (entity.getEntityType() != EntityType.HERO) {
			return true;
		}
		Actor attacker = (Actor) context.resolveSingleTarget(getSourceReference());
		if (attacker.hasAttribute(Attribute.CANNOT_ATTACK_HEROES) || attacker.hasAttribute(Attribute.AURA_CANNOT_ATTACK_HEROES) ||
				((attacker.hasAttribute(Attribute.RUSH) || attacker.hasAttribute(Attribute.AURA_RUSH))
						&& attacker.hasAttribute(Attribute.SUMMONING_SICKNESS)
						&& !(attacker.hasAttribute(Attribute.CHARGE) || attacker.hasAttribute(Attribute.AURA_CHARGE)))) {
			return false;
		}
		return true;
	}

	@Override
	@Suspendable
	public void execute(GameContext context, int playerId) {
		Actor defender = (Actor) context.resolveSingleTarget(getTargetReference());
		Actor attacker = (Actor) context.resolveSingleTarget(getSourceReference());

		context.getLogic().fight(context.getPlayer(playerId), attacker, defender, this);
	}

	public EntityReference getAttackerReference() {
		return getSourceReference();
	}

	@Override
	public String getDescription(GameContext context, int playerId) {
		Actor defender = (Actor) context.resolveSingleTarget(getTargetReference());
		Actor attacker = (Actor) context.resolveSingleTarget(getSourceReference());
		return String.format("%s attacked %s.", attacker.getName(), defender.getName());
	}
}
