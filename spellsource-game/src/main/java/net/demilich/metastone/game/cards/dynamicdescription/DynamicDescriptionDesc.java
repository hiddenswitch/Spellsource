package net.demilich.metastone.game.cards.dynamicdescription;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.entities.Entity;

/**
 * The serialized version of a dynamic description.
 */
@JsonDeserialize(using = DynamicDescriptionDeserializer.class)
public class DynamicDescriptionDesc extends Desc<DynamicDescriptionArg, DynamicDescription> {

	public DynamicDescriptionDesc() {
		super(DynamicDescriptionArg.class);
	}

	public DynamicDescriptionDesc(Class<? extends DynamicDescription> desctiptionClass) {
		super(desctiptionClass, DynamicDescriptionArg.class);
	}

	@Override
	protected Class<? extends Desc> getDescImplClass() {
		return DynamicDescriptionDesc.class;
	}

	@Override
	public DynamicDescriptionArg getClassArg() {
		return DynamicDescriptionArg.CLASS;
	}

	@Override
	public Desc<DynamicDescriptionArg, DynamicDescription> clone() {
		return copyTo(new DynamicDescriptionDesc(getDescClass()));
	}

	public String getDynamicDescription(DynamicDescriptionArg arg, GameContext context, Player player, Entity entity) {
		return ((DynamicDescription) get(arg)).resolveFinalString(context, player, entity);
	}
}
