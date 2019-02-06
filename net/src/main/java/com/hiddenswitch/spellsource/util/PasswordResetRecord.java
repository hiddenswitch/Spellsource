package com.hiddenswitch.spellsource.util;

import com.hiddenswitch.spellsource.impl.util.MongoRecord;

public class PasswordResetRecord extends MongoRecord {
	private static long DEFAULT_EXPIRATION_TIME = 24 * 60 * 60 * 1000;
	private String userId;
	private long expiresAt;

	public PasswordResetRecord() {
		super();
	}

	public PasswordResetRecord(String id) {
		super(id);
		expiresAt = System.currentTimeMillis() + DEFAULT_EXPIRATION_TIME;
	}

	public long getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(long expiresAt) {
		this.expiresAt = expiresAt;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
