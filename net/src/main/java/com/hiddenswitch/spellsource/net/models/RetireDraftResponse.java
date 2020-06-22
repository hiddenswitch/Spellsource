package com.hiddenswitch.spellsource.net.models;

import com.hiddenswitch.spellsource.net.impl.util.DraftRecord;

import java.io.Serializable;

public class RetireDraftResponse implements Serializable {
	private DraftRecord record;

	public RetireDraftResponse withRecord(DraftRecord record) {
		this.record = record;
		return this;
	}

	public DraftRecord getRecord() {
		return record;
	}

	public void setRecord(DraftRecord record) {
		this.record = record;
	}
}
