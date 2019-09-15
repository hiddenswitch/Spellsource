package com.hiddenswitch.spellsource;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * Extend this class to ensure the Spellsource card catalogue can find your cards.
 * <p>
 * The correct way to use this class is to extend it, and then create a method of the following pattern:
 * <pre>
 *   {@code
 *     @Override
 *     protected void configure() {
 *       add(YourCardResources.class);
 *     }
 *   }
 * </pre>
 * where {@code YourCardResources} is the name of your class that extends {@link AbstractCardResources}.
 */
public abstract class CardsModule extends AbstractModule {
	protected void add(Class<? extends CardResources> resourcesClass) {
		Multibinder<CardResources> cardsBinder = Multibinder.newSetBinder(binder(), CardResources.class);
		cardsBinder.addBinding().to(resourcesClass);
	}
}
