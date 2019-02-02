package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.AddDeathrattleSpell;
import net.demilich.metastone.game.spells.PutRandomSecretIntoPlaySpell;
import net.demilich.metastone.game.spells.ShuffleToDeckSpell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Puts a random secret into play. Then, adds a Deathrattle to the {@code source} that shuffles that secret into the
 * {@code source} owner's deck.
 * <p>
 * Implements Keeper's Pupil.
 */
public final class KeepersPupilSpell extends PutRandomSecretIntoPlaySpell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		// Choose a card, then construct a filter to draw that card.
		Card secretCard = player.getDeck().stream()
				.filter(c -> player.getSecrets().stream().map(Entity::getSourceCard).map(Card::getCardId).noneMatch(s -> s.equals(c.getCardId()))
						&& c.isSecret())
				.findFirst()
				.orElse(null);
		if (secretCard == null) {
			return;
		}

		putSecretIntoPlay(context, player, secretCard);
		SpellUtils.castChildSpell(context, player, AddDeathrattleSpell.create(ShuffleToDeckSpell.create(secretCard.getCardId())), source, source);
	}
}