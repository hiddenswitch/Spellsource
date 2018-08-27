package net.demilich.metastone.game.spells.custom;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
public class HeroPowerToSpellSpell extends Spell {
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        Card heroPower = player.getHero().getHeroPower();

        Card spellCard;

        if (heroPower.getCardId().equals("hero_power_deaths_shadow")) {
            spellCard = context.getCardById("spell_shadow_reflection");
        } else {
            CardDesc spellCardDesc = new CardDesc();
            spellCardDesc.setId(context.getLogic().generateCardId());
            spellCardDesc.setName(heroPower.getName());
            spellCardDesc.setType(CardType.SPELL);
            spellCardDesc.setDescription(heroPower.getDescription());
            spellCardDesc.setRarity(heroPower.getRarity());
            spellCardDesc.setBaseManaCost(heroPower.getBaseManaCost());
            spellCardDesc.setTargetSelection(heroPower.getTargetSelection());
            spellCardDesc.setSpell(heroPower.getSpell());
            spellCardDesc.setHeroClass(heroPower.getHeroClass());
            spellCardDesc.setSet(heroPower.getCardSet());
            spellCardDesc.setCollectible(false);
            spellCard = spellCardDesc.create();
        }
        context.getLogic().receiveCard(player.getId(), spellCard);
    }
}