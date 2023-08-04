package com.spellsource.tasks;

import java.util.Locale;

/**
 * Utilities for GraalVM.
 */
public final class GraalUtil {

	/**
	 * @return Return whether the JVM in use a GraalVM JVM.
	 */
	public static boolean isGraalJVM() {
		return isGraal("jvmci.Compiler", "java.vendor.version", "java.vendor");
	}

	private static boolean isGraal(String... props) {
		for (String prop : props) {
			String vv = System.getProperty(prop);
			if (vv != null && vv.toLowerCase(Locale.ENGLISH).contains("graal")) {
				return true;
			}
		}
		return false;
	}
}