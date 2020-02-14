open module spellsource.testutils {
	requires spellsource.game;
	requires spellsource.client;
	requires com.fasterxml.jackson.core;
	requires vertx.core;
	requires spellsource.core;
	requires org.junit.jupiter.api;
	requires commons.io;
	requires co.paralleluniverse.quasar.core;
	requires org.mockito;
	requires logback.classic;
	requires org.slf4j;
	requires com.google.common;
	requires annotations;
	requires org.apache.commons.lang3;
	exports com.hiddenswitch.spellsource.cards.test;
	exports com.hiddenswitch.spellsource.testutils;
	exports net.demilich.metastone.tests.util;
}