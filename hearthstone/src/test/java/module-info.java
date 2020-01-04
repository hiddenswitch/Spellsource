open module spellsource.hearthstone {
	requires spellsource.testutils;
	requires spellsource.game;
	requires annotations.java5;
	requires logback.classic;
	requires org.slf4j;
	requires org.mockito;
	requires co.paralleluniverse.quasar.core;
	requires vertx.core;
	requires org.junit.jupiter.api;
	requires org.junit.jupiter.params;
	requires reflections;
	requires com.google.common;
	exports com.hiddenswitch.spellsource.tests.hearthstone;
}