package net.demilich.metastone.game.entities.heroes;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;

/**
 * All the hero classes, including special hero class specifiers, in Spellsource.
 */
public class HeroClass {

	public static final String INHERIT = "INHERIT";
	public static final String SELF = "SELF";
	public static final String OPPONENT = "OPPONENT";
	public static final String ANY = "ANY";
	public static final String BROWN = "BROWN";
	public static final String GREEN = "GREEN";
	public static final String BLUE = "BLUE";
	public static final String GOLD = "GOLD";
	public static final String WHITE = "WHITE";
	public static final String BLACK = "BLACK";
	public static final String SILVER = "SILVER";
	public static final String VIOLET = "VIOLET";
	public static final String RED = "RED";
	public static final String OLIVE = "OLIVE";
	public static final String TEST = "TEST";
	public static final String CORAL = "CORAL";
	public static final String COPPER = "COPPER";
	public static final String DARKBLUE = "DARKBLUE";
	public static final String BLOOD = "BLOOD";
	public static final String NAVY = "NAVY";
	public static final String CAMO = "CAMO";
	public static final String BLUEGREY = "BLUEGREY";
	public static final String CRIMSON = "CRIMSON";
	public static final String MAGENTA = "MAGENTA";
	public static final String TWILIGHT = "TWILIGHT";
	public static final String CANDY = "CANDY";
	public static final String PEACH = "PEACH";


	/**
	 * Checks if the specified card has the specified hero class, respecting a {@link HeroClass#SELF} and a {@link
	 * HeroClass#OPPONENT} specs.
	 *
	 * @param context
	 * @param player
	 * @param card
	 * @param heroClass
	 * @return
	 */
	public static boolean hasHeroClass(GameContext context, Player player, Card card, String heroClass) {
		if (heroClass.equals(OPPONENT)) {
			heroClass = context.getOpponent(player).getHero().getHeroClass();
		} else if (heroClass.equals(SELF)) {
			heroClass = player.getHero().getHeroClass();
		}

		if (heroClass != null && card.hasHeroClass(heroClass)) {
			return false;
		}

		return true;
	}
}
