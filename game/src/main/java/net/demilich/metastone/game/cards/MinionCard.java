package net.demilich.metastone.game.cards;

import java.util.*;

import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.actions.PlayMinionCardAction;
import net.demilich.metastone.game.cards.desc.MinionCardDesc;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A minion card can be used to summon a minion.
 * <p>
 * To instantiate a minion from this card, use {@link #summon()}. However, it is usually more appropriate to play the
 * card instead:
 * <p>
 * <pre>
 *     GameContext context = GameContext.fromTwoRandomDecks();
 *     // Initialize the game context
 *     context.init();
 *     // Retrieve a valid minion card
 *     MinionCard minionCard = (MinionCard) CardCatalogue.getCardById("minion_bloodfen_raptor");
 *     PlayCardAction summonMinionAction = minionCard.play();
 *     context.getLogic().performGameAction(GameContext.PLAYER_1, summonMinionAction);
 * </pre>
 *
 * @see ActorCard for the base class of {@link MinionCard}, {@link HeroCard} and {@link WeaponCard}.
 */
public class MinionCard extends ActorCard {

	private static Logger logger = LoggerFactory.getLogger(MinionCard.class);
	private static final Set<Attribute> ignoredAttributes = new HashSet<Attribute>(
			Arrays.asList(Attribute.PASSIVE_TRIGGERS, Attribute.DECK_TRIGGER, Attribute.BASE_ATTACK,
					Attribute.BASE_HP, Attribute.SECRET, Attribute.CHOOSE_ONE, Attribute.BATTLECRY, Attribute.COMBO,
					Attribute.TRANSFORM_REFERENCE));

	public MinionCard(MinionCardDesc desc) {
		super(desc);
		setAttribute(Attribute.BASE_ATTACK, desc.baseAttack);
		setAttribute(Attribute.ATTACK, desc.baseAttack);
		setAttribute(Attribute.BASE_HP, desc.baseHp);
		setAttribute(Attribute.HP, desc.baseHp);
		setAttribute(Attribute.MAX_HP, desc.baseHp);
		if (desc.race != null) {
			setRace(desc.race);
		}
		this.desc = desc;
	}

	protected Minion createMinion(Attribute... tags) {
		MinionCardDesc desc = getDesc();
		Minion minion = new Minion(this);
		for (Attribute gameTag : getAttributes().keySet()) {
			if (!ignoredAttributes.contains(gameTag)) {
				minion.setAttribute(gameTag, getAttribute(gameTag));
			}
		}

		applyText(minion);

		minion.setBaseAttack(getBaseAttack());
		minion.setAttack(getAttack());
		minion.setHp(getHp());
		minion.setMaxHp(getHp());
		minion.setBaseHp(getBaseHp());
		minion.setHp(minion.getMaxHp());

		return minion;
	}

	@Override
	public MinionCardDesc getDesc() {
		return (MinionCardDesc) super.getDesc();
	}

	@Override
	public PlayCardAction play() {
		return new PlayMinionCardAction(getReference());
	}

	public void setRace(Race race) {
		setAttribute(Attribute.RACE, race);
	}

	public Minion summon() {
		return createMinion();
	}

	@Override
	public MinionCard clone() {
		return (MinionCard) super.clone();
	}
}
