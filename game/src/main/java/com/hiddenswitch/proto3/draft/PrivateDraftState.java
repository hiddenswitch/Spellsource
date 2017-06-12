package com.hiddenswitch.proto3.draft;

import net.demilich.metastone.game.cards.Card;

import java.util.List;
import java.util.Random;

/**
 * Created by bberman on 12/14/16.
 */
public class PrivateDraftState {
	public List<List<String>> cards;
	public Random random = new Random();

	public PrivateDraftState() {
	}
}
