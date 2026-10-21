package net.demilich.metastone.game.cards.desc;

import java.util.List;

/**
 * Service provider interface allowing external card modules (such as {@code spellsource-cards-private}) to register
 * additional packages for resolving card JSON component classes in {@link DescDeserializer}.
 */
public interface ComponentPackageProvider {
	/**
	 * Returns additional fully-qualified package names to search when resolving concrete component classes for the
	 * given base component class (e.g., {@code Spell.class}, {@code Condition.class}, {@code ValueProvider.class}).
	 *
	 * @param abstractComponentClass The base component class.
	 * @return A list of package names to search.
	 */
	List<String> getPackages(Class<?> abstractComponentClass);
}
