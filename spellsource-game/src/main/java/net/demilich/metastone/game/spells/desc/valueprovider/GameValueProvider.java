package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.spells.GameValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to retrieve a variable calculated in a {@link net.demilich.metastone.game.spells.MetaSpell}.
 * <p>
 * This value provider lets you "bake in" or calculate a value at one point of time, using it at a later point. This is
 * especially useful for effects that deal damage based on the number of minions destroyed, for example; an earlier part
 * of the spell destroyed the minions you need to count, so you can count how many minions were on the board before the
 * destroy spell was run, and then see how many of those old ones still remain.
 * <p>
 * To retrieve the {@link net.demilich.metastone.game.spells.desc.SpellArg#VALUE} from a {@link
 * net.demilich.metastone.game.spells.MetaSpell}, use {@link GameValue#SPELL_VALUE}:
 * <pre>
 *   {
 *     "class": "MetaSpell",
 *     "value": { ... },
 *     "spells": [
 *       {
 *         "class": "DamageSpell",
 *         "value": {
 *           "class": "GameValueProvider",
 *           "gameValue": "SPELL_VALUE"
 *         }
 *       }
 *     ]
 *   }
 * </pre>
 *
 * @see net.demilich.metastone.game.spells.MetaSpell for a complete description on how to use this value provider.
 */
public class GameValueProvider extends ValueProvider {

	private static Logger LOGGER = LoggerFactory.getLogger(GameValueProvider.class);

	public GameValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		GameValue gameValue = (GameValue) getDesc().get(ValueProviderArg.GAME_VALUE);
		switch (gameValue) {
			case LAST_MANA_COST:
				return (int) context.getEnvironment().get(Environment.LAST_MANA_COST);
			case SPELL_VALUE:
				// Query the top of the stack since that's almost always what's intended.
				if (context.getSpellValueStack().isEmpty()) {
					LOGGER.error("provideValue {} {}: The stack is empty!", context.getGameId(), host);
					return 0;
				}
				return context.getSpellValueStack().peekLast();
			default:
				break;
		}
		return 0;
	}

}
