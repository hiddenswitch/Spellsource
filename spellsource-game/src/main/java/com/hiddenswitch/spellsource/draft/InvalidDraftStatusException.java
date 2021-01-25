package com.hiddenswitch.spellsource.draft;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates the current state of the draft is invalid for the requested changes.
 */
public final class InvalidDraftStatusException extends DraftException {
	@org.jetbrains.annotations.NotNull
	private final DraftStatus status;
	private final DraftStatus expected;

	/**
	 * Creates a new draft status error.
	 *
	 * @param status   The current status
	 * @param expected The status the draft was expected to be in for the operation to succeed.
	 */
	public InvalidDraftStatusException(DraftStatus status, DraftStatus expected) {
		super(status.ordinal());
		this.status = status;
		this.expected = expected;
	}

	/**
	 * The status at the time the exception was thrown.
	 *
	 * @return
	 */
	@NotNull
	public DraftStatus getStatus() {
		return status;
	}

	/**
	 * The expected status.
	 *
	 * @return
	 */
	public DraftStatus getExpected() {
		return expected;
	}
}
