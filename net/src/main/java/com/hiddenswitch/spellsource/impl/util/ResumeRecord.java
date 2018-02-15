package com.hiddenswitch.spellsource.impl.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResumeRecord implements Serializable {
	private List<HashedLoginTokenRecord> loginTokens;

	public List<HashedLoginTokenRecord> getLoginTokens() {
		return loginTokens;
	}

	public void setLoginTokens(List<HashedLoginTokenRecord> loginTokens) {
		this.loginTokens = loginTokens;
	}
}