open module spellsource.customhearthstone {
	requires spellsource.testutils;
	requires spellsource.game;
	requires annotations.java5;
	requires logback.classic;
	requires org.slf4j;
	requires org.mockito;
	requires co.paralleluniverse.quasar.core;
	requires org.junit.jupiter.api;
	requires org.junit.jupiter.params;
	exports com.hiddenswitch.spellsource.tests.customhearthstone;
}