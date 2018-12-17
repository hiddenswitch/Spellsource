package net.demilich.metastone.game.spells;

/**
 * @deprecated Use a {@link RemoveCardSpell} with a sub spell like {@code {"class": "RemoveCardSpell", "spell":
 * 		{"class": ..., "target": "OUTPUT"}}} instead.
 */
@Deprecated
public class RemoveCardAndDoSomethingSpell extends RemoveCardSpell {
	private static final long serialVersionUID = -7741554759979817421L;
}
