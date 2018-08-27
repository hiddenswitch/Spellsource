package net.demilich.metastone.game.spells.custom;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.*;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
public class FireGameEventSpell extends Spell {

    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        String gameEventName = (String) desc.get(SpellArg.NAME);

        GameEvent gameEvent = null;
        Card card = SpellUtils.getCard(context, desc);

        switch (gameEventName) {
            case "SecretRevealedEvent":
                gameEvent = new SecretRevealedEvent(context, card != null ? card : (Card) target, player.getId());
                break;
            case "TurnEndEvent":
                gameEvent = new TurnEndEvent(context, player.getId());
                break;
            case "TurnStartEvent":
                gameEvent = new TurnStartEvent(context, player.getId());
                break;
            case "HeroPowerEffectTriggeredEvent":
                gameEvent = new HeroPowerEffectTriggeredEvent(context, player.getId(), card != null ? card : (Card) target);
                break;
            default:
                break;
        }

        if (gameEvent != null) {
            context.fireGameEvent(gameEvent);
        }
    }

}