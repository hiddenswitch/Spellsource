package com.hiddenswitch.spellsource.models;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by bberman on 1/22/17.
 */
public class GetCollectionRequest implements Serializable {
	private String userId;
	private String deckId;
	private List<GetCollectionRequest> requests;

	public GetCollectionRequest() {
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}


	public String getUserId() {
		return userId;
	}

	public GetCollectionRequest withUserId(final String userId) {
		this.userId = userId;
		return this;
	}

	public GetCollectionRequest withDeckId(String deckId) {
		this.deckId = deckId;
		return this;
	}

	public String getDeckId() {
		return deckId;
	}

	public void setDeckId(String deckId) {
		this.deckId = deckId;
	}

	public static GetCollectionRequest user(String userId) {
		return new GetCollectionRequest()
				.withUserId(userId);
	}

	public static GetCollectionRequest deck(String deckId) {
		return new GetCollectionRequest()
				.withDeckId(deckId);
	}

	public static GetCollectionRequest decks(String userId, List<String> decks) {
		return new GetCollectionRequest()
				.withUserId(userId)
				.withRequests(decks.stream().map(GetCollectionRequest::deck).map(request -> request.withUserId(userId)).collect(Collectors.toList()));
	}

	public List<GetCollectionRequest> getRequests() {
		return requests;
	}

	public void setRequests(List<GetCollectionRequest> requests) {
		this.requests = requests;
	}

	public GetCollectionRequest withRequests(final List<GetCollectionRequest> requests) {
		this.requests = requests;
		return this;
	}

	public boolean isBatchRequest() {
		return this.requests != null && this.requests.size() > 0;
	}
}


