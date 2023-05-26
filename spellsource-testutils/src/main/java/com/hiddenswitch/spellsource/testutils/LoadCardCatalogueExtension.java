package com.hiddenswitch.spellsource.testutils;

import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * This extension is loaded whenever spellsource-testutils is used. Its purpose is to efficiently load all the cards
 * from the classpath into the classpath card catalogue. This prevents onerous lock contention.
 */
public class LoadCardCatalogueExtension implements ExtensionContext.Store.CloseableResource, BeforeAllCallback {
	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		ClasspathCardCatalogue.CLASSPATH.loadCardsFromPackage();
	}

	@Override
	public void close() throws Throwable {
	}
}
