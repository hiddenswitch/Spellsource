package com.hiddenswitch.spellsource.micro;

import io.micronaut.core.annotation.Introspected;

import java.util.ArrayList;
import java.util.List;

@Introspected
public class RequestTrace {
	private long seed;
	private int catalogueVersion;
	private List<String> heroClasses;
	private List<RequestTraceDeck> deckCardIds;
	private String deckFormatName;
	private List<String> deckFormatSets;
	private List<String> secondPlayerBonusCards;
	private List<RequestTraceMulligan> mulligans;
	private List<Integer> actions = new ArrayList<>();
	private String id;
	private boolean traceErrors;
	private int version = 4;

	public RequestTrace() {
	}

	public long getSeed() {
		return seed;
	}

	public RequestTrace setSeed(long seed) {
		this.seed = seed;
		return this;
	}

	public int getCatalogueVersion() {
		return catalogueVersion;
	}

	public RequestTrace setCatalogueVersion(int catalogueVersion) {
		this.catalogueVersion = catalogueVersion;
		return this;
	}

	public List<String> getHeroClasses() {
		return heroClasses;
	}

	public RequestTrace setHeroClasses(List<String> heroClasses) {
		this.heroClasses = heroClasses;
		return this;
	}

	public List<RequestTraceDeck> getDeckCardIds() {
		return deckCardIds;
	}

	public RequestTrace setDeckCardIds(List<RequestTraceDeck> deckCardIds) {
		this.deckCardIds = deckCardIds;
		return this;
	}

	public String getDeckFormatName() {
		return deckFormatName;
	}

	public RequestTrace setDeckFormatName(String deckFormatName) {
		this.deckFormatName = deckFormatName;
		return this;
	}

	public List<String> getDeckFormatSets() {
		return deckFormatSets;
	}

	public RequestTrace setDeckFormatSets(List<String> deckFormatSets) {
		this.deckFormatSets = deckFormatSets;
		return this;
	}

	public List<String> getSecondPlayerBonusCards() {
		return secondPlayerBonusCards;
	}

	public RequestTrace setSecondPlayerBonusCards(List<String> secondPlayerBonusCards) {
		this.secondPlayerBonusCards = secondPlayerBonusCards;
		return this;
	}


	public List<Integer> getActions() {
		return actions;
	}

	public RequestTrace setActions(List<Integer> actions) {
		this.actions = actions;
		return this;
	}

	public String getId() {
		return id;
	}

	public RequestTrace setId(String id) {
		this.id = id;
		return this;
	}

	public boolean isTraceErrors() {
		return traceErrors;
	}

	public RequestTrace setTraceErrors(boolean traceErrors) {
		this.traceErrors = traceErrors;
		return this;
	}

	public int getVersion() {
		return version;
	}

	public RequestTrace setVersion(int version) {
		this.version = version;
		return this;
	}

	public List<RequestTraceMulligan> getMulligans() {
		return mulligans;
	}

	public RequestTrace setMulligans(List<RequestTraceMulligan> mulligans) {
		this.mulligans = mulligans;
		return this;
	}
}

