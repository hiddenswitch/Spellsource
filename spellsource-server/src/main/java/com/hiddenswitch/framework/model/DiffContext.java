package com.hiddenswitch.framework.model;


import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Implementors of this interface support being notified of changes to an ordered array.
 *
 * @param <RECORD> Value type
 * @param <KEY> Key type
 */
public interface DiffContext<RECORD, KEY> {
	/**
	 * Indicates that the given ID should be removed.
	 *
	 * @param id The ID of the record to remove.
	 */
	void removed(KEY id);

	/**
	 * Indicates that the new document with the given ID should be inserted before the given {@code beforeId}. If {@code
	 * beforeId} is {@code null}, this document should be added to the very end of the list.
	 *
	 * @param newDocId
	 * @param newDoc
	 * @param beforeId The document to insert {@code newDoc} before, or {@code null} if the document should be added to
	 *                 the end of the list.
	 */
	void addedBefore(KEY newDocId, RECORD newDoc, @Nullable KEY beforeId);

	/**
	 * A document with the specified ID has been added.
	 * @param newDocId
	 * @param newDoc
	 */
	void added(KEY newDocId, RECORD newDoc);

	void possiblyChanged(KEY newDocId, RECORD oldDoc, RECORD newDoc);

	void movedBefore(KEY id, @Nullable KEY beforeId);

	Function<RECORD, KEY> getKeyer();

	default Object lock() {
		return this;
	}
}

