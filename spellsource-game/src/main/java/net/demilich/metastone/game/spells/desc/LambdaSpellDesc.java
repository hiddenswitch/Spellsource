package net.demilich.metastone.game.spells.desc;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Represents a {@link SpellDesc} created from a {@link LambdaSpell} lambda function.
 * <p>
 * Be careful with variables brought in from outside the lambda. They will not be cloned when the {@link GameContext} is
 * cloned.
 */
public class LambdaSpellDesc extends SpellDesc {

	private final AbstractLambdaSpell lambdaSpell;

	/**
	 * Creates an instance of this class with the specified lambda.
	 *
	 * @param lambdaSpell
	 */
	public LambdaSpellDesc(AbstractLambdaSpell lambdaSpell) {
		super(LambdaSpell.class);
		this.lambdaSpell = lambdaSpell;
	}

	@Override
	public Spell create() {
		return new LambdaSpell(lambdaSpell);
	}

	@Override
	public @NotNull SpellDesc clone() {
		var desc = new LambdaSpellDesc(lambdaSpell);
		copyTo(desc);
		return desc;
	}

	/**
	 * Signature of a spell's {@link Spell#cast(GameContext, Player, SpellDesc, Entity, List)} method.
	 */
	@FunctionalInterface
	public interface AbstractLambdaSpell {
		/**
		 * Casts the spell.
		 *
		 * @param lambdaContext
		 * @param lambdaPlayer
		 * @param lambdaDesc
		 * @param lambdaSource
		 * @param lambdaTargets
		 * @param logger
		 */
		void cast(GameContext lambdaContext, Player lambdaPlayer, SpellDesc lambdaDesc, Entity lambdaSource, List<Entity> lambdaTargets, Logger logger);
	}

	public static class LambdaSpell extends Spell {
		private Logger LOGGER = LoggerFactory.getLogger(getClass());
		private AbstractLambdaSpell lambdaSpell;

		public LambdaSpell(AbstractLambdaSpell lambdaSpell) {
			this.lambdaSpell = lambdaSpell;
		}

		@Override
		public void cast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> targets) {
			lambdaSpell.cast(context, player, desc, source, targets, LOGGER);
		}

		@Override
		protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		}
	}
}
