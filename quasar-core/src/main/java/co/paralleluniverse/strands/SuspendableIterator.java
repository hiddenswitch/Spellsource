package co.paralleluniverse.strands;

import co.paralleluniverse.fibers.Suspendable;

import java.util.Iterator;

public interface SuspendableIterator<T> extends Iterator<T> {
	@Override
	@Suspendable
	T next();

	@Override
	@Suspendable
	boolean hasNext();
}
