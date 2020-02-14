package com.hiddenswitch.spellsource.core;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * Implementors of this interface will get loaded by the {@code CardCatalogue} to find all the cards available to play
 * in the game.
 *
 * @see AbstractCardResources for more about adding more cards to Spellsource.
 */
public interface CardResources extends AutoCloseable {

	AutoCloseable load();

	List<? extends CardResource> getResources();

	default List<String> getDraftBannedCardIds() {
		return Collections.emptyList();
	}

	default List<String> getHardRemovalCardIds() {
		return Collections.emptyList();
	}
}

