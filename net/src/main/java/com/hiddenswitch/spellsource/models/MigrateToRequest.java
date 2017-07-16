package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

public class MigrateToRequest implements Serializable {
	private Boolean latest;
	private Boolean rerun;
	private Integer version;

	public Boolean getLatest() {
		return latest;
	}

	public void setLatest(Boolean latest) {
		this.latest = latest;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public MigrateToRequest withLatest(final Boolean latest) {
		this.latest = latest;
		return this;
	}

	public MigrateToRequest withVersion(final Integer version) {
		this.version = version;
		return this;
	}

	public Boolean getRerun() {
		return rerun;
	}

	public void setRerun(Boolean rerun) {
		this.rerun = rerun;
	}

	public MigrateToRequest withRerun(final Boolean rerun) {
		this.rerun = rerun;
		return this;
	}
}
