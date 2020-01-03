module game {
	requires io.opentracing.api;
	requires io.opentracing.util;
	requires annotations.java5;
	requires co.paralleluniverse.quasar.core;
	requires commons.math3;
	requires logback.classic;
	requires slf4j.api;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.core;
	requires core;
	requires com.google.common;
	requires com.fasterxml.jackson.annotation;
	requires vertx.core;
	requires commons.lang3;
	exports com.hiddenswitch.spellsource.cards.base;
	exports com.hiddenswitch.spellsource.common;
	exports com.hiddenswitch.spellsource.draft;
	exports com.hiddenswitch.spellsource.util;
	exports net.demilich.metastone.game;
	exports net.demilich.metastone.game.cards;
	exports net.demilich.metastone.game.cards.desc;
	exports net.demilich.metastone.game.actions;
	exports net.demilich.metastone.game.decks;
	exports net.demilich.metastone.game.behaviour;
	exports net.demilich.metastone.game.logic;
}