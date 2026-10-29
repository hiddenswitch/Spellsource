package com.hiddenswitch.framework.tests;

import com.hiddenswitch.framework.impl.GraphQLQueryResolverImpl;
import com.hiddenswitch.framework.impl.SqlCachedCardCatalogue;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.AttributeMap;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.desc.CardDesc;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CatalogueVisibilityTests {
	private Card card(String id, boolean published, boolean archived, String set) {
		var desc = new CardDesc();
		desc.setId(id);
		desc.setName(id);
		desc.setSet(set);
		var attributes = new AttributeMap();
		attributes.put(Attribute.CREATED_BY_USER_ID, "author");
		if (published) attributes.put(Attribute.PUBLISHED, true);
		if (archived) attributes.put(Attribute.ARCHIVED, true);
		desc.setAttributes(attributes);
		return new Card(desc);
	}

	@Test
	void publicCatalogueDoesNotExposeDraftsArchivedCardsOrTestCards() {
		var catalogue = mock(SqlCachedCardCatalogue.class);
		when(catalogue.getAll()).thenReturn(new CardArrayList(List.of(
			card("published", true, false, "CUSTOM"),
			card("draft", false, false, "CUSTOM"),
			card("archived", true, true, "CUSTOM"),
			card("test", true, false, "TEST"))));
		var result = new GraphQLQueryResolverImpl(catalogue).catalogueCards();
		assertTrue(result.succeeded());
		assertEquals(List.of("published"), result.result().stream().map(c -> c.getId()).toList());
		assertEquals("author", result.result().getFirst().getCreatedBy());
		var script = new io.vertx.core.json.JsonObject((java.util.Map<String, Object>) result.result().getFirst().getCardScript());
		assertFalse(script.getJsonObject("attributes").containsKey("CREATED_BY_USER_ID"));
		assertFalse(script.getJsonObject("attributes").containsKey("PUBLISHED"));
		assertFalse(script.getJsonObject("attributes").containsKey("ARCHIVED"));
		assertTrue(catalogue.getAll().getFirst().hasAttribute(Attribute.PUBLISHED));
	}
}
