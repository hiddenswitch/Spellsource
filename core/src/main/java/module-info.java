open module spellsource.core {
	requires vertx.core;
	requires com.fasterxml.jackson.databind;
	requires annotations;
	requires io.github.classgraph;
	requires org.slf4j;
	requires com.google.common;
	exports com.hiddenswitch.spellsource.core;
}