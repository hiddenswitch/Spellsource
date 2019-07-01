package com.hiddenswitch.spellsource;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public abstract class CardsModule extends AbstractModule {
	protected void add(Class<? extends CardResources> resourcesClass) {
		Multibinder<CardResources> cardsBinder = Multibinder.newSetBinder(binder(), CardResources.class);
		cardsBinder.addBinding().to(resourcesClass);
	}
}
