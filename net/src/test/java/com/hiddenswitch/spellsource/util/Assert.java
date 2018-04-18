package com.hiddenswitch.spellsource.util;

import org.unitils.reflectionassert.ReflectionComparator;
import org.unitils.reflectionassert.ReflectionComparatorFactory;
import org.unitils.reflectionassert.ReflectionComparatorMode;
import org.unitils.reflectionassert.difference.Difference;
import org.unitils.reflectionassert.report.impl.DefaultDifferenceReport;

public class Assert {
	public static void assertReflectionEquals(Object lhs, Object rhs) {
		ReflectionComparator reflectionComparator = ReflectionComparatorFactory.createRefectionComparator(ReflectionComparatorMode.IGNORE_DEFAULTS);
		Difference difference = reflectionComparator.getDifference(lhs, rhs);
		if (difference != null) {
			org.junit.Assert.fail(new DefaultDifferenceReport().createReport(difference));
		}
	}
}
